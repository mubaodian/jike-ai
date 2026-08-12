package com.swl.jikeai.langgraph4j.node;

import com.swl.jikeai.constant.AppConstant;
import com.swl.jikeai.core.AiCodeGeneratorFacade;
import com.swl.jikeai.langgraph4j.state.WorkflowContext;
import com.swl.jikeai.langgraph4j.state.WorkflowState;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import com.swl.jikeai.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<WorkflowState> create() {
        return node_async(state -> {
            WorkflowContext context = state.getContext();
            log.info("执行节点: 代码生成");

            // 使用增强提示词作为发给 AI 的用户消息
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum generationType = context.getGenerationType();

            // 获取 AI 代码生成门面类服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型：{}({})",generationType.getValue(),generationType.getText());
            // 使用固定的 appId（后续在整合到业务中）
            Long appId = 4L;
            // 调用流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            // 同步等待流式输出完成
            String last = codeStream.blockLast(Duration.ofMinutes(10));
            log.info("blockLast 返回: {}", last);
            // 根据类型设置生成目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,generationType.getValue(),appId);
            log.info("AI 代码生成完成，生成目录：{}",generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);

            // 返回更新后的上下文
            Map<String, Object> result = new HashMap<>();
            result.put(WorkflowState.WORKFLOW_CONTEXT_KEY, context);
            return result;
        });
    }
}
