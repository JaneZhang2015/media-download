#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""追加中文翻译到 claude-docs 所有 txt 文件的最后一行"""

import os

BASE = r"E:\IntelliJidea\media-download\claude-docs"

# 文件路径 -> 最后一行的中文翻译
TRANSLATIONS = {
    r"Claude Code overview.txt":
        "【中文】• code.claude.com：演示、定价和产品详情",

    r"Claude Code_ AI-powered coding assistant for developers.txt":
        "【中文】逐周部署增长",

    r"docs\en\best-practices.txt":
        "【中文】CLAUDE.md——存储项目约定和持久化上下文（/docs/en/memory）；技能——赋予 Claude 领域专业知识和可复用的工作流（/docs/en/skills）；子代理——将工作卸载到隔离上下文（/docs/en/sub-agents）；代理团队——协调多个并行会话（/docs/en/agent-teams）；MCP——将 Claude 连接到外部服务（/docs/en/mcp）；钩子——使用钩子自动化工作流（/docs/en/hooks-guide）",

    r"docs\en\chrome.txt":
        "【中文】• Chrome 中 Claude 入门：Chrome 扩展程序完整文档，包括快捷键、计划任务和权限设置",

    r"docs\en\claude-code-on-the-web.txt":
        "【中文】• 数据使用",

    r"docs\en\claude-md.txt":
        "【中文】使用技能扩展 Claude（/docs/en/skills）；Claude Code 最佳实践（/docs/en/best-practices）；管理 Claude 的记忆（/docs/en/memory）",

    r"docs\en\cli-reference.txt":
        "【中文】• Agent SDK 文档——编程使用方式与集成",

    r"docs\en\common-workflows.txt":
        "【中文】最佳实践——充分利用 Claude Code 的模式（/docs/en/best-practices）；Claude Code 工作原理——了解代理循环和上下文管理（/docs/en/how-claude-code-works）；扩展 Claude Code——添加技能、钩子、MCP、子代理和插件（/docs/en/features-overview）；参考实现——克隆我们的开发容器参考实现",

    r"docs\en\desktop-quickstart.txt":
        "【中文】• 常用工作流：调试、重构、测试等教程",

    r"docs\en\desktop.txt":
        "【中文】提交 bug 时，请附上桌面应用版本、操作系统、确切的错误信息及相关日志。macOS 请查看 Console.app，Windows 请查看事件查看器 → Windows 日志 → 应用程序。",

    r"docs\en\features-overview.txt":
        "【中文】CLAUDE.md——存储项目上下文、约定和指令（/docs/en/memory）；技能——赋予 Claude 领域专业知识和可复用工作流（/docs/en/skills）；子代理——将工作卸载到隔离上下文（/docs/en/sub-agents）；代理团队——协调多个并行会话（/docs/en/agent-teams）；MCP——将 Claude 连接到外部服务（/docs/en/mcp）；钩子——使用钩子自动化工作流（/docs/en/hooks-guide）；插件——打包并共享功能集（/docs/en/plugins）；市场——托管和分发插件集合（/docs/en/plugin-marketplaces）",

    r"docs\en\github-actions.txt":
        "【中文】Claude 在创建 PR 及响应请求时将遵循上述准则。",

    r"docs\en\gitlab-ci-cd.txt":
        "【中文】• 自定义提示词：通过 job 中的 prompt/prompt_file 传递特定任务指令。针对不同 job 使用不同提示词（例如：review、implement、refactor）。",

    r"docs\en\hooks.txt":
        "【中文】限制与故障排查请参阅指南（/docs/en/hooks-guide#limitations-and-troubleshooting）。",

    r"docs\en\how-claude-code-works.txt":
        "【中文】添加扩展功能——添加技能、MCP 连接和自定义命令（/docs/en/features-overview）；常用工作流——典型任务的分步指南（/docs/en/common-workflows）",

    r"docs\en\jetbrains.txt":
        "【中文】故障排查指南（/docs/en/troubleshooting）。",

    r"docs\en\legal-and-compliance.txt":
        "【中文】© Anthropic PBC 版权所有。使用须遵守 Anthropic 适用的服务条款。",

    r"docs\en\mcp.txt":
        "【中文】上述设置仍适用于筛选实际加载的托管服务器。",

    r"docs\en\overview.txt":
        "【中文】• code.claude.com：演示、定价和产品详情",

    r"docs\en\quickstart.txt":
        "【中文】• 社区：加入我们的 Discord 获取技巧与支持",

    r"docs\en\settings.txt":
        "【中文】• 故障排查：常见配置问题的解决方案",

    r"docs\en\setup.txt":
        "【中文】[/代码]（代码块结束标记）",

    r"docs\en\skills.txt":
        "【中文】• 权限：控制工具和技能访问",

    r"docs\en\slack.txt":
        "【中文】网页版 Claude Code——了解更多有关网页版 Claude Code 的信息（/docs/en/claude-code-on-the-web）；Slack 版 Claude——通用 Claude for Slack 文档；Slack 应用市场——从 Slack 市场安装 Claude 应用；Claude 帮助中心——获取额外支持",

    r"docs\en\sub-agents.txt":
        "【中文】• 使用 MCP 服务器为子代理提供对外部工具和数据的访问权限",

    r"docs\en\third-party-integrations.txt":
        "【中文】• 配置权限：查看安全设置，定义 Claude Code 在您的环境中能做什么和不能做什么。",

    r"docs\en\troubleshooting.txt":
        "【中文】• 直接询问 Claude 其功能及特性——Claude 内置了对其文档的访问权限",

    r"docs\en\vs-code.txt":
        "【中文】• 配置 Claude Code 设置，自定义允许的命令、钩子等。这些设置在扩展程序和 CLI 之间共享。",

    r"docs\en\cursor_extension\anthropic.claude-code.txt":
        "【中文】我们找不到该页面。",

    r"docs\en\vscode_extension\anthropic.claude-code.txt":
        "【中文】我们找不到该页面。",
}

success = 0
failed = 0
for rel_path, translation in TRANSLATIONS.items():
    full_path = os.path.join(BASE, rel_path)
    if not os.path.exists(full_path):
        print(f"[跳过] 文件不存在: {rel_path}")
        failed += 1
        continue
    try:
        with open(full_path, "a", encoding="utf-8") as f:
            f.write("\n" + translation + "\n")
        print(f"[✓] {rel_path}")
        success += 1
    except Exception as e:
        print(f"[✗] {rel_path}: {e}")
        failed += 1

print(f"\n完成：成功 {success} 个，失败 {failed} 个")
