#!/bin/bash
# VS Code 视频下载脚本 for Linux/Mac

echo ""
echo "╔══════════════════════════════════════════╗"
echo "║    VS Code 文档视频下载工具 v1.0         ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "✗ 错误: 未检测到Java安装"
    echo "  请先安装JDK 11或更高版本"
    exit 1
fi

# 检查JAR文件是否存在
if [ ! -f "target/media-downloader-1.0.0.jar" ]; then
    echo "✗ 错误: JAR文件不存在"
    echo "  请先运行: mvn clean package"
    exit 1
fi

# 设置参数
URL="${1}"
OUTPUT_DIR="${2:-vscode-docs}"

if [ -z "$URL" ]; then
    echo "用法: ./download-videos.sh <URL> [输出目录]"
    echo ""
    echo "参数:"
    echo "  <URL>        - VS Code文档URL，例如: https://code.visualstudio.com/docs"
    echo "  [输出目录]   - 视频保存的目录，默认为 ./vscode-docs"
    echo ""
    echo "示例:"
    echo "  ./download-videos.sh https://code.visualstudio.com/docs ./my-docs"
    exit 1
fi

echo "下载地址: $URL"
echo "输出目录: $OUTPUT_DIR"
echo ""

# 执行下载
java -cp "target/media-downloader-1.0.0.jar" com.media.VSCodeDocumentDownloader "$URL" "$OUTPUT_DIR" --videos

if [ $? -ne 0 ]; then
    echo ""
    echo "✗ 视频下载失败!"
    exit 1
fi

echo ""
echo "✓ 视频下载完成!"
echo "  视频保存在: $OUTPUT_DIR"
