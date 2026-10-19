package com.swl.jikeai.config;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 流式请求所用 HTTP 客户端配置。
 * <p>
 * 背景（SSE 打字机效果丢失的根因）：
 * langchain4j-open-ai-spring-boot4-starter 的 OpenAiAutoConfiguration 在创建模型时，
 * 会显式传入 SpringRestClient 作为 HTTP 客户端（见 openAiStreamingChatModel
 * 上 @Qualifier("openAiStreamingChatModelHttpClientBuilder") 的那个 HttpClientBuilder 参数），
 * 完全绕过了 langchain4j 的 SPI 机制（HttpClientBuilderLoader），
 * 所以在 JikeAiApplication 里设置 langchain4j.http.clientBuilderFactory 系统属性对它无效。
 * <p>
 * 而 SpringRestClient 内部使用 Spring Boot 自动探测出来的请求工厂，
 * 本项目因为 selenium / webdrivermanager 引入了 Apache HttpClient 5，
 * 探测结果就是 HttpComponentsClientHttpRequestFactory，
 * 它读取 SSE 响应时会整段缓冲，导致后端的流式分片直到 AI 生成结束才一次性下发，
 * 前端表现就是"没有打字机效果、AI 气泡一直不动，最后一次性全部输出"。
 * <p>
 * 修复方式：
 * starter 的 openAiStreamingChatModelHttpClientBuilder 这个 Bean 上标注了
 * @ConditionalOnMissingBean(name = "openAiStreamingChatModelHttpClientBuilder")，
 * 因此这里用同名的 Bean 覆盖它，换成 langchain4j 官方的 JDK HttpClient 实现，
 * 该实现是边读边回调的，不会缓冲响应体，流式输出即可恢复正常。
 */
@Slf4j
@Configuration
public class AiStreamingHttpClientConfig {

    /**
     * 覆盖 langchain4j Spring Boot starter 默认的流式 HTTP 客户端（默认是 SpringRestClient）。
     *
     * @return 基于 JDK HttpClient 的构建器
     */
    @Bean("openAiStreamingChatModelHttpClientBuilder")
    public HttpClientBuilder openAiStreamingChatModelHttpClientBuilder() {
        log.info("流式 AI 请求使用 JDK HttpClient，避免 SSE 响应被 Apache HttpClient 缓冲");
        return new JdkHttpClientBuilder();
    }
}
