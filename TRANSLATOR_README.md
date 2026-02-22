# VS Code 文档翻译工具

这是一个使用火山引擎豆宝翻译API将VS Code英文文档转换为中文的Java工具。

## 功能特性

- ✅ 调用火山引擎翻译API（doubao-seed-translation-250915）
- ✅ 支持单文件或批量翻译
- ✅ 保留原文件夹结构
- ✅ 支持大文本分段处理
- ✅ 错误恢复和重试机制
- ✅ 详细的翻译报告
- ✅ 日志记录

## 前置条件

1. **Java** (11+)
2. **Maven** (用于构建)
3. **火山引擎API密钥** - 在 https://console.volcengine.com 获取

## 安装和配置

### 1. 获取API密钥

访问火山引擎控制台获取 `ARK_API_KEY`：
- 地址: https://console.volcengine.com
- 模型: `doubao-seed-translation-250915`

### 2. 设置环境变量

**Linux / Mac:**
```bash
export ARK_API_KEY="your_api_key_here"
```

**Windows (PowerShell):**
```powershell
$env:ARK_API_KEY = "your_api_key_here"
```

**Windows (CMD):**
```cmd
set ARK_API_KEY=your_api_key_here
```

## 使用方法

### 编译项目

```bash
cd e:\code\media-download
mvn clean package
```

### 方法 1: 使用默认目录翻译

默认情况下，将翻译 `vscode-docs` 目录到 `vscode-docs-zh` 目录。

```bash
java -cp target/media-downloader-1.0.0.jar:lib/* com.media.TranslatorMain
```

### 方法 2: 指定源和目标目录

```bash
java -cp target/media-downloader-1.0.0.jar:lib/* com.media.TranslatorMain ./source-docs ./translated-docs
```

### 方法 3: 使用Java程序调用

```java
import com.media.DoubaoTranslator;
import com.media.VsCodeDocsTranslator;
import java.nio.file.Paths;

public class Example {
    public static void main(String[] args) throws Exception {
        // 创建翻译器
        DoubaoTranslator translator = DoubaoTranslator.createFromEnv();
        
        // 翻译单个文本
        String english = "Welcome to Visual Studio Code";
        String chinese = translator.translate(english);
        System.out.println("翻译结果: " + chinese);
        
        // 或者批量翻译文件
        VsCodeDocsTranslator docTranslator = new VsCodeDocsTranslator(
            Paths.get("vscode-docs"),
            Paths.get("vscode-docs-zh"),
            translator
        );
        
        VsCodeDocsTranslator.TranslationReport report = 
            docTranslator.translateDirectoryWithReport();
        System.out.println(report);
        
        translator.close();
    }
}
```

## API接口详情

### 翻译单个文本

```java
DoubaoTranslator translator = new DoubaoTranslator(apiKey);
String translatedText = translator.translate("Hello, World!");
```

### 批量翻译

```java
List<String> texts = Arrays.asList(
    "Text 1",
    "Text 2",
    "Text 3"
);

Map<String, String> results = translator.translateBatch(texts);
// results: {
//   "Text 1" -> "文本 1",
//   "Text 2" -> "文本 2",
//   "Text 3" -> "文本 3"
// }
```

### 翻译目录

```java
VsCodeDocsTranslator docTranslator = new VsCodeDocsTranslator(
    Paths.get("source"),
    Paths.get("target"),
    translator
);

int successCount = docTranslator.translateDirectory();
```

## 翻译报告例例

```
翻译报告:
  总文件数: 150
  成功: 150
  失败: 0
  总数据量: 45.32 MB
  耗时: 3240 秒
  成功率: 100.00%
```

## 文件结构

```
DoubaoTranslator.java          # 核心翻译API调用类
VsCodeDocsTranslator.java      # 批量文档翻译工具
TranslatorMain.java            # 命令行入口
```

## 类说明

### DoubaoTranslator

核心翻译类，负责调用火山引擎翻译API。

**主要方法:**
- `translate(String text)` - 翻译单个文本
- `translateBatch(List<String> texts)` - 批量翻译多个文本
- `createFromEnv()` - 从环境变量创建实例

**配置:**
- API URL: `https://ark.cn-beijing.volces.com/api/v3/responses`
- 模型: `doubao-seed-translation-250915`
- 源语言: 英文 (en)
- 目标语言: 中文 (zh)

### VsCodeDocsTranslator

文档翻译工具，用于批量翻译文件和目录。

**主要方法:**
- `translateFile(Path sourceFile, Path targetFile)` - 翻译单个文件
- `translateDirectory()` - 递归翻译整个目录
- `translateDirectoryWithReport()` - 翻译并生成详细报告

**支持的文件格式:**
- `.txt` 文本文件
- `.md` Markdown文件

### TranslatorMain

命令行入口程序。

**用法:**
```
java com.media.TranslatorMain [源目录] [目标目录]
```

## 配置参数

在 `DoubaoTranslator.java` 中可修改的常量：

```java
private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/responses";
private static final String MODEL = "doubao-seed-translation-250915";
private static final String SOURCE_LANGUAGE = "en";   // 源语言
private static final String TARGET_LANGUAGE = "zh";   // 目标语言
```

在 `VsCodeDocsTranslator.java` 中可修改的常量：

```java
private static final long TRANSLATION_DELAY_MS = 500;  // API调用间隔
```

## 示例：翻译VS Code官方文档

**目录结构:**
```
vscode-docs/
├── docs/
│   ├── getstarted/
│   │   ├── tips-and-tricks.txt
│   │   └── introvideos.txt
│   ├── editor/
│   │   └── codebasics.txt
│   └── ...
```

**执行翻译:**
```bash
# 设置环境变量
export ARK_API_KEY="your_key"

# 编译
mvn clean package

# 翻译
java -cp target/media-downloader-1.0.0.jar:lib/* com.media.TranslatorMain
```

**输出:**
```
vscode-docs-zh/
├── docs/
│   ├── getstarted/
│   │   ├── tips-and-tricks.txt (已翻译为中文)
│   │   └── introvideos.txt     (已翻译为中文)
│   ├── editor/
│   │   └── codebasics.txt      (已翻译为中文)
│   └── ...
```

## 错误处理

程序包含完整的错误处理机制：

1. **API密钥缺失** - 程序会给出明确的设置指导
2. **网络错误** - 自动记录并跳过，保留原文
3. **文件读写错误** - 记录错误并继续处理其他文件
4. **解析错误** - 失败时保留原文内容

所有错误都会通过日志记录在 `logs/` 目录下。

## 常见问题

**Q: 翻译速度太慢？**
A: 在 `VsCodeDocsTranslator.java` 中调整 `TRANSLATION_DELAY_MS` 的值（单位：毫秒）。

**Q: 如何跳过某些文件？**
A: 修改 `SUPPORTED_EXTENSIONS` 列表，或在 `isSupportedFile()` 方法中添加过滤逻辑。

**Q: 翻译结果不准确？**
A: 可以在代码中添加人工审核步骤，或提交反馈给火山引擎改进模型。

**Q: 如何处理非常大的文件？**
A: 程序已实现文本分段翻译，按段落处理避免超时。

## 许可证

MIT License

## 支持

如有问题，请查看日志文件或提交Issue。
