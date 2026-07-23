package com.swl.jikeai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.swl.jikeai.ai.AiCodeGeneratorService;
import com.swl.jikeai.core.AiCodeGeneratorFacade;
import com.swl.jikeai.core.builder.VueProjectBuilder;
import com.swl.jikeai.core.handler.StreamHandlerExecutor;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.mapper.AppMapper;
import com.swl.jikeai.model.dto.app.AppQueryRequest;
import com.swl.jikeai.model.entity.App;
import com.swl.jikeai.model.entity.User;
import com.swl.jikeai.model.enums.ChatHistoryMessageTypeEnum;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import com.swl.jikeai.model.vo.AppVO;
import com.swl.jikeai.model.vo.UserVO;
import com.swl.jikeai.service.AppService;
import com.swl.jikeai.service.ChatHistoryService;
import com.swl.jikeai.service.ScreenshotService;
import com.swl.jikeai.service.UserService;
import com.swl.jikeai.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.swl.jikeai.constant.AppConstant.*;

/**
 * 应用 服务层实现。
 *
 * @author mubaodian
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private ScreenshotService screenshotService;


    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id无效");
        ThrowUtils.throwIf(StrUtil.isBlankIfStr(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        // 验证是否是本人
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        // 将用户消息添加到对话历史中
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 调用AI生成代码
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenType, appId);
        // 收集 AI 响应内容并在完成后记录到对话历史中
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenType);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id无效");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        // 验证是否是本人
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 检查是否已有 deployKey（有的话说明已部署，没有的话需要生成一个6位数的随机字符串）
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        // Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // 构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败，请稍后重试");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists() || !distDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录重命名为源目录
            sourceDir = distDir;
        }
        // 复制文件代码到部署目录
        String deployDirPath = CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败，请稍后重试:" + e.getMessage());
        }
        // 更新应用信息(部署标识和部署时间)
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用部署失败");
        // 构建应用访问 Url
        String deployUrl = StrUtil.format("{}/{}", CODE_DEPLOY_HOST, deployKey);
        // 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId,deployUrl);
        return deployUrl;
    }

    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl){
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() ->{
            // 使用截图服务生成截图并上传
            String cosUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(cosUrl);
            boolean result = this.updateById(updateApp);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            if (app.getUserId() != null) {
                UserVO userVO = userVOMap.get(app.getUserId());
                appVO.setUser(userVO);
            }
            return appVO;
        }).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public String getAppName(String userMessage) {
        String appNameJson = aiCodeGeneratorService.genAppName(userMessage);
        return JSONUtil.parseObj(appNameJson).getStr("name");
    }

    /**
     * 删除应用时关联删除对话历史（无论对话历史是否删除成功，都要保证应用删除，所以这里不用事务）
     *
     * @param id 数据主键
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用时关联删除对话历史失败:{}", e.getMessage());
        }
        // 再删除应用
        return super.removeById(id);
    }
}
