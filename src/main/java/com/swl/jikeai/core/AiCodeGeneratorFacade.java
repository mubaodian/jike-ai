package com.swl.jikeai.core;

import com.swl.jikeai.ai.AiCodeGeneratorService;
import com.swl.jikeai.ai.model.HtmlCodeResult;
import com.swl.jikeai.ai.model.MultiFileCodeResult;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI 代码生成门面类，组合内容生成和保存文件功能
 */
@Service
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage 用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> this.generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> this.generateAndSaveMultiFileCode(userMessage);
            default -> {
                String ErrorMessage = "不支持的代码生成类型：" + codeGenTypeEnum.getText();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrorMessage);
            }

        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     * @param userMessage 用户消息
     * @return 生成的 HTML 文件路径
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成多文件模式代码并保存
     * @param userMessage 用户消息
     * @return 生成的多文件模式代码文件路径
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

}
