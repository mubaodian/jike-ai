<template>
  <div id="appGeneratePage">
    <div class="header-bar">
      <div class="app-name">{{ appName }}</div>
      <a-button type="primary" :loading="deployLoading" @click="handleDeploy">
        部署应用
      </a-button>
    </div>

    <div class="content-area">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 消息区域 -->
        <div class="messages-container">
          <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <div v-if="streaming" class="message-item assistant">
            <div class="message-content">
              <span class="typing-indicator">输出中...</span>
            </div>
          </div>
        </div>

        <!-- 用户消息输入框 -->
        <div class="input-area">
          <a-input-group compact>
            <a-input
              v-model:value="userMessage"
              placeholder="输入您的需求..."
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
      <a-input-group compact>
        <a-input v-model:value="deployUrl" />
        <a-button type="primary" @click="handleCopyUrl">复制</a-button>
      </a-input-group>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message as antMessage } from 'ant-design-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import request from '@/request'

const route = useRoute()

const appId = ref<string>('')
const appName = ref('应用生成')
const codeGenType = ref('default')

const messages = ref<Array<{ role: 'user' | 'assistant'; content: string }>>([])
const userMessage = ref('')
const streaming = ref(false)
const previewUrl = ref('')

const deployLoading = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

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

      // 初始化时，如果还没有生成过，自动发送初始提示词
      if (res.data.data.initPrompt && messages.value.length === 0) {
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
  let aiMessage = ''

  try {
    console.log('调用 chatToGenCode 接口，appId:', appId.value, 'message:', message)

    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL

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
              aiMessage += parsed.d
              console.log('解析数据块:', parsed.d.substring(0, 100))
            }
          }
        } catch (error) {
          console.error('解析 SSE 数据失败:', error, '原始数据:', event.data)
        }
      })

      // 监听 done 事件（流式生成完成）
      eventSource.addEventListener('done', (event) => {
        console.log('收到 done 事件，流式生成完成')
        eventSource.close()
        resolve()
      })

      // 监听错误事件
      eventSource.addEventListener('error', (event) => {
        console.error('SSE 连接出错:', event)
        eventSource.close()
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

    console.log('生成完成，总长度:', aiMessage.length)

    if (aiMessage) {
      messages.value.push({
        role: 'assistant',
        content: aiMessage,
      })

      // 生成完成后显示预览
      updatePreviewUrl()
      antMessage.success('代码生成完成！')
    } else {
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
  previewUrl.value = `http://localhost:8123/api/static/${codeGenType.value}_${appId.value}/`
}

// 部署应用
const handleDeploy = async () => {
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
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.app-name {
  font-size: 18px;
  font-weight: bold;
  color: #000;
}

.content-area {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 16px;
  padding: 16px 20px;
}

.chat-section {
  display: flex;
  flex-direction: column;
  width: 50%;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  justify-content: flex-start;
}

.message-item.user {
  justify-content: flex-end;
}

.message-content {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 4px;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.5;
}

.message-item.user .message-content {
  background: #1890ff;
  color: #fff;
}

.message-item.assistant .message-content {
  background: #f0f0f0;
  color: #000;
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
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

.preview-section {
  width: 50%;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.preview-container {
  width: 100%;
  height: 100%;
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
  font-size: 16px;
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
</style>
