# VS Code 视频下载 - 使用示例

## 快速开始

### 1. 编译项目
```bash
mvn clean package
```

### 2. 运行视频下载工具

#### Windows 用户
```bash
download-videos.bat https://code.visualstudio.com/docs ./vscode-docs
```

#### Linux / Mac 用户
```bash
chmod +x download-videos.sh
./download-videos.sh https://code.visualstudio.com/docs ./vscode-docs
```

#### 直接使用 Java 命令
```bash
java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader \
  https://code.visualstudio.com/docs ./vscode-docs --videos
```

## 下载示例

### 示例 1: 下载所有 VS Code 文档中的视频
```bash
download-videos.bat https://code.visualstudio.com/docs ./all-videos
```

**预期输出：**
```
╔══════════════════════════════════════════╗
║      VS Code 视频下载 v1.0               ║
╚══════════════════════════════════════════╝

开始爬取视频: https://code.visualstudio.com/docs
✓ 找到 156 个文档章节，开始扫描视频...

[1/156] https://code.visualstudio.com/docs/getstarted/getting-started - 找到 3 个视频
[2/156] https://code.visualstudio.com/docs/introvideos - 找到 5 个视频
...
[156/156] https://code.visualstudio.com/docs/copilot - 找到 2 个视频

✓ 总共找到 47 个MP4视频

[1/47] 下载视频...
  URL: https://code.visualstudio.com/media/videos/intro-video.mp4
  进度: 100%
  ✓ 已保存: docs/getstarted/videos/intro-video.mp4 (125.50 MB)

[2/47] 下载视频...
  ...

✓ 视频下载完成!
  保存路径: E:\code\media-download\all-videos
```

### 示例 2: 只下载 Copilot 相关视频
```bash
download-videos.bat https://code.visualstudio.com/docs/copilot ./copilot-videos
```

结果目录结构：
```
copilot-videos/
├── agents/
│   └── video-name.mp4
├── chat/
│   └── video-name.mp4
├── customization/
│   └── video-name.mp4
└── guides/
    └── video-name.mp4
```

### 示例 3: 下载入门指南视频
```bash
download-videos.bat https://code.visualstudio.com/docs/introvideos ./intro-videos
```

结果目录结构：
```
intro-videos/
├── video-getting-started.mp4
├── video-extension-basics.mp4
├── video-keybindings.mp4
├── video-debugging.mp4
└── video-source-control.mp4
```

## 文件识别能力

工具能够识别以下类型的视频链接：

### 1. HTML5 视频标签
```html
<video src="/media/videos/demo.mp4"></video>
<video>
  <source src="/media/videos/demo.mp4">
</video>
```

### 2. iframe 嵌入
```html
<iframe src="https://example.com/video.mp4"></iframe>
```

### 3. 超链接
```html
<a href="/media/videos/download.mp4">下载视频</a>
```

### 4. 图片属性
```html
<img data-video="/media/videos/thumb.mp4">
```

### 5. JSON 数据
```json
{
  "video": "https://example.com/video.mp4",
  "mediaUrl": "/media/videos/demo.mp4"
}
```

## 目录结构示例

下载后的目录结构示例：

```
vscode-docs/
├── blogs.txt
├── Download.txt
├── License.txt
├── docs/
│   ├── azure/
│   │   └── media/
│   │       ├── container-tutorial.mp4
│   │       └── kubernetes-intro.mp4
│   ├── configure/
│   │   └── media/
│   │       ├── settings-guide.mp4
│   │       └── keybindings-tutorial.mp4
│   ├── containers/
│   │   └── media/
│   │       ├── docker-basics.mp4
│   │       ├── debug-containers.mp4
│   │       └── docker-compose-guide.mp4
│   ├── copilot/
│   │   ├── agents/
│   │   │   └── media/
│   │   │       ├── agent-intro.mp4
│   │   │       └── agent-advanced.mp4
│   │   ├── chat/
│   │   │   └── media/
│   │   │       └── chat-tutorial.mp4
│   │   └── guides/
│   │       └── media/
│   │           └── copilot-tips.mp4
│   ├── getting-started/
│   │   └── media/
│   │       ├── intro.mp4
│   │       ├── extensions.mp4
│   │       └── setup.mp4
│   ├── introvideos/
│   │   └── media/
│   │       ├── video-getting-started.mp4
│   │       ├── video-extension-basics.mp4
│   │       ├── video-keybindings.mp4
│   │       ├── video-debugging.mp4
│   │       └── video-source-control.mp4
│   └── ...
└── vscode-docs-zh/
    └── ...
```

## 配置选项

### 修改超时时间

编辑 `src/main/java/com/media/VSCodeDocumentDownloader.java`：

```java
private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)    // 连接超时（秒）
        .readTimeout(60, TimeUnit.SECONDS)       // 读取超时（秒）
        .writeTimeout(60, TimeUnit.SECONDS)      // 写入超时（秒）
        .build();
```

然后重新编译：
```bash
mvn clean package
```

## 故障排除

### 问题 1: "无法获取主页面内容"
**可能原因:**
- 网络连接失败
- URL 不正确
- 服务器不可达

**解决方案:**
- 检查网络连接：`ping code.visualstudio.com`
- 验证 URL 是否正确
- 尝试使用 VPN

### 问题 2: "JAR文件不存在"
**可能原因:**
- 没有编译项目

**解决方案:**
```bash
mvn clean package
```

### 问题 3: "Java 未找到"
**可能原因:**
- 没有安装 Java
- Java 版本过低（< 11）

**解决方案:**
```bash
# 检查 Java 版本
java -version

# 如果版本 < 11，请升级到 Java 11 或更高版本
```

### 问题 4: 下载速度慢
**可能原因:**
- 网络速度慢
- 服务器响应慢
- 磁盘 I/O 繁忙

**解决方案:**
- 使用更快的网络
- 关闭其他网络应用
- 使用 SSD 磁盘

## 高级用法

### 后台下载

**Windows (PowerShell):**
```powershell
Start-Process -NoNewWindow -FilePath "download-videos.bat" `
  -ArgumentList "https://code.visualstudio.com/docs", "./videos"
```

**Linux/Mac:**
```bash
nohup ./download-videos.sh https://code.visualstudio.com/docs ./videos > download.log 2>&1 &
```

### 监控下载进度

**实时查看日志:**
```bash
# Linux/Mac
tail -f download.log

# Windows PowerShell
Get-Content download.log -Wait
```

### 批量下载多个来源

创建批处理脚本 `batch-download.bat`：
```batch
@echo off
REM 下载多个来源的视频

echo 下载 Copilot 视频...
call download-videos.bat https://code.visualstudio.com/docs/copilot ./copilot-videos

echo 下载入门视频...
call download-videos.bat https://code.visualstudio.com/docs/introvideos ./intro-videos

echo 下载完成!
```

运行：
```bash
batch-download.bat
```

## 统计信息

通常情况下，VS Code 文档包含以下视频：

| 类别 | 数量 | 总大小 |
|------|------|--------|
| 入门视频 | 5 | ~500 MB |
| Copilot 教程 | 8 | ~800 MB |
| 扩展开发 | 12 | ~1.2 GB |
| 调试指南 | 3 | ~300 MB |
| 其他视频 | 20+ | ~2+ GB |
| **总计** | **47+** | **~4+ GB** |

## 许可证

本工具采用与 VS Code 文档相同的许可证。所有下载的内容遵守原始许可证协议。

---

**最后更新**: 2026-02-22  
**版本**: 1.0.0
