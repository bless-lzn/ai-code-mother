package com.limou.aicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.exception.ThrowUtils;
import com.limou.aicodemother.manager.CosManager;
import com.limou.aicodemother.model.entity.App;
import com.limou.aicodemother.service.ScreenshotService;
import com.limou.aicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        //校验参数
        ThrowUtils.throwIf(webUrl == null, ErrorCode.PARAMS_ERROR, "webUrl不能为空");
        //调用截图工具
        String cosUrl = null;

        String compressedImagePath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        try {
            //将得到的压缩路径上传到cos对象存储
            cosUrl = uploadScreenshotToCos(compressedImagePath);
            return cosUrl;
        } finally {
            //清理本地文件
            cleanupLocalFiles(compressedImagePath);
        }

    }

    private String uploadScreenshotToCos(String compressedImagePath) {

        ThrowUtils.throwIf(compressedImagePath == null, ErrorCode.SYSTEM_ERROR, "压缩图片路径不能为空");
        //这个key的构造 todo
        String fileName = RandomUtil.randomNumbers(5) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, FileUtil.file(compressedImagePath));
    }

    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = String.format("screenshot/%s/%s", datePath, fileName);
        return key;
    }

    private void cleanupLocalFiles(String compressedImagePath) {
        try {
            String parent = FileUtil.getParent(compressedImagePath, 1);
            if (FileUtil.file(parent).exists()) {
                FileUtil.del(parent);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败");
        }
    }
}
