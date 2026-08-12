package com.swl.jikeai.langgraph4j.state;

import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流状态 - 自定义 AgentState 实现
 * 避免使用 MessagesState 的自动消息管理，只维护 WorkflowContext
 */
public class WorkflowState extends AgentState {

    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 构造函数，用于初始化 WrokflowState 对象
     * @param initData 包含初始数据的 Map 集合
     */
    public WorkflowState(Map<String, Object> initData) {
        super(initData);
    }

    /**
     * 无参构造函数 - 创建新的空状态
     */
    public WorkflowState() {
        super(new HashMap<>());
    }

    /**
     * 从状态中获取 WorkflowContext
     */
    public WorkflowContext getContext() {
        return (WorkflowContext) this.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 将 WorkflowContext 更新到状态中（更新同一个 Map 实例）
     */
    public void setContext(WorkflowContext context) {
        this.data().put(WORKFLOW_CONTEXT_KEY, context);
    }
}
