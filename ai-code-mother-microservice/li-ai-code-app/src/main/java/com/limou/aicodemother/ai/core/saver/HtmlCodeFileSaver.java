package com.limou.aicodemother.ai.core.saver;

import cn.hutool.core.util.StrUtil;
import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;

public class HtmlCodeFileSaver extends CodeFileSaverTemplate<HtmlCodeResult>{
    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult htmlCodeResult, String baseDirPath) {
        super.writeToFile("index.html", baseDirPath, htmlCodeResult.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult codeResult) {
        super.validateInput(codeResult);
        if(StrUtil.isBlank(codeResult.getHtmlCode())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "HTML代码不能为空");
        }
    }
}
