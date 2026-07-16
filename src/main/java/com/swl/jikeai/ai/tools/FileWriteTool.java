package com.swl.jikeai.ai.tools;

import com.swl.jikeai.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileWriteTool {

    @Tool("Write a file to a specific path")
    public String writeFile(@P("Relative path of the file")String relativeFilePath, @P("Content to be written to the file")String content, @ToolMemoryId Long appId){
        try {
            Path path = Paths.get(relativeFilePath);
            if(!path.isAbsolute()){
                // 把相对路径转为绝对路径，创建基于appId的项目目录
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            // 创建父目录（如果不存在）
            Path parentDir = path.getParent();
            if(parentDir != null){
                Files.createDirectories(parentDir);
            }
            // 写入文件内容
            Files.write(path,content.getBytes(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件：{}",path.toAbsolutePath());
            // 注意返回相对路径，不能让AI把文件绝对路径返回给用户
            return "文件写入成功：" + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "写入文件失败：" + relativeFilePath + "，错误：" + e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }
}
