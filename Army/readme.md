<!--子模块pom添加以下内容-->
<dependencies>
    <!-- Web 容器 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 引入其他模块 -->
    <dependency>
        <groupId>com.military</groupId>
        <artifactId>military-system</artifactId>
    </dependency>
    <dependency>
        <groupId>com.military</groupId>
        <artifactId>military-business</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- 项目启动需要这个插件来打 Jar 包 -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
