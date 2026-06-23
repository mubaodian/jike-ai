<template>
  <div id="homePage">
    <!-- 内容容器 -->
    <div class="home-content">
      <div class="header-section">
        <h1 class="title">即刻 AI 应用平台</h1>
        <p class="subtitle">一句话轻松创建网站应用</p>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <div class="input-wrapper">
          <a-input
            v-model:value="prompt"
            size="large"
            placeholder="帮我创建个人博客网站"
            allow-clear
            @keyup.enter="handleCreateApp"
          />
          <a-button
            type="primary"
            :loading="loading"
            size="large"
            @click="handleCreateApp"
            class="generate-btn"
          >
            生成应用
          </a-button>
        </div>
        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      </div>

      <!-- 快捷提示词示例 -->
      <div class="quick-prompts">
        <div class="quick-prompts-list">
          <a-button
            v-for="(example, index) in quickExamples"
            :key="index"
            type="default"
            class="quick-prompt-btn"
            @click="selectQuickExample(example.prompt)"
          >
            {{ example.title }}
          </a-button>
        </div>
      </div>
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
            <AppCard
              v-for="app in myAppList"
              :key="app.id"
              :app="app"
              @view-chat="goToAppGenerate"
              @view-work="openViewWorkPage"
            />
          </div>
        </div>
      </a-spin>
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
            <AppCard
              v-for="app in goodAppList"
              :key="app.id"
              :app="app"
              @view-chat="goToAppGenerate"
              @view-work="openViewWorkPage"
            />
          </div>
        </div>
      </a-spin>
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
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getDeployUrl } from '@/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const prompt = ref('')
const loading = ref(false)
const errorMessage = ref('')

// 快捷提示词示例
const quickExamples = ref([
  {
    title: '个人博客网站',
    prompt:
      '创建一个个人博客网站，需要展示文章列表、详情页、分类功能、时间归档、搜索功能、评论区、关于我页面等。使用现代化的设计风格，深色主题。左侧导航栏显示分类，右侧主区域展示文章。支持Markdown格式的文章展示和代码高亮。',
  },
  {
    title: '产品展示页',
    prompt:
      '设计一个SaaS产品展示页面，包括顶部导航栏、英雄区域（大标题、副标题、CTA按钮）、产品特性部分、定价方案对比表、用户评价/案例、常见问题FAQ、底部联系方式等。配色方案为专业蓝色和白色组合，要求响应式设计在手机、平板、桌面上都表现良好。',
  },
  {
    title: '电商店铺首页',
    prompt:
      '建立一个现代电商平台首页，需要包含顶部搜索栏和导航、轮播图展示、热销产品网格、品牌介绍、优惠活动、新品上市、用户评价、底部友情链接和联系方式。产品卡片显示图片、价格、评分、购物车按钮。整体设计简洁专业，配色为黑白灰搭配亮色强调。',
  },
  {
    title: '在线教育平台',
    prompt:
      '开发一个在线教育平台首页，包括课程搜索、热门课程卡片、讲师介绍、学习路径展示、学生学习成果展示、平台优势说明、用户评价、常见问题、新闻资讯、底部社交媒体链接等。设计应突出学习氛围，使用蓝绿色系搭配，确保易于导航和课程查找。',
  },
])

const selectQuickExample = (examplePrompt: string) => {
  prompt.value = examplePrompt
}

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

// 打开查看作品页面
const openViewWorkPage = (deployKey: string) => {
  window.open(getDeployUrl(deployKey), '_blank')
}

// 页面加载时获取列表
onMounted(() => {
  fetchMyAppList()
  fetchGoodAppList()
})
</script>

<style scoped>
#homePage {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(-20deg, #e9defa 0%, #fbfcdb 100%);
  background-size: 100% 300%;
  animation: bgShift 8s ease-in-out infinite;
}

#homePage::before {
  content: '';
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(rgba(120, 100, 200, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(120, 100, 200, 0.12) 1px, transparent 1px);
  background-size: 48px 48px;
  -webkit-mask-image: linear-gradient(135deg, rgba(0,0,0,1) 0%, rgba(0,0,0,0.6) 40%, rgba(0,0,0,0) 75%);
  mask-image: linear-gradient(135deg, rgba(0,0,0,1) 0%, rgba(0,0,0,0.6) 40%, rgba(0,0,0,0) 75%);
  animation: gridPulse 8s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes gridPulse {
  0%   { opacity: 0.5; }
  50%  { opacity: 1; }
  100% { opacity: 0.5; }
}

#homePage > * {
  position: relative;
  z-index: 1;
}

@keyframes bgShift {
  0%   { background-position: 0% 0%; }
  50%  { background-position: 0% 100%; }
  100% { background-position: 0% 0%; }
}

/* 内容容器 */
.home-content {
  position: relative;
  padding: 64px 40px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #1a2a4a;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 16px;
  color: #7a8fad;
  margin: 0;
  font-weight: 400;
}

.input-section {
  margin-bottom: 24px;
}

.input-wrapper {
  display: flex;
  align-items: stretch;
  gap: 0;
  margin-bottom: 16px;
  max-width: 680px;
  margin-left: auto;
  margin-right: auto;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 24px rgba(60, 100, 180, 0.1);
  overflow: hidden;
}

:deep(.input-wrapper .ant-input-affix-wrapper),
:deep(.input-wrapper .ant-input) {
  flex: 1;
  border: none !important;
  background: transparent !important;
  border-radius: 0 !important;
  font-size: 15px;
  box-shadow: none !important;
  outline: none !important;
  height: auto !important;
}

:deep(.input-wrapper .ant-input-affix-wrapper:focus),
:deep(.input-wrapper .ant-input:focus),
:deep(.input-wrapper .ant-input-affix-wrapper-focused) {
  box-shadow: none !important;
}

.generate-btn {
  border-radius: 0 10px 10px 0 !important;
  padding: 0 28px !important;
  height: auto !important;
  min-height: 46px;
  align-self: stretch;
  font-weight: 600;
  font-size: 14px;
  background: #3b6fd4 !important;
  border: none !important;
  flex-shrink: 0;
  transition: background 0.2s ease;
}

.generate-btn:hover {
  background: #2d5bbf !important;
}

.error-message {
  color: #ff4d4f;
  margin: 8px 0 0 0;
  font-size: 14px;
  text-align: center;
}

/* 快捷提示词示例 */
.quick-prompts {
  margin-bottom: 48px;
}

.quick-prompts-title {
  text-align: center;
  font-size: 13px;
  color: #9aabcc;
  margin-bottom: 12px;
  font-weight: 400;
}

.quick-prompts-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  max-width: 800px;
  margin: 0 auto;
}

:deep(.quick-prompt-btn) {
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid rgba(100, 140, 210, 0.2) !important;
  border-radius: 20px !important;
  font-size: 13px !important;
  padding: 4px 16px !important;
  height: 32px !important;
  color: #4a6a9a !important;
  transition: all 0.2s ease;
  box-shadow: none !important;
}

:deep(.quick-prompt-btn:hover) {
  background: #fff !important;
  border-color: #3b6fd4 !important;
  color: #3b6fd4 !important;
}

/* 应用区域 - 无背景框，直接铺在页面上 */
.app-section {
  max-width: 1200px;
  margin: 0 auto 48px;
  padding: 0 40px;
}

.app-section h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #1a2a4a;
}

.app-cards-container {
  margin-bottom: 24px;
}

.app-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #9aabcc;
}
</style>
