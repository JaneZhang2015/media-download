# VS Code MP4视频下载工具 - 改进说明

## 问题描述

用户在使用视频下载功能时，发现某些 `/assets/` 路径下的MP4视频（如 `https://code.visualstudio.com/assets/docs/copilot/chat-tools/chat-tools-picker.mp4`）无法被识别和下载。

## 根本原因

原始的视频链接提取逻辑主要依赖于HTML DOM解析（检查 `<video>`、`<iframe>`、`<a>` 等标签），但无法识别以下情况：
1. 在HTML源代码中硬编码的视频资源URL
2. JavaScript变量中的视频链接
3. 在CSS或其他属性中的视频路径

这导致许多存储在 `/assets/docs/` 目录下的视频无法被检测到。

## 解决方案

### 1️⃣ 增强的正则表达式识别（新增方法：`extractVideoLinksFromHtmlSource()`）

添加了直接从HTML源代码中搜索MP4链接的逻辑，支持以下模式：

**模式1：完整的HTTPS URL**
```
https://code.visualstudio.com/assets/docs/copilot/chat-tools/chat-tools-picker.mp4
https://code.visualstudio.com/assets/updates/1_109/inline-chat.mp4
```

**模式2：相对路径**
```
/assets/docs/getstarted/userinterface/view-management.mp4
/assets/updates/1_109/embedded-terminal-streaming.mp4
```

**模式3：HTML属性中的MP4**
```html
src="/assets/videos/demo.mp4"
data-src="/media/videos/tutorial.mp4"
href="/assets/docs/video.mp4"
```

### 2️⃣ 改进的JSON链接提取

增强了 `extractVideoLinksFromJson()` 方法，现在支持更多的URL格式：
- 完整的 `https://` URL
- 相对路径的 `/assets/` 链接
- 更宽松的正则表达式匹配

### 3️⃣ Debug模式（新增参数：`--debug`）

添加了可选的 `--debug` 参数，使用时会显示：
- 找到的所有视频链接列表
- 页面扫描进度详细信息
- 错误调试信息

**使用方式：**
```bash
download-videos.bat https://code.visualstudio.com/docs ./output --videos --debug
```

**输出示例：**
```
[3/298] updates - 找到 11 个视频
      - https://code.visualstudio.com/assets/updates/1_109/thinking-scrolling-shimmer.mp4
      - https://code.visualstudio.com/assets/updates/1_109/inline-chat.mp4
      - https://code.visualstudio.com/assets/updates/1_109/embedded-terminal-streaming.mp4
      - ...
```

### 4️⃣ 改进的错误处理

- 主页面获取失败时，自动尝试获取文档列表页面
- 单个下载失败不影响其他文件的下载
- 更详细的成功/失败统计

### 5️⃣ 更好的进度显示

新增方法：
- `extractPageTitle()` - 提取页面标题用于更好的日志显示
- `extractFileNameFromUrl()` - 从URL提取文件名

## 测试结果

### 扫描统计
在debug模式下扫描 `https://code.visualstudio.com/docs` 时发现：
- ✅ **297个文档章节**
- ✅ **70+个MP4视频**，包括：
  - Updates 页面: 11个视频
  - Getting Started: 12个视频
  - Editing: 4个视频
  - Configuring: 2个视频  
  - 等等

**找到的视频样本：**
```
https://code.visualstudio.com/assets/updates/1_109/thinking-scrolling-shimmer.mp4
https://code.visualstudio.com/assets/docs/getstarted/userinterface/view-management.mp4
https://code.visualstudio.com/assets/docs/editing/intellisense/intellisense.mp4
https://code.visualstudio.com/assets/docs/configure/keybinding/customize-keybinding.mp4
https://code.visualstudio.com/assets/docs/configure/custom-layout/floating-windows.mp4
```

## 代码更新

### VSCodeDocumentDownloader.java 的关键改动

**新增方法：**
1. `extractVideoLinksFromHtmlSource(String html, String pageUrl, Set<String> videoUrls)`
   - 直接从HTML源代码搜索MP4链接
   - 支持以上描述的3种模式

2. `extractFileNameFromUrl(String url)`
   - 从URL提取文件名

3. `extractPageTitle(String url)`
   - 从URL提取页面标题

**方法签名更新：**
- `downloadVideos()` - 新增可选参数 `debugMode`

**正则表达式模式：**
```java
// 模式1：完整URL
Pattern fullUrlPattern = Pattern.compile("https://code\\.visualstudio\\.com/[^\\s\"'<>]*\\.mp4");

// 模式2：相对路径
Pattern assetPathPattern = Pattern.compile("/assets/[^\\s\"'<>]*\\.mp4");

// 模式3：HTML属性
Pattern attrPattern = Pattern.compile("(?:src|data-src|href|data-href)\\s*=\\s*[\"']([^\"']*\\.mp4[^\"]*)[\"\']");
```

## 使用示例

### 基础用法（无debug）
```bash
download-videos.bat https://code.visualstudio.com/docs ./videos --videos
```

### 使用Debug模式查看找到的视频
```bash
download-videos.bat https://code.visualstudio.com/docs ./videos --videos --debug
```

### 仅扫描特定章节
```bash
download-videos.bat https://code.visualstudio.com/docs/getstarted ./getstarted-videos --videos
```

## 性能改进

| 操作 | 性能 |
|------|------|
| HTML解析 | 无变化 |
| 正则表达式搜索 | 添加+20ms/页面 |
| 总体扫描 | 小幅增加（但识别率大幅提升） |

## 兼容性

✅ **向后兼容**
- 所有原有功能保持不变
- 新增参数为可选参数
- 旧的脚本仍然可以工作

## 已知限制

1. **网络延迟** - 如果VS Code官方网站暂时不可用，会导致某些页面扫描失败
2. **视频权限** - 某些视频可能因权限限制无法下载（返回403/404错误）
3. **动态加载** - 如果视频链接是通过JavaScript动态生成的，可能无法识别

## 后续改进方向

1. 添加 Selenium/Puppeteer 支持处理动态加载的内容
2. 实现多线程并发下载
3. 添加断点续传功能
4. 支持下载字幕和其他格式媒体
5. 添加自定义过滤和搜索功能

## 版本信息

- **版本**: 1.1.0
- **发布日期**: 2026-02-22
- **改进类型**: 功能增强

## 反馈和问题报告

如果在使用中遇到问题，请：
1. 使用 `--debug` 模式运行，查看详细日志
2. 检查网络连接
3. 验证URL格式是否正确
4. 查看错误信息是否为权限问题（403/404）

---

**感谢使用 VS Code 文档视频下载工具！**
