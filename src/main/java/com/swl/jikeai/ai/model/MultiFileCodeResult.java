package com.swl.jikeai.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 *  多文件代码生成结果
 */
@Description("生成多文件代码的结果")
@Data
public class MultiFileCodeResult {

    @Description(" HTML 代码")
    private String htmlCode;

    @Description(" CSS 代码")
    private String cssCode;

    @Description(" JS 代码")
    private String jsCode;

    @Description(" 多文件代码的描述信息")
    private String description;
}
