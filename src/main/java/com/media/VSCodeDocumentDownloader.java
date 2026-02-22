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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * VS Code 文档爬虫 - 从网页中抓取VS Code文档，生成分章节的txt文件
 */
public class VSCodeDocumentDownloader {
    private static final Logger logger = LoggerFactory.getLogger(VSCodeDocumentDownloader.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final OkHttpClient client = createUnsafeOkHttpClient();

    private static final Set<String> downloadedUrls = new HashSet<>();
    private static int fileCount = 0;

    /**
     * 创建不进行SSL验证的OkHttpClient（用于处理证书问题）
     */
    private static OkHttpClient createUnsafeOkHttpClient() {
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // 安装信任管理器
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            logger.error("创建OkHttpClient失败，使用默认配置", e);
            return new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String url = args[0];
        String outputDir = args.length > 1 ? args[1] : "./vscode-docs";
        boolean downloadVideos = args.length > 2 && args[2].equalsIgnoreCase("--videos");

        try {
            if (downloadVideos) {
                downloadVideos(url, outputDir);
            } else {
                downloadDocumentation(url, outputDir);
            }
        } catch (Exception e) {
            logger.error("下载失败", e);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 下载VS Code文档中的所有MP4视频
     */
    public static void downloadVideos(String startUrl, String outputDir) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      VS Code 视频下载 v1.0               ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("开始爬取视频: " + startUrl);

        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 获取主页面
        String html = fetchPage(startUrl);
        if (html == null) {
            throw new RuntimeException("无法获取主页面内容");
        }

        // 解析主页面，获取所有文档链接
        Document doc = Jsoup.parse(html);
        Set<String> docUrls = extractDocumentLinks(doc, startUrl);

        System.out.println("✓ 找到 " + docUrls.size() + " 个文档章节，开始扫描视频...");
        System.out.println();

        // 扫描每个文档以找到视频
        Map<String, Set<String>> videosByPage = new HashMap<>();
        int docCount = 1;
        for (String docUrl : docUrls) {
            try {
                String docHtml = fetchPage(docUrl);
                if (docHtml != null) {
                    Set<String> videos = extractVideoLinks(docHtml, docUrl);
                    if (!videos.isEmpty()) {
                        videosByPage.put(docUrl, videos);
                        System.out.println("[" + docCount + "/" + docUrls.size() + "] " + docUrl + 
                                         " - 找到 " + videos.size() + " 个视频");
                    }
                }
                docCount++;
            } catch (Exception e) {
                System.err.println("✗ 扫描失败 (" + docUrl + "): " + e.getMessage());
            }
        }

        System.out.println();
        int totalVideos = videosByPage.values().stream().mapToInt(Set::size).sum();
        System.out.println("✓ 总共找到 " + totalVideos + " 个MP4视频");
        System.out.println();

        // 下载视频
        int downloadCount = 1;
        for (Map.Entry<String, Set<String>> entry : videosByPage.entrySet()) {
            String pageUrl = entry.getKey();
            for (String videoUrl : entry.getValue()) {
                try {
                    System.out.println("[" + downloadCount + "/" + totalVideos + "] 下载视频...");
                    downloadVideoFile(videoUrl, pageUrl, outputPath);
                    downloadCount++;
                } catch (Exception e) {
                    System.err.println("✗ 下载失败: " + e.getMessage());
                }
            }
        }

        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("✓ 视频下载完成!");
        System.out.println("  保存路径: " + outputPath.toAbsolutePath());
        System.out.println("╚══════════════════════════════════════════╝");
    }

    /**
     * 下载VS Code文档
     */
    public static void downloadDocumentation(String startUrl, String outputDir) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      VS Code 文档爬虫 v1.0               ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("开始爬取文档: " + startUrl);

        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 获取主页面
        String html = fetchPage(startUrl);
        if (html == null) {
            throw new RuntimeException("无法获取主页面内容");
        }

        // 解析主页面，获取所有文档链接
        Document doc = Jsoup.parse(html);
        Set<String> docUrls = extractDocumentLinks(doc, startUrl);

        System.out.println("✓ 找到 " + docUrls.size() + " 个文档章节");
        System.out.println();

        // 下载每个文档
        int count = 1;
        for (String docUrl : docUrls) {
            try {
                System.out.println("[" + count + "/" + docUrls.size() + "] 下载: " + docUrl);
                downloadSingleDocument(docUrl, outputPath);
                count++;
            } catch (Exception e) {
                System.err.println("✗ 下载失败: " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("✓ 下载完成！共生成 " + fileCount + " 个文档");
        System.out.println("  保存路径: " + outputPath.toAbsolutePath());
        System.out.println("╚══════════════════════════════════════════╝");
    }

    /**
     * 从主页面提取所有文档链接
     */
    private static Set<String> extractDocumentLinks(Document doc, String baseUrl) {
        Set<String> urls = new LinkedHashSet<>();

        // 查找所有可能的文档链接
        Elements links = doc.select("a[href]");

        for (Element link : links) {
            String href = link.attr("href");
            String text = link.text().trim();

            // 过滤文档链接
            if (isValidDocumentLink(href, baseUrl)) {
                String absoluteUrl = resolveUrl(href, baseUrl);
                if (!downloadedUrls.contains(absoluteUrl) && !absoluteUrl.equals(baseUrl)) {
                    urls.add(absoluteUrl);
                    downloadedUrls.add(absoluteUrl);
                }
            }
        }

        // 如果是VS Code官方文档，还可以从侧边栏菜单提取链接
        Elements menuItems = doc.select(".docs-nav a[href], nav a[href], .sidebar a[href]");
        for (Element item : menuItems) {
            String href = item.attr("href");
            if (isValidDocumentLink(href, baseUrl)) {
                String absoluteUrl = resolveUrl(href, baseUrl);
                if (!downloadedUrls.contains(absoluteUrl)) {
                    urls.add(absoluteUrl);
                    downloadedUrls.add(absoluteUrl);
                }
            }
        }

        return urls;
    }

    /**
     * 从HTML中提取所有视频链接（MP4格式）
     */
    private static Set<String> extractVideoLinks(String html, String pageUrl) {
        Set<String> videoUrls = new LinkedHashSet<>();
        
        try {
            Document doc = Jsoup.parse(html);
            
            // 从video标签中提取src
            Elements videoTags = doc.select("video source[src], video[src]");
            for (Element videoTag : videoTags) {
                String src = videoTag.attr("src");
                if (src != null && src.toLowerCase().endsWith(".mp4")) {
                    String absoluteUrl = resolveUrl(src, pageUrl);
                    videoUrls.add(absoluteUrl);
                }
            }
            
            // 从iframe中提取视频链接
            Elements iframes = doc.select("iframe[src]");
            for (Element iframe : iframes) {
                String src = iframe.attr("src");
                if (src != null && src.contains("mp4")) {
                    String absoluteUrl = resolveUrl(src, pageUrl);
                    videoUrls.add(absoluteUrl);
                }
            }
            
            // 从a标签中提取MP4链接
            Elements links = doc.select("a[href*='.mp4'], a[href*='.MP4']");
            for (Element link : links) {
                String href = link.attr("href");
                if (href != null && (href.toLowerCase().endsWith(".mp4") || href.contains(".mp4"))) {
                    String absoluteUrl = resolveUrl(href, pageUrl);
                    videoUrls.add(absoluteUrl);
                }
            }
            
            // 从img标签的data-video属性中提取
            Elements imgs = doc.select("img[data-video]");
            for (Element img : imgs) {
                String video = img.attr("data-video");
                if (video != null && video.toLowerCase().contains(".mp4")) {
                    String absoluteUrl = resolveUrl(video, pageUrl);
                    videoUrls.add(absoluteUrl);
                }
            }
            
            // 从script中提取视频链接（JSON格式）
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String content = script.html();
                extractVideoLinksFromJson(content, pageUrl, videoUrls);
            }
            
            // 从HTML源直接提取所有/assets/下的MP4链接（补充正则表达式）
            extractVideoLinksFromHtmlSource(html, pageUrl, videoUrls);
            
        } catch (Exception e) {
            logger.warn("提取视频链接失败: " + pageUrl, e);
        }
        
        return videoUrls;
    }

    /**
     * 从JSON数据中提取视频链接
     */
    private static void extractVideoLinksFromJson(String json, String pageUrl, Set<String> videoUrls) {
        // 正则表达式匹配MP4链接
        Pattern pattern = Pattern.compile("(https?://[^\"\\s]+\\.mp4[^\"\\s]*)");
        var matcher = pattern.matcher(json);
        while (matcher.find()) {
            String url = matcher.group(1).replace("\\", "");
            if (url.lastIndexOf("\"") > 0) {
                url = url.substring(0, url.lastIndexOf("\""));
            }
            if (url.endsWith(".mp4")) {
                videoUrls.add(url);
            }
        }
    }

    /**
     * 直接从HTML源代码中提取所有MP4视频链接（包括assets路径）
     */
    private static void extractVideoLinksFromHtmlSource(String html, String pageUrl, Set<String> videoUrls) {
        // 模式1：完整的https://code.visualstudio.com地址
        Pattern fullUrlPattern = Pattern.compile("https://code\\.visualstudio\\.com/[^\\s\"'<>]*\\.mp4");
        var matcher = fullUrlPattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(0);
            if (!videoUrls.contains(url)) {
                videoUrls.add(url);
            }
        }
        
        // 模式2：相对路径 /assets/...
        Pattern assetPathPattern = Pattern.compile("/assets/[^\\s\"'<>]*\\.mp4");
        matcher = assetPathPattern.matcher(html);
        while (matcher.find()) {
            String path = matcher.group(0);
            try {
                java.net.URL pageUrlObj = new java.net.URL(pageUrl);
                String absoluteUrl = pageUrlObj.getProtocol() + "://" + pageUrlObj.getHost() + path;
                if (!videoUrls.contains(absoluteUrl)) {
                    videoUrls.add(absoluteUrl);
                }
            } catch (Exception e) {
                logger.debug("无法解析相对路径: " + path);
            }
        }
        
        // 模式3：在属性中的 .mp4 (src=, data-src=, href= 等)
        Pattern attrPattern = Pattern.compile("(?:src|data-src|href|data-href)\\s*=\\s*[\"']([^\"']*\\.mp4[^\"]*)[\"\']");
        matcher = attrPattern.matcher(html);
        while (matcher.find()) {
            String urlOrPath = matcher.group(1);
            if (urlOrPath.startsWith("http")) {
                // 完整URL
                if (!videoUrls.contains(urlOrPath)) {
                    videoUrls.add(urlOrPath);
                }
            } else if (urlOrPath.startsWith("/")) {
                // 相对路径
                try {
                    java.net.URL pageUrlObj = new java.net.URL(pageUrl);
                    String absoluteUrl = pageUrlObj.getProtocol() + "://" + pageUrlObj.getHost() + urlOrPath;
                    if (!videoUrls.contains(absoluteUrl)) {
                        videoUrls.add(absoluteUrl);
                    }
                } catch (Exception e) {
                    logger.debug("无法解析相对路径: " + urlOrPath);
                }
            }
        }
    }

    /**
     * 下载单个视频文件
     */
    private static void downloadVideoFile(String videoUrl, String sourcePageUrl, Path outputDir) throws Exception {
        System.out.println("  URL: " + videoUrl);
        
        try {
            Request request = new Request.Builder()
                    .url(videoUrl)
                    .header("User-Agent", USER_AGENT)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("下载失败 (HTTP " + response.code() + ")");
            }

            // 确定文件保存路径
            Path filePath = generateVideoFilePath(videoUrl, sourcePageUrl, outputDir);
            Files.createDirectories(filePath.getParent());

            // 下载文件
            try (InputStream is = response.body().byteStream();
                 OutputStream os = Files.newOutputStream(filePath)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                long fileSize = response.body().contentLength();
                
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    
                    // 显示下载进度
                    if (fileSize > 0) {
                        int progress = (int) ((totalBytes * 100) / fileSize);
                        System.out.print("\r  进度: " + progress + "%");
                    }
                }
                System.out.println("\r  ✓ 已保存: " + outputDir.relativize(filePath) + 
                                 " (" + formatFileSize(totalBytes) + ")");
            }

            response.close();

        } catch (Exception e) {
            logger.error("下载视频失败: " + videoUrl, e);
            throw e;
        }
    }

    /**
     * 生成视频文件保存路径
     */
    private static Path generateVideoFilePath(String videoUrl, String sourcePageUrl, Path outputDir) {
        try {
            // 获取视频原始文件名
            String videoFileName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            // 移除查询参数
            if (videoFileName.contains("?")) {
                videoFileName = videoFileName.substring(0, videoFileName.indexOf("?"));
            }
            
            // 确保以.mp4结尾
            if (!videoFileName.toLowerCase().endsWith(".mp4")) {
                videoFileName += ".mp4";
            }

            // 根据源页面URL生成目录结构
            Path pageDir = extractPageDirectory(sourcePageUrl, outputDir);
            Files.createDirectories(pageDir);

            // 避免重名
            Path filePath = pageDir.resolve(videoFileName);
            int counter = 1;
            while (Files.exists(filePath)) {
                String nameWithoutExt = videoFileName.substring(0, videoFileName.lastIndexOf(".mp4"));
                String newFilename = nameWithoutExt + "_" + counter + ".mp4";
                filePath = pageDir.resolve(newFilename);
                counter++;
            }

            return filePath;

        } catch (Exception e) {
            // 异常情况下保存到根目录
            String videoFileName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            if (videoFileName.contains("?")) {
                videoFileName = videoFileName.substring(0, videoFileName.indexOf("?"));
            }
            return outputDir.resolve(videoFileName.isEmpty() ? "video.mp4" : videoFileName);
        }
    }

    /**
     * 从页面URL提取目录结构
     */
    private static Path extractPageDirectory(String pageUrl, Path outputDir) {
        try {
            java.net.URL url = new java.net.URL(pageUrl);
            String path = url.getPath();
            
            // 移除起始的 "/" 和末尾的 "/"
            path = path.replaceAll("^/+", "").replaceAll("/+$", "");
            
            // 移除文件扩展名（如果有）
            if (path.contains(".")) {
                path = path.substring(0, path.lastIndexOf("/"));
            }
            
            if (path.isEmpty() || path.equals("docs")) {
                return outputDir.resolve("videos");
            }
            
            // 生成目录路径
            Path currentPath = outputDir;
            String[] pathParts = path.split("/");
            for (String part : pathParts) {
                String sanitized = sanitizeFilename(part);
                if (!sanitized.isEmpty()) {
                    currentPath = currentPath.resolve(sanitized);
                }
            }
            
            return currentPath;

        } catch (Exception e) {
            return outputDir.resolve("videos");
        }
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 检查是否是有效的文档链接
     */
    private static boolean isValidDocumentLink(String href, String baseUrl) {

        if (href == null || href.isEmpty()) {
            return false;
        }

        // 排除外部链接和特殊链接
        if (href.startsWith("http") && !href.contains("code.visualstudio.com")) {
            return false;
        }

        // 排除锚点链接
        if (href.startsWith("#")) {
            return false;
        }

        // 排除下载等特殊链接
        if (href.contains("download") || href.contains("api") || href.contains("release-notes")) {
            return false;
        }

        return true;
    }

    /**
     * 下载单个文档
     */
    private static void downloadSingleDocument(String docUrl, Path outputDir) throws Exception {
        String html = fetchPage(docUrl);
        if (html == null) {
            return;
        }

        Document doc = Jsoup.parse(html);

        // 提取标题
        String title = extractTitle(doc, docUrl);
        System.out.println("  标题: " + title);

        // 提取内容
        String content = extractContent(doc);

        // 根据URL生成目录结构
        Path filePath = generateFilePathFromUrl(docUrl, outputDir, title);

        // 创建必要的目录
        Files.createDirectories(filePath.getParent());

        // 避免重名
        int counter = 1;
        while (Files.exists(filePath)) {
            Path parent = filePath.getParent();
            String filename = filePath.getFileName().toString();
            String nameWithoutExt = filename.substring(0, filename.lastIndexOf(".txt"));
            String newFilename = nameWithoutExt + "_" + counter + ".txt";
            filePath = parent.resolve(newFilename);
            counter++;
        }

        // 保存文件
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("  ✓ 已保存: " + outputDir.relativize(filePath));
        fileCount++;
    }

    /**
     * 提取页面标题
     */
    private static String extractTitle(Document doc, String url) {
        // 尝试多种标题获取方式
        String title = null;

        // 方法1: h1标签
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            title = h1.text().trim();
        }

        // 方法2: HTML title标签
        if (title == null || title.isEmpty()) {
            Element titleElement = doc.selectFirst("title");
            if (titleElement != null) {
                title = titleElement.text().trim();
                // 移除 " | VS Code" 后缀
                if (title.contains("|")) {
                    title = title.substring(0, title.lastIndexOf("|")).trim();
                }
            }
        }

        // 方法3: og:title meta标签
        if (title == null || title.isEmpty()) {
            Element ogTitle = doc.selectFirst("meta[property=og:title]");
            if (ogTitle != null) {
                title = ogTitle.attr("content").trim();
            }
        }

        // 方法4: 从URL获取
        if (title == null || title.isEmpty()) {
            title = extractTitleFromUrl(url);
        }

        return title.isEmpty() ? "Untitled" : title;
    }

    /**
     * 从URL中提取标题
     */
    private static String extractTitleFromUrl(String url) {
        try {
            String path = new java.net.URL(url).getPath();
            String[] parts = path.split("/");
            if (parts.length > 0) {
                String last = parts[parts.length - 1];
                if (!last.isEmpty()) {
                    return last.replace("-", " ").replace("_", " ");
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return "Document";
    }

    /**
     * 提取页面内容
     */
    private static String extractContent(Document doc) {
        StringBuilder content = new StringBuilder();

        // 添加标题
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            content.append(h1.text()).append("\n");
            content.append("=".repeat(Math.max(0, h1.text().length()))).append("\n\n");
        }

        // 提取主要内容
        // 尝试找到文章主体
        Element mainContent = null;

        // 常见的内容容器选择器
        String[] contentSelectors = {
                "article",
                ".content",
                ".docs-content",
                ".main-content",
                "main",
                "#content",
                ".documentation"
        };

        for (String selector : contentSelectors) {
            mainContent = doc.selectFirst(selector);
            if (mainContent != null) {
                break;
            }
        }

        if (mainContent == null) {
            mainContent = doc.body();
        }

        // 移除不必要的元素
        mainContent.select("script, style, nav, footer, .sidebar, .toc, button, .advertisement").remove();

        // 提取文本和基本格式
        extractTextWithFormatting(mainContent, content);

        return content.toString().trim();
    }

    /**
     * 提取文本并保持基本格式
     */
    private static void extractTextWithFormatting(Element element, StringBuilder sb) {
        for (var node : element.childNodes()) {
            if (node instanceof org.jsoup.nodes.TextNode) {
                String text = ((org.jsoup.nodes.TextNode) node).text().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            } else if (node instanceof Element) {
                Element el = (Element) node;
                String tagName = el.tagName().toLowerCase();

                switch (tagName) {
                    case "h2":
                    case "h3":
                    case "h4":
                    case "h5":
                    case "h6":
                        sb.append("\n").append(el.text()).append("\n");
                        sb.append("-".repeat(Math.max(0, el.text().length()))).append("\n");
                        break;
                    case "p":
                        sb.append(el.text()).append("\n\n");
                        break;
                    case "li":
                        sb.append("• ").append(el.text()).append("\n");
                        break;
                    case "code":
                    case "pre":
                        sb.append("[代码]\n");
                        sb.append(el.text()).append("\n");
                        sb.append("[/代码]\n");
                        break;
                    case "a":
                        String href = el.attr("href");
                        if (href != null && !href.isEmpty()) {
                            sb.append(el.text()).append(" (").append(href).append(")");
                        } else {
                            sb.append(el.text());
                        }
                        break;
                    case "br":
                        sb.append("\n");
                        break;
                    default:
                        extractTextWithFormatting(el, sb);
                }
            }
        }
    }

    /**
     * 获取网页内容
     */
    private static String fetchPage(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                logger.warn("获取页面失败: " + url + " (" + response.code() + ")");
                return null;
            }

            String body = response.body().string();
            response.close();
            return body;
        } catch (Exception e) {
            logger.error("获取页面异常: " + url, e);
            return null;
        }
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

            java.net.URL baseUrlObj = new java.net.URL(baseUrl);

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
     * 根据URL生成文件路径（包含目录结构）
     */
    private static Path generateFilePathFromUrl(String docUrl, Path outputDir, String title) {
        try {
            java.net.URL url = new java.net.URL(docUrl);
            String path = url.getPath();

            // 移除起始的 "/" 和末尾的 "/"
            path = path.replaceAll("^/+", "").replaceAll("/+$", "");

            // 如果path为空，使用title作为文件名
            if (path.isEmpty()) {
                return outputDir.resolve(sanitizeFilename(title) + ".txt");
            }

            // 分割path为目录和文件名
            String[] pathParts = path.split("/");
            Path currentPath = outputDir;

            // 除了最后一部分，其余都作为目录
            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = sanitizeFilename(pathParts[i]);
                if (!part.isEmpty()) {
                    currentPath = currentPath.resolve(part);
                }
            }

            // 最后一部分作为文件名（如果没有扩展名则使用title）
            String lastPart = pathParts[pathParts.length - 1];
            String filename;
            if (lastPart.isEmpty() || lastPart.equals("docs")) {
                filename = sanitizeFilename(title) + ".txt";
            } else {
                filename = sanitizeFilename(lastPart) + ".txt";
            }

            return currentPath.resolve(filename);
        } catch (Exception e) {
            // 异常情况下使用输出目录 + title
            return outputDir.resolve(sanitizeFilename(title) + ".txt");
        }
    }

    /**
     * 清理文件名，移除非法字符
     */
    private static String sanitizeFilename(String filename) {
        // 移除或替换非法字符
        filename = filename.replaceAll("[<>:\"/\\\\|?*]", "_");
        // 移除前后空格
        filename = filename.trim();
        // 限制长度
        if (filename.length() > 200) {
            filename = filename.substring(0, 200);
        }
        return filename.isEmpty() ? "document" : filename;
    }

    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     VS Code 文档爬虫 v1.0                ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("用法: java VSCodeDocumentDownloader <URL> [输出目录] [选项]");
        System.out.println();
        System.out.println("参数:");
        System.out.println("  <URL>        - VS Code文档URL，例如: https://code.visualstudio.com/docs");
        System.out.println("  [输出目录]   - 文档保存的目录，默认为 ./vscode-docs");
        System.out.println("  [选项]       - 可选项: --videos 表示下载MP4视频而非文档");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  # 下载文档");
        System.out.println("  java VSCodeDocumentDownloader https://code.visualstudio.com/docs ./my-docs");
        System.out.println();
        System.out.println("  # 下载视频");
        System.out.println("  java VSCodeDocumentDownloader https://code.visualstudio.com/docs ./my-docs --videos");
        System.out.println();
    }
}
