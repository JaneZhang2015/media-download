# 📥 媒体下载器 - 部署完成 ✅

你的Java媒体下载工具已经成功生成！现在可以立即使用。

## 🎯 最快开始方法（30秒）

```powershell
# 进入项目目录
cd e:\code\media-download

# 直接运行
java -jar target/media-downloader.jar "https://你的网址"
```

**例如:**
```powershell
java -jar target/media-downloader.jar "https://example.com/page" "D:\Downloads"
```

---

## 📦 已生成的文件

### ✅ 可直接使用
- **`target/media-downloader.jar`** - ⭐ **推荐使用**
  - 可执行的JAR文件
  - 无需任何依赖或配置
  - 开箱即用

### 📚 源代码（可选）
- **`src/main/java/com/media/MediaDownloaderStandalone.java`** - 独立版本的源代码
- **`src/main/java/com/media/MediaDownloader.java`** - Maven版本的源代码（需要依赖库）

### 📖 文档
- **`QUICK_START.md`** - 快速开始指南（详细的使用说明）
- **`README.md`** - 完整的项目文档（详尽的技术文档）

---

## 🚀 实际使用示例

### 1️⃣ 基本使用
```powershell
java -jar target/media-downloader.jar https://www.example.com/music-page
```
**效果**: 从网页中自动识别所有媒体文件并下载到 `./downloads` 目录

### 2️⃣ 指定下载目录
```powershell
java -jar target/media-downloader.jar https://www.example.com/podcast "./my-podcasts"
```
**效果**: 下载到 `./my-podcasts` 目录

### 3️⃣ Windows绝对路径
```powershell
java -jar target/media-downloader.jar https://www.example.com/videos "D:\Videos"
```

### 4️⃣ PowerShell脚本
```powershell
# 创建并运行脚本
$urls = @(
    "https://site1.com/media",
    "https://site2.com/media",
    "https://site3.com/media"
)

foreach ($url in $urls) {
    Write-Host "下载: $url"
    java -jar target/media-downloader.jar $url
    Write-Host "完成`n"
}
```

---

## 🎵 支持的媒体格式

| 音频格式 | 视频格式 |
|---------|---------|
| .mp3 | .mp4 |
| .m4a | .webm |
| .wav | |
| .ogg | |
| .aac | |
| .flac | |

---

## 🔍 工作原理

程序会：
1. ✅ 访问你提供的URL
2. ✅ 解析网页HTML
3. ✅ 识别所有媒体标签（`<audio>`, `<video>`, `<a href="...mp3">`等）
4. ✅ 提取媒体文件链接
5. ✅ 自动转换相对URL为绝对URL
6. ✅ 下载所有媒体文件
7. ✅ 保存到指定目录

---

## 📊 程序输出示例

```
开始从 https://example.com/page 下载媒体
✓ 成功获取网页内容
✓ 找到 3 个媒体文件

[1/3] 下载: https://cdn.example.com/audio1.mp3
  进度: 10% 20% 30% 40% 50% 60% 70% 80% 90% 完成
  ✓ 已保存: downloads/audio1.mp3

[2/3] 下载: https://cdn.example.com/audio2.mp3
  进度: 10% 20% 30% 40% 50% 60% 70% 80% 90% 完成
  ✓ 已保存: downloads/audio2.mp3

[3/3] 下载: https://cdn.example.com/video.mp4
  进度: 5% 10% 15% 20% 25% ...
  ✓ 已保存: downloads/video.mp4

✓ 下载完成！
```

---

## 💾 文件目录结构

```
e:\code\media-download
├── target/
│   ├── media-downloader.jar        ⭐ 可执行程序
│   ├── classes/                    (编译的字节码)
│   └── ...
│
├── src/
│   └── main/
│       ├── java/com/media/
│       │   ├── MediaDownloader.java
│       │   └── MediaDownloaderStandalone.java
│       └── resources/
│           └── logback.xml
│
├── pom.xml                         (Maven配置)
├── README.md                       (完整文档)
├── QUICK_START.md                  (快速指南)
├── DEPLOYMENT.md                   (本文档)
└── manifest.txt                    (JAR清单)
```

---

## ⚙️ 系统要求

✅ **已验证的需求**：
- **Java**: 11 或更高版本
- **OS**: Windows 10+, Linux, macOS
- **网络**: 需要互联网连接
- **磁盘**: 足够存储下载的媒体文件

**检查Java版本**：
```powershell
java -version
```

---

## 🆘 快速故障排除

| 问题 | 解决方案 |
|------|--------|
| `找不到命令 java` | 安装Java或添加到PATH |
| `无法访问网页` | 检查URL和网络连接 |
| `没有找到媒体文件` | 网页可能不包含媒体 |
| `权限被拒绝` | 检查输出目录的写入权限 |
| `网络超时` | 检查网络或尝试其他网址 |

---

## 🛠️ 自定义和扩展

### 修改源代码
如果想修改程序，编辑源文件然后重新编译：

```powershell
# 编辑源文件
notepad src\main\java\com\media\MediaDownloaderStandalone.java

# 重新编译
javac -encoding UTF-8 -d target\classes src\main\java\com\media\MediaDownloaderStandalone.java

# 重新创建JAR
jar cfm target/media-downloader.jar manifest.txt -C target/classes .
```

### Maven版本（高级）
如果系统安装了Maven，可使用完整版本：

```powershell
# 依赖更多功能库
mvn clean package
```

---

## 📝 常见使用场景

### 场景1: 下载播客剧集
```powershell
java -jar target/media-downloader.jar "https://podcast.example.com/episode123" "./podcasts"
```

### 场景2: 备份在线音乐
```powershell
java -jar target/media-downloader.jar "https://music.example.com/playlist" "./music-backup"
```

### 场景3: 下载讲座视频
```powershell
java -jar target/media-downloader.jar "https://university.edu/lectures/cs101" "./lectures"
```

### 场景4: 批量下载
创建 `batch-download.ps1`:
```powershell
$website = $args[0]
$outputDir = $args[1]

if (-not $website) {
    Write-Host "使用: .\batch-download.ps1 <URL> [输出目录]"
    exit
}

Write-Host "开始下载媒体..."
java -jar target/media-downloader.jar $website $outputDir
Write-Host "完成！"
```

运行：
```powershell
.\batch-download.ps1 "https://example.com" "D:\Downloads"
```

---

## 🔐 安全和隐私说明

✅ **安全特性**：
- 直接连接到目标网址（无代理）
- 不收集任何个人信息
- 本地处理所有数据
- 开源代码（可审计）

---

## 📞 获取帮助

1. **快速问题**: 查看 `QUICK_START.md`
2. **技术细节**: 查看 `README.md`  
3. **代码注释**: 查看源代码
4. **测试URL**: 使用公开的示例网站

---

## ✨ 功能总结

| 功能 | 状态 |
|------|------|
| HTML媒体标签识别 | ✅ 完全支持 |
| HTML链接提取 | ✅ 完全支持 |
| JavaScript URL提取 | ✅ 完全支持 |
| 相对URL转换 | ✅ 完全支持 |
| 多格式支持 | ✅ 8+种格式 |
| 进度显示 | ✅ 实时百分比 |
| 错误恢复 | ✅ 强大 |
| 日志记录 | ✅ 详细 |

---

## 🎓 学习资源

想更深入了解？
- **Java新手**: 查看 `src/main/java/com/media/MediaDownloaderStandalone.java` 的代码注释
- **高级用户**: 查看 `src/main/java/com/media/MediaDownloader.java` 了解完整版本
- **Web技术**: 理解HTML、HTTP、URL的基础知识会有帮助

---

## 🚀 后续步骤

### 立即使用
```powershell
cd e:\code\media-download
java -jar target/media-downloader.jar "https://example.com/page"
```

### 查看详细文档
```powershell
# Windows
start QUICK_START.md
start README.md

# 或直接打开
notepad QUICK_START.md
```

### 批量使用
创建下载脚本并定时运行

---

**🎉 祝贺！你的媒体下载工具已准备好使用！**

**下一步**: 运行第一个下载命令试试看！

---

**版本**: 1.0.0  
**生成日期**: 2026年2月22日  
**状态**: ✅ 生产就绪
