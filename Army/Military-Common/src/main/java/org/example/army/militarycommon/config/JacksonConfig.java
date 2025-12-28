package org.example.army.militarycommon.config;

import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * 注册 JtsModule，使 Jackson 能够识别 JTS 的 Geometry 对象，
     * 并将其序列化为标准的 GeoJSON 格式供前端地图（Leaflet/OpenLayers）直接使用。
     */
    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }
}
