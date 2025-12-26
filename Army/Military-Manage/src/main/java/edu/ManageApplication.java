package edu;

import org.example.army.militarycommon.config.MybatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Military-Manage 模块启动类
 */
@SpringBootApplication
@Import(MybatisPlusConfig.class)
@MapperScan(basePackages = "org.example.army.militarycommon.mapper")
public class ManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageApplication.class, args);
    }
}
