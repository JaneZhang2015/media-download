# VS Code 文档视频下载工具

## 功能说明

此工具可以从VS Code官方文档网站自动爬取并下载所有MP4格式的视频文件，并保存到相应的目录结构中。

## 特性

✓ **自动扫描**: 递归扫描所有文档页面
✓ **智能识别**: 支持多种视频链接格式（video标签、iframe、a标签等）
✓ **保留结构**: 保持原始的文件夹层级结构
✓ **错误恢复**: 如果单个视频下载失败，继续下载其他视频
✓ **进度显示**: 实时显示下载进度
✓ **文件去重**: 避免保存重复的视频文件

## 支持的视频链接格式

- `<video src="...mp4"></video>` - HTML5视频标签
- `<video><source src="...mp4"></source></video>` - 视频源
- `<iframe src="...mp4"></iframe>` - 内嵌视频
- `<a href="...mp4">Download</a>` - 超链接
- `<img data-video="...mp4">` - 图片属性
- JSON数据中的MP4链接

## 安装要求

- Java 11 或更高版本
- Internet 连接
- 足够的磁盘空间（视频文件通常较大）

## 使用方法

### Windows 用户

```bash
download-videos.bat https://code.visualstudio.com/docs ./vscode-docs
```

### Linux / Mac 用户

```bash
chmod +x download-videos.sh
./download-videos.sh https://code.visualstudio.com/docs ./vscode-docs
```

### 命令行选项

```
java -cp "target/media-downloader-1.0.0.jar" com.media.VSCodeDocumentDownloader <URL> [输出目录] --videos
```

**参数说明:**
- `<URL>` - 必须，VS Code文档URL（如：https://code.visualstudio.com/docs）
- `[输出目录]` - 可选，默认为 `./vscode-docs`
- `--videos` - 可选，表示下载视频而非文档

## 使用示例

### 下载所有VS Code文档视频

```bash
download-videos.bat https://code.visualstudio.com/docs ./my-videos
```

**结果:**
```
vscode-docs/
├── docs/
│   ├── getstarted/
│   │   ├── video1.mp4
│   │   └── video2.mp4
│   ├── editor/
│   │   └── video3.mp4
│   └── introvideos/
│       ├── video4.mp4
│       └── video5.mp4
└── ...
```

### 下载VS Code Copilot相关视频

```bash
download-videos.bat https://code.visualstudio.com/docs/copilot ./copilot-videos
```

## 下载速度优化

### 1. 调整超时时间
编辑 `src/main/java/com/media/VSCodeDocumentDownloader.java`：

```java
private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // 增加连接超时
        .readTimeout(120, TimeUnit.SECONDS)    // 增加读取超时
        .build();
```

### 2. 网络问题处理
- 确保网络连接稳定
- 使用VPN或代理（如有需要）
- 避免在高峰时段下载

## 常见问题

### Q: 下载速度很慢
**A:** 这是正常的，取决于：
- 网络速度
- 服务器网络延迟
- 视频文件大小
- 系统硬盘速度

### Q: 某些视频下载失败怎么办？
**A:** 工具会自动跳过失败的视频并继续下载其他视频。失败的原因可能是：
- 视频链接已失效
- 网络连接中断
- 权限限制

### Q: 如何只下载特定文件夹内的视频？
**A:** 修改起始URL为特定文件夹：
```bash
download-videos.bat https://code.visualstudio.com/docs/introvideos ./intro-videos
```

### Q: 支持下载其他视频格式吗？
**A:** 当前仅支持MP4格式。如需支持其他格式（如WebM、MP3等），请修改代码中的格式检查逻辑。

## 高级用法

### 下载大量视频
如果需要下载大量视频，建议：

1. **增加超时时间**：修改源代码中的超时配置
2. **在后台运行**：
   ```bash
   # Linux/Mac
   nohup ./download-videos.sh https://code.visualstudio.com/docs ./videos > download.log 2>&1 &
   
   # Windows (PowerShell)
   Start-Process -NoNewWindow -FilePath "download-videos.bat" -ArgumentList "https://code.visualstudio.com/docs", "./videos"
   ```

### 监控下载进度
查看日志文件：
```bash
tail -f download.log
```

## 目录结构

下载的视频将按照源页面的路径结构保存：

```
vscode-docs/
├── blogs.txt
├── Download.txt
├── License.txt
├── docs/
│   ├── azure/
│   ├── configure/
│   ├── containers/
│   ├── copilot/           ← Copilot相关视频
│   │   ├── agents/
│   │   ├── chat/
│   │   └── videos/        ← 视频文件目录
│   ├── getstarted/
│   ├── introvideos/       ← 入门视频
│   │   └── video1.mp4
│   │   └── video2.mp4
│   └── ...
└── vscode-docs-zh/        ← 中文版本
    └── ...
```

## 许可证

本工具采用与VS Code documentation相同的许可证。

## 反馈和支持

如遇到问题，请：
1. 检查网络连接
2. 验证Java安装正确
3. 确认URL格式正确
4. 查看错误日志中的详细信息

祝下载愉快！
