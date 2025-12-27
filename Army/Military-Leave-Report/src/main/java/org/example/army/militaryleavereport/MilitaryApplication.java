package org.example.army.militaryleavereport; // 你的启动类包名

import org.mybatis.spring.annotation.MapperScan; // 引入包
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 核心修复：添加这行注解，指向 Mapper 所在的包路径
@MapperScan("org.example.army.militarycommon.mapper")
public class MilitaryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilitaryApplication.class, args);
    }
}

