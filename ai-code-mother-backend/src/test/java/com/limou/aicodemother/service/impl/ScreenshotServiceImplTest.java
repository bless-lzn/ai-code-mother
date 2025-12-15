package com.limou.aicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.exception.ThrowUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ScreenshotServiceImplTest {
    @Resource
    private ScreenshotServiceImpl screenshotServiceImpl;
    @Test
    public void generateAndUploadScreenshot() {
        String url = "https://cn.bing.com/";
        String result = screenshotServiceImpl.uploadScreenshotToCos("D:/idea-study/ai-code-mother-all/ai-code-mother-backend/tmp/screenshots/DJxZmU9f/29350_compressed.jpg");
        System.out.println(result);
    }



}