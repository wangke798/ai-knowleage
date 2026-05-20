package com.smartdocs.aikb.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置。文档解析使用独立线程池 {@code docParseExecutor}，
 * 与其他业务隔离，避免互相阻塞。
 */
@Slf4j
@Configuration
public class AsyncConfig {

    public static final String DOC_PARSE_EXECUTOR = "docParseExecutor";

    @Bean(name = DOC_PARSE_EXECUTOR)
    public Executor docParseExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(200);
        exec.setKeepAliveSeconds(60);
        exec.setThreadNamePrefix("doc-parse-");
        // 队列满时由调用方线程兜底执行，避免任务丢失
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 将 MDC 上下文（含 traceId）透传到子线程
        exec.setTaskDecorator(runnable -> {
            Map<String, String> ctx = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> prev = MDC.getCopyOfContextMap();
                if (ctx != null) MDC.setContextMap(ctx);
                try {
                    runnable.run();
                } finally {
                    if (prev != null) MDC.setContextMap(prev);
                    else MDC.clear();
                }
            };
        });
        exec.setWaitForTasksToCompleteOnShutdown(true);
        exec.setAwaitTerminationSeconds(30);
        exec.initialize();
        log.info("[Async] docParseExecutor initialized: core=2 max=4 queue=200");
        return exec;
    }
}
