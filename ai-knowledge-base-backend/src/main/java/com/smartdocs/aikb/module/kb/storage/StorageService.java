package com.smartdocs.aikb.module.kb.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件存储抽象。后续可替换为 MinIO / OSS 实现。
 */
public interface StorageService {

    /**
     * 保存文件。
     *
     * @param relativePath 相对存储根目录的路径（如 {@code kb/12/uuid.pdf}）
     * @param input        文件输入流（由调用方关闭）
     * @param size         预期大小（仅用于日志/校验，可为 null）
     * @return 实际保存的相对路径（一般与入参一致）
     */
    String save(String relativePath, InputStream input, Long size) throws IOException;

    /** 读取文件为输入流。调用方负责关闭。 */
    InputStream load(String relativePath) throws IOException;

    /** 删除文件，文件不存在时静默忽略。 */
    void delete(String relativePath);

    /** 文件是否存在 */
    boolean exists(String relativePath);
}
