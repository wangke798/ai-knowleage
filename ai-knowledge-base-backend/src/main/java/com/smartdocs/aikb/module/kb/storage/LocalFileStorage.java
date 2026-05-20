package com.smartdocs.aikb.module.kb.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

/**
 * 本地磁盘存储实现。根目录由 {@code app.upload.local-root} 配置，
 * 默认 {@code ./data/uploads}。所有 relativePath 必须是相对路径，
 * 禁止包含 {@code ..} 以防越权读写。
 */
@Slf4j
@Component
public class LocalFileStorage implements StorageService {

    private final Path root;

    public LocalFileStorage(@Value("${app.upload.local-root:./data/uploads}") String rootPath) {
        this.root = Paths.get(rootPath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root);
        log.info("[Storage] local root = {}", root);
    }

    @Override
    public String save(String relativePath, InputStream input, Long size) throws IOException {
        Path target = resolveSafe(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        return relativePath;
    }

    @Override
    public InputStream load(String relativePath) throws IOException {
        Path target = resolveSafe(relativePath);
        if (!Files.exists(target)) {
            throw new NoSuchFileException(relativePath);
        }
        return Files.newInputStream(target);
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path target = resolveSafe(relativePath);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("[Storage] delete failed: {}", relativePath, e);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        try {
            return Files.exists(resolveSafe(relativePath));
        } catch (IOException e) {
            return false;
        }
    }

    private Path resolveSafe(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IOException("relativePath blank");
        }
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("path traversal detected: " + relativePath);
        }
        return target;
    }
}
