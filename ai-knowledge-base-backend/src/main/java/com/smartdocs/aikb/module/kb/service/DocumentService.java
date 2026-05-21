package com.smartdocs.aikb.module.kb.service;

import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.module.kb.dto.KbDocumentVO;
import com.smartdocs.aikb.module.kb.entity.KbDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文档管理服务。
 */
public interface DocumentService {

    /** 上传文件到指定知识库。要求 EDITOR/OWNER 权限。 */
    KbDocumentVO upload(Long userId, Long kbId, MultipartFile file);

    /** 分页查询知识库下的文档列表。 */
    PageVO<KbDocumentVO> page(Long userId, Long kbId, long page, long size, String keyword);

    /** 文档详情。 */
    KbDocumentVO detail(Long userId, Long docId);

    /** 删除文档。EDITOR/OWNER。 */
    void delete(Long userId, Long docId);

    /** 重新解析。EDITOR/OWNER。 */
    KbDocumentVO reparse(Long userId, Long docId);

    /** 加载文件流，调用方关闭。同时返回文档实体用于响应头。 */
    DownloadResource download(Long userId, Long docId);

    /** 下载资源载体。 */
    record DownloadResource(KbDocument document, InputStream content) {
    }
}
