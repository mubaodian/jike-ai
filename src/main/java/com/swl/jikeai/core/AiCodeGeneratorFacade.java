package com.swl.jikeai.core;

import com.swl.jikeai.ai.AiCodeGeneratorService;
import com.swl.jikeai.ai.model.HtmlCodeResult;
import com.swl.jikeai.ai.model.MultiFileCodeResult;
import com.swl.jikeai.core.parser.CodeParserExecutor;
import com.swl.jikeai.core.saver.CodeFileSaverExecutor;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成门面类，组合内容生成和保存文件功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.execuitSaver(htmlCodeResult, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.execuitSaver(multiFileCodeResult, codeGenTypeEnum);
            }
            default -> {
                String ErrorMessage = "不支持的代码生成类型：" + codeGenTypeEnum.getText();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrorMessage);
            }

        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件流
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> hmltCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield this.processCodeStream(hmltCodeStream, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield this.processCodeStream(multiFileCodeStream, codeGenTypeEnum);
            }
            default -> {
                String ErrorMessage = "不支持的代码生成类型：" + codeGenTypeEnum.getText();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrorMessage);
            }

        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时拼接代码片段
        return codeStream
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码到文件
                    try {
                        String completeCode = codeBuilder.toString();
                        // 解析代码
                        Object parseResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                        // 保存代码到文件
                        File file = CodeFileSaverExecutor.execuitSaver(parseResult, codeGenTypeEnum);
                        log.info("保存成功，文件路径为{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存代码失败:{}", e.getMessage());
                    }
                });
    }

}
