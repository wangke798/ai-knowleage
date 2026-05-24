package com.smartdocs.aikb.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// DataSource 配置已由 dynamic-datasource-spring-boot3-starter 统一管理，
// 见 application-dev.yml -> spring.datasource.dynamic.*

@Configuration
public class DataSourceConfig {

    /**
     * 启动时先 repair（清除上次失败的迁移记录），再 migrate。
     * 保证 V3 等幂等脚本在任何状态下都能顺利跑完。
     */
    @Bean
    public FlywayMigrationStrategy flywayRepairAndMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
