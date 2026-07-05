package com.swl.jikeai.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.swl.jikeai.model.dto.chathistory.ChatHistoryQueryRequest;
import com.swl.jikeai.model.entity.ChatHistory;
import com.swl.jikeai.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author mubaodian
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     * @param appId 应用Id
     * @param message 消息内容
     * @param messageType 消息类型
     * @param userId 用户Id
     * @return 是否成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 删除对话历史
     * @param appId 应用Id
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询包装类
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
}
