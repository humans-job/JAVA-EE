package edu;

import org.example.army.militarycommon.config.MybatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MybatisPlusConfig.class)
@MapperScan(basePackages = "org.example.army.militarycommon.mapper")
public class AuthenticateApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthenticateApplication.class, args);
    }
}
