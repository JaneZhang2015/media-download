@echo off
REM ========================================
REM Maven 快速安装脚本 (Windows)
REM ========================================
chcp 65001 > nul

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════╗
echo ║     Maven 自动安装脚本 (Windows)          ║
echo ╚════════════════════════════════════════════╝
echo.

REM 检查Java
echo [1] 检查Java版本...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 需要先安装Java
    echo 下载地址: https://www.oracle.com/java/technologies/downloads/
    exit /b 1
)
java -version 2>&1 | findstr /R "version"
echo ✓ Java 已安装
echo.

REM 设置Maven安装目录
set MAVEN_INSTALL_DIR=C:\maven

echo [2] Maven 安装位置: %MAVEN_INSTALL_DIR%
echo.

REM 检查是否已安装
echo [3] 检查Maven是否已安装...
mvn -version >nul 2>&1
if errorlevel 0 (
    echo ✓ Maven 已安装，无需重复安装
    mvn -version
    exit /b 0
)

echo ⚠ Maven 未安装，需要手动下载
echo.
echo 请按以下步骤安装:
echo.
echo ════════════════════════════════════════════
echo  1️⃣  访问 https://maven.apache.org/download.cgi
echo  2️⃣  下载 "apache-maven-3.9.x-bin.zip"
echo  3️⃣  将zip文件放在 %MAVEN_INSTALL_DIR% 的父目录
echo  4️⃣  右键解压该zip文件
echo  5️⃣  重命名解压后的文件夹为 "maven"
echo  6️⃣  重启PowerShell/CMD，再运行此脚本
echo ════════════════════════════════════════════
echo.
echo 或者 👇 自动设置环境变量:
echo.
pause
cls

echo [4] 配置环境变量...

REM 如果用户已下载并解压，将此改为实际路径
REM setx MAVEN_HOME "C:\maven"

REM 检查Maven目录是否存在
if not exist "%MAVEN_INSTALL_DIR%" (
    echo ❌ %MAVEN_INSTALL_DIR% 不存在
    echo 请先手动下载并解压Maven
    exit /b 1
)

echo 在PowerShell中运行以下命令设置环境变量:
echo.
echo [Environment]::SetEnvironmentVariable("MAVEN_HOME","%MAVEN_INSTALL_DIR%","Machine")
echo $currentPath = [Environment]::GetEnvironmentVariable("Path","Machine")
echo [Environment]::SetEnvironmentVariable("Path","$currentPath;%MAVEN_INSTALL_DIR%\bin","Machine")
echo.
echo ✓ 请使用 "管理员" 权限运行 PowerShell，然后粘贴上面的命令
echo.
pause
