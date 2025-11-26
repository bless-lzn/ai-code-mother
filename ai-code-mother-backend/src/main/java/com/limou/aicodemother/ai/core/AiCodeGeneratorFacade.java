package com.limou.aicodemother.ai.core;

import com.limou.aicodemother.ai.AiCodeGeneratorService;
import com.limou.aicodemother.ai.core.parser.CodeParserExecutor;
import com.limou.aicodemother.ai.core.saver.CodeFileSaverExecutor;
import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
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
    private AiCodeGeneratorService aiCodeGeneratorService;

    //传入用户信息和生成类型


    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        //判断codeGenTypeEnum是否为空
        if (codeGenTypeEnum == null) {
            throw new RuntimeException("生成类型不能为空");
        }
        //switch
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> throw new RuntimeException("不支持的生成类型");
        };
    }

    /**
     * 获取多个文件代码并保存
     */

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        //判断codeGenTypeEnum是否为空
        if (codeGenTypeEnum == null) {
            throw new RuntimeException("生成类型不能为空");
        }
        //switch
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield this.processCodeStream(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield this.processCodeStream(result, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> throw new RuntimeException("不支持的生成类型");
        };
    }


    /**
     * 获取多个文件代码并保存
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {
        //2.得到Flue结果，将结果进行builder,整合和保存
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunked -> codeBuilder.append(chunked)).doOnComplete(() -> {
            try {
                String completeHtmlCode = codeBuilder.toString();
//                解析文件
                Object parserResult = CodeParserExecutor.executeParser(completeHtmlCode, codeGenType);
//                File fileDir = CodeFileSaver.saveMultiFileCodeResult(CodeParser.parseMultiFileCode(completeHtmlCode));
//                文件保存器
                File fileDir = CodeFileSaverExecutor.executeSaver(parserResult, codeGenType);
            } catch (Exception e) {
                log.error("生成代码失败", e);
            }
        });
    }


    /**
     * 获取多个文件代码并保存
     */
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        //1.调用AI
//        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        return processCodeStream(result, CodeGenTypeEnum.MULTI_FILE);
//    }

    /**
     * 生成HTML代码并保存
     */
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
//        return this.processCodeStream(result, CodeGenTypeEnum.HTML);
//    }


    /**
     * 生成HTML代码并保存
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML);
    }

    /**
     * 生成多个文件代码并保存
     */

    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE);
    }

}
