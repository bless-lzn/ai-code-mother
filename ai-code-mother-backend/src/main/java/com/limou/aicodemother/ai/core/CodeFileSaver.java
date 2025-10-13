package com.limou.aicodemother.ai.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.limou.aicodemother.ai.model.HtmlCodeResult;
import com.limou.aicodemother.ai.model.MultiFileCodeResult;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {
    //构建唯一的目标路径  tmp/code_output/bizType_雪花ID
    // 修改为
    private static final String FILE_SAVE_ROOT_DIR  = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_output";
    /**
     * 保存HtmlCodeResult代码
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile("index.html", baseDirPath, htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }



    /**
     * 保存MultiFileCodeResult代码
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile("index.html", baseDirPath, multiFileCodeResult.getHtmlCode());
        writeToFile("index.css", baseDirPath, multiFileCodeResult.getCssCode());
        writeToFile("script.js", baseDirPath, multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }


    /**
     * 生成唯一路径，参考雪花算法
     */

    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR  + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String fileName, String dirPath, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString( content,filePath,StandardCharsets.UTF_8);
    }

}
