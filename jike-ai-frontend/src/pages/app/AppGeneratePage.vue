<template>
  <div id="appGeneratePage">
    <div class="header-bar">
      <div class="app-name">{{ appName }}</div>
      <a-space>
        <a-button @click="showAppDetailDrawer = true">
          <template #icon>
            <info-circle-outlined />
          </template>
          应用详情
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

    <div class="content-area">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainerRef">
          <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
            <img v-if="msg.role === 'assistant'" src="@/assets/aiAvatar.png" class="message-avatar ai-avatar" />
            <a-avatar
              v-else
              :src="loginUserStore.loginUser.userAvatar"
              :size="32"
              class="message-avatar user-avatar"
            >
              {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
            </a-avatar>
            <div
              v-if="msg.role === 'assistant'"
              class="message-content markdown-body"
              v-html="renderMarkdown(msg.content)"
            />
            <div v-else class="message-content">{{ msg.content }}</div>
          </div>
          <div v-if="streaming && messages.length > 0 && messages[messages.length - 1]?.role !== 'assistant'" class="message-item assistant">
            <img src="@/assets/aiAvatar.png" class="message-avatar ai-avatar" />
            <div class="message-content">
              <span class="typing-indicator">输出中...</span>
            </div>
          </div>
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
              <a-button type="primary" :loading="streaming" disabled>
                发送
              </a-button>
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
          <a-button type="text" size="small" @click="openPreviewInNewWindow">
            <template #icon>
              <export-outlined />
            </template>
            新窗口打开
          </a-button>
        </div>
        <div v-if="previewUrl" class="preview-container">
          <iframe :src="previewUrl" class="preview-iframe"></iframe>
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
        <a-input
          v-model:value="deployUrl"
          :bordered="false"
          readonly
          class="deploy-url-input"
        />
        <div class="deploy-url-actions">
          <copy-outlined
            class="copy-icon"
            title="复制"
            @click="handleCopyUrl"
          />
          <export-outlined
            class="export-icon"
            title="新窗口打开"
            @click="openDeployUrlInNewWindow"
          />
        </div>
      </div>
    </a-modal>

    <!-- 应用详情模态框 -->
    <a-modal
      v-model:visible="showAppDetailDrawer"
      title="应用详情"
      :footer="null"
      width="400px"
    >
      <div class="app-detail-content">
        <!-- 应用基础信息 -->
        <div class="detail-section">
          <div class="detail-item">
            <span class="detail-label">创建者</span>
            <div class="creator-info">
              <a-avatar
                v-if="appUserInfo?.userAvatar"
                :src="appUserInfo.userAvatar"
                :size="32"
              />
              <a-avatar v-else :size="32">
                {{ appUserInfo?.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="creator-name">{{ appUserInfo?.userName }}</span>
            </div>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ createTime }}</span>
          </div>
        </div>

        <!-- 操作栏（仅本人或管理员可见） -->
        <div v-if="isOwnApp || isAdmin" class="action-section">
          <a-space>
            <a-button type="primary" @click="goToEditPage">修改</a-button>
            <a-popconfirm
              title="删除应用"
              description="确定要删除此应用吗？删除后无法恢复。"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteApp"
            >
              <a-button danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message as antMessage } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  CloudUploadOutlined,
  CopyOutlined,
  ExportOutlined,
} from '@ant-design/icons-vue'
import { getAppVoById, deployApp, deleteApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatTime } from '@/utils/time'
import { renderMarkdown } from '@/utils/markdown'
import { getStaticPreviewUrl } from '@/env'
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
const deployModalVisible = ref(false)
const deployUrl = ref('')
const showAppDetailDrawer = ref(false)

// 消息容器引用（用于自动滚动）
const messagesContainerRef = ref<HTMLElement | null>(null)

// 是否为查看模式（不自动发送初始消息）
const isViewMode = computed(() => route.query.view === '1')

// 是否是自己的应用
const isOwnApp = computed(() => appUserId.value === loginUserStore.loginUser.id)

// 是否是管理员
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    // 直接使用字符串参数，避免精度丢失
    const appIdStr = route.params.appId as string
    console.log('应用生成ID (字符串):', appIdStr)

    // 校验：不能为空，只能由数字组成
    if (!appIdStr || !/^\d+$/.test(appIdStr)) {
      antMessage.error('应用ID无效')
      return
    }

    appId.value = appIdStr
    const res = await getAppVoById({ id: appIdStr as unknown as number })
    console.log('获取应用信息响应:', res.data)

    if (res.data.code === 0 && res.data.data) {
      appName.value = res.data.data.appName || '应用'
      codeGenType.value = res.data.data.codeGenType || 'default'
      appUserId.value = res.data.data.userId
      appUserInfo.value = res.data.data.user
      createTime.value = formatTime(res.data.data.createTime)

      // 初始化时，如果还没有生成过且不是查看模式，自动发送初始提示词
      if (res.data.data.initPrompt && messages.value.length === 0 && !isViewMode.value) {
        await autoSendInitialPrompt(res.data.data.initPrompt)
      }
    } else {
      // 业务错误处理
      const errorMsg = res.data.message || '获取应用信息失败'
      antMessage.error(errorMsg)
      console.error('获取应用信息业务错误:', res.data)
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
  messages.value.push({
    role: 'user',
    content: msg,
  })
  userMessage.value = ''

  await streamGenCode(msg)
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
      }
    )

    await new Promise<void>((resolve, reject) => {
      eventSource.addEventListener('message', (event) => {
        try {
          const data = event.data.trim()
          console.log('收到 SSE 消息:', data.substring(0, 100))

          if (data) {
            // 解析 JSON 包装的数据: {"d": "内容"}
            const parsed = JSON.parse(data)
            if (parsed.d) {
              // 实时更新消息内容（打字机效果）
              aiMessageObj.content += parsed.d
              console.log('解析数据块:', parsed.d.substring(0, 100))

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

// 更新预览URL
const updatePreviewUrl = () => {
  previewUrl.value = getStaticPreviewUrl(codeGenType.value, appId.value)
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

<style>
</style>

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
  align-items: flex-end;
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

.app-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.detail-value {
  font-size: 14px;
  color: #000;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.creator-name {
  font-size: 14px;
  color: #000;
}

.action-section {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
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
