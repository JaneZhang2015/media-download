# VS Code 视频下载工具 - 故障排除与使用指南

## 问题诊断

如果你运行下载命令后 **没有任何文件被下载**，请按照以下步骤诊断和解决问题。

### 步骤 1: 验证视频识别功能（使用 DEBUG 模式）

首先，用 `--debug` 标志运行命令，查看工具是否能找到视频：

```bash
# Windows
java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader ^
  "https://code.visualstudio.com/docs" "./test-videos" --videos --debug

# 或使用脚本
download-videos-clean.bat "https://code.visualstudio.com/docs" "./test-videos" --debug
```

**预期输出示例：**
```
VS Code 视频下载 v1.0
===================

开始爬取视频: https://code.visualstudio.com/docs
  [DEBUG 模式已启用]
✓ 找到 297 个子文档章节，开始扫描视频...

[2/297] updates - 找到 11 个视频
      - https://code.visualstudio.com/assets/updates/1_109/inline-chat.mp4
      - https://code.visualstudio.com/assets/updates/1_109/embedded-terminal-streaming.mp4
      ...
[19/298] userinterface - 找到 3 个视频
      - https://code.visualstudio.com/assets/docs/getstarted/userinterface/view-management.mp4
      ...
```

### 步骤 2: 检查视频是否被找到

如果 DEBUG 模式输出显示了 ✓ 找到的视频，那证明识别功能正常。继续到步骤 3。

如果没有找到任何视频（显示"⚠ 没有找到视频"），请检查：

1. **网络连接**：确保你能访问 https://code.visualstudio.com/docs
2. **URL 正确性**：URL 必须以 `/docs` 开头
3. **页面加载**：某些视频可能是通过 JavaScript 动态加载的，静态爬虫可能无法获取

### 步骤 3: 移除 --debug 标志，执行真正的下载

验证找到视频后，运行不带 `--debug` 的命令来真正下载文件：

```bash
java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader ^
  "https://code.visualstudio.com/docs" "./vscode-videos" --videos
```

**预期输出示例：**
```
VS Code 视频下载 v1.0
===================

开始爬取视频: https://code.visualstudio.com/docs
✓ 找到 297 个子文档章节，开始扫描视频...
✓ 总共找到 70+ 个MP4视频

[1/70] inline-chat.mp4
  URL: https://code.visualstudio.com/assets/updates/1_109/inline-chat.mp4
  进度: 100% 
  ✓ 已保存: vscode-videos\updates\inline-chat.mp4 (5.23 MB)

[2/70] embedded-terminal-streaming.mp4
  进度: 100%
  ✓ 已保存: vscode-videos\updates\embedded-terminal-streaming.mp4 (3.15 MB)
...
```

### 步骤 4: 检查下载的文件

下载完成后，查看输出目录：

```bash
# Windows
dir /s ".\vscode-videos"

# 或在 PowerShell 中
Get-ChildItem -Recurse ".\vscode-videos"
```

应该看到类似的目录结构：
```
vscode-videos
├── updates/
│   ├── inline-chat.mp4
│   ├── embedded-terminal-streaming.mp4
│   └── ...
├── docs/
│   ├── getstarted/
│   │   └── userinterface/
│   │       ├── view-management.mp4
│   │       └── ...
│   └── editing/
│       └── intellisense/
│           ├── intellisense.mp4
│           └── ...
```

## 常见问题排查

### 问题：DEBUG 模式显示没有找到任何视频

**原因可能：**
1. 网络问题 - 无法访问 code.visualstudio.com
2. 页面格式更改 - VS Code 官方网站可能更新了 HTML 结构
3. 动态内容 - 视频可能通过 JavaScript 加载，静态爬虫无法识别

**解决方案：**
- 检查网络连接：`ping code.visualstudio.com`
- 在浏览器中手动访问 https://code.visualstudio.com/docs 确认页面可访问
- 如果是动态内容问题，可能需要使用浏览器自动化工具（Selenium 等）

### 问题：找到了视频但下载失败

**常见错误信息：**
```
✗ 下载失败: HTTP 404
✗ 下载失败: Connection timeout
✗ 下载失败: 无法访问 URL
```

**原因可能：**
1. 视频 URL 已失效或被移除
2. 网络连接中断
3. VS Code 官网临时无法访问

**解决方案：**
- 等待几分钟后重试
- 检查网络连接
- 某些视频可能确实不可用，工具会跳过失败的文件继续下载其他文件

### 问题：下载的文件损坏或无法播放

**原因可能：**
1. 网络中断，文件未完全下载
2. 磁盘空间不足

**解决方案：**
- 确保有足够的磁盘空间（至少 500 MB）
- 使用 `--debug` 模式重新下载失败的视频
- 检查文件大小是否合理（通常几 MB 到 50 MB）

## 详细的 HTML 元素支持说明

工具支持以下形式的视频元素：

### 1. 标准 video 标签（带 src 属性）
```html
<video src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4" controls></video>
```
✓ **支持** - 通过正则表达式和 DOM 选择器都能识别

### 2. Video 标签 + source 子元素
```html
<video controls>
  <source src="/assets/docs/editing/intellisense/intellisense.mp4" type="video/mp4">
</video>
```
✓ **支持** - 通过 CSS 选择器 `video source[src]` 识别

### 3. Iframe 嵌入
```html
<iframe src="https://code.visualstudio.com/assets/docs/copilot/sample.mp4"></iframe>
```
✓ **支持** - 通过 iframe 选择器识别

### 4. JSON 数据中的视频链接
```javascript
<script>
const videos = {
  "urls": ["https://code.visualstudio.com/assets/updates/1_109/inline-chat.mp4"]
}
</script>
```
✓ **支持** - 通过正则表达式从脚本标签中提取

### 5. 属性中的 /assets/ 路径
```html
<div data-video="/assets/docs/copilot/intro.mp4"></div>
```
✓ **支持** - 通过正则表达式识别

## 性能和下载限制

### 下载速度
- 取决于你的网络速度和 VS Code 官网的服务器速度
- 典型下载速度：1-5 MB/秒
- 70+ 个视频总计约 2-5 GB，完全下载需要 1-2 小时

### 并发限制
- 当前每次只下载一个文件
- 如需加快速度，可以手动修改代码中的并发数量（高级用户）

### 磁盘空间需求
- 所有 VS Code docs 视频：2-5 GB
- 确保输出目录所在硬盘有足够空间

## 获得帮助

如果问题仍未解决：

1. 运行 `mvn clean package` 重新编译
2. 检查 Java 版本：`java -version`（需要 Java 11 或更高）
3. 查看详细日志：检查输出中的错误信息
4. 手动测试网络：`curl https://code.visualstudio.com/docs`

## 相关文件和命令

- **编译项目**：`mvn clean package`
- **下载脚本**：`download-videos-clean.bat` (Windows) 或 `download-videos.sh` (Linux/Mac)
- **完整的主类**：`com.media.VSCodeDocumentDownloader`
- **支援的参数**：
  - `--videos` 表示下载视频（不加则下载文档）
  - `--debug` 显示找到的所有视频链接但不下载

## 更新日志

### v1.0（当前版本）
- 支持 /assets/ 路径的 MP4 识别
- 支持多种 HTML 元素格式（video, source, iframe）
- 支持从 JSON 中提取视频链接
- 支持 DEBUG 模式查看识别的视频
- 自动创建对应的目录结构
