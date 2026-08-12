package com.swl.jikeai.langgraph4j.node;

import com.swl.jikeai.langgraph4j.ai.ImageCollectionService;
import com.swl.jikeai.langgraph4j.state.WorkflowContext;
import com.swl.jikeai.langgraph4j.state.WorkflowState;
import com.swl.jikeai.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.HashMap;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 * 使用 AI 进行工具调用，收集不同类型的图片
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<WorkflowState> create(){
        return node_async(state ->{
            WorkflowContext context = state.getContext();
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            log.info("执行节点：图片收集");

            try {
                // 获取 AI 图片收集服务
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                // 使用 AI 服务进行智能图片收集
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败：{}",e.getMessage());
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);

            // 返回更新后的上下文，框架会将返回出去的Map合并到state.data()中
            Map<String, Object> result = new HashMap<>();
            result.put(WorkflowState.WORKFLOW_CONTEXT_KEY, context);
            return result;
        });
    }
}
