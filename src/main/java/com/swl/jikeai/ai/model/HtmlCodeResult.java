package com.swl.jikeai.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 *  Html代码生成结果
 */
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {


    @Description(" HTML 代码")
    private String htmlCode;

    @Description(" HTML 代码文件的描述信息")
    private String description;
}
