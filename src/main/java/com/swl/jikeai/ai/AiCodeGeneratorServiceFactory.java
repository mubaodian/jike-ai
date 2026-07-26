package com.swl.jikeai.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.swl.jikeai.ai.tools.FileWriteTool;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import com.swl.jikeai.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener(((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId：{}，原因：{}", key, cause);
            }))
            .build();

    /**
     * 根据 appId 获取独立的 AI Service 服务（带缓存）
     * 兼容老的逻辑
     *
     * @return AiCodeGeneratorService
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 获取独立的 AI Service 服务（带缓存）
     *
     * @return AiCodeGeneratorService
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId,CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 根据 appId 创建 AI Service 服务
     *
     * @param appId 应用Id
     * @return AiCodeGeneratorService
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
        log.info("为 appId：{} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(50)
                .build();
        // 加载对话历史到记忆中
        int i = chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 50);
        log.info("加载 appId：{} 的对话历史到记忆中，共加载 {} 条", appId, i);
        return switch (codeGenType) {
            // HTML 和 多文件生成使用默认的模型
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
            // Vue 项目生成使用推理模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(new FileWriteTool())
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest, "Error: there is no tool called: " + toolExecutionRequest.name()))
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型：" + codeGenType.getValue());
        };
    }

    /**
     * 获取默认的 AI Service 服务
     *
     * @return AiCodeGeneratorService
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }

    /**
     * 构建缓存键
     * @param appId 应用Id
     * @param codeGenType 代码类型
     * @return 缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}
