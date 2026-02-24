#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""下载 Claude Code 文档到 claude-docs 文件夹"""

import os
import time
import urllib.request
import urllib.error
from html.parser import HTMLParser

BASE_DIR = r"E:\IntelliJidea\media-download\claude-docs"
BASE_URL = "https://code.claude.com"

# 文件路径 -> URL 路径
PAGES = {
    "Claude Code overview.txt":
        "/",
    "Claude Code_ AI-powered coding assistant for developers.txt":
        "/",   # fallback; will try anthropic.com page too
    "docs/en/best-practices.txt":
        "/docs/en/best-practices",
    "docs/en/chrome.txt":
        "/docs/en/chrome",
    "docs/en/claude-code-on-the-web.txt":
        "/docs/en/claude-code-on-the-web",
    "docs/en/claude-md.txt":
        "/docs/en/claude-md",
    "docs/en/cli-reference.txt":
        "/docs/en/cli-reference",
    "docs/en/common-workflows.txt":
        "/docs/en/common-workflows",
    "docs/en/desktop-quickstart.txt":
        "/docs/en/desktop-quickstart",
    "docs/en/desktop.txt":
        "/docs/en/desktop",
    "docs/en/features-overview.txt":
        "/docs/en/features-overview",
    "docs/en/github-actions.txt":
        "/docs/en/github-actions",
    "docs/en/gitlab-ci-cd.txt":
        "/docs/en/gitlab-ci-cd",
    "docs/en/hooks.txt":
        "/docs/en/hooks",
    "docs/en/how-claude-code-works.txt":
        "/docs/en/how-claude-code-works",
    "docs/en/jetbrains.txt":
        "/docs/en/jetbrains",
    "docs/en/legal-and-compliance.txt":
        "/docs/en/legal-and-compliance",
    "docs/en/mcp.txt":
        "/docs/en/mcp",
    "docs/en/overview.txt":
        "/docs/en/overview",
    "docs/en/quickstart.txt":
        "/docs/en/quickstart",
    "docs/en/settings.txt":
        "/docs/en/settings",
    "docs/en/setup.txt":
        "/docs/en/setup",
    "docs/en/skills.txt":
        "/docs/en/skills",
    "docs/en/slack.txt":
        "/docs/en/slack",
    "docs/en/sub-agents.txt":
        "/docs/en/sub-agents",
    "docs/en/third-party-integrations.txt":
        "/docs/en/third-party-integrations",
    "docs/en/troubleshooting.txt":
        "/docs/en/troubleshooting",
    "docs/en/vs-code.txt":
        "/docs/en/vs-code",
    "docs/en/cursor_extension/anthropic.claude-code.txt":
        "/docs/en/cursor_extension/anthropic.claude-code",
    "docs/en/vscode_extension/anthropic.claude-code.txt":
        "/docs/en/vscode_extension/anthropic.claude-code",
}

class TextExtractor(HTMLParser):
    """将 HTML 转换为可读文本"""
    SKIP_TAGS = {"script", "style", "noscript", "head", "meta", "link"}
    BLOCK_TAGS = {"p", "div", "li", "h1", "h2", "h3", "h4", "h5", "h6",
                  "tr", "br", "hr", "section", "article", "header", "footer",
                  "nav", "aside", "pre", "blockquote"}

    def __init__(self):
        super().__init__()
        self.result = []
        self._skip = 0
        self._pending_newline = False

    def handle_starttag(self, tag, attrs):
        t = tag.lower()
        if t in self.SKIP_TAGS:
            self._skip += 1
        if t in self.BLOCK_TAGS:
            if self.result and not self.result[-1].endswith("\n"):
                self.result.append("\n")
        if t == "a":
            for name, val in attrs:
                if name == "href" and val:
                    self.result.append(f" ({val}) ")

    def handle_endtag(self, tag):
        t = tag.lower()
        if t in self.SKIP_TAGS:
            self._skip = max(0, self._skip - 1)
        if t in self.BLOCK_TAGS:
            if self.result and not self.result[-1].endswith("\n"):
                self.result.append("\n")

    def handle_data(self, data):
        if self._skip:
            return
        text = data.strip()
        if text:
            self.result.append(text + " ")

    def get_text(self):
        raw = "".join(self.result)
        # 合并多余空行
        lines = []
        blank = 0
        for line in raw.splitlines():
            s = line.strip()
            if s:
                lines.append(s)
                blank = 0
            else:
                blank += 1
                if blank <= 1:
                    lines.append("")
        return "\n".join(lines)


def fetch_page(url: str) -> str | None:
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/120.0.0.0 Safari/537.36"
        ),
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
    }
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            charset = "utf-8"
            ct = resp.headers.get("Content-Type", "")
            if "charset=" in ct:
                charset = ct.split("charset=")[-1].strip().split(";")[0].strip()
            html = resp.read().decode(charset, errors="replace")
            parser = TextExtractor()
            parser.feed(html)
            return parser.get_text()
    except urllib.error.HTTPError as e:
        print(f"    HTTP {e.code}: {url}")
        return None
    except Exception as e:
        print(f"    Error: {e}")
        return None


def save_file(rel_path: str, content: str):
    full = os.path.join(BASE_DIR, rel_path.replace("/", os.sep))
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8") as f:
        f.write(content)


def main():
    os.makedirs(BASE_DIR, exist_ok=True)
    success = 0
    failed = 0
    skipped = 0

    for rel_path, url_path in PAGES.items():
        dest_file = os.path.join(BASE_DIR, rel_path.replace("/", os.sep))
        if os.path.exists(dest_file) and os.path.getsize(dest_file) > 100:
            print(f"[跳过-已存在] {rel_path}")
            skipped += 1
            continue

        # 特殊处理：overview.txt 和首页区分
        if rel_path == "Claude Code overview.txt":
            url = BASE_URL + "/docs/en/overview"
        elif rel_path == "Claude Code_ AI-powered coding assistant for developers.txt":
            url = "https://www.anthropic.com/claude-code"
        else:
            url = BASE_URL + url_path

        print(f"[下载] {rel_path}")
        print(f"       {url}")
        text = fetch_page(url)
        if text and len(text.strip()) > 50:
            save_file(rel_path, text)
            print(f"  [✓] 已保存 ({len(text)} 字符)")
            success += 1
        else:
            # 备用 URL：尝试 docs.anthropic.com
            alt_url = None
            if "/docs/en/" in url_path:
                slug = url_path.replace("/docs/en/", "")
                alt_url = f"https://docs.anthropic.com/en/claude-code/{slug}"
            elif rel_path == "Claude Code_ AI-powered coding assistant for developers.txt":
                alt_url = BASE_URL + "/"

            if alt_url:
                print(f"       备用: {alt_url}")
                text2 = fetch_page(alt_url)
                if text2 and len(text2.strip()) > 50:
                    save_file(rel_path, text2)
                    print(f"  [✓] 备用成功 ({len(text2)} 字符)")
                    success += 1
                else:
                    print(f"  [✗] 两个 URL 均失败")
                    failed += 1
            else:
                print(f"  [✗] 失败")
                failed += 1

        time.sleep(0.5)

    print(f"\n完成：成功 {success} 个，跳过 {skipped} 个，失败 {failed} 个")


if __name__ == "__main__":
    main()
