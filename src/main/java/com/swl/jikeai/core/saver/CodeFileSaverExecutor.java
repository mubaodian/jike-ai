package com.swl.jikeai.core.saver;


import com.swl.jikeai.ai.model.HtmlCodeResult;
import com.swl.jikeai.ai.model.MultiFileCodeResult;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     *  执行代码保存
     * @param codeResult 代码结果对象
     * @param codeGenTypeEnum 代码生成类型
     * @return 保存的目录路径
     */
    public static File execuitSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult,appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult,appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum);
        };
    }
}
