package com.limou.aicodemother.ai.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum CodeGenTypeEnum {
    HTML("原生 HTML 模式","html"),
    MULTI_FILE("多文件模式","multi-file");

    private final String text;
    private final String value;
    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 根据value获取枚举项
     * @param value
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for (CodeGenTypeEnum item : CodeGenTypeEnum.values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
