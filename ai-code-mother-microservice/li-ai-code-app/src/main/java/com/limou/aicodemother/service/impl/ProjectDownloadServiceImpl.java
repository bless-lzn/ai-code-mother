package com.limou.aicodemother.service.impl;

import cn.hutool.core.util.ZipUtil;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.exception.ThrowUtils;
import com.limou.aicodemother.service.ProjectDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    /**
     * 检查路径是否允许包含在压缩包中
     *
     * @param projectRoot 项目根目录
     * @param fullPath    完整路径
     * @return 是否允许
     */
    public boolean isPathAllowed(Path projectRoot, Path fullPath) {
        // 获取相对路径
        Path relativePath = projectRoot.relativize(fullPath);
        // 检查路径中的每一部分
        for (Path part : relativePath) {
            String partName = part.toString();
            // 检查是否在忽略名称列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            // 检查文件扩展名
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        //校验参数
        ThrowUtils.throwIf(projectPath == null, ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        ThrowUtils.throwIf(downloadFileName == null, ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是目录");
        log.info("开始下载项目: {}", projectPath);
        //设置Http响应头---响应类型为zip
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", downloadFileName));
        //定义文本过滤器  ------会多次调用
        FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());
        try {
            log.info("开始压缩项目: {}", projectPath);
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
        } catch (IOException e) {
            log.error("压缩项目失败: {}", projectPath, e);
            throw new RuntimeException("压缩项目失败");
        }
    }
}
