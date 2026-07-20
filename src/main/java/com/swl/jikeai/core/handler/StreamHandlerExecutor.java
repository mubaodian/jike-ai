package com.swl.jikeai.core.handler;

import com.swl.jikeai.model.entity.User;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import com.swl.jikeai.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理执行器
 * 根据代码生成类型创建合适的流处理器
 * 1. 传统的 Flux<String> 流（HTML、MULIT_FLIE）-> SimpleTextStreamHandler
 * 2. TokenStream 格式的复杂流（VUE_PROJECT）-> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            // 简单文本处理器不需要依赖注入
            case HTML,MULTI_FILE -> new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
            // 使用注入的组件实例
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}
