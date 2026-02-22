# 🆕 升级指南 - 从快速版到Pro版

你遇到了"**未找到媒体文件**"的问题？这是因为媒体是**JavaScript动态加载**的！

以下是完整的升级和使用指南。

---

## ⚡ 快速修复（3步）

### Step 1️⃣: 确保已安装Maven

```bash
mvn -version
```

如果没有显示版本号，[点击安装Maven](https://maven.apache.org/download.cgi)

### Step 2️⃣: 编译Pro版本

```bash
cd e:\code\media-download
mvn clean package
```

等待编译完成（第一次可能需要下载依赖，耗时较长）

### Step 3️⃣: 运行新版本

```bash
java -jar target/media-downloader-2.0.0.jar "https://你的网址"
```

**完成！** 新版本会自动打开浏览器并获取动态加载的媒体。

---

## 📊 版本对比

| 特性 | 快速版 v1.0 | Pro版 v2.0 |
|------|-----------|----------|
| **启动速度** | ⚡ 快 | 🔄 中等 |
| **文件大小** | 5 KB | ~100 MB |
| **编译需求** | 无 | 需要Maven |
| **浏览器** | 无 | Chrome (自动) |
| **静态HTML** | ✅ 完全支持 | ✅ 完全支持 |
| **动态JavaScript** | ❌ 不支持 | ✅ **完全支持** |
| **API加载** | ❌ 不支持 | ✅ **完全支持** |

**你需要Pro版本的标志:**
- 网页加载时媒体URL集中显示（Network标签中的XHR请求）
- 右键查看网页源代码找不到媒体URL
- 使用了React、Vue、Angular等前端框架
- 媒体通过JavaScript在window对象中定义

---

## 🔧 安装Maven（如果需要）

### Windows

1. 下载: https://maven.apache.org/download.cgi -> apache-maven-3.9.x
2. 解压到合适位置，如 `C:\apache-maven-3.9.0`
3. 添加到系统环境变量:
   - 右键 "此电脑" -> "属性"
   - "高级系统设置" -> "环境变量"
   - 新建 `MAVEN_HOME = C:\apache-maven-3.9.0`
   - 编辑 `Path`，添加 `%MAVEN_HOME%\bin`
4. 开启新的命令行窗口，验证:
   ```bash
   mvn -version
   ```

### macOS

```bash
# 使用Homebrew（推荐）
brew install maven

# 或手动安装
curl -O https://archive.apache.org/dist/maven/maven-3/3.9.0/binaries/apache-maven-3.9.0-bin.tar.gz
tar xzf apache-maven-3.9.0-bin.tar.gz
sudo mv apache-maven-3.9.0 /usr/local/

# 添加到 ~/.bash_profile 或 ~/.zshrc
export PATH="/usr/local/apache-maven-3.9.0/bin:$PATH"
```

### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install maven
```

验证:
```bash
mvn -version
```

---

## 📦 编译详解

### 完整编译步骤

```bash
# 1. 进入项目目录
cd e:\code\media-download

# 2. 清理后编译（第一次）
mvn clean package

# 后续编译可以用（跳过清理）
mvn package

# 快速编译（跳过测试）
mvn clean package -DskipTests
```

### 编译输出

编译完成后，你会看到:
```
BUILD SUCCESS
...
media-downloader-2.0.0.jar
```

JAR文件位置: `target/media-downloader-2.0.0.jar`

---

## 🎯 使用Pro版本

### 基本用法

```bash
java -jar target/media-downloader-2.0.0.jar "https://example.com/page"
```

### 指定输出目录

```bash
java -jar target/media-downloader-2.0.0.jar "https://example.com/page" "D:\Downloads"
```

### 使用静态模式（快速版）

```bash
java -jar target/media-downloader-2.0.0.jar "https://example.com/page" "./output" "static"
```

### 完整示例

```bash
# 下载音频
java -jar target/media-downloader-2.0.0.jar "https://music.example.com/song"

# 下载播客
java -jar target/media-downloader-2.0.0.jar "https://podcast.example.com/episode" "./podcasts"

# 下载视频
java -jar target/media-downloader-2.0.0.jar "https://video.example.com/movie" "./videos"
```

---

## 🖥️ 程序运行时的情况

当你运行Pro版本时，会看到:

```
初始化Selenium WebDriver...
打开网页: https://example.com/page
[方法1] 从页面源代码提取媒体URL
[方法2] 执行JavaScript获取媒体信息
[方法3] 查找HTML媒体标签

✓ 找到 5 个媒体文件

[1/5] 下载: https://cdn.example.com/file1.mp3
  进度: 10% 20% 30% ... 100% 完成
  ✓ 已保存: downloads/file1.mp3

...

✓ 下载完成！
```

同时，会自动打开一个Chrome浏览器窗口，显示程序正在访问的网页。

---

## 🔍 调试步骤

如果Pro版本仍然找不到媒体，按以下步骤调试:

### 步骤1: 在浏览器中手动检查

1. 打开网页
2. 按 **F12** 打开开发者工具
3. 进入 **Console** 标签
4. 执行以下命令:

```javascript
// 查找所有<audio>标签
document.querySelectorAll('audio')

// 查找所有<video>标签
document.querySelectorAll('video')

// 查找所有<source>标签
document.querySelectorAll('source')

// 查看是否有全局media对象
window.media
window.player
window.config
```

### 步骤2: 查看Network请求

1. 打开开发者工具的 **Network** 标签
2. 刷新网页
3. 查找 **XHR** 和 **Fetch** 请求
4. 找到返回媒体URL的API（如 `/api/media`, `/api/playlist` 等）
5. 查看响应，找到媒体URL

### 步骤3: 增加等待时间

如果网页加载较慢，编辑 `src/main/java/com/media/MediaDownloaderPro.java`:

找到此行:
```java
Thread.sleep(5000);  // 等待5秒
```

改为:
```java
Thread.sleep(10000); // 等待10秒
```

然后重新编译:
```bash
mvn clean package
```

---

## 🎯 常见场景

### 场景1: 网站使用JavaScript库计载媒体（React/Vue）

**症状**: 右键查看源代码看不到媒体URL

**解决**: 使用Pro版本自动处理。它会等待JavaScript执行并获取动态加载的内容。

### 场景2: 媒体通过API获取

**症状**: Network标签中看到对 `/api/media` 的请求返回媒体URL

**解决**: Pro版本的第2层和第3层方法会从最终渲染的HTML中提取URL。

### 场景3: 媒体在iframe中

**症状**: 媒体在`<iframe>`标签中

**局限**: 当前版本可能无法跨iframe获取。考虑:
1. 直接访问iframe的src URL
2. 使用开发者工具找到嵌入的实际媒体URL

### 场景4: 需要登录才能访问

**解决方式**: 编辑代码添加Cookie认证（详见DYNAMIC_MODE.md）

---

## 🚀 优化建议

### 如果编译较慢

第一次编译会下载所有依赖，耗时较长（可能10-30分钟）。后续编译会快得多。

加速方法:
```bash
# 跳过测试编译
mvn clean package -DskipTests

# 使用之前的构建缓存
mvn package
```

### 如果想使用无头模式（不显示浏览器窗口）

编辑 `MediaDownloaderPro.java`，在以下行取消注释:

```java
// options.addArguments("--headless");
```

改为:

```java
options.addArguments("--headless");
```

### 如果想并发下载

修改代码使用Java的 `ExecutorService` 线程池（详见源代码注释）

---

## 📞 遇到问题？

### 编译失败

```
[ERROR] BUILD FAILURE
```

**解决**:
1. 检查Java版本: `java -version`（需要11+）
2. 检查Maven配置: `mvn -version`
3. 删除缓存: `mvn clean`
4. 重新编译: `mvn clean package -DskipTests`

### "找不到ChromeDriver"

不用担心，程序会自动下载（需要网络连接）。第一次会较慢。

### 程序很慢

- 首次运行下载ChromeDriver较慢（~150MB）
- 之后会快很多
- 可启用无头模式加快速度

### 还是找不到媒体

1. 查看[DYNAMIC_MODE.md](DYNAMIC_MODE.md)获取深入指导
2. 增加等待时间（见上面的调试步骤3）
3. 检查网页本身是否有媒体文件
4. 尝试其他网址测试

---

## ✅ 完整检查清单

- [ ] Java 11+ 已安装? `java -version`
- [ ] Maven 已安装? `mvn -version`  
- [ ] 克隆/下载项目到本地
- [ ] 进入项目目录: `cd e:\code\media-download`
- [ ] 编译项目: `mvn clean package`
- [ ] 测试运行: `java -jar target/media-downloader-2.0.0.jar "https://example.com"`
- [ ] 查看输出，确认找到媒体
- [ ] 检查 `downloads` 目录是否有文件

---

## 🎉 下一步

1. **快速开始**
   ```bash
   mvn clean package
   java -jar target/media-downloader-2.0.0.jar "https://your-url"
   ```

2. **深入学习**
   - 阅读 [DYNAMIC_MODE.md](DYNAMIC_MODE.md) 了解完整功能
   - 查看源代码注释理解工作原理
   - 自定义代码以满足特殊需求

3. **遇到问题**
   - [README.md](README.md) - 常见问题
   - [DEPLOYMENT.md](DEPLOYMENT.md) - 部署指南
   - 源代码中的详细注释

---

**祝你使用愉快！** 🚀
