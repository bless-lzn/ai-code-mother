package com.limou.aicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.limou.aicodemother.mapper")
//@ComponentScan("com.yupi")
//@EnableDubbo
public class LiAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiAiCodeUserApplication.class, args);
    }
}
