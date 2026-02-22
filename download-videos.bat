@echo off
chcp 65001 > nul
REM VS Code 视频下载脚本 for Windows

echo.
echo ╔══════════════════════════════════════════╗
echo ║    VS Code 文档视频下载工具 v1.0         ║
echo ╚══════════════════════════════════════════╝
echo.

REM 检查Java是否安装
where java >nul 2>nul
if errorlevel 1 (
    echo ✗ 错误: 未检测到Java安装
    echo   请先安装JDK 11或更高版本
    exit /b 1
)

REM 检查JAR文件是否存在
if not exist "target\media-downloader-1.0.0.jar" (
    echo ✗ 错误: JAR文件不存在
    echo   请先运行: mvn clean package
    exit /b 1
)

REM 设置参数
set "URL=%1"
set "OUTPUT_DIR=%2"

if "%URL%"=="" (
    echo 用法: download-videos.bat ^<URL^> [输出目录]
    echo.
    echo 参数:
    echo   ^<URL^>        - VS Code文档URL，例如: https://code.visualstudio.com/docs
    echo   [输出目录]   - 视频保存的目录，默认为 ./vscode-docs
    echo.
    echo 示例:
    echo   download-videos.bat https://code.visualstudio.com/docs ./my-docs
    exit /b 1
)

if "%OUTPUT_DIR%"=="" (
    set "OUTPUT_DIR=vscode-docs"
)

echo 下载地址: %URL%
echo 输出目录: %OUTPUT_DIR%
echo.

REM 执行下载
java -cp "target\media-downloader-1.0.0.jar" com.media.VSCodeDocumentDownloader "%URL%" "%OUTPUT_DIR%" --videos

if errorlevel 1 (
    echo.
    echo ✗ 视频下载失败!
    exit /b 1
)

echo.
echo ✓ 视频下载完成!
echo   视频保存在: %OUTPUT_DIR%
