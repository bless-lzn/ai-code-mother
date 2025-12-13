package com.limou.aicodemother;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@EnableDubbo
//@ComponentScan("com.limou")
public class LiAiCodeScreenshotApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiAiCodeScreenshotApplication.class, args);
    }
}
