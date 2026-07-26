package com.swl.jikeai.ai;

import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户需求选择代码生成类型
     * @param userPrompt 用户提示词
     * @return 代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
