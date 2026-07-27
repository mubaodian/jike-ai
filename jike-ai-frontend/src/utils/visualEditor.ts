import { ref, onUnmounted } from 'vue'

export interface SelectedElement {
  tagName: string
  id: string
  className: string
  textContent: string
  outerHTML: string
}

/**
 * 注入到 iframe 内的可视化编辑脚本
 * 通过 postMessage 与主页面通信
 */
const INJECTED_SCRIPT = `
(function() {
  if (window.__visualEditorInjected) return;
  window.__visualEditorInjected = true;

  const HOVER_BORDER = '2px solid #0fec89';
  const SELECTED_BORDER = '2px solid #00b333';

  let hoveredEl = null;
  let selectedEl = null;
  const originalOutlines = new WeakMap();

  function saveOutline(el) {
    if (!originalOutlines.has(el)) {
      originalOutlines.set(el, el.style.outline || '');
    }
  }

  function restoreOutline(el) {
    if (el) {
      el.style.outline = originalOutlines.get(el) || '';
      originalOutlines.delete(el);
    }
  }

  function handleMouseOver(e) {
    const target = e.target;
    if (!target || target === selectedEl || target === document.body || target === document.documentElement) return;
    if (hoveredEl && hoveredEl !== selectedEl) {
      restoreOutline(hoveredEl);
    }
    hoveredEl = target;
    saveOutline(target);
    target.style.outline = HOVER_BORDER;
  }

  function handleMouseOut(e) {
    const target = e.target;
    if (!target || target === selectedEl) return;
    restoreOutline(target);
    if (hoveredEl === target) {
      hoveredEl = null;
    }
  }

  function handleClick(e) {
    e.preventDefault();
    e.stopPropagation();

    const target = e.target;
    if (!target) return;

    // 清除之前选中的元素
    if (selectedEl) {
      restoreOutline(selectedEl);
    }

    selectedEl = target;
    saveOutline(target);
    target.style.outline = SELECTED_BORDER;

    // 提取元素信息
    const textContent = (target.textContent || '').trim().substring(0, 200);
    const clone = target.cloneNode(false);
    clone.textContent = textContent.substring(0, 100) + (textContent.length > 100 ? '...' : '');
    const outerHTML = clone.outerHTML;

    // 通过 postMessage 发送选中元素信息给主页面
    window.parent.postMessage({
      type: 'VISUAL_EDITOR_SELECT',
      payload: {
        tagName: target.tagName.toLowerCase(),
        id: target.id || '',
        className: target.className || '',
        textContent: textContent,
        outerHTML: outerHTML
      }
    }, '*');
  }

  document.addEventListener('mouseover', handleMouseOver, true);
  document.addEventListener('mouseout', handleMouseOut, true);
  document.addEventListener('click', handleClick, true);

  // 监听主页面的控制消息
  window.addEventListener('message', function(e) {
    if (!e.data || !e.data.type) return;

    if (e.data.type === 'VISUAL_EDITOR_CLEAR') {
      // 清除选中状态
      if (selectedEl) {
        restoreOutline(selectedEl);
        selectedEl = null;
      }
    } else if (e.data.type === 'VISUAL_EDITOR_EXIT') {
      // 退出编辑模式，清除所有状态并移除事件
      if (hoveredEl) {
        restoreOutline(hoveredEl);
        hoveredEl = null;
      }
      if (selectedEl) {
        restoreOutline(selectedEl);
        selectedEl = null;
      }
      document.removeEventListener('mouseover', handleMouseOver, true);
      document.removeEventListener('mouseout', handleMouseOut, true);
      document.removeEventListener('click', handleClick, true);
      window.__visualEditorInjected = false;
    }
  });
})();
`

/**
 * 可视化编辑器 composable
 * 通过 postMessage + 注入脚本实现 iframe 内元素的高亮选择
 */
export function useVisualEditor(iframeSelector: () => HTMLIFrameElement | null) {
  const editMode = ref(false)
  const selectedElement = ref<SelectedElement | null>(null)

  // 监听 iframe 发来的消息
  function handleMessage(e: MessageEvent) {
    if (!e.data || e.data.type !== 'VISUAL_EDITOR_SELECT') return
    selectedElement.value = e.data.payload as SelectedElement
  }

  // 进入编辑模式：向 iframe 注入脚本
  function enterEditMode() {
    const iframe = iframeSelector()
    if (!iframe || !iframe.contentWindow) return

    editMode.value = true
    window.addEventListener('message', handleMessage)

    // 注入可视化编辑脚本
    injectScript(iframe)
  }

  // 注入脚本到 iframe
  function injectScript(iframe: HTMLIFrameElement) {
    try {
      const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document
      if (!iframeDoc) return

      const script = iframeDoc.createElement('script')
      script.textContent = INJECTED_SCRIPT
      iframeDoc.head.appendChild(script)
    } catch {
      // 跨域时降级为 postMessage 方式注入（需要 iframe 内页面配合）
      console.warn('无法直接注入脚本，尝试通过 postMessage 注入')
      iframe.contentWindow?.postMessage(
        { type: 'VISUAL_EDITOR_INJECT', script: INJECTED_SCRIPT },
        '*',
      )
    }
  }

  // 退出编辑模式
  function exitEditMode() {
    const iframe = iframeSelector()
    if (iframe?.contentWindow) {
      iframe.contentWindow.postMessage({ type: 'VISUAL_EDITOR_EXIT' }, '*')
    }

    window.removeEventListener('message', handleMessage)
    editMode.value = false
    selectedElement.value = null
  }

  // 仅清除选中元素（不退出编辑模式）
  function clearSelection() {
    const iframe = iframeSelector()
    if (iframe?.contentWindow) {
      iframe.contentWindow.postMessage({ type: 'VISUAL_EDITOR_CLEAR' }, '*')
    }
    selectedElement.value = null
  }

  // 构建带元素上下文的提示词
  function buildPromptWithContext(userMessage: string): string {
    if (!selectedElement.value) return userMessage

    const el = selectedElement.value
    let context = `[用户选中了页面元素]\n`
    context += `标签: <${el.tagName}>`
    if (el.id) context += ` id="${el.id}"`
    if (el.className) context += ` class="${el.className}"`
    context += `\n`
    if (el.textContent) {
      context += `文本内容: ${el.textContent.substring(0, 150)}\n`
    }
    context += `HTML: ${el.outerHTML}\n`
    context += `[用户的修改要求]\n`
    context += userMessage

    return context
  }

  // 获取选中元素的简要描述（用于 alert 显示）
  function getSelectionSummary(): string {
    if (!selectedElement.value) return ''
    const el = selectedElement.value
    let desc = `<${el.tagName}>`
    if (el.id) desc += `#${el.id}`
    if (el.className) {
      const classes = el.className.split(/\s+/).slice(0, 3).join('.')
      desc += `.${classes}`
    }
    if (el.textContent) {
      desc += ` "${el.textContent.substring(0, 50)}${el.textContent.length > 50 ? '...' : ''}"`
    }
    return desc
  }

  // 组件卸载时清理
  onUnmounted(() => {
    if (editMode.value) {
      exitEditMode()
    }
  })

  return {
    editMode,
    selectedElement,
    enterEditMode,
    exitEditMode,
    clearSelection,
    buildPromptWithContext,
    getSelectionSummary,
  }
}
