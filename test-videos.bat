@echo off
REM 测试脚本 - 下载VS Code文档中的MP4视频

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════╗
echo ║  VS Code MP4视频下载工具测试                    ║
echo ╚════════════════════════════════════════════════╝
echo.

REM 检查Java
where java >nul 2>nul
if errorlevel 1 (
    echo ✗ 错误: 未找到Java
    exit /b 1
)

REM 检查JAR
if not exist "target\media-downloader-1.0.0.jar" (
    echo ✗ 错误: JAR文件不存在，请先运行: mvn clean package
    exit /b 1
)

echo ✓ 环境检查通过
echo.

REM 测试配置
set "TEST_URL=https://code.visualstudio.com/docs"
set "OUTPUT_DIR=test-all-videos"

echo 开始扫描视频...
echo URL: %TEST_URL%
echo 输出目录: %OUTPUT_DIR%
echo.

REM 运行下载（带debug模式看到找到的视频）
java -cp "target\media-downloader-1.0.0.jar" com.media.VSCodeDocumentDownloader ^
  "%TEST_URL%" "%OUTPUT_DIR%" --videos

echo.
echo ╔════════════════════════════════════════════════╗
echo ✓ 测试完成
echo ╚════════════════════════════════════════════════╝
