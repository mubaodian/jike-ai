package com.swl.jikeai.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  AI 代码生成类型路由服务工厂
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    /**
     *  创建专用于路由的 ChatModel（不开启 json_object 模式）
     */
    private ChatModel routingChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .strictJsonSchema(true)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     *  创建 AI 代码生成类型路由服务实例
     * @return Ai Service 实例对象
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routingChatModel())
                .build();
    }
}
