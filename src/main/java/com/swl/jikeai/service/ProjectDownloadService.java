package com.swl.jikeai.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务
 */
public interface ProjectDownloadService {

    /**
     *  下载压缩包
     * @param projectPath 项目根路径
     * @param downloadFileName 下载文件名
     * @param httpServletResponse httpServletResponse
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse httpServletResponse);
}
