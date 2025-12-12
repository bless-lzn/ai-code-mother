package com.limou.aicodemother.ai.model;

import jdk.jfr.Description;
import lombok.Data;

@Data
@Description("生成 HTML 代码文件的结果")
public class HtmlCodeResult {
    @Description("HTML代码")
    private String htmlCode;
    @Description("生成代码的描述")
    private String description;
}
