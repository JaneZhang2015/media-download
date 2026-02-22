# 快速开始指南

## 📋 已创建的文件

你现在拥有一个完整的VS Code文档翻译系统，包含以下文件：

### 核心类文件

1. **DoubaoTranslator.java** - 火山引擎翻译API调用类
   - 封装HTTP请求和API交互
   - 支持单个和批量翻译
   - 完整的错误处理

2. **VsCodeDocsTranslator.java** - 文档翻译工具
   - 递归翻译整个目录
   - 保留文件夹结构
   - 生成详细的翻译报告

3. **TranslatorMain.java** - 命令行入口
   - 直接从命令行运行
   - 支持自定义源和目标目录
   - 环境变量配置

4. **TranslatorExample.java** - 使用示例代码
   - 演示4个使用场景
   - 包含高级用法
   - 参考实现

### 文档和脚本

- **TRANSLATOR_README.md** - 详细功能文档
- **CONFIG_GUIDE.md** - 配置和优化指南
- **translate.sh** - Linux/Mac启动脚本
- **translate.bat** - Windows启动脚本

## 🚀 快速开始（5分钟）

### 第1步：获取API密钥

访问 https://console.volcengine.com，获取 `ARK_API_KEY`

### 第2步：设置环境变量

**Windows (PowerShell):**
```powershell
$env:ARK_API_KEY = "your_api_key_here"
```

**Linux/Mac:**
```bash
export ARK_API_KEY="your_api_key_here"
```

### 第3步：编译项目

```bash
cd e:\code\media-download
mvn clean package
```

### 第4步：运行翻译

**Windows:**
```batch
translate.bat vscode-docs vscode-docs-zh
```

**Linux/Mac:**
```bash
./translate.sh vscode-docs vscode-docs-zh
```

## 📚 API 使用示例

### 1. 翻译单个文本

```java
DoubaoTranslator translator = DoubaoTranslator.createFromEnv();
String result = translator.translate("Hello, World!");
System.out.println(result);
translator.close();
```

### 2. 批量翻译

```java
DoubaoTranslator translator = DoubaoTranslator.createFromEnv();
List<String> texts = Arrays.asList("Text 1", "Text 2", "Text 3");
Map<String, String> results = translator.translateBatch(texts);
translator.close();
```

### 3. 翻译文件

```java
DoubaoTranslator translator = DoubaoTranslator.createFromEnv();
VsCodeDocsTranslator docTranslator = new VsCodeDocsTranslator(
    Paths.get("source"),
    Paths.get("target"),
    translator
);
docTranslator.translateFile(
    Paths.get("source/file.txt"),
    Paths.get("target/file.txt")
);
translator.close();
```

### 4. 翻译整个目录

```java
DoubaoTranslator translator = DoubaoTranslator.createFromEnv();
VsCodeDocsTranslator docTranslator = new VsCodeDocsTranslator(
    Paths.get("source"),
    Paths.get("target"),
    translator
);
VsCodeDocsTranslator.TranslationReport report = 
    docTranslator.translateDirectoryWithReport();
System.out.println(report);
translator.close();
```

## 🛠️ 功能特性

✅ 调用火山引擎翻译API（doubao-seed-translation-250915）
✅ 支持单文件和批量翻译
✅ 保留原文件夹结构
✅ 大文本自动分段处理
✅ 错误恢复和重试机制
✅ 详细的翻译报告和日志
✅ 跨平台支持（Windows/Linux/Mac）

## 📊 翻译报告示例

```
翻译报告:
  总文件数: 150
  成功: 150
  失败: 0
  总数据量: 45.32 MB
  耗时: 3240 秒
  成功率: 100.00%
```

## 🔧 常见操作

### 查看项目结构

```bash
ls -la src/main/java/com/media/
```

### 查看生成的JAR

```bash
ls -la target/media-downloader-*.jar
```

### 运行单元测试（如有）

```bash
mvn test
```

### 清除编译文件

```bash
mvn clean
```

## 📝 日志位置

日志文件保存在 `logs/` 目录下：

```
logs/
├── translator.log          # 当前日志
├── translator.2024-01-01.1.log   # 历史日志
└── translator.2024-01-01.2.log
```

## 🔑 环境变量

### 必需

- `ARK_API_KEY` - 火山引擎API密钥

### 可选

- `JAVA_OPTS` - Java运行时选项 (如 `-Xmx4g`)
- `LOG_LEVEL` - 日志级别 (INFO, DEBUG, WARN, ERROR)

## 📖 详细文档

- [TRANSLATOR_README.md](TRANSLATOR_README.md) - 完整功能说明
- [CONFIG_GUIDE.md](CONFIG_GUIDE.md) - 配置和优化指南

## 🎯 下一步

1. **运行示例代码**
   ```bash
   java -cp target/media-downloader-1.0.0.jar:lib/* \
       com.media.TranslatorExample
   ```

2. **自定义配置**
   编辑 `DoubaoTranslator.java` 中的常量，如语言、API延迟等

3. **集成到你的项目**
   复制 `DoubaoTranslator.java` 到你的项目中使用

4. **处理大规模翻译**
   根据 `CONFIG_GUIDE.md` 中的建议优化内存和并发

## ❓ 常见问题

**Q: 如何修改翻译语言？**
A: 编辑 `DoubaoTranslator.java` 中的:
```java
private static final String SOURCE_LANGUAGE = "en";  // 改为其他语言代码
private static final String TARGET_LANGUAGE = "zh";  // 改为其他语言代码
```

**Q: 翻译速度太慢？**
A: 在 `VsCodeDocsTranslator.java` 中减少 `TRANSLATION_DELAY_MS` 值

**Q: 如何支持更多文件格式？**
A: 修改 `SUPPORTED_EXTENSIONS` 列表为所需格式

**Q: 如何保存翻译结果到数据库？**
A: 在 `VsCodeDocsTranslator.java` 的 `translateFile()` 方法中添加数据库保存逻辑

## 📞 支持

遇到问题？

1. 检查日志文件 `logs/translator.log`
2. 查看 [TRANSLATOR_README.md](TRANSLATOR_README.md) 的故障排除部分
3. 验证 API 密钥是否正确设置

## 📄 许可证

MIT License

---

祝你使用愉快！如有问题，欢迎反馈。
