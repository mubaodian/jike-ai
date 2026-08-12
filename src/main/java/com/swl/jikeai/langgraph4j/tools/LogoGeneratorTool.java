package com.swl.jikeai.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.swl.jikeai.langgraph4j.model.ImageResource;
import com.swl.jikeai.langgraph4j.model.enums.ImageCategoryEnum;
import com.swl.jikeai.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logo图片生成工具
 */
@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model}")
    private String imageModel;

    @Resource
    private CosManager cosManager;

    @Tool("根据秒速生成Logo设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();

        try {
            // 构建 Logo 设计提示词
            String logoPropmt = String.format("生成 Logo，Logo中禁止包含任何文字！ Logo介绍：%s", description);
            ImageSynthesisParam param =
                    ImageSynthesisParam.builder()
                            .apiKey(dashScopeApiKey)
                            .model(imageModel)
                            .prompt(logoPropmt)
                            .n(1)
                            .size("512*512")
                            .build();

            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for (Map<String, String> map : results) {
                    String imageUrl = map.get("url");
                    if(StrUtil.isNotBlank(imageUrl)){
                        // 下载图片到临时文件
                        File tempLogoFile = FileUtil.createTempFile("logo_", ".png", true);
                        HttpUtil.downloadFile(imageUrl, tempLogoFile);
                        // 上传到COS
                        String keyName = String.format("/logo/%s/%s",
                                RandomUtil.randomString(5), tempLogoFile.getName());
                        String cosUrl = cosManager.uploadFile(keyName, tempLogoFile);
                        // 清理临时文件
                        FileUtil.del(tempLogoFile);
                        if (StrUtil.isNotBlank(cosUrl)) {
                            logoList.add(ImageResource.builder()
                                    .category(ImageCategoryEnum.LOGO)
                                    .description(description)
                                    .url(cosUrl)
                                    .build());
                        }
                    }
                }
            }
        } catch (NoApiKeyException e) {
            log.error("生成 Logo 失败：{}",e.getMessage());
        }
        return logoList;
    }
}
