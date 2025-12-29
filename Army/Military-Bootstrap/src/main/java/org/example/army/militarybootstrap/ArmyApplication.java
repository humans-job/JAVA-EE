package org.example.army.militarybootstrap;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 部队民兵管理平台 - 统一启动入口
 */
@Slf4j
@SpringBootApplication
// 扫描所有子模块的组件 (假设所有模块包名都以 org.example 开头)
@ComponentScan(basePackages = "org.example.army")
// 扫描所有子模块的 Mapper 接口
@MapperScan("org.example.**.mapper")
public class ArmyApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ArmyApplication.class, args);

        // 打印启动成功的日志信息
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (path == null) {
            path = "";
        }

        log.info("\n----------------------------------------------------------\n\t" +
                "Application Military-Army is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Doc: \t\thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");
    }
}

