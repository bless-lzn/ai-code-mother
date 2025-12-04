package com.limou.aicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.exception.ThrowUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

@Slf4j
public class WebScreenshotUtils {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    public static String saveWebPageScreenshot(String webUrl) {
        //校验参数
        ThrowUtils.throwIf(webUrl == null, ErrorCode.PARAMS_ERROR, "webUrl不能为空");
        try {
            //创建临时文件目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots" + File.separator + RandomUtil.randomString(8);

            FileUtil.mkdir(rootPath);
            String imagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + ".png";
            //访问网页,todo,并发环境下会出现截错图的现象。
            webDriver.get(webUrl);
            //等待页面加载完成
            waitForPageLoad(webDriver);
            //截图
            byte[] imagesBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            //保存文件
            saveImage(imagesBytes, imagePath);
            log.info("图片保存成功：{}", imagePath);
            //压缩文件
            //生成压缩文件路径
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(imagePath, compressedImagePath);
            log.info("图片压缩成功：{}", compressedImagePath);
            //删除原始文件，保留压缩文件
            FileUtil.del(imagePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("保存图片失败", e);
            return null;
        }
    }


    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    private static void saveImage(byte[] imagesBytes, String imagePath) {
        try {
            //加载图片到指定位置
            FileUtil.writeBytes(imagesBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    private static void compressImage(String sourceImagePath, String targetImagePath) {
        final float quality = 0.3f;
        try {
            //加载图片到指定位置
            ImgUtil.compress(FileUtil.file(sourceImagePath), FileUtil.file(targetImagePath), quality);
        } catch (IORuntimeException e) {
            log.error("压缩图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    private static void cleanTempFiles(){
        FileUtil.clean(System.getProperty("user.dir") + File.separator + "tmp"+ File.separator + "screenshots");
    }


}
