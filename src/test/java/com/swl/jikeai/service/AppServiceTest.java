package com.swl.jikeai.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppServiceTest {

    @Resource
    private AppService appService;

    @Test
    void getAppName() {
        String appName = appService.getAppName("做一个贪吃蛇小游戏，30行代码");
        System.out.println(appName);
        Assertions.assertNotNull(appName);
    }
}