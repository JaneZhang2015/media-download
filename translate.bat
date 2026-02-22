@echo off
REM VS Code 文档翻译工具 - Windows 启动脚本

setlocal enabledelayedexpansion

echo ========================================
echo VS Code 文档翻译工具
echo ========================================
echo.

REM 检查 API 密钥
if "!ARK_API_KEY!"=="" (
    echo ❌ 错误: 未设置环境变量 ARK_API_KEY
    echo.
    echo 请先设置 API 密钥:
    echo   set ARK_API_KEY=your_api_key_here
    echo.
    exit /b 1
)

echo ✓ API密钥已设置
echo.

REM 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未找到 Java，请安装 Java 11 或更高版本
    exit /b 1
)

for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /R "version"') do (
    echo ✓ %%i
)
echo.

REM 设置目录
set SOURCE_DIR=%1
set TARGET_DIR=%2

if "!SOURCE_DIR!"=="" (
    set SOURCE_DIR=vscode-docs
)

if "!TARGET_DIR!"=="" (
    set TARGET_DIR=vscode-docs-zh
)

if not exist "!SOURCE_DIR!" (
    echo ❌ 源目录不存在: !SOURCE_DIR!
    echo.
    echo 用法: translate.bat [源目录] [目标目录]
    exit /b 1
)

echo 源目录: !SOURCE_DIR!
echo 目标目录: !TARGET_DIR!
echo.

REM 编译项目
echo 编译项目...
call mvn clean package -q
if errorlevel 1 (
    echo ❌ 编译失败
    exit /b 1
)

echo ✓ 编译成功
echo.

REM 运行翻译
echo 启动翻译...
echo.

java -cp target/media-downloader-1.0.0.jar;lib/* ^
    com.media.TranslatorMain "!SOURCE_DIR!" "!TARGET_DIR!"

echo.
echo ✓ 翻译完成！

endlocal
