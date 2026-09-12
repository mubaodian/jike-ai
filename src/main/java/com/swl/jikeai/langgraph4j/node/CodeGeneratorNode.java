package com.swl.jikeai.langgraph4j.node;

import com.swl.jikeai.constant.AppConstant;
import com.swl.jikeai.core.AiCodeGeneratorFacade;
import com.swl.jikeai.langgraph4j.model.QualityResult;
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

            // 构造用户消息（包含原始提示词和可能的错误修复信息）
            String userMessage = buildUserMessage(context);
            // 获取生成类型
            CodeGenTypeEnum generationType = context.getGenerationType();

            // 获取 AI 代码生成门面类服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型：{}({})", generationType.getValue(), generationType.getText());
            // 使用固定的 appId（后续在整合到业务中）
            Long appId = 2L;
            // 调用流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            // 同步等待流式输出完成
            String last = codeStream.blockLast(Duration.ofMinutes(10));
            log.info("blockLast 返回: {}", last);
            // 根据类型设置生成目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录：{}", generatedCodeDir);

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

    /**
     * 重新构造用户消息，如果存在质检失败则添加错误信息
     * @param context 工作流上下文
     * @return 用户消息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityFailed(qualityResult)) {
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     *
     * @param qualityResult 质检结果
     * @return 布尔值
     */
    private static boolean isQualityFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }
}
