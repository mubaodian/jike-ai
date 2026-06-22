import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked 使用 highlight.js 进行代码高亮
marked.setOptions({
  breaks: true,
  gfm: true,
  pedantic: false,
})

// 自定义代码块渲染器
marked.use({
  renderer: {
    code({ text, lang }: { text: string; lang?: string }) {
      const language = lang || 'plaintext'
      let highlighted = text

      // 尝试使用 highlight.js 进行语法高亮
      try {
        if (hljs.getLanguage(language)) {
          highlighted = hljs.highlight(text, { language }).value
        } else {
          highlighted = hljs.highlightAuto(text).value
        }
      } catch (error) {
        console.error('代码高亮失败:', error)
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


