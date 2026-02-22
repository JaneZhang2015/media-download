#!/bin/bash

# VS Code 文档翻译工具 - Linux/Mac 启动脚本

set -e

echo "========================================"
echo "VS Code 文档翻译工具"
echo "========================================"
echo ""

# 检查 API 密钥
if [ -z "$ARK_API_KEY" ]; then
    echo "❌ 错误: 未设置环境变量 ARK_API_KEY"
    echo ""
    echo "请先设置 API 密钥:"
    echo "  export ARK_API_KEY=\"your_api_key_here\""
    echo ""
    exit 1
fi

echo "✓ API密钥已设置"
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ 未找到 Java，请安装 Java 11 或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[0-9]+' | head -1)
echo "✓ Java 版本: $JAVA_VERSION"
echo ""

# 检查源目录
SOURCE_DIR="${1:-vscode-docs}"
TARGET_DIR="${2:-vscode-docs-zh}"

if [ ! -d "$SOURCE_DIR" ]; then
    echo "❌ 源目录不存在: $SOURCE_DIR"
    echo ""
    echo "用法: ./translate.sh [源目录] [目标目录]"
    exit 1
fi

echo "源目录: $SOURCE_DIR"
echo "目标目录: $TARGET_DIR"
echo ""

# 编译项目
echo "编译项目..."
if mvn clean package -q; then
    echo "✓ 编译成功"
    echo ""
else
    echo "❌ 编译失败"
    exit 1
fi

# 运行翻译
echo "启动翻译..."
echo ""

java -cp target/media-downloader-1.0.0.jar:lib/* \
    com.media.TranslatorMain "$SOURCE_DIR" "$TARGET_DIR"

echo ""
echo "✓ 翻译完成！"
