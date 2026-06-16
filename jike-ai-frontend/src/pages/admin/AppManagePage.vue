<template>
  <div id="appManagePage">
    <a-card title="应用管理">
      <!-- 搜索条件 -->
      <a-form layout="inline" class="search-form" @finish="handleSearch">
        <a-form-item label="应用名称">
          <a-input v-model:value="searchForm.appName" placeholder="请输入应用名称" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 应用列表 -->
      <a-table
        :columns="columns"
        :data-source="appList"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        :scroll="{ x: 1000 }"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="goToAppDetail(record.id)">
                编辑
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
              <a-popconfirm
                title="设置为精选"
                :description="`确定将此应用设置为精选吗？优先级将设置为 99`"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleSetFeatured(record.id, record.appName)"
              >
                <a-button type="link" size="small">精选</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
} from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)

const searchForm = reactive({
  appName: '',
})

const appList = ref<API.AppVO[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '30', '40', '50'],
})

const columns = [
  {
    title: '应用ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
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
    title: '创建者',
    dataIndex: ['user', 'userName'],
    key: 'userName',
    width: 120,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 100,
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
    width: 200,
    fixed: 'right' as const,
  },
]

// 检查管理员权限
const checkAdmin = () => {
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限访问此页面')
    router.push('/')
  }
}

// 获取应用列表
const fetchAppList = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      appName: searchForm.appName || undefined,
    })

    if (res.data.code === 0 && res.data.data) {
      appList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    message.error('获取应用列表失败')
    console.error('获取应用列表错误:', error)
  } finally {
    loading.value = false
  }
}

// 删除应用
const handleDeleteApp = async (appId: number) => {
  try {
    const res = await deleteAppByAdmin({ id: appId })
    if (res.data.code === 0) {
      message.success('应用删除成功')
      await fetchAppList()
    } else {
      message.error(res.data.message || '删除失败')
    }
  } catch (error) {
    message.error('删除应用出错')
    console.error('删除应用错误:', error)
  }
}

// 设置为精选（优先级设置为99）
const handleSetFeatured = async (appId: number, appName: string) => {
  try {
    const res = await updateAppByAdmin({
      id: appId,
      priority: 99,
    })

    if (res.data.code === 0) {
      message.success('已设置为精选')
      await fetchAppList()
    } else {
      message.error(res.data.message || '设置失败')
    }
  } catch (error) {
    message.error('设置精选出错')
    console.error('设置精选错误:', error)
  }
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchAppList()
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchAppList()
}

// 重置搜索
const handleReset = () => {
  searchForm.appName = ''
  pagination.current = 1
  fetchAppList()
}

// 跳转到应用详情编辑页
const goToAppDetail = (appId: number) => {
  router.push(`/app/detail/${appId}`)
}

onMounted(() => {
  checkAdmin()
  fetchAppList()
})
</script>

<style scoped>
#appManagePage {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-form {
  margin-bottom: 16px;
}

:deep(.ant-card) {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}
</style>