@echo off
chcp 65001 > nul
REM ======================================
REM 媒体下载器Pro 版本 - 编译和运行脚本
REM ======================================

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════╗
echo ║      媒体下载器 Pro v2.0 - 编译脚本       ║
echo ╚════════════════════════════════════════════╝
echo.

REM 检查Java
echo [1] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 找不到Java，请先安装Java 11 或更高版本
    echo 下载地址: https://www.oracle.com/java/technologies/downloads/
    exit /b 1
)
echo ✓ Java 已安装
echo.

REM 检查Maven
echo [2] 检查Maven环境...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ⚠ 警告: 未找到Maven
    echo.
    echo 解决方案:
    echo   方案A: 安装Maven (https://maven.apache.org/download.cgi)
    echo   方案B: 使用包装式Maven (./mvnw)
    echo.
    echo 使用快速版本 (已编译): 
    echo   java -jar target/media-downloader.jar "https://your-url"
    echo.
    exit /b 1
)
echo ✓ Maven 已安装
echo.

REM 编译
echo [3] 编译项目...
mvn clean package -DskipTests
if errorlevel 1 (
    echo ❌ 编译失败
    exit /b 1
)
echo ✓ 编译成功
echo.

REM 运行
echo [4] 运行媒体下载器 Pro
set /p URL="请输入网页URL: "
if "!URL!"=="" (
    echo ❌ 错误: URL不能为空
    exit /b 1
)

set /p OUTPUT="请输入输出目录 (默认 ./downloads): "
if "!OUTPUT!"=="" set OUTPUT=./downloads

java -jar target/media-downloader-2.0.0.jar "!URL!" "!OUTPUT!"

echo.
pause
