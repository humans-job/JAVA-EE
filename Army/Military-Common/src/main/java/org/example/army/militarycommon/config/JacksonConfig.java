package org.example.army.militarycommon.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // 关键：标记为主配置，强制覆盖 Spring Boot 默认的 ObjectMapper
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. 注册 JTS 模块 (解决 Point 无法反序列化的问题)
        mapper.registerModule(new JtsModule());
        mapper.registerModule(new JavaTimeModule());
        // 2. (可选) 配置忽略 JSON 中存在但 Java 对象中不存在的字段，防止报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}

