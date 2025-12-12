package com.limou.aicodemother.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

public class CacheKeyUtils {

    /**
     * 获取缓存的key
     *为啥不用对象的哈希值进行缓存？
     * 答：因为对象的哈希值可能重复，并且对象的哈希值是对象的与对象的内容关系不大。
     * @param obj 传入查询的对象
     * @return
     */

    public static String getCacheKey(Object obj) {
        //1,判断
        if (obj == null) {
            return DigestUtil.md5Hex("null");
        }
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
