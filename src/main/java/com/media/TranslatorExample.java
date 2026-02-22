package com.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 翻译工具使用示例
 * 
 * 演示如何在你的项目中使用 DoubaoTranslator 和 VsCodeDocsTranslator
 */
public class TranslatorExample {
    private static final Logger logger = LoggerFactory.getLogger(TranslatorExample.class);
    
    public static void main(String[] args) {
        // 示例1: 翻译单个文本
        example1_translateSingleText();
        
        // 示例2: 批量翻译文本
        example2_translateBatch();
        
        // 示例3: 翻译单个文件
        example3_translateSingleFile();
        
        // 示例4: 翻译整个目录
        example4_translateDirectory();
    }
    
    /**
     * 示例1: 翻译单个文本
     */
    public static void example1_translateSingleText() {
        logger.info("===== 示例1: 翻译单个文本 =====");
        
        DoubaoTranslator translator = null;
        try {
            // 创建翻译器（从环境变量读取API密钥）
            translator = DoubaoTranslator.createFromEnv();
            
            // 要翻译的英文文本
            String englishText = "Welcome to Visual Studio Code";
            
            // 执行翻译
            String chineseText = translator.translate(englishText);
            
            // 输出结果
            logger.info("源文本: {}", englishText);
            logger.info("译文: {}", chineseText);
            
        } catch (Exception e) {
            logger.error("翻译失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
    
    /**
     * 示例2: 批量翻译多个文本
     */
    public static void example2_translateBatch() {
        logger.info("\n===== 示例2: 批量翻译文本 =====");
        
        DoubaoTranslator translator = null;
        try {
            translator = DoubaoTranslator.createFromEnv();
            
            // 要翻译的多个英文文本
            List<String> englishTexts = Arrays.asList(
                "Install VS Code extensions",
                "Debug your Python code",
                "Configure your settings",
                "Work with Git repositories",
                "Optimize your productivity"
            );
            
            // 批量翻译
            Map<String, String> results = translator.translateBatch(englishTexts);
            
            // 输出结果
            logger.info("翻译结果:");
            results.forEach((original, translation) -> {
                logger.info("  英文: {} -> 中文: {}", original, translation);
            });
            
        } catch (Exception e) {
            logger.error("翻译失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
    
    /**
     * 示例3: 翻译单个文件
     */
    public static void example3_translateSingleFile() {
        logger.info("\n===== 示例3: 翻译单个文件 =====");
        
        DoubaoTranslator translator = null;
        VsCodeDocsTranslator docTranslator = null;
        
        try {
            translator = DoubaoTranslator.createFromEnv();
            
            // 创建文档翻译工具
            docTranslator = new VsCodeDocsTranslator(
                Paths.get("vscode-docs"),
                Paths.get("vscode-docs-zh"),
                translator
            );
            
            // 翻译单个文件
            docTranslator.translateFile(
                Paths.get("vscode-docs/docs/editor/basics.txt"),
                Paths.get("vscode-docs-zh/docs/editor/basics.txt")
            );
            
            logger.info("单文件翻译完成");
            
        } catch (Exception e) {
            logger.error("翻译失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
    
    /**
     * 示例4: 翻译整个目录
     */
    public static void example4_translateDirectory() {
        logger.info("\n===== 示例4: 翻译整个目录 =====");
        
        DoubaoTranslator translator = null;
        VsCodeDocsTranslator docTranslator = null;
        
        try {
            translator = DoubaoTranslator.createFromEnv();
            
            // 创建文档翻译工具
            docTranslator = new VsCodeDocsTranslator(
                Paths.get("vscode-docs"),
                Paths.get("vscode-docs-zh"),
                translator
            );
            
            // 翻译整个目录并获取报告
            VsCodeDocsTranslator.TranslationReport report = 
                docTranslator.translateDirectoryWithReport();
            
            // 输出翻译报告
            logger.info("\n{}", report);
            
            if (report.successFiles == report.totalFiles) {
                logger.info("✓ 所有文件翻译成功！");
            } else {
                logger.warn("⚠ 部分文件翻译失败");
                report.failedFiles.forEach(file -> 
                    logger.warn("  失败: {}", file)
                );
            }
            
        } catch (Exception e) {
            logger.error("翻译失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
}

/**
 * 高级使用示例：自定义翻译流程
 */
class AdvancedExample {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedExample.class);
    
    /**
     * 示例：带有错误恢复的翻译
     */
    public static void translationWithErrorHandling() {
        DoubaoTranslator translator = null;
        
        try {
            translator = DoubaoTranslator.createFromEnv();
            
            String text = "Your text here";
            int maxRetries = 3;
            int retryCount = 0;
            String result = null;
            
            // 重试逻辑
            while (retryCount < maxRetries) {
                try {
                    result = translator.translate(text);
                    break;  // 成功，退出重试
                } catch (IOException e) {
                    retryCount++;
                    if (retryCount >= maxRetries) {
                        logger.error("达到最大重试次数，翻译失败", e);
                        result = text;  // 返回原文
                    } else {
                        logger.warn("翻译失败，正在重试... ({}/{})", retryCount, maxRetries);
                        Thread.sleep(1000 * retryCount);  // 递增延迟
                    }
                }
            }
            
            logger.info("最终结果: {}", result);
            
        } catch (Exception e) {
            logger.error("错误处理失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
    
    /**
     * 示例：自定义翻译器配置
     */
    public static void customTranslatorConfiguration() {
        // 直接使用API密钥创建翻译器
        String apiKey = "your-api-key-here";
        DoubaoTranslator translator = new DoubaoTranslator(apiKey);
        
        try {
            String result = translator.translate("Test");
            logger.info("结果: {}", result);
        } catch (Exception e) {
            logger.error("翻译失败", e);
        } finally {
            translator.close();
        }
    }
    
    /**
     * 示例：处理文件清单
     */
    public static void translateFileList(List<String> filePaths) {
        DoubaoTranslator translator = null;
        VsCodeDocsTranslator docTranslator = null;
        
        try {
            translator = DoubaoTranslator.createFromEnv();
            docTranslator = new VsCodeDocsTranslator(
                Paths.get("."),
                Paths.get("output"),
                translator
            );
            
            // 逐个翻译指定的文件
            for (String filePath : filePaths) {
                try {
                    String targetPath = "output/" + filePath;
                    docTranslator.translateFile(
                        Paths.get(filePath),
                        Paths.get(targetPath)
                    );
                } catch (Exception e) {
                    logger.error("文件翻译失败: {}", filePath, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("处理失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
}
