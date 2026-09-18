package com.swl.jikeai.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI生成 app 名称
 */
public interface AiCodeGenAppNameService {

    /**
     * AI自动生成应用名称
     * @param userMessage 用户消息
     * @return app名称
     */
    @SystemMessage(fromResource = "prompt/gen-appname-system-prompt.txt")
    String genAppName(String userMessage);
}
