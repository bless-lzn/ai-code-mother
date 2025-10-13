package com.limou.aicodemother.ai;

import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface AiCodeGeneratorService {
    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户输入
     * @return 生成的 HTML 代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 批量生成文件代码
     *
     * @param userMessage 用户输入
     * @return 生成的文件代码
     */
    @SystemMessage(fromResource = "prompt/codegen-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

}
