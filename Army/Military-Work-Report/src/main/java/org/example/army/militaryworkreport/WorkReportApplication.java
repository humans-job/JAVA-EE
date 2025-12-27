package org.example.army.militaryworkreport;

import org.example.army.militarycommon.config.MybatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@MapperScan(basePackages = "org.example.army.militarycommon.mapper")
public class WorkReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkReportApplication.class, args);
    }
}
