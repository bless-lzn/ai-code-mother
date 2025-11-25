package com.limou.aicodemother.ai.core.saver;

import com.limou.aicodemother.ai.core.CodeFileSaver;
import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;

import java.io.File;

public class CodeFileSaverExecutor {
    //多态进行匹配
    private static final CodeFileSaverTemplate<HtmlCodeResult> htmlCodeFileSaver = new HtmlCodeFileSaver();
    private static final CodeFileSaverTemplate<MultiFileCodeResult> multiFileCodeFileSaver = new MultiFileCodeFileSaver();

    /**
     * 执行保存
     *
     * @param codeResult
     * @param codeGenType
     * @return
     */

    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        switch (codeGenType) {
            case HTML:
                return CodeFileSaver.saveHtmlCodeResult((HtmlCodeResult) codeResult);
            case MULTI_FILE:
                return CodeFileSaver.saveMultiFileCodeResult((MultiFileCodeResult) codeResult);
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成模式");
        }
    }
}
