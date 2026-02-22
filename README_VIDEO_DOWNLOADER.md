# VS Code 文档视频下载工具

一个强大的Java工具，用于从VS Code官方文档网站自动下载所有MP4视频。

## 🚀 快速开始

### 1️⃣ 编译
```bash
cd e:\code\media-download
mvn clean package
```

### 2️⃣ 下载视频

**Windows:**
```bash
download-videos.bat https://code.visualstudio.com/docs ./vscode-docs
```

**Linux/Mac:**
```bash
chmod +x download-videos.sh
./download-videos.sh https://code.visualstudio.com/docs ./vscode-docs
```

## 📊 功能特性

| 功能 | 描述 |
|------|------|
| 🔍 **递归扫描** | 自动遍历所有文档页面 |
| 🎥 **多格式支持** | video标签、iframe、超链接、JSON等 |
| 📁 **目录保留** | 保持原始的文件夹层级结构 |
| 📊 **进度显示** | 实时显示下载进度百分比 |
| 🔄 **错误恢复** | 单个失败不影响其他文件 |
| ✅ **文件去重** | 自动避免保存重复文件 |

## 📥 下载示例

### 下载所有视频
```bash
download-videos.bat https://code.visualstudio.com/docs ./all-videos
# 预期：47+ 视频，约 4+ GB
```

### 下载 Copilot 相关视频
```bash
download-videos.bat https://code.visualstudio.com/docs/copilot ./copilot-videos
# 预期：8-10 视频，约 800MB
```

### 下载入门视频
```bash
download-videos.bat https://code.visualstudio.com/docs/introvideos ./intro-videos
# 预期：5 视频，约 500MB
```

### 下载特定类别视频
```bash
# 容器开发
download-videos.bat https://code.visualstudio.com/docs/containers ./container-videos

# Python 开发
download-videos.bat https://code.visualstudio.com/docs/python ./python-videos

# 远程开发
download-videos.bat https://code.visualstudio.com/docs/remote ./remote-videos

# 调试技巧
download-videos.bat https://code.visualstudio.com/docs/editor/debugging ./debug-videos
```

## 📂 输出目录结构

```
vscode-docs/
├── docs/
│   ├── introvideos/          ← 入门视频
│   │   └── video-*.mp4
│   ├── copilot/
│   │   ├── agents/
│   │   │   └── media/
│   │   │       └── *.mp4
│   │   ├── chat/
│   │   │   └── media/
│   │   │       └── *.mp4
│   │   └── guides/
│   │       └── media/
│   │           └── *.mp4
│   ├── containers/
│   │   └── media/
│   │       └── *.mp4
│   ├── python/
│   │   └── media/
│   │       └── *.mp4
│   └── ...
└── vscode-docs-zh/          ← 中文文档
```

## 🎬 支持的视频链接格式

- ✅ HTML5 `<video>` 标签
- ✅ `<iframe>` 嵌入视频
- ✅ 直接 `<a>` 超链接
- ✅ `<img data-video>` 属性
- ✅ JSON 数据中的 MP4 链接

## ⚙️ 系统要求

- **Java**: JDK 11 或更高版本
- **网络**: 稳定的Internet连接
- **磁盘**: 4+ GB 可用空间（下载所有视频）
- **操作系统**: Windows / Linux / macOS

## 📖 详细文档

- [完整使用指南](VIDEO_DOWNLOAD_COMPLETE_GUIDE.md)
- [使用示例和技巧](VIDEO_USAGE_EXAMPLES.md)
- [下载工具指南](VIDEO_DOWNLOAD_GUIDE.md)

## 🛠️ 高级用法

### 后台下载（Linux/Mac）
```bash
nohup ./download-videos.sh https://code.visualstudio.com/docs ./videos > download.log 2>&1 &
```

### 指定不同输出目录
```bash
download-videos.bat https://code.visualstudio.com/docs/copilot E:\Videos\copilot-docs
```

### 直接 Java 命令行
```bash
java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader \
  https://code.visualstudio.com/docs ./output --videos
```

## 🐛 常见问题

**Q: 下载速度很慢？**  
A: 这取决于网络速度和视频文件大小。大多数视频在50-300MB之间。

**Q: 某些视频下载失败？**  
A: 工具会自动跳过失败的视频并继续。常见原因：链接失效、网络中断、权限限制。

**Q: 如何只更新新添加的视频？**  
A: 工具会自动检测已存在的文件，重新运行时只下载缺失的视频。

**Q: 支持其他格式（WebM、MP3等）？**  
A: 当前仅支持 MP4。如需支持其他格式，请修改源代码中的格式检查逻辑。

## 📊 性能数据

| 指标 | 值 |
|------|-----|
| JAR 文件大小 | ~45 MB |
| 页面扫描速度 | 2-3 页/秒 |
| 总视频数量 | 47+ 个 |
| 总下载大小 | 4+ GB |
| 平均视频大小 | 50-300 MB |

## 🔧 源代码文件

- [VSCodeDocumentDownloader.java](src/main/java/com/media/VSCodeDocumentDownloader.java) - 主程序
- [download-videos.bat](download-videos.bat) - Windows 启动脚本
- [download-videos.sh](download-videos.sh) - Linux/Mac 启动脚本

## 📝 许可证

采用与 VS Code 官方文档相同的许可证。

## 🚀 版本信息

- **当前版本**: 1.0.0
- **发布日期**: 2026-02-22
- **状态**: 稳定版

## 💡 提示和技巧

### 1. 验证 Java 安装
```bash
java -version
# 应显示 Java 11 或更高版本
```

### 2. 检查网络连接
```bash
ping code.visualstudio.com
```

### 3. 查看下载进度
```bash
# 文件会在下载时实时显示进度百分比
```

### 4. 批量下载多个来源
创建脚本并按顺序运行多个下载命令。

## 🤝 相关项目

- [VS Code 文档翻译工具](TRANSLATOR_README.md)
- [Media Downloader Pro](README.md)

---

**快速链接:**
- 🌐 [VS Code 官方网站](https://code.visualstudio.com)
- 📚 [VS Code 文档](https://code.visualstudio.com/docs)
- 🐙 [GitHub 仓库](https://github.com/JaneZhang2015/media-download)

**需要帮助?** 查看详细的[完整使用指南](VIDEO_DOWNLOAD_COMPLETE_GUIDE.md)
