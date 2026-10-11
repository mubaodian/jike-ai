import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked 使用 highlight.js 进行代码高亮
marked.setOptions({
  breaks: true,
  gfm: true,
  pedantic: false,
})

// 语言别名：highlight.js 未注册的语言名映射到已注册的近亲，
// 避免回退到 highlightAuto（它会遍历全部 ~190 种语言，流式渲染下极其昂贵）
const LANGUAGE_ALIASES: Record<string, string> = {
  vue: 'xml',
  tsx: 'typescript',
  jsx: 'javascript',
}

// 解析出 highlight.js 实际支持的语言，不支持则返回 undefined
const resolveLanguage = (lang?: string): string | undefined => {
  if (!lang) {
    return undefined
  }
  // 只取 info string 的第一个词，例如 "html title=xxx" -> "html"
  const name = lang.trim().split(/\s+/)[0]?.toLowerCase()
  if (!name) {
    return undefined
  }
  const aliased = LANGUAGE_ALIASES[name] || name
  return hljs.getLanguage(aliased) ? aliased : undefined
}

// 自定义代码块渲染器
marked.use({
  renderer: {
    code({ text, lang }: { text: string; lang?: string }) {
      const language = lang?.trim().split(/\s+/)[0] || 'plaintext'
      const resolved = resolveLanguage(lang)
      let highlighted: string

      if (resolved) {
        // 尝试使用 highlight.js 进行语法高亮
        try {
          highlighted = hljs.highlight(text, { language: resolved }).value
        } catch (error) {
          console.error('代码高亮失败:', error)
          highlighted = escapeHtml(text)
        }
      } else {
        // 未注册的语言不回退到 highlightAuto，直接按纯文本转义
        highlighted = escapeHtml(text)
      }

      // 返回带有 hljs 类的 pre 和 code 元素，确保 highlight.js 样式生效
      return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
    },
  },
})

/**
 * 将 Markdown 内容渲染为 HTML
 * @param content Markdown 内容
 * @returns HTML 字符串
 */
export const renderMarkdown = (content: string): string => {
  try {
    const html = marked.parse(content)
    // marked 返回的是字符串
    if (typeof html === 'string') {
      return html
    }
    return escapeHtml(content)
  } catch (error) {
    console.error('Markdown 渲染失败:', error)
    // 出错时返回原始内容（已转义）
    return escapeHtml(content)
  }
}

/**
 * HTML 转义（防止 XSS）
 * @param text 要转义的文本
 * @returns 转义后的文本
 */
function escapeHtml(text: string): string {
  const map: { [key: string]: string } = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  }
  return text.replace(/[&<>"']/g, (char: string) => map[char] || char)
}


