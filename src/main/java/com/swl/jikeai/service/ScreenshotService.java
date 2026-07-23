package com.swl.jikeai.service;

/**
 * 截图服务
 */
public interface ScreenshotService {

    /**
     * 生成本地截图并上传 COS 对象存储
     * @param webUrl 网页Url
     * @return 可访问的对象存储Url
     */
    String generateAndUploadScreenshot(String webUrl);
}
