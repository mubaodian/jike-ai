<template>
  <div id="appGeneratePage">
    <div class="header-bar">
      <div class="header-left">
        <div class="app-name">
          {{ appName }}
          <a-tag v-if="codeGenType" color="blue" style="margin-left: 12px">{{ getCodeGenTypeDisplay(codeGenType) }}</a-tag>
        </div>
      </div>
      <div class="header-right">
        <a-space>
          <a-button @click="showAppDetailDrawer = true">
            <template #icon>
              <info-circle-outlined />
            </template>
            应用详情
          </a-button>
          <a-button @click="downloadCode" :loading="downloading" :disabled="!isOwnApp">
            <template #icon>
              <DownloadOutlined />
            </template>
            下载代码
          </a-button>

          <a-button
            type="primary"
            :loading="deployLoading"
            :disabled="!isOwnApp"
            :title="!isOwnApp ? '只能部署自己的应用' : ''"
            @click="handleDeploy"
          >
            <template #icon>
              <cloud-upload-outlined />
            </template>
            部署应用
          </a-button>
        </a-space>
      </div>
    </div>

    <div class="content-area">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainerRef">
          <!-- 加载更多历史消息 -->
          <div v-if="hasMore" class="load-more-container">
            <a-button type="text" size="small" :loading="historyLoading" @click="loadMoreHistory">
              加载更多历史消息
            </a-button>
          </div>
          <div v-if="historyLoading && messages.length === 0" class="history-loading">
            <a-spin size="small" />
            <span>加载历史消息中...</span>
          </div>
          <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
            <img
              v-if="msg.role === 'assistant'"
              src="@/assets/aiAvatar.png"
              class="message-avatar ai-avatar"
            />
            <a-avatar
              v-else
              shape="circle"
              :src="loginUserStore.loginUser.userAvatar"
              :size="32"
              class="message-avatar user-avatar"
            >
              {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
            </a-avatar>
            <div v-if="msg.role === 'assistant'" class="message-content markdown-body">
              <AssistantMessageContent :content="msg.content" />
            </div>

            <div v-else class="message-content">{{ msg.content }}</div>
          </div>
          <div
            v-if="
              streaming &&
              messages.length > 0 &&
              messages[messages.length - 1]?.role !== 'assistant'
            "
            class="message-item assistant"
          >
            <img src="@/assets/aiAvatar.png" class="message-avatar ai-avatar" />
            <div class="message-content">
              <span class="typing-indicator">输出中...</span>
            </div>
          </div>
        </div>

        <!-- 选中元素提示 -->
        <div v-if="selectedElement" class="selected-element-alert">
          <a-alert type="info" closable @close="handleClearSelection">
            <template #message>
              <span class="selected-element-text">
                已选中元素: {{ getSelectionSummary() }}
              </span>
            </template>
          </a-alert>
        </div>

        <!-- 用户消息输入框 -->
        <div class="input-area">
          <a-tooltip v-if="!isOwnApp" title="无法在别人的作品下对话哦~" placement="top">
            <a-input-group compact>
              <a-input
                v-model:value="userMessage"
                placeholder="请描述你想生成的网站，越详细效果越好哦"
                allow-clear
                disabled
              />
              <a-button type="primary" :loading="streaming" disabled> 发送 </a-button>
            </a-input-group>
          </a-tooltip>
          <a-input-group v-else compact>
            <a-input
              v-model:value="userMessage"
              placeholder="请描述你想生成的网站，越详细效果越好哦"
              allow-clear
              @keyup.enter="handleSendMessage"
            />
            <a-button type="primary" :loading="streaming" @click="handleSendMessage">
              发送
            </a-button>
          </a-input-group>
        </div>
      </div>

      <!-- 右侧网页展示区域 -->
      <div class="preview-section">
        <div v-if="previewUrl" class="preview-header">
          <span class="preview-title">实时预览</span>
          <div class="preview-actions">
            <a-button
              :type="editMode ? 'primary' : 'default'"
              size="small"
              :ghost="editMode"
              @click="toggleEditMode"
            >
              <template #icon>
                <edit-outlined />
              </template>
              {{ editMode ? '退出编辑' : '可视化编辑' }}
            </a-button>
            <a-button type="text" size="small" @click="openPreviewInNewWindow">
              <template #icon>
                <export-outlined />
              </template>
              新窗口打开
            </a-button>
          </div>
        </div>
        <div v-if="previewUrl" class="preview-container" :class="{ 'edit-mode-active': editMode }">
          <iframe ref="previewIframeRef" :key="previewKey" :src="previewUrl" class="preview-iframe" @load="handleIframeLoad"></iframe>
        </div>
        <div v-else class="preview-empty">
          <p>等待生成网站内容...</p>
        </div>
      </div>
    </div>

    <!-- 部署成功弹窗 -->
    <a-modal v-model:visible="deployModalVisible" title="应用部署成功" :footer="null">
      <p>您的应用已成功部署！</p>
      <p>访问地址：</p>
      <div class="deploy-url-container">
        <a-input v-model:value="deployUrl" :bordered="false" readonly class="deploy-url-input" />
        <div class="deploy-url-actions">
          <copy-outlined class="copy-icon" title="复制" @click="handleCopyUrl" />
          <export-outlined
            class="export-icon"
            title="新窗口打开"
            @click="openDeployUrlInNewWindow"
          />
        </div>
      </div>
    </a-modal>

    <!-- 应用详情模态框 -->
    <AppDetailModal
      v-model:visible="showAppDetailDrawer"
      :app-user-info="appUserInfo"
      :create-time="createTime"
      :code-gen-type="codeGenType"
      :is-own-app="isOwnApp"
      :is-admin="isAdmin"
      @edit="goToEditPage"
      @delete="handleDeleteApp"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, reactive, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message as antMessage } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  CloudUploadOutlined,
  CopyOutlined,
  ExportOutlined,
  DownloadOutlined,
  EditOutlined,
} from '@ant-design/icons-vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import AssistantMessageContent from '@/components/AssistantMessageContent.vue'
import { getAppVoById, deployApp, deleteApp } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatTime } from '@/utils/time'
import { renderMarkdown } from '@/utils/markdown'
import { getStaticPreviewUrl } from '@/env'
import { getCodeGenTypeDisplay } from '@/constants/codeGenType'
import { useVisualEditor } from '@/utils/visualEditor'
import request from '@/request'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>('')
const appName = ref('应用生成')
const codeGenType = ref('default')
const appUserId = ref<number>()
const appUserInfo = ref<API.UserVO>()
const createTime = ref('')

const messages = ref<Array<{ role: 'user' | 'assistant'; content: string }>>([])
const userMessage = ref('')
const streaming = ref(false)
const previewUrl = ref('')

const deployLoading = ref(false)
const downloading = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')
const showAppDetailDrawer = ref(false)
const previewKey = ref(0)

// iframe 引用
const previewIframeRef = ref<HTMLIFrameElement | null>(null)

// 可视化编辑器
const {
  editMode,
  selectedElement,
  enterEditMode,
  exitEditMode,
  clearSelection,
  buildPromptWithContext,
  getSelectionSummary,
} = useVisualEditor(() => previewIframeRef.value)

// 切换编辑模式
const toggleEditMode = () => {
  if (editMode.value) {
    exitEditMode()
  } else {
    enterEditMode()
  }
}

// 清除选中元素
const handleClearSelection = () => {
  clearSelection()
}

// iframe 加载完成后，如果处于编辑模式则重新绑定事件
const handleIframeLoad = () => {
  if (editMode.value) {
    exitEditMode()
    enterEditMode()
  }
}

// 消息容器引用（用于自动滚动）
const messagesContainerRef = ref<HTMLElement | null>(null)

// 对话历史相关状态
const historyLoading = ref(false)
const hasMore = ref(false)
const lastCreateTime = ref<string | undefined>(undefined)

// 是否是自己的应用
const isOwnApp = computed(() => appUserId.value === loginUserStore.loginUser.id)

// 是否是管理员
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 加载对话历史（游标分页，向前加载）
const loadHistory = async (appIdStr: string) => {
  historyLoading.value = true
  try {
    const res = await listAppChatHistory({
      appId: appIdStr as unknown as number,
      pageSize: 10,
      lastCreateTime: lastCreateTime.value,
    })
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records || []
      // 将历史消息转为展示格式，插入到消息列表头部（升序展示）
      const historyMessages = records
        .map((item) => ({
          role: (item.messageType === 'ai' ? 'assistant' : 'user') as 'user' | 'assistant',
          content: item.message || '',
        }))
        .reverse()
      messages.value = [...historyMessages, ...messages.value]
      // 游标：取本页最早一条的 createTime
      const oldest = records[records.length - 1]
      if (oldest) {
        lastCreateTime.value = oldest.createTime ?? undefined
      }
      // 是否还有更多：本页返回条数等于 pageSize 则可能有更多
      hasMore.value = records.length >= 10
      return records.length
    }
  } catch (error) {
    console.error('加载历史消息错误:', error)
  } finally {
    historyLoading.value = false
  }
  return 0
}

// 点击"加载更多历史消息"
const loadMoreHistory = async () => {
  if (!appId.value) return
  const scrollEl = messagesContainerRef.value
  const prevScrollHeight = scrollEl?.scrollHeight ?? 0
  await loadHistory(appId.value)
  // 保持滚动位置不跳动
  await nextTick()
  if (scrollEl) {
    scrollEl.scrollTop = scrollEl.scrollHeight - prevScrollHeight
  }
}

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    const appIdStr = route.params.appId as string
    if (!appIdStr || !/^\d+$/.test(appIdStr)) {
      antMessage.error('应用ID无效')
      return
    }

    appId.value = appIdStr
    const res = await getAppVoById({ id: appIdStr as unknown as number })

    if (res.data.code === 0 && res.data.data) {
      appName.value = res.data.data.appName || '应用'
      codeGenType.value = res.data.data.codeGenType || 'default'
      appUserId.value = res.data.data.userId
      appUserInfo.value = res.data.data.user
      createTime.value = formatTime(res.data.data.createTime)

      // 先加载最近10条历史消息
      const historyCount = await loadHistory(appIdStr)

      // 如果有至少2条历史消息，显示预览
      if (messages.value.length >= 2) {
        updatePreviewUrl()
      }

      // 仅当是自己的应用且没有历史消息时，才自动触发初始提示词
      const ownApp = res.data.data.userId === loginUserStore.loginUser.id
      if (res.data.data.initPrompt && historyCount === 0 && ownApp) {
        await autoSendInitialPrompt(res.data.data.initPrompt)
      } else if (historyCount > 0) {
        // 有历史消息时滚动到底部
        await nextTick()
        if (messagesContainerRef.value) {
          messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
        }
      }
    } else {
      antMessage.error(res.data.message || '获取应用信息失败')
    }
  } catch (error) {
    antMessage.error('获取应用信息失败，请检查应用是否存在')
    console.error('获取应用信息错误:', error)
  }
}

// 自动发送初始提示词
const autoSendInitialPrompt = async (prompt: string) => {
  messages.value.push({
    role: 'user',
    content: prompt,
  })

  await streamGenCode(prompt)
}

// 发送消息
const handleSendMessage = async () => {
  if (!isOwnApp.value) {
    antMessage.warning('无法在别人的作品下对话哦~')
    return
  }

  if (!userMessage.value.trim()) {
    return
  }

  const msg = userMessage.value
  // 构建带元素上下文的提示词
  const promptToSend = buildPromptWithContext(msg)

  messages.value.push({
    role: 'user',
    content: msg,
  })
  userMessage.value = ''

  // 发送后清除选中元素并退出编辑模式
  if (editMode.value) {
    exitEditMode()
  }

  await streamGenCode(promptToSend)
}

// 流式生成代码
const streamGenCode = async (message: string) => {
  if (streaming.value) {
    antMessage.warning('正在生成中，请稍候...')
    return
  }

  if (!appId.value) {
    antMessage.error('应用ID未获取，请刷新重试')
    return
  }

  streaming.value = true

  try {
    console.log('调用 chatToGenCode 接口，appId:', appId.value, 'message:', message)

    // 创建响应式 AI 消息对象并添加到消息列表（实现打字机效果）
    const aiMessageObj = reactive({
      role: 'assistant' as const,
      content: '',
    })
    messages.value.push(aiMessageObj)

    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || 'http://localhost:8123/api'

    // 使用 EventSource 处理 SSE（Server-Sent Events）
    const eventSource = new EventSource(
      `${baseURL}/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(message)}`,
      {
        withCredentials: true, // 如果需要发送 cookie
      },
    )

    await new Promise<void>((resolve, reject) => {
      eventSource.addEventListener('message', (event) => {
        try {
          const data = event.data.trim()
          // console.log('收到 SSE 消息:', data.substring(0, 100))

          if (data) {
            // 解析 JSON 包装的数据: {"d": "内容"}
            const parsed = JSON.parse(data)
            if (parsed.d) {
              const content = parsed.d
              // 所有内容都累加到 AI 消息中（包括思考内容）
              aiMessageObj.content += content
              console.log('解析数据块:', content.substring(0, 100))

              // 自动滚动到最新消息
              if (messagesContainerRef.value) {
                setTimeout(() => {
                  messagesContainerRef.value!.scrollTop = messagesContainerRef.value!.scrollHeight
                }, 0)
              }
            }
          }
        } catch (error) {
          console.error('解析 SSE 数据失败:', error, '原始数据:', event.data)
        }
      })

      // 监听 done 事件（流式生成完成）
      eventSource.addEventListener('done', (event) => {
        console.log('收到 done 事件，流式生成完成，总长度:', aiMessageObj.content.length)
        eventSource.close()
        resolve()
      })

      // 监听错误事件
      eventSource.addEventListener('error', (event) => {
        console.error('SSE 连接出错:', event)
        eventSource.close()
        // 删除已添加的不完整消息
        messages.value.pop()
        reject(new Error('流式连接中断'))
      })

      // 设置超时保护（5分钟）
      setTimeout(() => {
        if (eventSource.readyState !== EventSource.CLOSED) {
          eventSource.close()
          reject(new Error('请求超时'))
        }
      }, 300000) // 5分钟超时
    })

    if (aiMessageObj.content) {
      // 生成完成后显示预览
      updatePreviewUrl()
      antMessage.success('代码生成完成！')
    } else {
      // 删除空消息
      messages.value.pop()
      antMessage.warning('未获取到生成内容')
    }
  } catch (error: any) {
    console.error('生成代码出错:', error)
    antMessage.error(`生成代码出错: ${error.message}`)
  } finally {
    streaming.value = false
  }
}

// 下载代码
const downloadCode = async () => {
  if (!appId.value) {
    antMessage.error('应用ID不存在')
    return
  }
  downloading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/download/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
    })
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`)
    }
    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId.value}.zip`
    // 下载文件
    const blob = await response.blob()
    const downloadUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.click()
    // 清理
    URL.revokeObjectURL(downloadUrl)
    antMessage.success('代码下载成功')
  } catch (error) {
    console.error('下载失败：', error)
    antMessage.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

// 更新预览URL
const updatePreviewUrl = () => {
  previewUrl.value = getStaticPreviewUrl(codeGenType.value, appId.value)
  previewKey.value++
}

// 部署应用
const handleDeploy = async () => {
  if (!isOwnApp.value) {
    antMessage.warning('只能部署自己的应用')
    return
  }

  if (!appId.value) {
    antMessage.warning('应用ID不存在')
    return
  }

  deployLoading.value = true

  try {
    // 转换为数字作为 API 参数
    const res = await deployApp({ appId: appId.value as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      antMessage.success('应用部署成功')
    } else {
      antMessage.error(res.data.message || '部署失败')
    }
  } catch (error) {
    antMessage.error('部署应用出错')
    console.error('部署应用错误:', error)
  } finally {
    deployLoading.value = false
  }
}

// 复制部署URL
const handleCopyUrl = async () => {
  try {
    await navigator.clipboard.writeText(deployUrl.value)
    antMessage.success('已复制到剪贴板')
  } catch (error) {
    antMessage.error(`复制失败:${error}，请手动复制`)
  }
}

// 在新窗口打开预览
const openPreviewInNewWindow = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 在新窗口打开部署地址
const openDeployUrlInNewWindow = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// 跳转到编辑页面
const goToEditPage = () => {
  router.push(`/app/detail/${appId.value}`)
}

// 删除应用
const handleDeleteApp = async () => {
  try {
    const res = await deleteApp({ id: appId.value as unknown as number })
    if (res.data.code === 0) {
      antMessage.success('应用删除成功')
      showAppDetailDrawer.value = false
      await router.push('/')
    } else {
      antMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    antMessage.error('删除应用出错')
    console.error('删除应用错误:', error)
  }
}

// 页面加载
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
#appGeneratePage {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.app-name {
  font-size: 16px;
  font-weight: bold;
  color: #000;
}

.content-area {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 12px;
  padding: 12px 16px;
}

.chat-section {
  display: flex;
  flex-direction: column;
  width: 40%;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.load-more-container {
  display: flex;
  justify-content: center;
  padding: 4px 0 8px;
}

:deep(.load-more-container .ant-btn) {
  color: #1890ff;
  font-size: 12px;
  border: 1px dashed #91caff;
  border-radius: 12px;
  padding: 0 14px;
  height: 26px;
  background: #e6f4ff;
}

:deep(.load-more-container .ant-btn:hover) {
  background: #bae0ff;
  border-color: #1890ff;
}

.history-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #1890ff;
  font-size: 13px;
  background: #e6f4ff;
  border-radius: 8px;
  margin: 4px 0;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-item {
  display: flex;
  justify-content: flex-start;
  gap: 6px;
  align-items: flex-start;
}

.message-item.user {
  justify-content: flex-end;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-avatar {
  order: 2;
  border-radius: 50%;
  overflow: hidden;
}

.message-content {
  max-width: 70%;
  padding: 6px 10px;
  border-radius: 4px;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.4;
}

.message-item.user .message-content {
  background: #1890ff;
  color: #fff;
  order: 1;
}

.message-item.assistant .message-content {
  background: #f0f0f0;
  color: #000;
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
  transition: transform 0.2s ease;
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

/* Markdown 样式 */
.markdown-body {
  white-space: normal;
  padding: 10px !important;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin: 8px 0 4px 0;
  font-weight: 600;
  line-height: 1.3;
}

.markdown-body h1 {
  font-size: 18px;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 2px;
}

.markdown-body h2 {
  font-size: 16px;
}

.markdown-body h3 {
  font-size: 14px;
}

.markdown-body h4 {
  font-size: 13px;
}

.markdown-body p {
  margin: 4px 0;
  line-height: 1.5;
}

.markdown-body ul,
.markdown-body ol {
  margin: 4px 0;
  padding-left: 20px;
}

.markdown-body li {
  margin: 2px 0;
  line-height: 1.4;
}

.markdown-body code:not(.hljs) {
  background: #f0f0f0;
  padding: 2px 4px;
  border-radius: 2px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #d63384;
}

.markdown-body pre {
  background: #1e1e1e !important;
  border-radius: 3px;
  overflow-x: auto;
  margin: 4px 0;
  padding: 8px !important;
}

.markdown-body pre code {
  background: none !important;
  padding: 0 !important;
  border-radius: 0;
  font-size: 12px;
  line-height: 1.4;
  font-family: 'Courier New', Courier, monospace;
}

.markdown-body pre code.hljs {
  padding: 0 !important;
  background: none !important;
  color: #e8e8e8;
}

.markdown-body blockquote {
  border-left: 3px solid #dfe2e5;
  margin: 4px 0;
  padding: 0 8px;
  color: #6a737d;
  font-size: 13px;
}

.markdown-body blockquote p {
  margin: 2px 0;
}

.markdown-body table {
  border-collapse: collapse;
  margin: 4px 0;
  font-size: 12px;
}

.markdown-body table th,
.markdown-body table td {
  border: 1px solid #dfe2e5;
  padding: 4px 8px;
  text-align: left;
}

.markdown-body table th {
  background: #f6f8fa;
  font-weight: 600;
}

.markdown-body a {
  color: #0366d6;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.typing-indicator {
  display: inline-block;
  animation: blink 1.4s infinite;
}

@keyframes blink {
  0%,
  60%,
  100% {
    opacity: 1;
  }
  30% {
    opacity: 0.5;
  }
}

.input-area {
  padding: 10px;
  border-top: 1px solid #f0f0f0;
}

.preview-section {
  width: 60%;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-title {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.preview-container {
  width: 100%;
  flex: 1;
  overflow: auto;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 14px;
}

.edit-mode-active {
  outline: 2px dashed #1890ff;
  outline-offset: -2px;
}

.selected-element-alert {
  padding: 8px 10px 0;
}

.selected-element-text {
  font-size: 12px;
  color: #333;
  word-break: break-all;
}

:deep(.ant-input-group) {
  display: flex;
  gap: 8px;
}

:deep(.ant-input-group .ant-input) {
  flex: 1;
}

:deep(.ant-modal) {
  max-width: 600px;
}

.deploy-url-container {
  display: flex;
  align-items: center;
  position: relative;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  transition: border-color 0.3s;
}

.deploy-url-container:hover {
  border-color: #40a9ff;
}

.deploy-url-input {
  flex: 1;
  padding: 8px 12px !important;
  border: none !important;
  font-family: monospace;
  font-size: 13px;
}

:deep(.deploy-url-input input) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
}

.deploy-url-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  border-left: 1px solid #f0f0f0;
  opacity: 0;
  transition: opacity 0.3s;
}

.deploy-url-container:hover .deploy-url-actions {
  opacity: 1;
}

.copy-icon,
.export-icon {
  font-size: 16px;
  cursor: pointer;
  color: #1890ff;
  transition: color 0.3s;
}

.copy-icon:hover,
.export-icon:hover {
  color: #40a9ff;
}
</style>
