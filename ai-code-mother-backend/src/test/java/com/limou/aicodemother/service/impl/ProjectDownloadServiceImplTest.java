package com.limou.aicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDownloadServiceImplTest {

    @Test
    void downloadProjectAsZip() {

        ProjectDownloadServiceImpl projectDownloadService = new ProjectDownloadServiceImpl();
        projectDownloadService.isPathAllowed(Paths.get("D:\\idea-study\\ai-code-mother-all\\ai-code-mother-backend\\tmp\\code_output"),        Paths.get("D:\\idea-study\\ai-code-mother-all\\ai-code-mother-backend\\tmp\\code_output\\vue_project_1\\index.html"));
    }

}