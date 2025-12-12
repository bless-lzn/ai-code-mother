package com.limou.aicodemother.ai.core;

import cn.hutool.json.JSONUtil;
import com.limou.aicodemother.ai.AiCodeGeneratorService;
import com.limou.aicodemother.ai.AiCodeGeneratorServiceFactory;
import com.limou.aicodemother.ai.core.builder.VueProjectBuilder;
import com.limou.aicodemother.ai.core.parser.CodeParserExecutor;
import com.limou.aicodemother.ai.core.saver.CodeFileSaverExecutor;
import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.ai.model.message.AiResponseMessage;
import com.limou.aicodemother.ai.model.message.ToolExecutedMessage;
import com.limou.aicodemother.ai.model.message.ToolRequestMessage;
import com.limou.aicodemother.constant.AppConstant;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.exception.ThrowUtils;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面类,组合生成和保存
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    //传入用户信息和生成类型

    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        //判断codeGenTypeEnum是否为空
        if (codeGenTypeEnum == null) {
            throw new RuntimeException("生成类型不能为空");
        }
        ThrowUtils.throwIf(appId==null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        //switch
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> throw new RuntimeException("不支持的生成类型");
        };
    }

    /**
     * 获取多个文件代码并保存
     */

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        //判断codeGenTypeEnum是否为空
        if (codeGenTypeEnum == null) {
            throw new RuntimeException("生成类型不能为空");
        }
        ThrowUtils.throwIf(appId==null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        //switch
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield this.processCodeStream(result, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield this.processCodeStream(result, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield this.processTokenStream(tokenStream,appId);
            }
            default -> throw new RuntimeException("不支持的生成类型");
        };
    }




    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream,Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        String projectPath= AppConstant.CODE_OUTPUT_ROOT_DIR+"/vue_project_"+appId;
                        //改为同步构造，不在添加历史消息的时候再次进行构建
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }



    /**
     * 处理代码流
     * @param codeStream
     * @param codeGenType
     * 通用的流式代码处理方法
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appId) {
        ThrowUtils.throwIf(appId==null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        //2.得到Flue结果，将结果进行builder,整合和保存
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunked -> codeBuilder.append(chunked)).doOnComplete(() -> {
            try {
                String completeHtmlCode = codeBuilder.toString();
//                解析文件
                Object parserResult = CodeParserExecutor.executeParser(completeHtmlCode, codeGenType);
//                File fileDir = CodeFileSaver.saveMultiFileCodeResult(CodeParser.parseMultiFileCode(completeHtmlCode));
//                文件保存器
                File fileDir = CodeFileSaverExecutor.executeSaver(parserResult, codeGenType,appId);
            } catch (Exception e) {
                log.error("生成代码失败", e);
            }
        });
    }


    /**
     * 获取多个文件代码并保存
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage,Long appId) {
        //1.调用AI
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        return processCodeStream(result, CodeGenTypeEnum.MULTI_FILE,appId);
    }

    /**
     * 生成HTML代码并保存
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage,Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        return this.processCodeStream(result, CodeGenTypeEnum.HTML,appId);
    }


    /**
     * 生成HTML代码并保存
     */
    private File generateAndSaveHtmlCode(String userMessage,Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML,appId);
    }

    /**
     * 生成多个文件代码并保存
     */

    private File generateAndSaveMultiFileCode(String userMessage,Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
    }

}
