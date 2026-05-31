package com.swl.jikeai.core.parser;

/**
 * 代码解析器策略接口
 * @param <T>
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     * @param codeContent 原代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
