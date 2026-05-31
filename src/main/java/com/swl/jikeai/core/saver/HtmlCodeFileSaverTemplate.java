package com.swl.jikeai.core.saver;

import cn.hutool.core.util.StrUtil;
import com.swl.jikeai.ai.model.HtmlCodeResult;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected void saveFiles(String dirPath, HtmlCodeResult codeResult) {
        writeToFile(dirPath, "index.html", codeResult.getHtmlCode());
    }

    @Override
    protected String getBizType() {
        return CodeGenTypeEnum.HTML.getValue();
    }

    @Override
    protected void validateInput(HtmlCodeResult codeResult) {
        super.validateInput(codeResult);
        if (StrUtil.isBlank(codeResult.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML 不能为空");
        }
    }
}
