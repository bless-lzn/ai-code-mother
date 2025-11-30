package com.limou.aicodemother.ai.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;


public abstract class CodeFileSaverTemplate<T> {
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_output";

    /**
     * 保存代码
     *
     * @param codeResult 代码结果
     * @return 文件目录对象
     */
    public final File saveCode(T codeResult,Long appId) {
        //1.验证输入
        validateInput(codeResult);
        //2.构建唯一目录
        String baseDirPath = buildUniqueDir(appId);
        //3.保存代码(具体实现交给子类)
        saveFiles(codeResult, baseDirPath);
        //4.返回文件目录对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入
     * codeResult 输入的内容
     */
    protected void validateInput(T codeResult) {
        if (codeResult == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码结果不能为空");
        }
    }

    /**
     * 构建唯一目录
     */
    private String buildUniqueDir(Long appId) {
        String bizType = getCodeGenType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }
    /**
     * 写入单个文件
     */
    protected  void writeToFile(String fileName, String dirPath, String content) {
        String filePath = dirPath + File.separator + fileName;

        FileUtil.writeString( content,filePath, StandardCharsets.UTF_8);
    }


    /**
     * 获取生成类型
     */
    protected abstract CodeGenTypeEnum getCodeGenType();

    /**
     * 保存文件
     */
    protected abstract void saveFiles(T codeResult, String baseDirPath);

}
