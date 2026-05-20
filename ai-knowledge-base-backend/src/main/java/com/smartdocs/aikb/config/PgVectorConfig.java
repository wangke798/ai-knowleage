package com.smartdocs.aikb.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 从 DynamicRoutingDataSource 中取出 pgvector 数据源，
 * 构建专用 JdbcTemplate 和 VectorStore。
 */
@Configuration
public class PgVectorConfig {

    @Bean("pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(DataSource dataSource) {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        return new JdbcTemplate(ds.getDataSource("pgvector"));
    }

    @Bean
    public VectorStore vectorStore(
            @Qualifier("pgVectorJdbcTemplate") JdbcTemplate pgVectorJdbcTemplate,
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(pgVectorJdbcTemplate, embeddingModel)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .dimensions(1024)
                .initializeSchema(true)
                .build();
    }
}
