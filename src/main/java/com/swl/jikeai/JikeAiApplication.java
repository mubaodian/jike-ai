package com.swl.jikeai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.swl.jikeai.mapper")
public class JikeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JikeAiApplication.class, args);
    }

}
