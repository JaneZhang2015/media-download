# Maven 安装指南

## 📥 Maven 下载和安装

Maven 是Java项目的构建工具，用于编译、打包Java项目。

---

## Windows 系统安装

### 方案1️⃣: 官方网站下载（最新版本）

#### 步骤1: 下载Maven

访问官方网站：https://maven.apache.org/download.cgi

选择 **Binary zip archive** 下载最新版本（如 `apache-maven-3.9.x-bin.zip`）

#### 步骤2: 解压文件

1. 下载后，右键解压到一个简单的位置，如：
   ```
   C:\apache-maven-3.9.x
   ```
   或
   ```
   C:\maven
   ```

2. 确保看到以下文件夹结构：
   ```
   C:\apache-maven-3.9.x\
   ├── bin\          (包含 mvn.cmd)
   ├── boot\
   ├── conf\
   ├── lib\
   └── LICENSE
   ```

#### 步骤3: 配置环境变量

**方法A: 通过系统设置（推荐）**

1. 右键点击"此电脑" → 选择"属性"
2. 点击"高级系统设置"
3. 点击"环境变量"按钮
4. 在"系统变量"下点击"新建"
   ```
   变量名: MAVEN_HOME
   变量值: C:\apache-maven-3.9.x
   ```
5. 编辑"Path"变量，添加：
   ```
   %MAVEN_HOME%\bin
   ```
6. 点击"确定"保存

**方法B: 通过PowerShell（管理员）**

```powershell
[Environment]::SetEnvironmentVariable("MAVEN_HOME","C:\apache-maven-3.9.x","Machine")
$currentPath = [Environment]::GetEnvironmentVariable("Path","Machine")
[Environment]::SetEnvironmentVariable("Path","$currentPath;C:\apache-maven-3.9.x\bin","Machine")
```

重启PowerShell使生效。

#### 步骤4: 验证安装

关闭并重新打开PowerShell / CMD，运行：

```bash
mvn -version
```

看到类似输出表示成功：
```
Apache Maven 3.9.x
Maven home: C:\apache-maven-3.9.x
Java version: 11.0.x
```

---

## macOS 系统安装

### 方案1️⃣: 使用Homebrew（最简单）

```bash
brew install maven
```

验证：
```bash
mvn -version
```

### 方案2️⃣: 手动下载安装

```bash
# 1. 下载
curl -O https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# 2. 解压
tar xzf apache-maven-3.9.6-bin.tar.gz

# 3. 移动到合适位置
sudo mv apache-maven-3.9.6 /usr/local/

# 4. 编辑 ~/.bash_profile 或 ~/.zshrc（新Mac系统用zshrc）
echo 'export PATH="/usr/local/apache-maven-3.9.6/bin:$PATH"' >> ~/.bash_profile

# 5. 重新加载配置
source ~/.bash_profile

# 6. 验证
mvn -version
```

---

## Linux 系统安装

### Ubuntu / Debian

```bash
# 更新包列表
sudo apt-get update

# 安装Maven
sudo apt-get install maven

# 验证
mvn -version
```

### CentOS / RHEL

```bash
sudo yum install maven
mvn -version
```

### 手动安装（任何Linux）

```bash
# 1. 下载
wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# 2. 解压
tar xzf apache-maven-3.9.6-bin.tar.gz

# 3. 移动
sudo mv apache-maven-3.9.6 /opt/

# 4. 编辑 /etc/profile 或 ~/.bashrc
sudo nano /etc/profile

# 添加这两行：
export M2_HOME=/opt/apache-maven-3.9.6
export PATH=$M2_HOME/bin:$PATH

# 5. 重新加载
source /etc/profile

# 6. 验证
mvn -version
```

---

## 🔧 配置 Maven（可选）

### 修改仓库镜像（加速下载）

编辑 `MAVEN_HOME/conf/settings.xml`，在 `<mirrors>` 节点上添加阿里云镜像：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Mirror</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

这样可以大大加快依赖下载速度。

---

## ✅ 完整检查清单

- [ ] 下载 Maven zip 文件
- [ ] 解压到合适位置（记住路径）
- [ ] 设置 MAVEN_HOME 环境变量
- [ ] 添加 Maven bin 目录到 PATH
- [ ] 重启终端/命令行
- [ ] 运行 `mvn -version` 验证安装
- [ ] 看到版本号表示成功 ✅

---

## 🚀 现在编译项目

安装完Maven后，进行以下步骤编译Pro版本：

```bash
# 进入项目目录
cd e:\code\media-download

# 编译（第一次会下载依赖，耗时较长）
mvn clean package -DskipTests

# 等待编译完成...
```

编译成功会看到：
```
BUILD SUCCESS
```

然后可以运行Pro版本：
```bash
java -jar target/media-downloader-2.0.0.jar "https://your-url"
```

---

## 📂 Maven 安装位置参考

**Windows:**
```
C:\maven
C:\apache-maven-3.9.6
D:\tools\maven
```

**macOS/Linux:**
```
/usr/local/apache-maven-3.9.6
/opt/maven
~/maven
```

---

## 🐛 常见问题

### Q: 下载很慢？
A: 
1. 使用镜像源（见上面的配置部分）
2. 或者使用代理
3. 或者改用阿里云镜像

### Q: mvn 命令找不到？
A:
1. 检查环境变量是否正确设置
2. 确保解压路径没有中文和特殊字符
3. 重启Power Shell/CMD

### Q: 如何卸载Maven？
A:
1. 删除 Maven 文件夹
2. 删除环境变量中的 MAVEN_HOME
3. 从 PATH 中删除 %MAVEN_HOME%\bin

### Q: 如何切换Maven版本？
A:
1. 下载新版本Maven
2. 修改 MAVEN_HOME 变量指向新版本
3. 重启终端验证

---

## 📚 更多资源

- 官方网站: https://maven.apache.org/
- 官方文档: https://maven.apache.org/guides/
- 下载中心: https://maven.apache.org/download.cgi

---

**现在可以开始编译项目了！** 🎉
