package com.smartdocs.aikb.module.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartdocs.aikb.common.exception.BusinessException;
import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.common.result.ResultCode;
import com.smartdocs.aikb.config.UploadProperties;
import com.smartdocs.aikb.module.kb.entity.KbDocument;
import com.smartdocs.aikb.module.kb.entity.KbDocumentChunk;
import com.smartdocs.aikb.module.kb.entity.KnowledgeBase;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentChunkMapper;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentMapper;
import com.smartdocs.aikb.module.kb.mapper.KnowledgeBaseMapper;
import com.smartdocs.aikb.module.kb.service.DocumentParseService;
import com.smartdocs.aikb.module.kb.service.DocumentService;
import com.smartdocs.aikb.module.kb.service.KnowledgeBaseService;
import com.smartdocs.aikb.module.kb.storage.StorageService;
import com.smartdocs.aikb.module.user.entity.SysUser;
import com.smartdocs.aikb.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    /** 解析状态常量 */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";

    private static final Set<String> WRITABLE_ROLES = Set.of(
            KnowledgeBaseServiceImpl.ROLE_OWNER, KnowledgeBaseServiceImpl.ROLE_EDITOR);

    private final KbDocumentMapper documentMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final SysUserMapper sysUserMapper;
    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentParseService documentParseService;
    private final StorageService storageService;
    private final VectorStore vectorStore;
    private final UploadProperties uploadProperties;

    @Override
    @Transactional
    public Map<String, Object> upload(Long userId, Long kbId, MultipartFile file) {
        requireWrite(userId, kbId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件为空");
        }
        long size = file.getSize();
        if (size > uploadProperties.getMaxFileSize()) {
            throw new BusinessException(ResultCode.DOC_SIZE_EXCEEDED,
                    "单文件最大 " + (uploadProperties.getMaxFileSize() / 1024 / 1024) + " MB");
        }
        String mime = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        if (!uploadProperties.getAllowedMimeTypes().contains(mime)) {
            // markdown / txt 浏览器有时给的 mime 不规范，按扩展名兜底
            String ext = extOf(file.getOriginalFilename());
            if (!isAllowedExt(ext)) {
                throw new BusinessException(ResultCode.DOC_TYPE_NOT_SUPPORTED, "不支持的文件类型：" + mime);
            }
        }

        String hash;
        byte[] bytes;
        try {
            bytes = file.getBytes();
            hash = sha256(bytes);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "读取上传文件失败");
        }

        // 同 KB 内按 hash 去重
        KbDocument exist = documentMapper.selectOne(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getKbId, kbId)
                .eq(KbDocument::getFileHash, hash)
                .last("LIMIT 1"));
        if (exist != null) {
            throw new BusinessException(ResultCode.DOC_DUPLICATE);
        }

        String originalName = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename() : "未命名";
        String ext = extOf(originalName);
        String relativePath = "kb/" + kbId + "/" + UUID.randomUUID().toString().replace("-", "")
                + (ext.isEmpty() ? "" : "." + ext);

        try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
            storageService.save(relativePath, in, size);
        } catch (IOException e) {
            log.error("[Document] save failed kbId={} name={}", kbId, originalName, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        KbDocument entity = new KbDocument();
        entity.setKbId(kbId);
        entity.setName(originalName);
        entity.setStoragePath(relativePath);
        entity.setFileSize(size);
        entity.setMimeType(mime);
        entity.setFileHash(hash);
        entity.setVersion(1);
        entity.setParseStatus(STATUS_PENDING);
        entity.setUploaderId(userId);
        documentMapper.insert(entity);

        // 触发异步解析：必须在事务提交后再投递，否则异步线程会读不到刚 insert 的记录
        triggerParseAfterCommit(entity.getId());

        return toMap(entity, sysUserMapper.selectById(userId));
    }

    private void triggerParseAfterCommit(Long docId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    documentParseService.parseAsync(docId);
                }
            });
        } else {
            documentParseService.parseAsync(docId);
        }
    }

    @Override
    public PageVO<Map<String, Object>> page(Long userId, Long kbId, long page, long size, String keyword) {
        requireRead(userId, kbId);
        LambdaQueryWrapper<KbDocument> qw = new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getKbId, kbId)
                .orderByDesc(KbDocument::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            qw.like(KbDocument::getName, keyword);
        }
        Page<KbDocument> result = documentMapper.selectPage(new Page<>(page, size), qw);
        List<KbDocument> records = result.getRecords();
        if (records.isEmpty()) {
            return PageVO.map(result, d -> toMap(d, null));
        }
        Set<Long> uploaderIds = records.stream().map(KbDocument::getUploaderId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(uploaderIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        return PageVO.map(result, d -> toMap(d, userMap.get(d.getUploaderId())));
    }

    @Override
    public Map<String, Object> detail(Long userId, Long docId) {
        KbDocument doc = requireDoc(docId);
        requireRead(userId, doc.getKbId());
        return toMap(doc, sysUserMapper.selectById(doc.getUploaderId()));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long docId) {
        KbDocument doc = requireDoc(docId);
        requireWrite(userId, doc.getKbId());
        documentMapper.deleteById(docId);
        // 级联物理删除切片
        chunkMapper.delete(new LambdaQueryWrapper<KbDocumentChunk>().eq(KbDocumentChunk::getDocId, docId));
        // 物理删除文件（幂等）
        storageService.delete(doc.getStoragePath());
        // 删除向量库中的向量
        try {
            Filter.Expression byDoc = new FilterExpressionBuilder()
                    .eq("docId", doc.getId().toString()).build();
            vectorStore.delete(byDoc);
        } catch (Exception e) {
            log.warn("[Document] vector delete failed docId={}: {}", docId, e.toString());
        }
    }

    @Override
    public Map<String, Object> reparse(Long userId, Long docId) {
        KbDocument doc = requireDoc(docId);
        requireWrite(userId, doc.getKbId());
        // 立刻置 PENDING 以反馈给前端；实际状态机由 parseAsync 推进
        KbDocument upd = new KbDocument();
        upd.setId(docId);
        upd.setParseStatus(STATUS_PENDING);
        upd.setParseError(null);
        upd.setChunkCount(null);
        documentMapper.updateById(upd);
        triggerParseAfterCommit(docId);
        doc.setParseStatus(STATUS_PENDING);
        doc.setParseError(null);
        doc.setChunkCount(null);
        return toMap(doc, sysUserMapper.selectById(doc.getUploaderId()));
    }

    @Override
    public DownloadResource download(Long userId, Long docId) {
        KbDocument doc = requireDoc(docId);
        requireRead(userId, doc.getKbId());
        try {
            InputStream in = storageService.load(doc.getStoragePath());
            return new DownloadResource(doc, in);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND, "文件已丢失");
        }
    }

    // ---------------- helpers ----------------

    private KbDocument requireDoc(Long docId) {
        KbDocument doc = documentMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        return doc;
    }

    private void requireRead(Long userId, Long kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null) {
            throw new BusinessException(ResultCode.KB_NOT_FOUND);
        }
        if (knowledgeBaseService.resolveRole(userId, kbId) == null) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
    }

    private void requireWrite(Long userId, Long kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null) {
            throw new BusinessException(ResultCode.KB_NOT_FOUND);
        }
        String role = knowledgeBaseService.resolveRole(userId, kbId);
        if (role == null || !WRITABLE_ROLES.contains(role)) {
            throw new BusinessException(ResultCode.KB_NO_PERMISSION);
        }
    }

    private Map<String, Object> toMap(KbDocument doc, SysUser uploader) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", doc.getId());
        m.put("kbId", doc.getKbId());
        m.put("name", doc.getName());
        m.put("fileSize", doc.getFileSize());
        m.put("mimeType", doc.getMimeType());
        m.put("fileHash", doc.getFileHash());
        m.put("version", doc.getVersion());
        m.put("parseStatus", doc.getParseStatus());
        m.put("parseError", doc.getParseError());
        m.put("chunkCount", doc.getChunkCount());
        m.put("uploaderId", doc.getUploaderId());
        if (uploader != null) {
            m.put("uploaderName", StringUtils.hasText(uploader.getNickname())
                    ? uploader.getNickname() : uploader.getUsername());
        }
        m.put("createTime", doc.getCreateTime());
        m.put("updateTime", doc.getUpdateTime());
        return m;
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String extOf(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) return "";
        return name.substring(idx + 1).toLowerCase();
    }

    private static final Set<String> ALLOWED_EXT = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "txt", "md", "markdown", "html", "htm");

    private static boolean isAllowedExt(String ext) {
        return ALLOWED_EXT.contains(ext);
    }
}
