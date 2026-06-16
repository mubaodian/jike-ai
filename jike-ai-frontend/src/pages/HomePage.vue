<template>
  <div id="homePage">
    <div class="header-section">
      <h1 class="title">即刻 AI 应用平台</h1>
      <p class="subtitle">只需一句话，即刻生成完整应用</p>
    </div>

    <!-- 用户提示词输入框 -->
    <div class="input-section">
      <a-input-search
        v-model:value="prompt"
        size="large"
        placeholder="请描述您想要的应用功能，例如：生成一个待办事项列表应用"
        allow-clear
        @search="handleCreateApp"
        @keyup.enter="handleCreateApp"
      >
        <template #enterButton>
          <a-button type="primary" :loading="loading">生成应用</a-button>
        </template>
      </a-input-search>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </div>

    <!-- 我的应用分页列表 -->
    <div class="app-section">
      <h2>我的应用</h2>
      <a-table
        :columns="appColumns"
        :data-source="myAppList"
        :pagination="myAppPagination"
        :loading="myAppLoading"
        @change="handleMyAppTableChange"
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="goToAppDetail(record.id)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="goToAppGenerate(record.id)">
                生成
              </a-button>
              <a-popconfirm
                title="删除应用"
                description="确定要删除此应用吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDeleteApp(record.id)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 精选应用分页列表 -->
    <div class="app-section">
      <h2>精选应用</h2>
      <a-table
        :columns="appColumns"
        :data-source="goodAppList"
        :pagination="goodAppPagination"
        :loading="goodAppLoading"
        @change="handleGoodAppTableChange"
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="goToAppGenerate(record.id)">
                生成
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage, deleteApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const prompt = ref('')
const loading = ref(false)
const errorMessage = ref('')

const myAppLoading = ref(false)
const goodAppLoading = ref(false)

const myAppList = ref<API.AppVO[]>([])
const goodAppList = ref<API.AppVO[]>([])

const myAppPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: false,
  showQuickJumper: true,
})

const goodAppPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: false,
  showQuickJumper: true,
})

const appColumns = [
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    width: 150,
  },
  {
    title: '应用描述',
    dataIndex: 'initPrompt',
    key: 'initPrompt',
    width: 250,
    ellipsis: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right' as const,
  },
]

// 创建应用
const handleCreateApp = async () => {
  if (!prompt.value.trim()) {
    errorMessage.value = '请输入应用描述'
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await addApp({ initPrompt: prompt.value })
    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功，跳转到生成页面')
      const appId = String(res.data.data)
      console.log('应用ID:', appId)
      prompt.value = ''
      await router.push(`/app/generate/${appId}`)
    } else {
      errorMessage.value = res.data.message || '应用创建失败'
    }
  } catch (error) {
    errorMessage.value = '应用创建出错，请稍后重试'
    console.error('创建应用错误:', error)
  } finally {
    loading.value = false
  }
}

// 获取我的应用列表
const fetchMyAppList = async () => {
  myAppLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppPagination.current,
      pageSize: myAppPagination.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      myAppList.value = res.data.data.records || []
      myAppPagination.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    message.error('获取应用列表失败')
    console.error('获取应用列表错误:', error)
  } finally {
    myAppLoading.value = false
  }
}

// 获取精选应用列表
const fetchGoodAppList = async () => {
  goodAppLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: goodAppPagination.current,
      pageSize: goodAppPagination.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      goodAppList.value = res.data.data.records || []
      goodAppPagination.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    message.error('获取精选应用列表失败')
    console.error('获取精选应用列表错误:', error)
  } finally {
    goodAppLoading.value = false
  }
}

// 删除应用
const handleDeleteApp = async (appId: number) => {
  try {
    const res = await deleteApp({ id: appId })
    if (res.data.code === 0) {
      message.success('应用删除成功')
      await fetchMyAppList()
    } else {
      message.error(res.data.message || '删除失败')
    }
  } catch (error) {
    message.error('删除应用出错')
    console.error('删除应用错误:', error)
  }
}

// 表格分页变化
const handleMyAppTableChange = (pagination: any) => {
  myAppPagination.current = pagination.current
  myAppPagination.pageSize = pagination.pageSize
  fetchMyAppList()
}

const handleGoodAppTableChange = (pagination: any) => {
  goodAppPagination.current = pagination.current
  goodAppPagination.pageSize = pagination.pageSize
  fetchGoodAppList()
}

// 跳转到应用生成页
const goToAppGenerate = (appId: number) => {
  router.push(`/app/generate/${appId}`)
}

// 跳转到应用详情页
const goToAppDetail = (appId: number) => {
  router.push(`/app/detail/${appId}`)
}

// 页面加载时获取列表
onMounted(() => {
  fetchMyAppList()
  fetchGoodAppList()
})
</script>

<style scoped>
#homePage {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #000;
}

.subtitle {
  font-size: 16px;
  color: #999;
  margin: 0;
}

.input-section {
  margin-bottom: 50px;
}

.error-message {
  color: #ff4d4f;
  margin-top: 8px;
  font-size: 14px;
}

.app-section {
  margin-bottom: 50px;
}

.app-section h2 {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #000;
}

:deep(.ant-table) {
  border-radius: 4px;
}
</style>
