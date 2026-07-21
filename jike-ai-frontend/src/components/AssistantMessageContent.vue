<template>
  <div class="assistant-message">
    <template v-for="(segment, idx) in segments" :key="idx">
      <div v-if="segment.type === 'thinking'" class="thinking-box">
        <div class="thinking-header" @click="toggleThinking(idx)">
          <span class="thinking-toggle">{{ openSet.has(idx) ? '▼' : '▶' }}</span>
          <span>💭 AI 思考过程</span>
        </div>
        <div v-if="openSet.has(idx)" class="thinking-content" v-html="renderMarkdown(segment.content)"></div>
      </div>
      <div v-else-if="segment.content.trim()" v-html="renderMarkdown(segment.content)"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

interface Props {
  content: string
}

interface Segment {
  type: 'thinking' | 'text'
  content: string
}

const props = defineProps<Props>()

// 记录哪些思考块是展开的
const openSet = reactive(new Set<number>())

const toggleThinking = (idx: number) => {
  if (openSet.has(idx)) {
    openSet.delete(idx)
  } else {
    openSet.add(idx)
  }
}

// 将内容分割为多个段（思考块 + 普通文本交替）
const segments = computed<Segment[]>(() => {
  const result: Segment[] = []
  const regex = /\[thinking\]([\s\S]*?)\[\/thinking\]/g
  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = regex.exec(props.content)) !== null) {
    // 匹配前的普通文本
    if (match.index > lastIndex) {
      result.push({ type: 'text', content: props.content.slice(lastIndex, match.index) })
    }
    // 思考块
    result.push({ type: 'thinking', content: match[1] || '' })
    lastIndex = regex.lastIndex
  }

  // 剩余的普通文本
  if (lastIndex < props.content.length) {
    result.push({ type: 'text', content: props.content.slice(lastIndex) })
  }

  return result
})
</script>

<style scoped>
.assistant-message {
  width: 100%;
}

.thinking-box {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 12px;
  overflow: hidden;
}

.thinking-header {
  padding: 10px 12px;
  background: #f9f9f9;
  border-bottom: 1px solid #e8e8e8;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 13px;
  user-select: none;
  transition: background 0.2s ease;
}

.thinking-header:hover {
  background: #f0f0f0;
}

.thinking-toggle {
  display: inline-block;
  width: 16px;
  text-align: center;
  font-size: 12px;
}

.thinking-content {
  padding: 10px 12px;
  background: #fafafa;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  max-height: 400px;
  overflow-y: auto;
}

:deep(.thinking-content h1),
:deep(.thinking-content h2),
:deep(.thinking-content h3),
:deep(.thinking-content h4),
:deep(.thinking-content h5),
:deep(.thinking-content h6) {
  margin: 6px 0 3px 0;
  font-size: 12px;
}

:deep(.thinking-content code:not(.hljs)) {
  background: #e8e8e8;
  padding: 2px 4px;
  border-radius: 2px;
  font-size: 12px;
}

:deep(.thinking-content pre) {
  background: #1e1e1e;
  border-radius: 3px;
  padding: 6px;
  margin: 4px 0;
  overflow-x: auto;
}

:deep(.thinking-content pre code) {
  background: none;
  padding: 0;
  font-size: 12px;
}
</style>
