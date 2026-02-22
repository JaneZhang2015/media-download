package com.media;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * 媒体下载器 - 无依赖版本（仅使用标准库）
 * 从网页中下载媒体文件并保存为mp3
 */
public class MediaDownloaderStandalone {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

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
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 从URL下载媒体
     */
    public static void downloadMediaFromUrl(String url, String outputDir) throws Exception {
        System.out.println("开始从 " + url + " 下载媒体");

        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 获取网页内容
        String htmlContent = fetchHtmlContent(url);
        System.out.println("✓ 成功获取网页内容");

        // 解析媒体链接
        Set<String> mediaUrls = extractMediaUrls(htmlContent, url);

        if (mediaUrls.isEmpty()) {
            System.out.println("⚠ 未找到媒体文件");
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
                System.err.println("✗ 下载失败: " + mediaUrl);
                System.err.println("  错误: " + e.getMessage());
            }
        }

        System.out.println("\n✓ 下载完成！");
    }

    /**
     * 获取网页内容
     */
    private static String fetchHtmlContent(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP请求失败: " + conn.getResponseCode());
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
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

        // 提取 <audio><source src="..."/>  </audio>
        Pattern audioSourcePattern = Pattern.compile("<source[^>]*src=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        matcher = audioSourcePattern.matcher(htmlContent);
        while (matcher.find()) {
            String src = matcher.group(1);
            if (isMediaFile(src) && !src.contains("video")) {
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

        // 提取 <a> 标签中指向媒体文件的链接
        Pattern linkPattern = Pattern.compile("<a[^>]*href=[\"']([^\"']+\\.(?:mp3|mp4|m4a|wav|ogg|webm|aac|flac))([\"'][^>]*)?>", Pattern.CASE_INSENSITIVE);
        matcher = linkPattern.matcher(htmlContent);
        while (matcher.find()) {
            String href = matcher.group(1);
            mediaUrls.add(resolveUrl(href, baseUrl));
        }

        // 从JavaScript代码中提取媒体URL
        Pattern jsUrlPattern = Pattern.compile("(?:url|src|source|media)\\s*[:=]\\s*[\"']+(https?://[^\"'\\s;,]+\\.(?:mp3|mp4|m4a|wav|ogg|webm|aac|flac))", Pattern.CASE_INSENSITIVE);
        matcher = jsUrlPattern.matcher(htmlContent);
        while (matcher.find()) {
            String url = matcher.group(1);
            mediaUrls.add(url);
        }

        // 从JSON中提取媒体URL
        Pattern jsonUrlPattern = Pattern.compile("\"(https?://[^\"]+(?:\\.mp3|\\.mp4|\\.m4a|\\.wav|\\.ogg|\\.webm|\\.aac|\\.flac))\"");
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
        return lowerUrl.matches(".*\\.(mp3|mp4|m4a|wav|ogg|webm|aac|flac)($|\\?)");
    }

    /**
     * 解析相对URL为绝对URL
     */
    private static String resolveUrl(String url, String baseUrl) throws MalformedURLException {
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
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║        媒体下载器 v1.0 (独立版)     ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println();
        System.out.println("用法: java MediaDownloaderStandalone <URL> [输出目录]");
        System.out.println();
        System.out.println("参数:");
        System.out.println("  <URL>          - 要访问的网页URL，必须");
        System.out.println("  [输出目录]     - 媒体文件保存的目录，默认为 ./downloads");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java MediaDownloaderStandalone https://example.com/page");
        System.out.println("  java MediaDownloaderStandalone https://example.com/page \"D:\\\\Downloads\"");
        System.out.println();
        System.out.println("功能:");
        System.out.println("  ✓ 自动识别 <audio> 和 <video> 标签");
        System.out.println("  ✓ 支持HTML链接提取");
        System.out.println("  ✓ 支持JavaScript中的URL提取");
        System.out.println("  ✓ 支持JSON数据解析");
        System.out.println("  ✓ 自动转换相对URL为绝对URL");
        System.out.println("  ✓ 支持格式: mp3, mp4, m4a, wav, ogg, webm, aac, flac");
        System.out.println();
    }
}
