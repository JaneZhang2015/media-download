# 媒体下载器 - Media Downloader

一个用Java编写的工具，可以从网页中自动识别和下载媒体文件（音频/视频），并将其保存为MP3格式。

## 版本区别

### 🆕 v2.0.0 - MediaDownloaderPro (Pro版 - 支持动态加载)
- ⭐ **支持JavaScript动态加载的媒体**
- 使用Selenium浏览器自动化引擎
- 自动下载ChromeDriver
- 三层提取机制：页面源代码 + JavaScript + DOM查询
- 适合现代Web应用

### 📦 v1.0.0 - MediaDownloaderStandalone (快速版 - 无依赖)
- 无需任何外部依赖
- 仅使用Java标准库
- 启动快速，适合简单网站
- 文件小（仅5KB）

## 功能特性

- 🎵 **自动识别媒体文件** - 识别网页中的 `<audio>` 和 `<video>` 标签
- 🔗 **URL智能过滤** - 从HTML中提取所有媒体链接
- 📄 **JSON数据提取** - 可从JavaScript代码中的JSON数据中提取媒体URL
- 🌐 **相对URL处理** - 自动将相对URL转换为绝对URL
- 💾 **批量下载** - 一次下载网页中的所有媒体文件
- 📊 **支持多种格式** - mp3, mp4, m4a, wav, ogg, webm, aac, flac
- **🆕 动态加载媒体** - 支持JavaScript动态加载的内容（v2.0仅有）

## 系统要求

### 快速版 (v1.0.0 - Standalone)
- Java 11 或更高版本
- 网络连接
- 无需其他依赖

### Pro版 (v2.0.0 - 动态加载)
- Java 11 或更高版本
- Maven 3.6 或更高版本（用于编译）
- 网络连接（用于自动下载ChromeDriver）
- Chrome浏览器（或者让Selenium自动下载）

## 快速开始

### 方案1️⃣: 使用快速版（推荐 - 无需编译）

```bash
# 已编译完成，直接运行
java -jar target/media-downloader.jar https://example.com/music-page
```

### 方案2️⃣: 使用Pro版（支持动态加载媒体）

```bash
# 编译Pro版
mvn clean package

# 运行Pro版
java -jar target/media-downloader-2.0.0.jar https://example.com/music-page
```

## 详细使用说明

## 详细使用说明

### 基本用法 (快速版)
```bash
java -jar target/media-downloader.jar <URL>
```

### 指定输出目录 (快速版)
```bash
java -jar target/media-downloader.jar <URL> <输出目录>
```

### Pro版本使用方式 (需先编译)

#### 编译
```bash
mvn clean package
```

#### 运行（动态加载）
```bash
java -jar target/media-downloader-2.0.0.jar https://example.com/page
```

#### 运行（指定输出目录和模式）
```bash
java -jar target/media-downloader-2.0.0.jar https://example.com/page "./output" "dynamic"
```

### 使用示例

### 使用示例

```bash
# 快速版 - 下载媒体到默认目录 (./downloads)
java -jar target/media-downloader.jar https://example.com/music-page

# 快速版 - 下载媒体到指定目录
java -jar target/media-downloader.jar https://example.com/music-page ./my-music

# Pro版 - 动态加载（先编译）
mvn clean package
java -jar target/media-downloader-2.0.0.jar https://example.com/page

# Windows系统示例
java -jar target/media-downloader.jar https://example.com/page D:\Downloads
```

## 什么时候使用哪个版本？

### 使用快速版 ✅
- 网页包含静态的 `<audio>` 或 `<video>` 标签
- 媒体URL在HTML源代码中
- 需要快速启动，文件小
- 无需编译，开箱即用

### 使用Pro版 ✅
- 网页使用JavaScript动态加载媒体
- 媒体通过API调用获取（fetch/AJAX）
- 媒体在 window.media 等全局对象中
- 需要执行JavaScript获取URL

## 代码说明

### 核心类: `MediaDownloader` (快速版)

#### 主要方法

| 方法名 | 说明 |
|--------|------|
| `downloadMediaFromUrl(String url, String outputDir)` | 主下载方法，从URL下载所有媒体 |
| `fetchHtmlContent(String url)` | 获取网页HTML内容 |
| `extractMediaUrls(String htmlContent, String baseUrl)` | 从HTML中提取媒体URL |
| `downloadAndSaveMedia(String mediaUrl, Path outputDir, int index)` | 下载并保存单个媒体文件 |
| `extractUrlsFromJson(String jsonContent)` | 从JSON数据中提取媒体URL |

### 核心类: `MediaDownloaderPro` (Pro版 - 新增)

#### 新增方法

| 方法名 | 说明 |
|--------|------|
| `downloadMediaWithSelenium(String url, String outputDir)` | 使用Selenium打开浏览器下载媒体 |
| `extractMediaViaJavaScript(WebDriver driver)` | 执行JavaScript获取动态加载的媒体URL |
| `extractFromHtmlElements(WebDriver driver, String baseUrl)` | 通过DOM查询获取HTML元素中的媒体 |

**Pro版的优势**: 支持JavaScript动态加载的媒体文件！

### 依赖库

项目使用以下主要库：

**快速版 (v1.0.0)**:
- **jsoup** (1.15.4) - HTML解析
- **okhttp3** (4.11.0) - HTTP客户端，支持自定义超时和User-Agent
- **gson** (2.10.1) - JSON处理
- **logback** (1.4.11) - 日志系统
- **slf4j** (2.0.7) - 日志facade

**Pro版新增 (v2.0.0)**:
- **Selenium** (4.15.0) - 浏览器自动化框架（用于执行JavaScript）
- **WebDriverManager** (5.6.3) - 自动管理ChromeDriver

## 工作原理

### 快速版 (v1.0.0)

1. **获取网页内容** - 使用HTTP请求获取网页HTML
2. **解析HTML** - 使用正则表达式和简单解析提取媒体链接
3. **下载文件** - 使用HTTP客户端下载各个媒体文件
4. **保存文件** - 将文件保存到指定输出目录

### Pro版 (v2.0.0) - 三层提取机制

1. **启动浏览器** - 使用Selenium启动Chrome浏览器
2. **访问网页** - 打开目标URL，等待JavaScript执行
3. **多层提取**:
   - **第1层**: 从页面源代码提取（正则表达式）
   - **第2层**: 执行JavaScript获取动态URL
   - **第3层**: 通过DOM查询获取HTML元素
4. **合并结果** - 去重，获取所有项目的媒体URL
5. **下载文件** - 逐个下载所有找到的媒体
6. **保存文件** - 将文件保存到指定位置

## 文件结构

```
media-download/
├── pom.xml                                  # Maven配置文件
├── src/
│   └── main/
│       ├── java/
│       │   └── com/media/
│       │       ├── MediaDownloader.java    # 快速版完整功能版
│       │       ├── MediaDownloaderStandalone.java  # 快速版独立版（无依赖）
│       │       └── MediaDownloaderPro.java  # Pro版（支持动态加载）⭐
│       └── resources/
│           └── logback.xml                 # 日志配置
├── target/                                  # 编译输出目录
│   ├── media-downloader.jar                # 快速版（已编译）
│   └── media-downloader-2.0.0.jar          # Pro版（需编译）
├── README.md                               # 本文件
├── DYNAMIC_MODE.md                         # Pro版详细说明 ⭐ 新增
├── DEPLOYMENT.md                           # 部署指南
├── QUICK_START.md                          # 快速开始指南
└── START_HERE.md                           # 项目总结
```

## 日志输出

程序会在以下位置记录日志：
- **控制台** - 实时显示处理进度
- **日志文件** - 保存在 `./logs/app.log`

## 常见问题

### Q: 下载完成但没有找到任何媒体文件？
A: 这通常是因为媒体是通过**JavaScript动态加载**的。解决方案：
1. **使用Pro版本** - 它支持JavaScript动态加载
2. 编译: `mvn clean package`
3. 运行: `java -jar target/media-downloader-2.0.0.jar "你的URL"`
4. 详见: [DYNAMIC_MODE.md](DYNAMIC_MODE.md)

### Q: 如何诊断问题是否是动态加载？
A: 在浏览器开发者工具中：
1. 按 F12 打开开发者工具
2. 进入 **Console** 标签
3. 执行: `document.querySelector('audio')` 或 `document.querySelector('video')`
4. 如果返回 null，说明是动态加载
5. 如果返回元素，快速版应该可以工作

### Q: 下载的文件扩展名不对？
A: 程序会优先保持原始文件名，但音频文件会被重命名为 `.mp3`。如果需要转换音频格式，可以使用FFmpeg或其他音频转换工具。

### Q: 支持哪些媒体格式？
A: 程序支持：mp3, mp4, m4a, wav, ogg, webm, aac, flac

### Q: 网页无法访问？
A: 检查是否：
- 网络连接正常
- URL格式正确（包含 http:// 或 https://）
- 网站没有进行反爬虫限制
- User-Agent正确（程序已配置为Firefox浏览器）

### Q: 如何处理需要登录的网页？
A: 当前版本不支持自动登录。可以以下方式处理：
1. 在浏览器中登录后，获取Cookie，修改代码添加Cookie头
2. 使用Web自动化工具（如Selenium）结合本程序

## 性能优化建议

- **超时设置** - 当前设置为30秒连接超时，60秒读写超时
- **并发下载** - 可修改代码使用线程池实现并发下载
- **断点续传** - 可添加断点续传功能处理大文件

## 许可证

MIT License - 自由使用和修改

## 贡献

欢迎提交Bug报告和功能建议！

## 如何选择版本？

```
快速版 (v1.0.0)              Pro版 (v2.0.0)
├─ 无需编译                ├─ 需要Maven编译
├─ 启动快速                ├─ 启动现代浏览器
├─ 文件小(5KB)            ├─ 包含Selenium库
├─ 静态HTML解析           └─ 支持动态JavaScript
└─ 开箱即用               

优先使用快速版，如果找不到媒体，改用Pro版！
```

### 快速迁移指南

```bash
# 1. 尝试快速版（已编译）
java -jar target/media-downloader.jar "https://your-url"

# 2. 如果未找到媒体，编译并使用Pro版
mvn clean package
java -jar target/media-downloader-2.0.0.jar "https://your-url"

# 3. 查看详细说明
# 快速版: README.md
# Pro版: DYNAMIC_MODE.md
```

## 下一步

- 📖 阅读 [DYNAMIC_MODE.md](DYNAMIC_MODE.md) 了解Pro版详情
- 🚀 尝试: `java -jar target/media-downloader.jar "https://your-url"`
- 💻 编译Pro版: `mvn clean package`
- 🐛 遇到问题? 查看常见问题部分

## 更新日志

### v2.0.0 (2026-02-22) - Pro版发布
- ✨ **新增Pro版本** - 支持JavaScript动态加载媒体
- ✅ 集成Selenium浏览器自动化
- ✅ 三层提取机制（页面源代码 + JavaScript + DOM查询）
- ✅ 自动下载ChromeDriver
- ✅ 支持更多复杂网站场景
- ✅ 完整的调试输出

### v1.0.0 (2026-02-22) - 初始版本
- ✨ 初始版本发布
- ✅ 支持HTML媒体标签识别
- ✅ 支持URL链接提取
- ✅ 支持JSON数据解析
- ✅ 支持相对/绝对URL转换
