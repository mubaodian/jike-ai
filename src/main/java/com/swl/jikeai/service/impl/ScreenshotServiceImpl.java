package com.swl.jikeai.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.manager.CosManager;
import com.swl.jikeai.service.ScreenshotService;
import com.swl.jikeai.utils.ThrowUtils;
import com.swl.jikeai.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.OPERATION_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL：{}", webUrl);
        // 生成截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        log.info("本地截图生成成功，路径：{}", localScreenshotPath);
        // 上传截图
        try {
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图上传成功，{} -> {}", webUrl, cosUrl);
            return cosUrl;
        } finally {
            // 清理本地文件
            cleanuoLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回 null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        // 检验参数
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在：{}", localScreenshotPath);
            return null;
        }
        // 生成 COS 对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        // 上传截图到 COS
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2026/7/23/filename.jpg
     *
     * @param fileName 文件名
     * @return 对象存储键
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     * @param localFilePath 本地文件路径
     */
    private void cleanuoLocalFile(String localFilePath){
        File file = new File(localFilePath);
        if (file.exists()){
            File parentDir = file.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理：{}",localFilePath);
        }
    }

}
