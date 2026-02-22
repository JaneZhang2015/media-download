package com.media;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * 媒体下载器Pro - 支持动态加载内容
 * 使用Selenium自动化浏览器来获取JavaScript动态加载的媒体
 */
public class MediaDownloaderPro {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int WAIT_TIMEOUT = 30; // 秒

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String url = args[0];
        String outputDir = args.length > 1 ? args[1] : "./downloads";
        String mode = args.length > 2 ? args[2] : "dynamic"; // dynamic 或 static

        try {
            if ("static".equalsIgnoreCase(mode)) {
                System.out.println("使用静态解析模式");
                MediaDownloaderStandalone.downloadMediaFromUrl(url, outputDir);
            } else {
                System.out.println("使用动态加载模式（Selenium Browser）");
                downloadMediaWithSelenium(url, outputDir);
            }
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 使用Selenium动态加载页面并获取媒体URL
     */
    public static void downloadMediaWithSelenium(String url, String outputDir) throws Exception {
        System.out.println("初始化Selenium WebDriver...");
        
        // 自动下载ChromeDriver - 指定版本为145
        try {
            WebDriverManager.chromedriver()
                    .browserVersion("145")
                    .setup();
        } catch (Exception e) {
            System.out.println("使用指定版本失败，尝试使用最新缓存的版本...");
            try {
                // 尝试使用缓存的最新版本
                WebDriverManager.chromedriver()
                        .forceDownload()
                        .avoidResolutionCache()
                        .setup();
            } catch (Exception e2) {
                System.out.println("版本指定失败，尝试自动检测系统ChromeDriver...");
                // 最后尝试：使用系统上已有的ChromeDriver或浏览器版本
                System.out.println("警告：WebDriverManager初始化失败 - " + e2.getMessage());
                System.out.println("请确保已安装Chrome浏览器，并且系统PATH中包含chromedriver");
                throw new RuntimeException("无法初始化WebDriver", e2);
            }
        }

        // 配置Chrome选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=" + USER_AGENT);
        // 可选：无头模式（不显示浏览器窗口）
        // options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);

        try {
            System.out.println("打开网页: " + url);
            driver.get(url);

            // 等待页面加载5秒
            Thread.sleep(5000);

            // 获取页面源代码
            String pageSource = driver.getPageSource();

            // 创建输出目录
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            // 方法1: 从页面源代码中提取媒体URL
            System.out.println("\n[方法1] 从页面源代码提取媒体URL");
            Set<String> mediaUrls = extractMediaUrls(pageSource, url);

            // 方法2: 执行JavaScript获取媒体对象
            System.out.println("\n[方法2] 执行JavaScript获取媒体信息");
            Set<String> jsMediaUrls = extractMediaViaJavaScript(driver);
            mediaUrls.addAll(jsMediaUrls);

            // 方法3: 查找页面中的<audio>和<video>标签
            System.out.println("\n[方法3] 查找HTML媒体标签");
            Set<String> htmlMediaUrls = extractFromHtmlElements(driver, url);
            mediaUrls.addAll(htmlMediaUrls);

            if (mediaUrls.isEmpty()) {
                System.out.println("⚠ 未找到媒体文件");
                System.out.println("\n调试信息: 以下是页面中找到的所有src属性:");
                Pattern srcPattern = Pattern.compile("src=[\"']([^\"']+[.](mp3|mp4|m4a|wav|ogg|webm|aac|flac|m3u8))[\"']");
                Matcher matcher = srcPattern.matcher(pageSource);
                while (matcher.find()) {
                    System.out.println("  - " + matcher.group(1));
                }
                return;
            }

            System.out.println("✓ 找到 " + mediaUrls.size() + " 个媒体文件");

            // 下载媒体文件
            int count = 1;
            for (String mediaUrl : mediaUrls) {
                try {
                    System.out.println("\n[" + count + "/" + mediaUrls.size() + "] 下载: " + mediaUrl);
                    downloadAndSaveMedia(mediaUrl, outputPath, count);
                    count++;
                } catch (Exception e) {
                    System.err.println("✗ 下载失败: " + e.getMessage());
                }
            }

            System.out.println("\n✓ 下载完成！");

        } finally {
            driver.quit();
        }
    }

    /**
     * 执行JavaScript获取页面中的媒体对象
     */
    private static Set<String> extractMediaViaJavaScript(WebDriver driver) {
        Set<String> urls = new LinkedHashSet<>();

        try {
            // 方法1: 获取所有<audio>标签的src
            Object audioSrcs = (Object) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('audio')).map(el => el.src || el.querySelector('source')?.src).filter(s => s)"
            );
            if (audioSrcs instanceof java.util.List) {
                ((java.util.List<?>) audioSrcs).forEach(src -> {
                    if (src != null) {
                        urls.add(resolveUrl(src.toString(), driver.getCurrentUrl()));
                    }
                });
            }

            // 方法2: 获取所有<video>标签的src
            Object videoSrcs = (Object) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('video')).map(el => el.src || el.querySelector('source')?.src).filter(s => s)"
            );
            if (videoSrcs instanceof java.util.List) {
                ((java.util.List<?>) videoSrcs).forEach(src -> {
                    if (src != null) {
                        urls.add(resolveUrl(src.toString(), driver.getCurrentUrl()));
                    }
                });
            }

            // 方法3: 获取所有<source>标签
            Object sources = (Object) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('source')).map(el => el.src).filter(s => s && s.match(/\\.(mp3|mp4|m4a|wav|ogg|webm|aac|flac)/))"
            );
            if (sources instanceof java.util.List) {
                ((java.util.List<?>) sources).forEach(src -> {
                    if (src != null) {
                        urls.add(resolveUrl(src.toString(), driver.getCurrentUrl()));
                    }
                });
            }

            // 方法4: 获取页面上定义的window.media或全局变量
            try {
                Object mediaObj = (Object) ((JavascriptExecutor) driver).executeScript("return window.media || window.Media || null");
                if (mediaObj != null) {
                    System.out.println("✓ 找到window.media对象");
                }
            } catch (Exception e) {
                // 忽略
            }

        } catch (Exception e) {
            System.err.println("执行JavaScript失败: " + e.getMessage());
        }

        return urls;
    }

    /**
     * 从HTML元素中提取媒体
     */
    private static Set<String> extractFromHtmlElements(WebDriver driver, String baseUrl) {
        Set<String> urls = new LinkedHashSet<>();

        try {
            // 获取所有<audio>标签
            java.util.List<WebElement> audios = driver.findElements(By.tagName("audio"));
            for (WebElement audio : audios) {
                String src = audio.getAttribute("src");
                if (src != null && !src.isEmpty()) {
                    urls.add(resolveUrl(src, baseUrl));
                }
            }

            // 获取所有<video>标签
            java.util.List<WebElement> videos = driver.findElements(By.tagName("video"));
            for (WebElement video : videos) {
                String src = video.getAttribute("src");
                if (src != null && !src.isEmpty()) {
                    urls.add(resolveUrl(src, baseUrl));
                }
            }

            // 获取所有<source>标签
            java.util.List<WebElement> sources = driver.findElements(By.tagName("source"));
            for (WebElement source : sources) {
                String src = source.getAttribute("src");
                if (src != null && !src.isEmpty() && isMediaFile(src)) {
                    urls.add(resolveUrl(src, baseUrl));
                }
            }

        } catch (Exception e) {
            System.err.println("提取HTML元素失败: " + e.getMessage());
        }

        return urls;
    }

    /**
     * 从HTML中提取媒体URL
     */
    private static Set<String> extractMediaUrls(String htmlContent, String baseUrl) throws MalformedURLException {
        Set<String> mediaUrls = new LinkedHashSet<>();

        // 提取 <audio> 标签中的 src 属性
        Pattern audioPattern = Pattern.compile("<audio[^>]*src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = audioPattern.matcher(htmlContent);
        while (matcher.find()) {
            String src = matcher.group(1);
            mediaUrls.add(resolveUrl(src, baseUrl));
        }

        // 提取 <audio><source src="..."/>
        Pattern audioSourcePattern = Pattern.compile("<source[^>]*src=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        matcher = audioSourcePattern.matcher(htmlContent);
        while (matcher.find()) {
            String src = matcher.group(1);
            if (isMediaFile(src)) {
                mediaUrls.add(resolveUrl(src, baseUrl));
            }
        }

        // 提取 <video> 标签中的 src 属性
        Pattern videoPattern = Pattern.compile("<video[^>]*src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        matcher = videoPattern.matcher(htmlContent);
        while (matcher.find()) {
            String src = matcher.group(1);
            mediaUrls.add(resolveUrl(src, baseUrl));
        }

        // 从JavaScript中提取URL
        Pattern jsUrlPattern = Pattern.compile("(?:url|src|source|media)\\s*[:=]\\s*[\"']+(https?://[^\"'\\s;,]+\\.(?:mp3|mp4|m4a|wav|ogg|webm|aac|flac|m3u8))", Pattern.CASE_INSENSITIVE);
        matcher = jsUrlPattern.matcher(htmlContent);
        while (matcher.find()) {
            String url = matcher.group(1);
            mediaUrls.add(url);
        }

        // 从JSON中提取URL
        Pattern jsonUrlPattern = Pattern.compile("\"(https?://[^\"]+(?:\\.mp3|\\.mp4|\\.m4a|\\.wav|\\.ogg|\\.webm|\\.aac|\\.flac|\\.m3u8))\"");
        matcher = jsonUrlPattern.matcher(htmlContent);
        while (matcher.find()) {
            String url = matcher.group(1);
            mediaUrls.add(url);
        }

        return mediaUrls;
    }

    /**
     * 判断是否是媒体文件
     */
    private static boolean isMediaFile(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.matches(".*\\.(mp3|mp4|m4a|wav|ogg|webm|aac|flac|m3u8)($|\\?)");
    }

    /**
     * 解析相对URL为绝对URL
     */
    private static String resolveUrl(String url, String baseUrl) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }

            if (url.startsWith("//")) {
                String protocol = baseUrl.startsWith("https") ? "https:" : "http:";
                return protocol + url;
            }

            URL baseUrlObj = new URL(baseUrl);

            if (url.startsWith("/")) {
                return baseUrlObj.getProtocol() + "://" + baseUrlObj.getHost() + url;
            }

            // 相对URL
            String basePath = baseUrlObj.getPath();
            if (!basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.lastIndexOf("/") + 1);
            }
            return baseUrlObj.getProtocol() + "://" + baseUrlObj.getHost() + basePath + url;
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 下载并保存媒体文件
     */
    private static void downloadAndSaveMedia(String mediaUrl, Path outputDir, int index) throws IOException {
        URL url = new URL(mediaUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("下载失败: HTTP " + conn.getResponseCode());
        }

        // 获取文件名
        String filename = getFilenameFromUrl(mediaUrl, index);
        Path filePath = outputDir.resolve(filename);

        // 保存文件
        try (InputStream is = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            long contentLength = conn.getContentLengthLong();

            System.out.print("  进度: ");
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                if (contentLength > 0) {
                    int percent = (int) ((totalBytes * 100) / contentLength);
                    if (percent % 10 == 0 && percent > 0) {
                        System.out.print(percent + "% ");
                    }
                }
            }
            System.out.println("完成");
            System.out.println("  ✓ 已保存: " + filePath);
        }
    }

    /**
     * 从URL中获取文件名
     */
    private static String getFilenameFromUrl(String url, int index) {
        try {
            // 移除查询参数
            String cleanUrl = url.split("\\?")[0];
            String filename = cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1);

            if (filename.isEmpty()) {
                filename = "media_" + index + ".mp3";
            } else {
                // URL解码
                filename = URLDecoder.decode(filename, "UTF-8");
                // 统一为mp3格式
                if (isAudioFile(filename)) {
                    filename = filename.replaceAll("\\.(mp3|mp4|m4a|wav|ogg|aac|webm|flac)$", ".mp3");
                }
            }

            return filename;
        } catch (Exception e) {
            return "media_" + index + ".mp3";
        }
    }

    /**
     * 判断是否是音频文件
     */
    private static boolean isAudioFile(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.matches(".*\\.(mp3|mp4|m4a|wav|ogg|aac|webm|flac)$");
    }

    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      媒体下载器 Pro v2.0 (动态加载版)     ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("用法: java MediaDownloaderPro <URL> [输出目录] [模式]");
        System.out.println();
        System.out.println("参数:");
        System.out.println("  <URL>          - 要访问的网页URL，必须");
        System.out.println("  [输出目录]     - 媒体文件保存的目录，默认为 ./downloads");
        System.out.println("  [模式]         - dynamic(动态,默认) 或 static(静态)");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java MediaDownloaderPro https://example.com/page");
        System.out.println("  java MediaDownloaderPro https://example.com/page \"D:\\\\Downloads\" dynamic");
        System.out.println("  java MediaDownloaderPro https://example.com/page \"./output\" static");
        System.out.println();
        System.out.println("功能特性:");
        System.out.println("  ✓ 支持JavaScript动态加载的媒体");
        System.out.println("  ✓ 自动识别HTML媒体标签");
        System.out.println("  ✓ 执行JavaScript获取媒体对象");
        System.out.println("  ✓ 支持相对和绝对URL");
        System.out.println("  ✓ 支持音频和视频格式");
        System.out.println();
        System.out.println("注意: 动态模式需要安装ChromeDriver (会自动下载)");
        System.out.println();
    }
}
