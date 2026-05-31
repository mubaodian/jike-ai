package com.swl.jikeai.core.saver;

import cn.hutool.core.util.StrUtil;
import com.swl.jikeai.ai.model.MultiFileCodeResult;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;

public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected void saveFiles(String dirPath, MultiFileCodeResult codeResult) {
        writeToFile(dirPath, "index.html", codeResult.getHtmlCode());
        writeToFile(dirPath, "style.css", codeResult.getCssCode());
        writeToFile(dirPath, "script.js", codeResult.getJsCode());
    }

    @Override
    protected String getBizType() {
        return CodeGenTypeEnum.MULTI_FILE.getValue();
    }

    @Override
    protected void validateInput(MultiFileCodeResult codeResult) {
        super.validateInput(codeResult);
        // 至少有HTML代码，CSS和JS可以为空
        if (StrUtil.isBlank(codeResult.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML 不能为空");
        }
    }
}
