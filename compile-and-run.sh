#!/bin/bash

# ==========================================
# 媒体下载器Pro 版本 - 编译和运行脚本
# ==========================================

echo ""
echo "╔════════════════════════════════════════════╗"
echo "║      媒体下载器 Pro v2.0 - 编译脚本       ║"
echo "╚════════════════════════════════════════════╝"
echo ""

# 检查Java
echo "[1] 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 找不到Java，请先安装Java 11 或更高版本"
    echo "下载地址: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi
java -version
echo "✓ Java 已安装"
echo ""

# 检查Maven
echo "[2] 检查Maven环境..."
if ! command -v mvn &> /dev/null; then
    echo "⚠ 警告: 未找到Maven"
    echo ""
    echo "解决方案:"
    echo "  方案A: 安装Maven"
    echo "    macOS: brew install maven"
    echo "    Ubuntu/Debian: sudo apt-get install maven"
    echo "  方案B: 下载 https://maven.apache.org/download.cgi"
    echo ""
    echo "使用快速版本 (已编译):"
    echo "  java -jar target/media-downloader.jar 'https://your-url'"
    echo ""
    exit 1
fi
mvn -version | head -1
echo "✓ Maven 已安装"
echo ""

# 编译
echo "[3] 编译项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✓ 编译成功"
echo ""

# 运行
echo "[4] 运行媒体下载器 Pro"
read -p "请输入网页URL: " URL
if [ -z "$URL" ]; then
    echo "❌ 错误: URL不能为空"
    exit 1
fi

read -p "请输入输出目录 (默认 ./downloads): " OUTPUT
OUTPUT=${OUTPUT:=./downloads}

java -jar target/media-downloader-2.0.0.jar "$URL" "$OUTPUT"

echo ""
