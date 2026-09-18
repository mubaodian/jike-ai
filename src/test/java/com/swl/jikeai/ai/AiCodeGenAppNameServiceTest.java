package com.swl.jikeai.ai;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class AiCodeGenAppNameServiceTest {

    @Resource
    private AiCodeGenAppNameService aiCodeGenAppNameService;

    @Test
    void genAppName() {
        String appName = aiCodeGenAppNameService.genAppName("创建一个个人博客网站，需要展示文章列表、详情页、分类功能、时间归档、搜索功能、评论区、关于我页面等。使用现代化的设计风格，深色主题。左侧导航栏显示分类，右侧主区域展示文章。支持Markdown格式的文章展示和代码高亮。");
        log.info("app name: {}", appName);
        Assertions.assertNotNull(appName);
    }
}