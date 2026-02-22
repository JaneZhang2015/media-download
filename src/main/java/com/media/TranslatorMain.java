package com.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * VS Code文档翻译主程序入口
 * 
 * 使用说明:
 * 1. 设置环境变量: export ARK_API_KEY="你的API密钥"
 * 2. Windows: set ARK_API_KEY=你的API密钥
 * 
 * 执行翻译:
 * java -cp lib/* com.media.TranslatorMain
 * 
 * 或指定源和目标目录:
 * java -cp lib/* com.media.TranslatorMain [源目录] [目标目录]
 */
public class TranslatorMain {
    private static final Logger logger = LoggerFactory.getLogger(TranslatorMain.class);
    
    private static final String DEFAULT_SOURCE_DIR = "vscode-docs";
    private static final String DEFAULT_TARGET_DIR = "vscode-docs-zh";
    
    public static void main(String[] args) {
        String sourceDir = DEFAULT_SOURCE_DIR;
        String targetDir = DEFAULT_TARGET_DIR;
        
        // 解析命令行参数
        if (args.length > 0) {
            sourceDir = args[0];
        }
        if (args.length > 1) {
            targetDir = args[1];
        }
        
        final String source = sourceDir;
        final String target = targetDir;
        
        logger.info("===============================================");
        logger.info("VS Code 文档翻译工具");
        logger.info("===============================================");
        logger.info("源目录: {}", source);
        logger.info("目标目录: {}", target);
        
        // 验证API密钥
        String apiKey = System.getenv("ARK_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("❌ 错误: 未设置环境变量 ARK_API_KEY");
            logger.error("请先设置API密钥:");
            logger.error("  Linux/Mac: export ARK_API_KEY=\"your_api_key\"");
            logger.error("  Windows: set ARK_API_KEY=your_api_key");
            System.exit(1);
        }
        
        // 验证源目录
        if (!Files.exists(Paths.get(source))) {
            logger.error("❌ 源目录不存在: {}", source);
            System.exit(1);
        }
        
        DoubaoTranslator translator = null;
        VsCodeDocsTranslator docsTranslator = null;
        
        try {
            // 初始化翻译器
            logger.info("\n初始化翻译器...");
            translator = new DoubaoTranslator(apiKey);
            
            // 创建文档翻译工具
            docsTranslator = new VsCodeDocsTranslator(
                    Paths.get(source),
                    Paths.get(target),
                    translator
            );
            
            // 执行翻译
            logger.info("\n开始翻译文档...");
            VsCodeDocsTranslator.TranslationReport report = 
                    docsTranslator.translateDirectoryWithReport();
            
            // 打印报告
            logger.info("\n{}", report);
            
            if (report.successFiles == report.totalFiles) {
                logger.info("\n✓ 翻译成功完成！");
                logger.info("已翻译的文件保存在: {}", Paths.get(target).toAbsolutePath());
            } else {
                logger.warn("\n⚠ 翻译部分失败");
                if (!report.failedFiles.isEmpty()) {
                    logger.warn("失败的文件:");
                    report.failedFiles.forEach(file -> logger.warn("  - {}", file));
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ 翻译过程失败", e);
            System.exit(1);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
}
