# VS Code 文档翻译工具 - 配置示例

## API 配置

### 火山引擎认证
- **API Key**: 从 https://console.volcengine.com 获取
- **API Endpoint**: https://ark.cn-beijing.volces.com/api/v3/responses
- **Model**: doubao-seed-translation-250915

### 翻译参数配置

**在 DoubaoTranslator.java 中修改:**

```java
// 翻译语言配置
private static final String SOURCE_LANGUAGE = "en";  // 英文
private static final String TARGET_LANGUAGE = "zh";  // 中文

// 支持的其他语言示例:
// "en" - 英文
// "zh" - 中文
// "ja" - 日文
// "ko" - 韩文
// "es" - 西班牙文
// "fr" - 法文
// "de" - 德文
// "ru" - 俄文
// 更多语言代码: https://docs.volcengine.com/
```

**在 VsCodeDocsTranslator.java 中修改:**

```java
// API 调用延迟 (毫秒)
private static final long TRANSLATION_DELAY_MS = 500;

// 支持的文件扩展名
private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".txt", ".md");

// 如需支持更多格式，修改为:
private static final List<String> SUPPORTED_EXTENSIONS = 
    Arrays.asList(".txt", ".md", ".html", ".xml", ".json");
```

## 运行时环境变量

### Linux/Mac

```bash
# 基本配置
export ARK_API_KEY="your_api_key_here"

# 可选：设置Java内存
export JAVA_OPTS="-Xms512m -Xmx2g"

# 可选：设置日志级别
export LOG_LEVEL="INFO"  # DEBUG, INFO, WARN, ERROR

# 运行翻译
./translate.sh vscode-docs vscode-docs-zh
```

### Windows (PowerShell)

```powershell
# 基本配置
$env:ARK_API_KEY = "your_api_key_here"

# 可选：设置Java内存
$env:JAVA_OPTS = "-Xms512m -Xmx2g"

# 运行翻译
.\translate.bat vscode-docs vscode-docs-zh
```

### Windows (CMD)

```cmd
REM 基本配置
set ARK_API_KEY=your_api_key_here

REM 可选：设置Java内存
set JAVA_OPTS=-Xms512m -Xmx2g

REM 运行翻译
translate.bat vscode-docs vscode-docs-zh
```

## 日志配置

编辑 `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 日志输出到控制台 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss} [%-5level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 日志输出到文件 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/translator.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%-5level] %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/translator.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 根日志级别 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

    <!-- 特定包的日志级别 -->
    <logger name="com.media" level="DEBUG" />
</configuration>
```

## 性能优化建议

### 内存配置

对于大规模翻译，增加Java堆内存：

```bash
# Linux/Mac
export JAVA_OPTS="-Xms1g -Xmx4g"

# Windows
set JAVA_OPTS=-Xms1g -Xmx4g
```

### 并发配置 (高级用法)

可以修改代码以支持并发翻译：

```java
// 在 VsCodeDocsTranslator.java 中添加
private static final int THREAD_POOL_SIZE = 4;  // 并发线程数

// 使用 ExecutorService 并发处理文件
ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
for (Path file : filesToTranslate) {
    executor.submit(() -> translateFile(file));
}
```

### API 请求优化

```java
// 减少 API 调用延迟（更快但可能触发限流）
private static final long TRANSLATION_DELAY_MS = 100;

// 或者使用指数退避重试策略
// 在网络不稳定时自动重试
```

## 故障排除

### 问题 1: API 连接超时

**症状**: `SocketTimeoutException`

**解决**:
```java
// 增加超时时间 (秒)
private static final int CONNECT_TIMEOUT = 60;  // 默认30秒
private static final int READ_TIMEOUT = 120;    // 默认60秒
```

### 问题 2: 内存不足

**症状**: `OutOfMemoryError`

**解决**:
```bash
# 增加JVM堆内存
export JAVA_OPTS="-Xmx4g"

# 或启用垃圾回收优化
export JAVA_OPTS="-Xmx4g -XX:+UseG1GC"
```

### 问题 3: API 速率限制

**症状**: `429 Too Many Requests`

**解决**:
```java
// 增加 API 请求间隔
private static final long TRANSLATION_DELAY_MS = 1000;  // 改为1秒
```

### 问题 4: 某些文件翻译失败

**症状**: 日志显示部分文件失败

**解决**:
- 检查文件编码是否为 UTF-8
- 查看详细错误日志: `logs/translator.log`
- 手动翻译失败的文件或重新运行

## 成本估算

根据火山引擎定价：

```
假设翻译 50MB 英文文档
- 平均5个英文字符 = 1个中文字符
- 50MB ≈ 10,000,000 字符
- 翻译成本 ≈ 根据当前API价格计算
```

详见：https://www.volcengine.com/product/translate

## 自定义扩展

### 添加其他翻译服务

可以创建新的翻译器实现 `Translator` 接口：

```java
public interface Translator {
    String translate(String text) throws IOException;
    void close();
}

public class GoogleTranslator implements Translator { ... }
public class BaiduTranslator implements Translator { ... }
```

### 添加预处理步骤

在翻译前清理或格式化文本：

```java
private String preprocessText(String text) {
    // 移除多余空格
    // 规范化换行符
    // 替换特殊字符
    return text;
}
```

### 添加后处理步骤

在翻译后优化结果：

```java
private String postprocessText(String text) {
    // 修复常见翻译错误
    // 应用词汇替换字典
    // 格式优化
    return text;
}
```

## 更多信息

- 火山引擎文档: https://www.volcengine.com/docs/
- 翻译API文档: https://docs.volcengine.com/translate
- 项目主页: [项目GitHub地址]
