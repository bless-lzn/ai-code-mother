package com.limou.aicodemother.ai.core.saver;

import cn.hutool.core.util.StrUtil;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;

public class MultiFileCodeFileSaver extends CodeFileSaverTemplate<MultiFileCodeResult>{

    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult multiFileCodeResult, String baseDirPath) {
        writeToFile("index.html", baseDirPath, multiFileCodeResult.getHtmlCode());
        writeToFile("style.css", baseDirPath, multiFileCodeResult.getCssCode());
        writeToFile("script.js", baseDirPath, multiFileCodeResult.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult codeResult) {
        super.validateInput(codeResult);
        if(StrUtil.isBlank(codeResult.getHtmlCode())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "HTML代码不能为空");
        }
    }
}
