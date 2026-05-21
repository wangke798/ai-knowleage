package com.smartdocs.aikb.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 雪花 ID 等 Long 包装类型序列化为 JSON 字符串，避免 JS Number 精度丢失（19 位整数超出 Number.MAX_SAFE_INTEGER）。
 *
 * <p>仅对 {@code Long}（包装类，DB PK 等）启用；{@code long}（原始类型，分页计数、时长等聚合标量）保持数字输出，避免破坏前端
 * 的数学运算（如 Math.ceil(total / size)）。
 * <p>反序列化保持默认：前端发字符串或数字均可被 Jackson 解析为 Long。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("LongToStringModule");
            module.addSerializer(Long.class, new LongToStringSerializer());
            builder.modulesToInstall(module);
        };
    }

    private static final class LongToStringSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toString());
            }
        }
    }
}
