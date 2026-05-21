package com.smartdocs.aikb.module.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartdocs.aikb.config.AsyncConfig;
import com.smartdocs.aikb.module.kb.entity.KbDocument;
import com.smartdocs.aikb.module.kb.entity.KbDocumentChunk;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentChunkMapper;
import com.smartdocs.aikb.module.kb.mapper.KbDocumentMapper;
import com.smartdocs.aikb.module.kb.service.DocumentParseService;
import com.smartdocs.aikb.module.kb.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.smartdocs.aikb.module.kb.service.impl.DocumentServiceImpl.*;

/**
 * 解析流水线实现：Tika 抽取 → 文本清洗 → 字符滑窗切片 → 写库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseServiceImpl implements DocumentParseService {

    /** Tika 解析最大字符数。设大一些以兼容长文档；解析失败会改写 status=FAILED。 */
    private static final int TIKA_MAX_CHARS = 5 * 1024 * 1024;

    private final KbDocumentMapper documentMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final StorageService storageService;
    private final VectorStore vectorStore;

    @Value("${app.parse.chunk-size:800}")
    private int chunkSize;

    @Value("${app.parse.chunk-overlap:100}")
    private int chunkOverlap;

    @Value("${app.parse.embed-batch-size:16}")
    private int embedBatchSize;

    @Override
    @Async(AsyncConfig.DOC_PARSE_EXECUTOR)
    public void parseAsync(Long docId) {
        KbDocument doc = documentMapper.selectById(docId);
        if (doc == null) {
            log.warn("[Parse] doc not found docId={}", docId);
            return;
        }

        // 标记 PROCESSING
        updateStatus(docId, STATUS_PROCESSING, null, null);

        String text;
        try {
            text = extract(doc);
        } catch (Exception e) {
            log.error("[Parse] extract failed docId={} name={}", docId, doc.getName(), e);
            updateStatus(docId, STATUS_FAILED, "解析失败：" + safeMsg(e), 0);
            return;
        }

        String cleaned = clean(text);
        if (cleaned.isEmpty()) {
            updateStatus(docId, STATUS_FAILED, "未抽取到文本内容", 0);
            return;
        }

        try {
            List<KbDocumentChunk> chunks = persistChunks(doc, cleaned);
            try {
                embedChunks(doc, chunks);
            } catch (Exception e) {
                log.error("[Parse] embed failed docId={}", docId, e);
                updateStatus(docId, STATUS_FAILED, "向量化失败：" + safeMsg(e), chunks.size());
                return;
            }
            updateStatus(docId, STATUS_DONE, null, chunks.size());
            log.info("[Parse] done docId={} chunks={} embedded", docId, chunks.size());
        } catch (Exception e) {
            log.error("[Parse] persist chunks failed docId={}", docId, e);
            updateStatus(docId, STATUS_FAILED, "切片入库失败：" + safeMsg(e), 0);
        }
    }

    // ---------------- helpers ----------------

    /** 抽取文本：优先 Tika，全格式通吃；md/txt 直接走 Tika 的 text/plain 即可。 */
    private String extract(KbDocument doc) throws IOException, TikaException, SAXException {
        try (InputStream in = storageService.load(doc.getStoragePath())) {
            BodyContentHandler handler = new BodyContentHandler(TIKA_MAX_CHARS);
            Metadata metadata = new Metadata();
            if (doc.getName() != null) {
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, doc.getName());
            }
            new AutoDetectParser().parse(in, handler, metadata, new ParseContext());
            return handler.toString();
        }
    }

    /** 简单清洗：标准化换行、剔除控制字符、压缩连续空行。 */
    private String clean(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\r\n", "\n").replace('\r', '\n');
        s = s.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
        s = s.replaceAll("\n{3,}", "\n\n");
        // 行尾空格
        s = s.replaceAll("[ \t]+\n", "\n");
        return s.trim();
    }

    @Transactional
    protected List<KbDocumentChunk> persistChunks(KbDocument doc, String text) {
        // 删除旧切片（重解析场景）。
        // 注：本方法被同类的 parseAsync 直接调用，Spring AOP 事务在自调用场景不生效，
        // 这里 @Transactional 主要表明语义；失败由外层 try/catch 兜底回写 FAILED。
        chunkMapper.delete(new LambdaQueryWrapper<KbDocumentChunk>().eq(KbDocumentChunk::getDocId, doc.getId()));

        // 同时清掉该文档在向量库的旧记录（按 metadata.docId 过滤）
        try {
            Filter.Expression byDoc = new FilterExpressionBuilder()
                    .eq("docId", doc.getId().toString()).build();
            vectorStore.delete(byDoc);
        } catch (Exception e) {
            log.warn("[Parse] vector delete-old failed docId={}: {}", doc.getId(), e.toString());
        }

        List<String> pieces = slidingWindow(text, chunkSize, chunkOverlap);
        List<KbDocumentChunk> chunks = new ArrayList<>(pieces.size());
        for (int i = 0; i < pieces.size(); i++) {
            String content = pieces.get(i);
            KbDocumentChunk c = new KbDocumentChunk();
            c.setDocId(doc.getId());
            c.setKbId(doc.getKbId());
            c.setSeq(i);
            c.setContent(content);
            c.setCharCount(content.length());
            // 粗略 token 估算：英文按 4 字符/词，中文按 1.5 字符/token，取折中 3 字符/token
            c.setTokenCount(Math.max(1, content.length() / 3));
            chunks.add(c);
        }
        for (KbDocumentChunk c : chunks) {
            chunkMapper.insert(c);
        }
        return chunks;
    }

    /**
     * 将 chunks 分批写入向量库。每条 Document 携带 metadata：
     * docId / kbId / chunkId / seq / docName。
     * 同时把向量库分配的 id 回写到 {@code kb_document_chunk.vector_id}。
     */
    protected void embedChunks(KbDocument doc, List<KbDocumentChunk> chunks) {
        if (chunks.isEmpty()) return;
        int total = chunks.size();
        for (int from = 0; from < total; from += embedBatchSize) {
            int to = Math.min(from + embedBatchSize, total);
            List<KbDocumentChunk> batch = chunks.subList(from, to);
            List<Document> docs = new ArrayList<>(batch.size());
            for (KbDocumentChunk c : batch) {
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("docId", String.valueOf(doc.getId()));
                meta.put("kbId", String.valueOf(doc.getKbId()));
                meta.put("chunkId", String.valueOf(c.getId()));
                meta.put("seq", c.getSeq());
                if (doc.getName() != null) meta.put("docName", doc.getName());
                Document d = new Document(c.getContent(), meta);
                docs.add(d);
                c.setVectorId(d.getId());
            }
            vectorStore.add(docs);
            for (KbDocumentChunk c : batch) {
                KbDocumentChunk upd = new KbDocumentChunk();
                upd.setId(c.getId());
                upd.setVectorId(c.getVectorId());
                chunkMapper.updateById(upd);
            }
            log.info("[Embed] docId={} batch [{}..{}) ok", doc.getId(), from, to);
        }
    }

    /**
     * 字符滑窗切片。{@code overlap} 必须小于 {@code size}，否则按 0 处理。
     * 为避免硬切断单词/语义，优先在最近的换行/句末标点处回退切点（前 1/4 范围内）。
     */
    static List<String> slidingWindow(String text, int size, int overlap) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty() || size <= 0) {
            return out;
        }
        int ov = (overlap < 0 || overlap >= size) ? 0 : overlap;
        int len = text.length();
        int start = 0;
        while (start < len) {
            int end = Math.min(start + size, len);
            if (end < len) {
                // 在 [end - size/4, end] 内寻找软切点
                int softFrom = Math.max(start + size / 2, end - size / 4);
                int softEnd = lastBreak(text, softFrom, end);
                if (softEnd > start) {
                    end = softEnd;
                }
            }
            String piece = text.substring(start, end).trim();
            if (!piece.isEmpty()) {
                out.add(piece);
            }
            if (end >= len) break;
            start = Math.max(end - ov, start + 1);
        }
        return out;
    }

    /** 在 [from, to) 内查找最后一个换行/句末符号位置（不含），找不到返回 -1。 */
    private static int lastBreak(String s, int from, int to) {
        for (int i = to - 1; i >= from; i--) {
            char c = s.charAt(i);
            if (c == '\n' || c == '。' || c == '！' || c == '？'
                    || c == '.' || c == '!' || c == '?' || c == ';' || c == '；') {
                return i + 1;
            }
        }
        return -1;
    }

    private void updateStatus(Long docId, String status, String error, Integer chunkCount) {
        KbDocument upd = new KbDocument();
        upd.setId(docId);
        upd.setParseStatus(status);
        upd.setParseError(error);
        upd.setChunkCount(chunkCount);
        documentMapper.updateById(upd);
    }

    private static String safeMsg(Throwable e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
        return msg.length() > 480 ? msg.substring(0, 480) : msg;
    }
}
