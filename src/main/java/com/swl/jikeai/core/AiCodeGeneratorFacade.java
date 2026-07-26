package com.swl.jikeai.core;

import cn.hutool.json.JSONUtil;
import com.swl.jikeai.ai.AiCodeGeneratorService;
import com.swl.jikeai.ai.AiCodeGeneratorServiceFactory;
import com.swl.jikeai.ai.model.HtmlCodeResult;
import com.swl.jikeai.ai.model.MultiFileCodeResult;
import com.swl.jikeai.ai.model.message.AiResponseMessage;
import com.swl.jikeai.ai.model.message.AiThinkingMessage;
import com.swl.jikeai.ai.model.message.ToolExecutedMessage;
import com.swl.jikeai.ai.model.message.ToolRequestMessage;
import com.swl.jikeai.core.parser.CodeParserExecutor;
import com.swl.jikeai.core.saver.CodeFileSaverExecutor;
import com.swl.jikeai.exception.BusinessException;
import com.swl.jikeai.exception.ErrorCode;
import com.swl.jikeai.model.enums.CodeGenTypeEnum;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     * 该方法作为代码生成的统一入口，根据传入的代码生成类型调用不同的代码生成服务，
     *
     * @param userMessage     用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        // 根据 appId 获取独立的 AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.execuitSaver(htmlCodeResult, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.execuitSaver(multiFileCodeResult, codeGenTypeEnum, appId);
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
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        // 根据 appId 获取独立的 AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> hmltCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield this.processCodeStream(hmltCodeStream, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield this.processCodeStream(multiFileCodeStream, codeGenTypeEnum, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield this.processTokenStream(tokenStream);
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
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
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
                        File file = CodeFileSaverExecutor.execuitSaver(parseResult, codeGenTypeEnum, appId);
                        log.info("保存成功，文件路径为{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存代码失败:{}", e.getMessage());
                    }
                });
    }

    /**
     * 将 TokenStream 转换为 Flux<String> ，并传递工具调用信息
     *
     * @param tokenStream TokenStream对象
     * @return 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse((String response) -> {
                AiResponseMessage aiResponseMessage = new AiResponseMessage(response);
                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
            })
                    .onPartialThinking((PartialThinking partialThinking) ->{
                        String data = partialThinking.text();
                        AiThinkingMessage aiThinkingMessage = new AiThinkingMessage(data);
                        sink.next(JSONUtil.toJsonStr(aiThinkingMessage));
                    })
                    .onPartialToolCall((PartialToolCall partialToolCall )-> {
                        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                                .id(partialToolCall.id())
                                .name(partialToolCall.name())
                                .arguments(partialToolCall.partialArguments())
                                .build();
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse chatResponse) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

}
