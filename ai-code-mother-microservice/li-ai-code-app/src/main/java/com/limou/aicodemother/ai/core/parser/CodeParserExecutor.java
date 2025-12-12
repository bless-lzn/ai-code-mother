package com.limou.aicodemother.ai.core.parser;

import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;

public class CodeParserExecutor {

    private static final CodeParser<HtmlCodeResult> HTML_CODE_PARSER = new HtmlCodeParser();
    private static final CodeParser<MultiFileCodeResult> MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType){
        switch (codeGenType){
            case HTML:
                return HTML_CODE_PARSER.parseCode(codeContent);
            case MULTI_FILE:
                return MULTI_FILE_CODE_PARSER.parseCode(codeContent);
            default:
                return new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成模式");
        }
    }

}
