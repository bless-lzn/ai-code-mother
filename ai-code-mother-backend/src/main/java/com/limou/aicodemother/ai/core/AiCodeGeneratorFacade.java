package com.limou.aicodemother.ai.core;

import com.limou.aicodemother.ai.AiCodeGeneratorService;
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
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
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
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> throw new RuntimeException("不支持的生成类型");
        };
    }

    /**
     * 获取多个文件代码并保存
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        //1.调用AI
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);

        //2.得到Flue结果，将结果进行builder,整合和保存
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunked -> codeBuilder.append(chunked)).doOnComplete(() -> {
            try {
                String completeHtmlCode = codeBuilder.toString();
                File fileDir = CodeFileSaver.saveMultiFileCodeResult(CodeParser.parseMultiFileCode(completeHtmlCode));
            } catch (Exception e) {
                log.error("生成代码失败", e);
            }
        });
    }

    /**
     * 生成HTML代码并保存
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);

        //2.得到Flue结果，将结果进行builder,整合和保存
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunked -> codeBuilder.append(chunked)).doOnComplete(() -> {
            try {
                String completeHtmlCode = codeBuilder.toString();
                File fileDir = CodeFileSaver.saveHtmlCodeResult(CodeParser.parseHtmlCode(completeHtmlCode));
            } catch (Exception e) {
                log.error("生成代码失败", e);
            }
        });


    }


    /**
     * 生成HTML代码并保存
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成多个文件代码并保存
     */

    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

}
