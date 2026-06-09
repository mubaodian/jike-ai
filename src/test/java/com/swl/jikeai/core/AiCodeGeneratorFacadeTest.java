package com.swl.jikeai.core;

import com.swl.jikeai.ai.AiCodeGeneratorService;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

//    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("创建一个登录界面,不超过20行代码", CodeGenTypeEnum.MULTI_FILE,1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("创建一个登录界面,不超过20行代码", CodeGenTypeEnum.MULTI_FILE,1L);
        // 阻塞等待所有数据收集完成
        List<String> codeList = codeStream.collectList().block();
        Assertions.assertNotNull(codeList);
        String joinStr = String.join("", codeList);
        Assertions.assertNotNull(joinStr);
    }
}