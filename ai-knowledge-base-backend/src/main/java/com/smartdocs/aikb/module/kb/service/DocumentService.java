package com.smartdocs.aikb.module.kb.service;

import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.module.kb.entity.KbDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

public interface DocumentService {

    Map<String, Object> upload(Long userId, Long kbId, MultipartFile file);

    PageVO<Map<String, Object>> page(Long userId, Long kbId, long page, long size, String keyword);

    Map<String, Object> detail(Long userId, Long docId);

    void delete(Long userId, Long docId);

    Map<String, Object> reparse(Long userId, Long docId);

    DownloadResource download(Long userId, Long docId);

    record DownloadResource(KbDocument document, InputStream content) {}
}
