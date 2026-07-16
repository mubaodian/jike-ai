package com.swl.jikeai.ai;

import com.swl.jikeai.ai.model.HtmlCodeResult;
import com.swl.jikeai.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return HTML 代码结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 多文件代码结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 Vue 项目代码（流式）
     * @param appID 应用Id
     * @param userMessage 用户消息
     * @return Vue 项目代码结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId Long appID, @UserMessage String userMessage);

    /**
     * AI自动生成应用名称
     * @param userMessage 用户消息
     * @return app名称
     */
    @SystemMessage(fromResource = "prompt/gen-appname-system-prompt.txt")
    String genAppName(String userMessage);
}
