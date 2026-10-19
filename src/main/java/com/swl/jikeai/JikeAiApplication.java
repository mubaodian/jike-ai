package com.swl.jikeai;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.swl.jikeai.mapper")
public class JikeAiApplication {

    static {
        // 必须在任何 langchain4j 模型创建之前设置。
        // 作用范围：代码里手动 builder 构建的模型（如 ReasoningStreamingChatModelConfig、
        // qwen 相关模型）；它们走 langchain4j 的 SPI（HttpClientBuilderLoader），
        // 类路径上同时存在 SpringRestClient 和 JDK HttpClient 两个实现，不指定会报冲突。
        // SpringRestClient 内部用 Spring Boot 自动探测的请求工厂，项目里 selenium/webdrivermanager
        // 引入了 Apache HttpClient 5，会被选中并整段缓冲 SSE 响应，
        // 导致 AI 流式输出要等生成结束才一次性到达前端（前端没有打字机效果）。
        // 注意：spring-boot4 starter 自动装配的模型显式传入了 SpringRestClient、不走 SPI，
        // 由 AiStreamingHttpClientConfig 覆盖其 openAiStreamingChatModelHttpClientBuilder 解决。
        System.setProperty("langchain4j.http.clientBuilderFactory",
                JdkHttpClientBuilderFactory.class.getName());
    }

    public static void main(String[] args) {
        SpringApplication.run(JikeAiApplication.class, args);
    }

}
