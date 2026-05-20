package com.smartdocs.aikb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 文档上传相关配置。对应 {@code app.upload.*}。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /** 单文件大小上限（字节） */
    private long maxFileSize = 50L * 1024 * 1024;

    /** 允许的 MIME 类型白名单 */
    private List<String> allowedMimeTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/markdown",
            "text/html");

    /** 本地存储根目录（仅 LocalFileStorage 使用） */
    private String localRoot = "./data/uploads";
}
