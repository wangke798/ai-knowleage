package com.smartdocs.aikb.module.kb.controller;

import com.smartdocs.aikb.common.result.PageVO;
import com.smartdocs.aikb.common.result.Result;
import com.smartdocs.aikb.common.util.CurrentUserHolder;
import com.smartdocs.aikb.module.kb.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/kb/{kbId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public Result<Map<String, Object>> upload(@PathVariable Long kbId,
                                              @RequestParam("file") MultipartFile file) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(documentService.upload(userId, kbId, file));
    }

    @GetMapping
    public Result<PageVO<Map<String, Object>>> page(
            @PathVariable Long kbId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(documentService.page(userId, kbId, page, size, keyword));
    }

    @GetMapping("/{docId}")
    public Result<Map<String, Object>> detail(@PathVariable Long kbId, @PathVariable Long docId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(documentService.detail(userId, docId));
    }

    @DeleteMapping("/{docId}")
    public Result<Void> delete(@PathVariable Long kbId, @PathVariable Long docId) {
        Long userId = CurrentUserHolder.requireUserId();
        documentService.delete(userId, docId);
        return Result.success();
    }

    @PostMapping("/{docId}/reparse")
    public Result<Map<String, Object>> reparse(@PathVariable Long kbId, @PathVariable Long docId) {
        Long userId = CurrentUserHolder.requireUserId();
        return Result.success(documentService.reparse(userId, docId));
    }

    @GetMapping("/{docId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long kbId, @PathVariable Long docId) {
        Long userId = CurrentUserHolder.requireUserId();
        DocumentService.DownloadResource res = documentService.download(userId, docId);
        String filename = URLEncoder.encode(res.document().getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String mime = res.document().getMimeType() != null
                ? res.document().getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(mime))
                .contentLength(res.document().getFileSize() == null ? -1L : res.document().getFileSize())
                .body(new InputStreamResource(res.content()));
    }
}
