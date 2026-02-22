package com.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * VS Code文档翻译工具
 * 用于批量翻译vscode-docs目录下的英文文本为中文
 */
public class VsCodeDocsTranslator {
    private static final Logger logger = LoggerFactory.getLogger(VsCodeDocsTranslator.class);
    
    private final DoubaoTranslator translator;
    private final Path sourceDir;
    private final Path targetDir;
    
    // 配置参数
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".txt", ".md");
    private static final long TRANSLATION_DELAY_MS = 500; // API调用间隔（毫秒）
    
    /**
     * 创建翻译工具实例
     * @param sourceDir 源文档目录
     * @param targetDir 输出目录
     * @param translator 翻译器实例
     */
    public VsCodeDocsTranslator(Path sourceDir, Path targetDir, DoubaoTranslator translator) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.translator = translator;
        
        validatePaths();
    }
    
    /**
     * 验证路径
     */
    private void validatePaths() {
        if (!Files.exists(sourceDir)) {
            throw new IllegalArgumentException("源目录不存在: " + sourceDir);
        }
        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("源路径不是目录: " + sourceDir);
        }
        
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建输出目录: " + targetDir, e);
        }
    }
    
    /**
     * 翻译单个文件
     * @param sourceFile 源文件路径
     * @param targetFile 输出文件路径
     */
    public void translateFile(Path sourceFile, Path targetFile) throws IOException {
        logger.info("开始翻译文件: {}", sourceFile);
        
        // 创建目标目录
        Files.createDirectories(targetFile.getParent());
        
        // 读取源文件
        String content = new String(Files.readAllBytes(sourceFile), StandardCharsets.UTF_8);
        
        // 分段翻译（避免单次内容过长）
        String translatedContent = translateLargeContent(content);
        
        // 写入目标文件
        Files.write(targetFile, translatedContent.getBytes(StandardCharsets.UTF_8));
        logger.info("文件翻译完成: {}", targetFile);
    }
    
    /**
     * 翻译大文本内容（分段处理）
     */
    private String translateLargeContent(String content) throws IOException {
        List<String> paragraphs = splitContent(content);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < paragraphs.size(); i++) {
            String paragraph = paragraphs.get(i);
            
            // 跳过空段落
            if (paragraph.trim().isEmpty()) {
                result.append(paragraph);
                continue;
            }
            
            try {
                String translated = translator.translate(paragraph);
                result.append(translated);
                
                // 添加延迟以避免API限流
                if (i < paragraphs.size() - 1) {
                    Thread.sleep(TRANSLATION_DELAY_MS);
                }
            } catch (Exception e) {
                logger.warn("段落翻译失败，保留原文: {}", paragraph.substring(0, Math.min(50, paragraph.length())), e);
                result.append(paragraph); // 失败时保留原文
            }
        }
        
        return result.toString();
    }
    
    /**
     * 分割内容为段落（按行分割，保留空行）
     */
    private List<String> splitContent(String content) {
        List<String> paragraphs = new ArrayList<>();
        String[] lines = content.split("\n", -1);
        
        for (String line : lines) {
            paragraphs.add(line);
            paragraphs.add("\n");
        }
        
        return paragraphs;
    }
    
    /**
     * 递归翻译目录下的所有文件
     * @return 翻译成功的文件数量
     */
    public int translateDirectory() throws IOException {
        logger.info("开始翻译目录: {} -> {}", sourceDir, targetDir);
        
        List<Path> filesToTranslate = findFilesToTranslate();
        logger.info("发现 {} 个待翻译文件", filesToTranslate.size());
        
        int successCount = 0;
        for (Path sourceFile : filesToTranslate) {
            try {
                Path relativePath = sourceDir.relativize(sourceFile);
                Path targetFile = targetDir.resolve(relativePath);
                
                translateFile(sourceFile, targetFile);
                successCount++;
            } catch (Exception e) {
                logger.error("文件翻译失败: {}", sourceFile, e);
            }
        }
        
        logger.info("目录翻译完成: {} / {} 文件成功", successCount, filesToTranslate.size());
        return successCount;
    }
    
    /**
     * 查找需要翻译的文件
     */
    private List<Path> findFilesToTranslate() throws IOException {
        try (var stream = Files.walk(sourceDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 检查文件是否支持
     */
    private boolean isSupportedFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    
    /**
     * 翻译并统计信息
     */
    public TranslationReport translateDirectoryWithReport() throws IOException {
        TranslationReport report = new TranslationReport();
        report.startTime = System.currentTimeMillis();
        
        try {
            List<Path> filesToTranslate = findFilesToTranslate();
            report.totalFiles = filesToTranslate.size();
            
            for (Path sourceFile : filesToTranslate) {
                try {
                    Path relativePath = sourceDir.relativize(sourceFile);
                    Path targetFile = targetDir.resolve(relativePath);
                    
                    long fileSize = Files.size(sourceFile);
                    translateFile(sourceFile, targetFile);
                    
                    report.successFiles++;
                    report.totalBytes += fileSize;
                } catch (Exception e) {
                    logger.error("文件翻译失败: {}", sourceFile, e);
                    report.failedFiles.add(sourceFile.toString());
                }
            }
        } finally {
            report.endTime = System.currentTimeMillis();
        }
        
        return report;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 翻译报告
     */
    public static class TranslationReport {
        public int totalFiles;
        public int successFiles;
        public int failedFiles() {
            return failedFiles.size();
        }
        public long totalBytes;
        public long startTime;
        public long endTime;
        public List<String> failedFiles = new ArrayList<>();
        
        public long getDuration() {
            return endTime - startTime;
        }
        
        @Override
        public String toString() {
            return String.format(
                    "翻译报告:\n" +
                    "  总文件数: %d\n" +
                    "  成功: %d\n" +
                    "  失败: %d\n" +
                    "  总数据量: %.2f MB\n" +
                    "  耗时: %d 秒\n" +
                    "  成功率: %.2f%%",
                    totalFiles,
                    successFiles,
                    failedFiles(),
                    totalBytes / (1024.0 * 1024.0),
                    getDuration() / 1000,
                    totalFiles > 0 ? (successFiles * 100.0 / totalFiles) : 0
            );
        }
    }
    
    // ==================== 测试方法 ====================
    
    public static void main(String[] args) throws IOException {
        DoubaoTranslator translator = null;
        VsCodeDocsTranslator docsTranslator = null;
        
        try {
            // 初始化翻译器
            translator = DoubaoTranslator.createFromEnv();
            
            // 设置源目录和目标目录
            Path sourceDir = Paths.get("vscode-docs");
            Path targetDir = Paths.get("vscode-docs-zh");
            
            if (!Files.exists(sourceDir)) {
                logger.error("源目录不存在: {}", sourceDir.toAbsolutePath());
                System.exit(1);
            }
            
            docsTranslator = new VsCodeDocsTranslator(sourceDir, targetDir, translator);
            
            // 执行翻译并生成报告
            TranslationReport report = docsTranslator.translateDirectoryWithReport();
            
            logger.info("\n{}", report);
            
        } catch (Exception e) {
            logger.error("翻译过程失败", e);
            System.exit(1);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
}
