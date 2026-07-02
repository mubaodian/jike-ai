<template>
  <div id="appManagePage">
    <a-card title="应用管理">
      <!-- 搜索条件 -->
      <a-form layout="inline" class="search-form" :model="searchForm" @finish="handleSearch">
        <a-form-item label="应用名称" name="appName">
          <a-input v-model:value="searchForm.appName" placeholder="请输入应用名称" />
        </a-form-item>
        <a-form-item label="创作者ID" name="userId">
          <a-input
            v-model:value="searchForm.userId"
            placeholder="请输入创作者ID"
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="生成类型" name="codeGenType">
          <a-select
            v-model:value="searchForm.codeGenType"
            placeholder="选择生成类型"
            allow-clear
            :dropdown-match-select-width="false"
            style="width: 120px"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option
              v-for="option in CODE_GEN_TYPE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
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
        :scroll="{ x: 1200 }"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'cover'">
            <div v-if="record.cover" class="cover-preview">
              <img :src="record.cover" :alt="record.appName" />
            </div>
            <div v-else class="cover-placeholder">
              <RobotOutlined class="cover-placeholder-icon" />
            </div>
          </template>
          <template v-else-if="column.key === 'userName'">
            {{ record.user?.userName || '-' }}
          </template>
          <template v-else-if="column.key === 'codeGenType'">
            <a-tag color="blue">{{ record.codeGenType || '-' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'priority'">
            <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
            <a-tag v-else color="default">普通</a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
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
                v-if="record.priority !== 99"
                title="设置为精选"
                description="确定将此应用设置为精选吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleSetFeatured(record.id)"
              >
                <a-button type="link" size="small">精选</a-button>
              </a-popconfirm>
              <a-popconfirm
                v-else
                title="取消精选"
                description="确定取消此应用的精选状态吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleCancelFeatured(record.id)"
              >
                <a-button type="link" danger size="small" style="color: #faad14">取消精选</a-button>
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
import { RobotOutlined } from '@ant-design/icons-vue'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
} from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatTime } from '@/utils/time'
import { CODE_GEN_TYPE_OPTIONS } from '@/constants/codeGenType'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)

const searchForm = reactive({
  appName: '',
  userId: '',
  codeGenType: '',
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
    title: '封面',
    dataIndex: 'cover',
    key: 'cover',
    width: 100,
  },
  {
    title: '应用描述',
    dataIndex: 'initPrompt',
    key: 'initPrompt',
    width: 200,
    ellipsis: true,
  },
  {
    title: '创作者',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 120,
  },
  {
    title: '精选状态',
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
    width: 280,
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
      userId: searchForm.userId ? searchForm.userId as unknown as number : undefined,
      codeGenType: searchForm.codeGenType || undefined,
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
const handleSetFeatured = async (appId: number) => {
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

// 取消精选（优先级设置为0）
const handleCancelFeatured = async (appId: number) => {
  try {
    const res = await updateAppByAdmin({
      id: appId,
      priority: 0,
    })

    if (res.data.code === 0) {
      message.success('已取消精选')
      await fetchAppList()
    } else {
      message.error(res.data.message || '取消失败')
    }
  } catch (error) {
    message.error('取消精选出错')
    console.error('取消精选错误:', error)
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
  searchForm.userId = ''
  searchForm.codeGenType = ''
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
  max-width: 1600px;
  margin: 0 auto;
}

.search-form {
  margin-bottom: 16px;
}

:deep(.ant-card) {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.cover-preview {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg,#cfd9df 0%, #e2ebf0 100%);
  border-radius: 4px;
}

.cover-placeholder-icon {
  font-size: 22px;
  color: rgba(255, 255, 255, 0.85);
}
</style>
