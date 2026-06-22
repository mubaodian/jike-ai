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

    <!-- 我的应用卡片列表 -->
    <div class="app-section">
      <h2>我的应用</h2>
      <a-spin :spinning="myAppLoading">
        <div class="app-cards-container">
          <div v-if="myAppList.length === 0" class="empty-state">
            <p>暂无应用，创建一个新应用开始吧</p>
          </div>
          <div v-else class="app-cards">
            <div v-for="app in myAppList" :key="app.id" class="app-card">
              <!-- 应用封面 -->
              <div class="card-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
                <div v-else class="cover-placeholder">
                  <span>无封面</span>
                </div>
                <!-- 悬停按钮覆盖层 -->
                <div class="card-overlay">
                  <a-space>
                    <a-button type="primary" size="small" @click="goToAppGenerate(app.id!)">
                      查看对话
                    </a-button>
                    <a-button
                      v-if="app.deployKey"
                      size="small"
                      @click="openViewWorkPage(app.deployKey)"
                    >
                      查看作品
                    </a-button>
                  </a-space>
                </div>
              </div>
              <!-- 卡片底部信息 -->
              <div class="card-footer">
                <!-- 左侧：用户头像 -->
                <div class="user-avatar">
                  <a-avatar v-if="app.user?.userAvatar" :src="app.user.userAvatar" :size="40" />
                  <a-avatar v-else :size="40">
                    {{ app.user?.userName?.charAt(0) || 'U' }}
                  </a-avatar>
                </div>
                <!-- 右侧：应用信息 -->
                <div class="app-info">
                  <div class="app-name">{{ app.appName }}</div>
                  <div class="user-name">{{ app.user?.userName }}</div>
                </div>
              </div>
              <!-- 卡片操作 -->
              <div class="card-actions">
                <a-popconfirm
                  title="删除应用"
                  description="确定要删除此应用吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteApp(app.id!)"
                >
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
      <!-- 分页 -->
      <div class="pagination-container" v-if="myAppList.length > 0">
        <a-pagination
          :current="myAppPagination.current"
          :page-size="myAppPagination.pageSize"
          :total="myAppPagination.total"
          :show-quick-jumper="true"
          :show-size-changer="false"
          @change="handleMyAppPageChange"
        />
      </div>
    </div>

    <!-- 精选应用卡片列表 -->
    <div class="app-section">
      <h2>精选应用</h2>
      <a-spin :spinning="goodAppLoading">
        <div class="app-cards-container">
          <div v-if="goodAppList.length === 0" class="empty-state">
            <p>暂无精选应用</p>
          </div>
          <div v-else class="app-cards">
            <div v-for="app in goodAppList" :key="app.id" class="app-card">
              <!-- 应用封面 -->
              <div class="card-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
                <div v-else class="cover-placeholder">
                  <span>无封面</span>
                </div>
                <!-- 悬停按钮覆盖层 -->
                <div class="card-overlay">
                  <a-space>
                    <a-button type="primary" size="small" @click="goToAppGenerate(app.id!)">
                      查看对话
                    </a-button>
                    <a-button
                      v-if="app.deployKey"
                      size="small"
                      @click="openViewWorkPage(app.deployKey)"
                    >
                      查看作品
                    </a-button>
                  </a-space>
                </div>
              </div>
              <!-- 卡片底部信息 -->
              <div class="card-footer">
                <!-- 左侧：用户头像 -->
                <div class="user-avatar">
                  <a-avatar v-if="app.user?.userAvatar" :src="app.user.userAvatar" :size="40" />
                  <a-avatar v-else :size="40">
                    {{ app.user?.userName?.charAt(0) || 'U' }}
                  </a-avatar>
                </div>
                <!-- 右侧：应用信息 -->
                <div class="app-info">
                  <div class="app-name">{{ app.appName }}</div>
                  <div class="user-name">{{ app.user?.userName }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
      <!-- 分页 -->
      <div class="pagination-container" v-if="goodAppList.length > 0">
        <a-pagination
          :current="goodAppPagination.current"
          :page-size="goodAppPagination.pageSize"
          :total="goodAppPagination.total"
          :show-quick-jumper="true"
          :show-size-changer="false"
          @change="handleGoodAppPageChange"
        />
      </div>
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

// 创建应用
const handleCreateApp = async () => {
  // 防止重复提交：如果已在加载中，直接返回
  if (loading.value) {
    return
  }

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
      sortField: 'createTime',
      sortOrder: 'desc',
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
      sortField: 'createTime',
      sortOrder: 'desc',
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
const handleMyAppPageChange = (page: number) => {
  myAppPagination.current = page
  fetchMyAppList()
}

const handleGoodAppPageChange = (page: number) => {
  goodAppPagination.current = page
  fetchGoodAppList()
}

// 跳转到应用生成页
const goToAppGenerate = (appId: number) => {
  router.push(`/app/generate/${appId}?view=1`)
}

// 跳转到应用详情页
const goToAppDetail = (appId: number) => {
  router.push(`/app/detail/${appId}`)
}

// 打开查看作品页面
const openViewWorkPage = (deployKey: string) => {
  window.open(`http://localhost/${deployKey}`, '_blank')
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

.app-cards-container {
  margin-bottom: 24px;
}

.app-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.app-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.app-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.app-card:hover .card-overlay {
  opacity: 1;
}

.card-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #f5f5f5;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  color: #999;
  font-size: 14px;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card-footer {
  padding: 12px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-grow: 1;
}

.user-avatar {
  flex-shrink: 0;
}

.app-info {
  flex-grow: 1;
  min-width: 0;
}

.app-name {
  font-size: 14px;
  font-weight: 500;
  color: #000;
  margin-bottom: 4px;
  word-break: break-word;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-name {
  font-size: 12px;
  color: #999;
  word-break: break-word;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  padding: 8px 12px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}
</style>
