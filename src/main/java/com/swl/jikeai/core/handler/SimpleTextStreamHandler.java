package com.swl.jikeai.core.handler;

import cn.hutool.core.util.StrUtil;
import com.swl.jikeai.model.entity.User;
import com.swl.jikeai.model.enums.ChatHistoryMessageTypeEnum;
import com.swl.jikeai.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML、MULIT_FILE）
     * 直接手机完整的文本响应
     * @param originFlux 原始流
     * @param chatHistoryService 对话历史服务
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chuck -> {
                    // 收集 AI 响应内容
                    aiResponseBuilder.append(chuck);
                    return chuck;
                })
                .doOnComplete(() -> {
                            // 响应完后，添加 AI 回复到对话历史中
                            String aiResponse = aiResponseBuilder.toString();
                            if (StrUtil.isNotBlank(aiResponse)) {
                                chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                            }
                        }
                )
                .doOnError(e -> {
                    // 如果 AI 回复失败,则添加错误信息到对话历史中
                    String errorMessage = "AI回复失败:" + e.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }
}
