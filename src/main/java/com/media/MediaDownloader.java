package com.media;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 媒体下载器 - 从网页中下载媒体文件并保存为mp3
 */
public class MediaDownloader {
    private static final Logger logger = LoggerFactory.getLogger(MediaDownloader.class);
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String url = args[0];
        String outputDir = args.length > 1 ? args[1] : "./downloads";

        try {
            downloadMediaFromUrl(url, outputDir);
        } catch (Exception e) {
            logger.error("下载失败", e);
            System.err.println("错误: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 从URL下载媒体
     */
    public static void downloadMediaFromUrl(String url, String outputDir) throws Exception {
        logger.info("开始从 {} 下载媒体", url);

        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 获取网页内容
        String htmlContent = fetchHtmlContent(url);
        logger.info("成功获取网页内容");

        // 解析媒体链接
        Set<String> mediaUrls = extractMediaUrls(htmlContent, url);
        
        if (mediaUrls.isEmpty()) {
            logger.warn("未找到媒体文件");
            return;
        }

        logger.info("找到 {} 个媒体文件", mediaUrls.size());

        // 下载媒体文件
        int count = 1;
        for (String mediaUrl : mediaUrls) {
            try {
                downloadAndSaveMedia(mediaUrl, outputPath, count++);
            } catch (Exception e) {
                logger.error("下载媒体失败: {}", mediaUrl, e);
            }
        }

        logger.info("下载完成");
    }

    /**
     * 获取网页内容
     */
    private static String fetchHtmlContent(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP请求失败: " + response.code());
            }
            String body = response.body().string();
            return body;
        }
    }

    /**
     * 从HTML中提取媒体URL
     */
    private static Set<String> extractMediaUrls(String htmlContent, String baseUrl) {
        Set<String> mediaUrls = new LinkedHashSet<>();
        Document doc = Jsoup.parse(htmlContent);

        // 提取 <audio> 标签中的 src 属性
        Elements audioSrcs = doc.select("audio source[src]");
        for (Element element : audioSrcs) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                mediaUrls.add(resolveUrl(src, baseUrl));
            }
        }

        // 提取 <audio> 标签的 src 属性
        Elements audioElements = doc.select("audio[src]");
        for (Element element : audioElements) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                mediaUrls.add(resolveUrl(src, baseUrl));
            }
        }

        // 提取 <video> 标签中的 src 属性
        Elements videoSrcs = doc.select("video source[src]");
        for (Element element : videoSrcs) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                mediaUrls.add(resolveUrl(src, baseUrl));
            }
        }

        // 提取 <video> 标签的 src 属性
        Elements videoElements = doc.select("video[src]");
        for (Element element : videoElements) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                mediaUrls.add(resolveUrl(src, baseUrl));
            }
        }

        // 提取 <a> 标签中指向媒体文件的链接
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if (isMediaFile(href)) {
                mediaUrls.add(resolveUrl(href, baseUrl));
            }
        }

        // 从script标签中提取媒体URL (JSON数据)
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            String scriptContent = script.html();
            mediaUrls.addAll(extractUrlsFromJson(scriptContent));
        }

        return mediaUrls;
    }

    /**
     * 从JSON中提取媒体URL
     */
    private static Set<String> extractUrlsFromJson(String jsonContent) {
        Set<String> urls = new LinkedHashSet<>();
        // 匹配常见的URL模式
        Pattern pattern = Pattern.compile("\"(https?://[^\"]+(?:\\.mp3|\\.mp4|\\.m4a|\\.wav|\\.ogg|\\.webm))\"");
        Matcher matcher = pattern.matcher(jsonContent);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }

    /**
     * 判断是否是媒体文件
     */
    private static boolean isMediaFile(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.matches(".*\\.(mp3|mp4|m4a|wav|ogg|webm|aac|flac)($|\\?)");
    }

    /**
     * 解析相对URL为绝对URL
     */
    private static String resolveUrl(String url, String baseUrl) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        if (url.startsWith("//")) {
            String protocol = baseUrl.startsWith("https") ? "https:" : "http:";
            return protocol + url;
        }

        if (url.startsWith("/")) {
            try {
                java.net.URL baseUrlObj = new java.net.URL(baseUrl);
                return baseUrlObj.getProtocol() + "://" + baseUrlObj.getHost() + url;
            } catch (Exception e) {
                return url;
            }
        }

        // 相对URL
        try {
            java.net.URL baseUrlObj = new java.net.URL(baseUrl);
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
        logger.info("下载媒体: {}", mediaUrl);

        Request request = new Request.Builder()
                .url(mediaUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("下载失败: HTTP " + response.code());
            }

            // 获取文件名
            String filename = getFilenameFromUrl(mediaUrl, index);
            Path filePath = outputDir.resolve(filename);

            // 保存文件
            try (InputStream is = response.body().byteStream();
                 FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            logger.info("文件已保存: {}", filePath);
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
                filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
                // 统一为mp3格式 (如果是音频文件)
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
        System.out.println("=== 媒体下载器 ===");
        System.out.println("用法: java -jar media-downloader.jar <URL> [输出目录]");
        System.out.println();
        System.out.println("参数:");
        System.out.println("  <URL>          - 要访问的网页URL，必须");
        System.out.println("  [输出目录]     - 媒体文件保存的目录，默认为 ./downloads");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar media-downloader.jar https://example.com/page");
        System.out.println("  java -jar media-downloader.jar https://example.com/page ./output");
        System.out.println();
        System.out.println("功能说明:");
        System.out.println("  - 自动从网页中识别 <audio> 和 <video> 标签");
        System.out.println("  - 支持提取 HTML 中的媒体链接");
        System.out.println("  - 支持从 JavaScript 代码中的 JSON 数据中提取URL");
        System.out.println("  - 自动处理相对URL和绝对URL");
        System.out.println("  - 支持的格式: mp3, mp4, m4a, wav, ogg, webm, aac, flac");
    }
}
