# 🆕 动态加载媒体 - 完整指南

你遇到的"未找到媒体文件"问题通常是因为媒体是**通过JavaScript动态加载**的，而静态HTML解析无法获取。

现在已为你更新了工具，支持**动态媒体加载**！

---

## 🎯 问题分析

### 为什么找不到媒体？

网页可能使用以下方式加载媒体：

```javascript
// 1. 动态创建
const audio = new Audio('http://cdn.example.com/song.mp3');

// 2. 从API获取
fetch('/api/media').then(r => r.json()).then(data => {
    player.src = data.url;
});

// 3. 通过JavaScript注入
document.querySelector('audio').src = 'http://example.com/media.mp3';

// 4. MediaPlayer对象
window.media = {
    url: 'http://example.com/song.mp3',
    title: 'Song Title'
};
```

这些都**无法通过简单的HTML解析获取**，需要使用浏览器自动化工具。

---

## ✨ 新版本的优势

### MediaDownloaderPro v2.0 - 三层提取机制

```
┌─────────────────────────────────────────┐
│   获取网页 (使用Selenium浏览器引擎)      │
└──────────────┬──────────────────────────┘
               │
         等待JavaScript执行 (5秒)
               │
     ┌─────────┴──────────┬─────────────┐
     ▼                    ▼             ▼
  页面源代码         JavaScript        HTML元素
  正则匹配          动态执行           DOM查询
  ├─ <audio>       ├─ audio.src      ├─ <audio>
  ├─ <video>       ├─ video.src      ├─ <video>
  ├─ <source>      ├─ <source>       ├─ <source>
  └─ JSON中的URL   └─ window.media   └─ 链接href
```

---

## 🚀 使用方法

### 1️⃣ 编译新版本（使用Maven）

```bash
cd e:\code\media-download

# 编译
mvn clean package

# 生成的JAR
target/media-downloader-2.0.0.jar
```

### 2️⃣ 运行程序

```bash
# 基本使用（自动下载ChromeDriver）
java -jar target/media-downloader-2.0.0.jar "https://example.com/page"

# 指定输出目录
java -jar target/media-downloader-2.0.0.jar "https://example.com/page" "D:\Downloads"

# 使用静态模式（旧版本）
java -jar target/media-downloader-2.0.0.jar "https://example.com/page" "./output" "static"
```

---

## 📋 工作流程详解

### 动态模式工作原理

```
1. 初始化Selenium WebDriver
   ↓
2. 打开Chrome浏览器（自动）
   ↓
3. 访问网页URL
   ↓
4. 等待5秒（JavaScript执行）
   ↓
5. 方法1: 获取页面源代码
   ├─ 正则表达式提取媒体URL
   └─ 从JSON中提取URL
   ↓
6. 方法2: 执行JavaScript脚本
   ├─ 获取 document.querySelectorAll('audio')
   ├─ 获取 document.querySelectorAll('video')
   ├─ 获取 document.querySelectorAll('source')
   └─ 获取 window.media 或全局变量
   ↓
7. 方法3: DOM元素查询
   ├─ 直接查找<audio>标签
   ├─ 直接查找<video>标签
   └─ 直接查找<source>标签
   ↓
8. 合并所有找到的URL（去重）
   ↓
9. 下载所有媒体文件
   ↓
10. 保存到输出目录
```

---

## 🔍 调试技巧

### 如何在浏览器开发者工具中找媒体对象

```javascript
// 按 F12 打开开发者工具，进入 Console 标签

// 1. 查看HTML中的媒体标签
document.querySelectorAll('audio')      // 所有<audio>
document.querySelectorAll('video')      // 所有<video>
document.querySelectorAll('source')     // 所有<source>

// 2. 查看媒体URL
document.querySelector('audio').src     // 获取src
document.querySelector('audio').currentSrc  // 当前播放源

// 3. 查看全局变量
window.media        // 媒体对象
window.player       // 播放器对象
window.config       // 配置对象

// 4. 在Network标签中查看请求
// 查看所有 XHR/Fetch 请求，找到返回媒体URL的API
// 如: /api/media, /api/playlist, /stream.m3u8 等
```

---

## 📊 程序输出示例

```
初始化Selenium WebDriver...
打开网页: https://example.com/music

[方法1] 从页面源代码提取媒体URL
[方法2] 执行JavaScript获取媒体信息
[方法3] 查找HTML媒体标签

✓ 找到 5 个媒体文件

[1/5] 下载: https://cdn.example.com/song1.mp3
  进度: 10% 20% 30% ... 100% 完成
  ✓ 已保存: downloads/song1.mp3

[2/5] 下载: https://cdn.example.com/song2.m4a
  ...

✓ 下载完成！
```

---

## ⚙️ 实现细节

### 三层提取机制的优势

| 方法 | 优点 | 缺点 |
|------|------|------|
| **页面源代码** | 快速、使用正则表达式 | 只能获取静态 URL |
| **JavaScript执行** | 可以获取动态生成的 URL | 需要浏览器引擎 |
| **DOM查询** | 直接访问已加载的元素 | 依赖页面结构 |

**结合使用** = 最大覆盖率! ✅

---

## 🎵 支持的场景

### ✅ 可以获取的媒体

- HTML `<audio>` 和 `<video>` 标签中的媒体
- JavaScript 动态创建的媒体元素
- API 返回的媒体 URL
- JSON 数据中的媒体链接
- 通过 fetch/AJAX 加载的媒体
- m3u8 播放列表
- 流媒体 URL

### ❌ 无法获取的媒体

- 受 CORS 限制的跨域资源（可能需要特殊设置）
- 需要登录认证的媒体（需要传入 Cookie）
- 完全加密/DRM 保护的内容
- 很长时间后才加载的内容（默认等待5秒）

---

## 🔧 高级配置

### 修改等待时间

编辑源代码，找到：

```java
// 等待页面加载5秒
Thread.sleep(5000);
```

改为：
```java
// 等待10秒（对于加载较慢的网站）
Thread.sleep(10000);
```

### 添加Cookie（登录认证）

修改 `MediaDownloaderPro.java`，在 `driver.get(url)` 后添加：

```java
driver.get(url);

// 添加Cookie
Cookie cookie = new Cookie("session_id", "your_session_id");
driver.manage().addCookie(cookie);

// 刷新页面
driver.navigate().refresh();
```

### 无头模式（后台运行，不显示浏览器）

取消注释此行：

```java
options.addArguments("--headless");
```

---

## 📝 完整示例

### 场景：下载Spotify播放列表

```bash
java -jar target/media-downloader-2.0.0.jar "https://spotify.com/playlist/xyz"
```

程序会：
1. 打开Spotify播放列表页面
2. 等待JavaScript加载歌曲列表
3. 执行JavaScript获取所有歌曲的媒体URL
4. 下载所有歌曲

### 场景：下载YouTube视频

```bash
java -jar target/media-downloader-2.0.0.jar "https://www.youtube.com/watch?v=xyz"
```

注意：YouTube的媒体经过加密，可能需要特殊处理。

---

## 🐛 常见问题

### Q: 报错 "找不到ChromeDriver"？
**A:** 程序会自动下载，但需要：
- 网络连接
- 足够的磁盘空间（~150MB）
- 管理员权限

### Q: 找到媒体但下载失败？
**A:** 可能原因：
1. 媒体URL需要特殊Header（Referer、Authorization等）
2. 媒体是流媒体（.m3u8 等）
3. 媒体受地理限制

### Q: 程序很慢？
**A:** 解决方案：
1. 启用无头模式（不显示浏览器）
2. 减少等待时间
3. 使用静态模式（如果适用）

### Q: 怎样获取需要登录的媒体？
**A:** 选项：
1. 获取登录后的Cookie，添加到代码中
2. 使用 Selenium 自动登录（修改代码）
3. 直接从 Network 标签获取媒体URL

---

## 🚀 下一步

### 使用Maven编译最新版本

```bash
cd e:\code\media-download
mvn clean package -DskipTests
```

### 运行新版本

```bash
java -jar target/media-downloader-2.0.0.jar "你的网址"
```

### 监控浏览器窗口

程序会打开实际的Chrome浏览器窗口，你可以看到它访问网站的全过程。

---

## 📚 文档对照

| 需要 | 查看 |
|------|------|
| 快速开始 | 本文档 |
| 静态模式 | README.md |
| 完整API | 源代码注释 |
| 部署指南 | DEPLOYMENT.md |

---

## ✨ 性能提示

- 首次运行会下载 ChromeDriver (~150MB)
- 之后会使用缓存的驱动，速度会更快
- 每个网页处理时间：1-10秒（取决于页面大小）
- 下载速度：取决于网络和服务器

---

## 🎓 深入学习

如果你想自定义功能，查看以下部分：

1. **JavaScript执行**: `extractMediaViaJavaScript()` 方法
2. **HTML解析**: `extractMediaUrls()` 方法
3. **元素查询**: `extractFromHtmlElements()` 方法
4. **URL处理**: `resolveUrl()` 方法

---

**现在试试新版本吧！** 🚀

```bash
mvn clean package
java -jar target/media-downloader-2.0.0.jar "https://你的网址"
```
