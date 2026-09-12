package com.swl.jikeai.langgraph4j.node;

import com.swl.jikeai.langgraph4j.ai.ImageCollectionPlanService;
import com.swl.jikeai.langgraph4j.ai.ImageCollectionService;
import com.swl.jikeai.langgraph4j.model.ImageCollectionPlan;
import com.swl.jikeai.langgraph4j.model.ImageResource;
import com.swl.jikeai.langgraph4j.state.WorkflowContext;
import com.swl.jikeai.langgraph4j.state.WorkflowState;
import com.swl.jikeai.langgraph4j.tools.ImageSearchTool;
import com.swl.jikeai.langgraph4j.tools.LogoGeneratorTool;
import com.swl.jikeai.langgraph4j.tools.MermaidDiagramTool;
import com.swl.jikeai.langgraph4j.tools.UndrawIllustrationTool;
import com.swl.jikeai.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 * 使用 AI 进行工具调用，收集不同类型的图片
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<WorkflowState> create() {
        return node_async(state -> {
            WorkflowContext context = state.getContext();
            String originalPrompt = context.getOriginalPrompt();
            List<ImageResource> collectedImages = new ArrayList<>();
            log.info("执行节点：图片收集");

            try {
                // 1. 获取图片收集计划
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 2. 并发执行各种图片收集任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
                // 并发执行内容图片搜索
                if (plan.getContentImageTasks() != null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }
                // 并发执行插画图片搜索
                if (plan.getIllustrationTasks() != null) {
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }
                // 并发执行架构图生成
                if (plan.getDiagramTasks() != null) {
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }
                // 并发执行Logo生成
                if (plan.getLogoTasks() != null) {
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                logoTool.generateLogos(task.description())));
                    }
                }
                // 3.等待所有任务完成并收集结果
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allTasks.join();
                // 4.遍历，收集所有结果
                for(CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();
                    if(images != null){
                        collectedImages.addAll(images);
                    }
                }
                log.info("并发图片收集完成，共收集到 {} 张图片",collectedImages.size());
            } catch (Exception e) {
                log.error("图片收集失败：{}", e.getMessage(),e);
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageList(collectedImages);

            // 返回更新后的上下文，框架会将返回出去的Map合并到state.data()中
            Map<String, Object> result = new HashMap<>();
            result.put(WorkflowState.WORKFLOW_CONTEXT_KEY, context);
            return result;
        });
    }
}
