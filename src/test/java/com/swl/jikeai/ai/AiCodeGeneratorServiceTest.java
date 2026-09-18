package com.swl.jikeai.ai;

import com.swl.jikeai.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Slf4j
class AiCodeGeneratorServiceTest {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

//    @Test

    /// /    void generateHtmlCode(){
    /// /        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做一个程序员博客，20行代码");
    /// /        Assertions.assertNotNull(result);
    /// /    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做一个留言板，30行代码");
        Assertions.assertNotNull(result);
    }


}