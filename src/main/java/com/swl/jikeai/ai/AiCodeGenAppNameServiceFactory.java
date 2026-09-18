package com.swl.jikeai.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  App 名称生成服务工厂类
 */
@Slf4j
@Configuration
public class AiCodeGenAppNameServiceFactory {

    @Value("${langchain4j.open-ai.qwen-chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.qwen-chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.qwen-chat-model.model-name}")
    private String modelName;

    /**
     *  创建专门生成 app name 的chatModel
     */
    private ChatModel appNameChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     *  创建App Name 生成服务实例
     * @return Ai Service 实例对象
     */
    @Bean
    public AiCodeGenAppNameService aiCodeGenAppNameService() {
        return AiServices.builder(AiCodeGenAppNameService.class)
                .chatModel(appNameChatModel())
                .build();
    }
}
