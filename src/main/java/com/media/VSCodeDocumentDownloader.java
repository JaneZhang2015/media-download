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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * VS Code 文档爬虫 - 从网页中抓取VS Code文档，生成分章节的txt文件
 */
public class VSCodeDocumentDownloader {
    private static final Logger logger = LoggerFactory.getLogger(VSCodeDocumentDownloader.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final Set<String> downloadedUrls = new HashSet<>();
    private static int fileCount = 0;

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String url = args[0];
        String outputDir = args.length > 1 ? args[1] : "./vscode-docs";

        try {
            downloadDocumentation(url, outputDir);
        } catch (Exception e) {
            logger.error("文档下载失败", e);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
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
        System.out.println("用法: java VSCodeDocumentDownloader <URL> [输出目录]");
        System.out.println();
        System.out.println("参数:");
        System.out.println("  <URL>        - VS Code文档URL，例如: https://code.visualstudio.com/docs");
        System.out.println("  [输出目录]   - 文档保存的目录，默认为 ./vscode-docs");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java VSCodeDocumentDownloader https://code.visualstudio.com/docs ./my-docs");
        System.out.println();
    }
}
