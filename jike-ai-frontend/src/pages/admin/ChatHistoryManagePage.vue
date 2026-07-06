<template>
  <div id="chatHistoryManagePage">
    <a-card title="对话管理">
      <!-- 搜索条件 -->
      <a-form layout="inline" class="search-form" :model="searchForm" @finish="handleSearch">
        <a-form-item label="应用ID" name="appId">
          <a-input
            v-model:value="searchForm.appId"
            placeholder="请输入应用ID"
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="用户ID" name="userId">
          <a-input
            v-model:value="searchForm.userId"
            placeholder="请输入用户ID"
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="消息类型" name="messageType">
          <a-select
            v-model:value="searchForm.messageType"
            placeholder="选择消息类型"
            allow-clear
            :dropdown-match-select-width="false"
            style="width: 120px"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="ai">AI</a-select-option>
            <a-select-option value="user">用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 对话历史列表 -->
      <a-table
        :columns="columns"
        :data-source="chatHistoryList"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'messageType'">
            <a-tag :color="record.messageType === 'ai' ? 'blue' : 'green'">
              {{ record.messageType === 'ai' ? 'AI' : '用户' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'message'">
            <div class="message-cell">
              <span :class="expandedRows.has(record.id) ? '' : 'message-clamped'">
                {{ record.message || '-' }}
              </span>
              <a
                v-if="record.message && record.message.length > 80"
                class="message-toggle"
                @click="toggleRow(record.id)"
              >
                {{ expandedRows.has(record.id) ? '收起' : '展开' }}
              </a>
            </div>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm
              title="删除对话记录"
              description="确定要删除此条对话记录吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
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
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatTime } from '@/utils/time'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const expandedRows = ref(new Set<number>())

const toggleRow = (id: number | undefined) => {
  if (id === undefined) return
  if (expandedRows.value.has(id)) {
    expandedRows.value.delete(id)
  } else {
    expandedRows.value.add(id)
  }
  // 触发响应式更新
  expandedRows.value = new Set(expandedRows.value)
}

const searchForm = reactive({
  appId: '',
  userId: '',
  messageType: '',
})

const chatHistoryList = ref<API.ChatHistory[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '30', '40', '50'],
})

const columns = [
  {
    title: '记录ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    key: 'appId',
    width: 100,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 100,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 100,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    width: 400,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 80,
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

// 获取对话历史列表
const fetchChatHistoryList = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      appId: searchForm.appId ? (searchForm.appId as unknown as number) : undefined,
      userId: searchForm.userId ? (searchForm.userId as unknown as number) : undefined,
    })

    if (res.data.code === 0 && res.data.data) {
      // 客户端按消息类型过滤（后端不支持该参数时降级处理）
      let records = res.data.data.records || []
      if (searchForm.messageType) {
        records = records.filter((r) => r.messageType === searchForm.messageType)
      }
      chatHistoryList.value = records
      pagination.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    message.error('获取对话历史失败')
    console.error('获取对话历史错误:', error)
  } finally {
    loading.value = false
  }
}

// 删除对话记录（管理员接口暂无，预留）
const handleDelete = async (_id: number) => {
  message.warning('暂不支持删除操作')
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchChatHistoryList()
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchChatHistoryList()
}

// 重置搜索
const handleReset = () => {
  searchForm.appId = ''
  searchForm.userId = ''
  searchForm.messageType = ''
  pagination.current = 1
  fetchChatHistoryList()
}

onMounted(() => {
  checkAdmin()
  fetchChatHistoryList()
})
</script>

<style scoped>
#chatHistoryManagePage {
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

.message-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  word-break: break-all;
  white-space: pre-wrap;
}

.message-clamped {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-toggle {
  font-size: 12px;
  color: #1890ff;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}
</style>
