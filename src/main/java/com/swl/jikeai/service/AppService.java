package com.swl.jikeai.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.swl.jikeai.model.dto.app.AppQueryRequest;
import com.swl.jikeai.model.entity.App;
import com.swl.jikeai.model.entity.User;
import com.swl.jikeai.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author mubaodian
 */
public interface AppService extends IService<App> {
    /**
     * 与ai对话生成应用代码
     * @param appId 应用id
     * @param message 用户提示词
     * @param loginUser 登录用户
     * @return 代码流
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 应用部署
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 部署标识
     */
    String deployApp(Long appId,User loginUser);

    /**
     * 异步生成应用截图并更新封面
     * @param appId 应用ID
     * @param appUrl 应用访问URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 获取脱敏后的app信息
     *
     * @param app 应用
     * @return 脱敏后的app
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后的app信息列表
     * @param appList 应用列表
     * @return  脱敏后的app列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询构造器
     *
     * @param appQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * AI自动获取应用名称
     * @param userMessage 用户提示词
     * @return 应用名称
     */
    String getAppName(String userMessage);
}
