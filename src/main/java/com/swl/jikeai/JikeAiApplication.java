package com.swl.jikeai;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.swl.jikeai.mapper")
public class JikeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JikeAiApplication.class, args);
    }

}
