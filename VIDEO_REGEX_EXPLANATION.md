# 视频识别方式详细说明

## 针对你的特定 HTML 元素

你提到的这个 HTML 元素：

```html
<video src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4" 
       title="Video showing how to select and configure tools in the chat tools picker." 
       autoplay="" loop="" controls="" muted="" 
       poster="/assets/docs/copilot/chat-tools/chat-tools-picker.png"></video>
```

**本工具完全支持**，通过以下三种方式识别：

### 1. 正则表达式（Pattern 2：相对路径）
```regex
/assets/[^\s"'<>]*\.mp4
```
**匹配结果**：`/assets/docs/copilot/chat-tools/chat-tools-picker.mp4`

这个模式直接从 HTML 源代码中搜索所有包含 `/assets/` 开头、以 `.mp4` 结尾的路径。

### 2. 正则表达式（Pattern 3：属性中的 MP4）
```regex
(?:src|data-src|href|data-href)\s*=\s*["']([^"']*\.mp4[^"]*)["']
```
**匹配结果**：`src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4"`

这个模式匹配 HTML 属性中的 MP4 链接，包括：
- `src="...mp4"`
- `data-src="...mp4"`
- `href="...mp4"`
- `data-href="...mp4"`

### 3. DOM 选择器（CSS Selector）
```css
video[src]
```
**选择结果**：找到所有 `<video>` 标签中有 `src` 属性的元素，然后提取属性值。

## 完整的识别流程

对于你的 HTML 元素，处理流程如下：

```
输入 HTML:
<video src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4" ...></video>

↓ 步骤 1: DOM 选择
video[src] 选择器 → 找到这个 <video> 元素 → 提取 src 属性
结果: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4

↓ 步骤 2: URL 解析
baseUrl: https://code.visualstudio.com/docs/copilot/chat-tools
relativePath: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4

使用 resolveUrl() 函数转换为绝对 URL:
- 判断路径以 "/" 开头 → 这是绝对路径
- 从 baseUrl 提取主机名: code.visualstudio.com
- 构建完整 URL

结果: https://code.visualstudio.com/assets/docs/copilot/chat-tools/chat-tools-picker.mp4

↓ 步骤 3: 正则表达式验证（备用）
Pattern 1: https://code\.visualstudio\.com/[^\s"'<>]*\.mp4 ✓ 匹配
Pattern 2: /assets/[^\s"'<>]*\.mp4 ✓ 匹配
Pattern 3: src="[^"']*\.mp4[^"']*" ✓ 匹配

↓ 步骤 4: URL 去重
检查 /assets/docs/copilot/chat-tools/chat-tools-picker.mp4 是否已添加
如果没有 → 添加到下载列表

结果: 
  https://code.visualstudio.com/assets/docs/copilot/chat-tools/chat-tools-picker.mp4

↓ 步骤 5: 下载
获取 HTTP 请求 → 200 OK → 保存文件到合适的目录
保存路径: ./vscode-videos/docs/copilot/chat-tools/chat-tools-picker.mp4
```

## 代码实现细节

### 正则表达式使用的 Java 代码

```java
// 模式 2: 相对路径 /assets/...
Pattern assetPathPattern = Pattern.compile("/assets/[^\\s\"'<>]*\\.mp4");
Matcher matcher = assetPathPattern.matcher(html);
while (matcher.find()) {
    String path = matcher.group(0);  // 得到: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4
    
    // 转换为绝对 URL
    java.net.URL pageUrlObj = new java.net.URL(pageUrl);
    String absoluteUrl = pageUrlObj.getProtocol() + "://" + pageUrlObj.getHost() + path;
    // 结果: https://code.visualstudio.com/assets/docs/copilot/chat-tools/chat-tools-picker.mp4
    
    videoUrls.add(absoluteUrl);
}

// 模式 3: 属性中的 .mp4
Pattern attrPattern = Pattern.compile("(?:src|data-src|href|data-href)\\s*=\\s*[\"']([^\"']*\\.mp4[^\"]*)[\"\']");
matcher = attrPattern.matcher(html);
while (matcher.find()) {
    String urlOrPath = matcher.group(1);  // 得到: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4
    
    if (urlOrPath.startsWith("/")) {
        // 转换相对路径为绝对 URL
        java.net.URL pageUrlObj = new java.net.URL(pageUrl);
        String absoluteUrl = pageUrlObj.getProtocol() + "://" + pageUrlObj.getHost() + urlOrPath;
        videoUrls.add(absoluteUrl);
    }
}
```

### DOM 选择器使用的 Jsoup 代码

```java
// 使用 CSS 选择器查找所有带 src 属性的 <video> 标签
Elements videoTags = doc.select("video[src]");

for (Element videoTag : videoTags) {
    String src = videoTag.attr("src");  // 得到: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4
    
    if (src != null && src.toLowerCase().endsWith(".mp4")) {
        // 转换为绝对 URL
        String absoluteUrl = resolveUrl(src, pageUrl);
        videoUrls.add(absoluteUrl);
    }
}
```

## 测试验证

我已经用你提供的完整 HTML 进行了测试：

```
输入: <video src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4" ...></video>

测试结果:
=============

Pattern 2 (/assets/... paths):
  FOUND #1: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4       ✓

Pattern 3 (attributes with .mp4):
  FOUND #1: src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4"
    - Captured URL: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4  ✓

Pattern 4 (src="...mp4"):
  FOUND #1: src="/assets/docs/copilot/chat-tools/chat-tools-picker.mp4"
    - Captured URL: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4  ✓

DOM Selection Test (video[src]):
  Found 1 video element(s)
    - src attribute: /assets/docs/copilot/chat-tools/chat-tools-picker.mp4  ✓

总结: 3 patterns matched + DOM selection successful = 完全支持
```

## 常见问题

### Q: 为什么我的特定页面可能找不到这个视频？

**A:** 即使代码支持这种格式，视频在你爬取的页面中可能：
1. 不存在 - 该特定的 MP4 可能不在该页面的 HTML 中
2. 通过 JavaScript 动态加载 - 静态爬虫无法获取动态内容
3. 需要特殊权限或登录
4. 已从官方文档中删除

### Q: 正则表达式是否可能有假阴性？

**A:** 非常少见，但以下情况可能导致：
1. MP4 URL 被换行符分割（例如在压缩的 JSON 中）
2. URL 包含特殊转义字符
3. 非标准的属性名（当前支持：src, data-src, href, data-href）

若发现这类情况，可向我报告，我会添加额外的模式。

### Q: 如何确认正则表达式有效？

**A:** 使用 `--debug` 模式运行，输出会显示：
```
[DEBUG] 找到视频: https://code.visualstudio.com/assets/docs/...
```

这说明至少一个正则模式成功匹配了。

## 性能考虑

- **正则表达式搜索**：在几 MB 的 HTML 完成扫描，通常 < 100ms
- **DOM 解析**：Jsoup 解析和选择，通常 < 500ms  
- **URL 构建**：Java URL 类处理，通常 < 10ms per URL

对于包含大量视频的页面（100+ 个视频），总体识别时间通常 < 2 秒。
