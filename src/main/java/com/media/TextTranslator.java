package com.media;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

/**
 * 文本翻译器 - 将指定文件夹中的所有txt文件翻译成中文
 * 使用 MyMemory Translation API（免费，无需API密钥）
 */
public class TextTranslator {
    private static final Logger logger = LoggerFactory.getLogger(TextTranslator.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private static int translatedCount = 0;
    private static int failedCount = 0;
    
    // 延迟配置
    private static final long DELAY_BETWEEN_REQUESTS = 3000; // 请求间延迟 (毫秒) - 3秒
    private static final long DELAY_BETWEEN_FILES = 5000; // 文件间延迟 (毫秒) - 5秒
    private static final int MAX_RETRIES = 5; // 最大重试次数
    private static final long INITIAL_RETRY_DELAY = 5000; // 初始重试延迟 (毫秒) - 5秒
    private static long lastRequestTime = 0; // 上一次请求时间

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String inputDir = args[0];
        String outputDir = args.length > 1 ? args[1] : "./zh";

        try {
            translateFilesInDirectory(inputDir, outputDir);
        } catch (Exception e) {
            logger.error("翻译失败", e);
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 翻译目录中的所有txt文件
     */
    public static void translateFilesInDirectory(String inputDir, String outputDir) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      文本翻译器 v1.0 (英文→中文)         ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("输入目录: " + inputDir);
        System.out.println("输出目录: " + outputDir);
        System.out.println();

        Path inputPath = Paths.get(inputDir);
        Path outputPath = Paths.get(outputDir);

        if (!Files.exists(inputPath)) {
            throw new RuntimeException("输入目录不存在: " + inputDir);
        }

        // 创建输出目录
        Files.createDirectories(outputPath);

        // 扫描所有txt文件
        System.out.println("扫描txt文件...");
        Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".txt")) {
                    try {
                        translateFile(file, inputPath, outputPath);
                    } catch (Exception e) {
                        System.err.println("✗ 翻译失败: " + file + " - " + e.getMessage());
                        failedCount++;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("✓ 翻译完成！");
        System.out.println("  成功: " + translatedCount + " 个文件");
        if (failedCount > 0) {
            System.out.println("  失败: " + failedCount + " 个文件");
        }
        System.out.println("  保存路径: " + outputPath.toAbsolutePath());
        System.out.println("╚══════════════════════════════════════════╝");
    }

    /**
     * 翻译单个文件
     */
    private static void translateFile(Path sourceFile, Path inputDir, Path outputDir) throws Exception {
        // 读取文件内容
        String content = new String(Files.readAllBytes(sourceFile), StandardCharsets.UTF_8);
        System.out.println("[翻译] " + inputDir.relativize(sourceFile));

        // 翻译内容
        String translatedContent = translateText(content);

        // 生成输出路径（保持目录结构）
        Path relativePath = inputDir.relativize(sourceFile);
        Path outputFile = outputDir.resolve(relativePath);

        // 创建必要的目录
        Files.createDirectories(outputFile.getParent());

        // 保存翻译后的文件
        Files.write(outputFile, translatedContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("  ✓ 已保存: " + outputDir.relativize(outputFile));
        translatedCount++;

        // 文件间延迟
        System.out.println("  ⏳ 等待 " + (DELAY_BETWEEN_FILES/1000) + " 秒以避免限流...");
        Thread.sleep(DELAY_BETWEEN_FILES);
    }

    /**
     * 翻译文本（英文→中文）
     * 使用 MyMemory Translation API
     */
    private static String translateText(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 如果文本过长，分段翻译
        int maxLength = 500;
        if (text.length() > maxLength) {
            return translateLongText(text);
        }

        return translateChunk(text);
    }

    /**
     * 翻译长文本（分段处理）
     */
    private static String translateLongText(String text) throws Exception {
        StringBuilder result = new StringBuilder();

        // 按段落分割
        String[] paragraphs = text.split("\n\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                result.append("\n\n");
                continue;
            }

            // 按句子进一步分割
            String[] sentences = paragraph.split("(?<=[.!?]\\s)");
            StringBuilder currentChunk = new StringBuilder();

            for (String sentence : sentences) {
                if (currentChunk.length() + sentence.length() > 500) {
                    if (currentChunk.length() > 0) {
                        result.append(translateChunk(currentChunk.toString())).append(" ");
                        currentChunk = new StringBuilder();
                    }
                }
                currentChunk.append(sentence);
            }

            if (currentChunk.length() > 0) {
                result.append(translateChunk(currentChunk.toString())).append("\n\n");
            }
        }

        return result.toString().trim();
    }

    /**
     * 翻译单个文本块（带重试机制）
     */
    private static String translateChunk(String text) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String result = performTranslation(text);
                if (result != null && !result.equals(text)) {
                    return result;
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) {
                    long delayMs = INITIAL_RETRY_DELAY * (long) Math.pow(2, attempt - 1);
                    long delaySeconds = delayMs / 1000;
                    System.out.println("  ⚠ 第 " + attempt + " 次尝试失败 (" + e.getMessage() + ")");
                    System.out.println("    等待 " + delaySeconds + " 秒后重试... (第 " + (attempt+1) + "/" + MAX_RETRIES + ")");
                    Thread.sleep(delayMs);
                } else {
                    System.out.println("  ⚠ 第 " + attempt + " 次尝试失败，翻译失败，使用原文本");
                    return text;
                }
            }
        }
        
        return text;
    }

    /**
     * 执行翻译操作
     */
    private static String performTranslation(String text) throws Exception {
        // 确保请求间隔
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;
        if (timeSinceLastRequest < DELAY_BETWEEN_REQUESTS) {
            long waitTime = DELAY_BETWEEN_REQUESTS - timeSinceLastRequest;
            Thread.sleep(waitTime);
        }
        lastRequestTime = System.currentTimeMillis();

        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=en|zh-CN";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        Response response = client.newCall(request).execute();

        try {
            int responseCode = response.code();
            
            if (responseCode == 429) {
                // API限流
                throw new RuntimeException("API限流 (HTTP 429)，请稍后重试");
            }

            if (!response.isSuccessful()) {
                logger.warn("翻译API返回错误: " + responseCode);
                return null;
            }

            String responseBody = response.body().string();

            // 解析JSON响应
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            int responseStatus = json.get("responseStatus").getAsInt();

            if (responseStatus == 200) {
                JsonObject data = json.getAsJsonObject("responseData");
                return data.get("translatedText").getAsString();
            } else if (responseStatus == 429) {
                throw new RuntimeException("API限流 (status 429)，请稍后重试");
            } else {
                logger.warn("翻译失败，状态码: " + responseStatus);
                return null;
            }
        } finally {
            response.close();
        }
    }

    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      文本翻译器 v1.0 (英文→中文)         ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("用法: java TextTranslator <输入目录> [输出目录]");
        System.out.println();
        System.out.println("参数:");
        System.out.println("  <输入目录>   - 包含txt文件的目录");
        System.out.println("  [输出目录]   - 翻译后文件保存的目录，默认为 ./zh");
        System.out.println();
        System.out.println("功能:");
        System.out.println("  • 递归扫描输入目录中的所有txt文件");
        System.out.println("  • 将每个文件内容从英文翻译为中文");
        System.out.println("  • 在输出目录中保持相同的目录结构");
        System.out.println("  • 使用免费的 MyMemory Translation API");
        System.out.println("  • 自动重试机制（指数退避）");
        System.out.println("  • 智能延迟控制（避免API限流）");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java TextTranslator ./vscode-docs ./vscode-docs-zh");
        System.out.println("  java TextTranslator ./documents");
        System.out.println();
        System.out.println("并发控制配置:");
        System.out.println("  • 请求间延迟: " + DELAY_BETWEEN_REQUESTS + "ms");
        System.out.println("  • 文件间延迟: " + DELAY_BETWEEN_FILES + "ms");
        System.out.println("  • 最大重试次数: " + MAX_RETRIES);
        System.out.println("  • 初始重试延迟: " + INITIAL_RETRY_DELAY + "ms (指数增长)");
        System.out.println();
        System.out.println("注意事项:");
        System.out.println("  • 需要网络连接");
        System.out.println("  • 首次运行需要较长时间（避免API限流）");
        System.out.println("  • 大批量文件翻译建议：");
        System.out.println("    1. 先翻译一小部分测试");
        System.out.println("    2. 根据网络状况调整延迟参数");
        System.out.println("    3. 可分次翻译大批量文件");
        System.out.println();
    }
}
