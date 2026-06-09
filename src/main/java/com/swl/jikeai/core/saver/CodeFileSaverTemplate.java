package com.swl.jikeai.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.swl.jikeai.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * 抽象代码文件保存器 - 模板方法类
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件根目录
    protected static final String FILE_SAVE_ROOT_DIR = CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法：保存代码的标准流程
     *
     * @param codeResult 代码结果对象
     * @return 保存的目录
     */
    public final File saveCode(T codeResult,Long appId) {
        // 校验输出
        validateInput(codeResult);
        // 构建唯一的目录路径
        String baseDirPath = buildUniqueDir(appId);
        // 保存文件
        saveFiles(baseDirPath, codeResult);
        // 返回文件
        return new File(baseDirPath);
    }

    /**
     * 校验输出结果,子类支持自定义校验逻辑
     *
     * @param codeResult 代码结果对象
     */
    protected void validateInput(T codeResult) {
        if (codeResult == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码输出结果为空");
        }
    }


    /**
     * 构建唯一的目录路径：根目录+业务类型_雪花ID
     *
     * @return 唯一的目录路径
     */
    protected String buildUniqueDir(Long appId) {
        String bizType = getBizType();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     *
     * @param dirPath  目录路径
     * @param fileName 文件名
     * @param content  文件内容
     */
    protected void writeToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 保存文件的具体实现，子类需要根据业务类型实现不同的文件保存逻辑
     *
     * @param dirPath    目录路径
     * @param codeResult 代码结果对象
     */
    protected abstract void saveFiles(String dirPath, T codeResult);

    /**
     * 获取业务类型，子类需要实现
     *
     * @return 业务类型
     */
    protected abstract String getBizType();
}
