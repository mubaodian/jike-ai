<template>
  <div id="userManagePage">
    <a-card title="用户管理">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="账号">
          <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
      <a-divider />
      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-image v-if="record.userAvatar" :src="record.userAvatar" :width="100" />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
          <template v-else-if="column.dataIndex === 'userProfile'">
            <a-popover v-if="record.userProfile && record.userProfile.length > 3">
              <template #content>
                {{ record.userProfile }}
              </template>
              {{ record.userProfile.substring(0, 3) }}...
            </a-popover>
            <span v-else>{{ record.userProfile || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button danger @click="doDelete(record.id)" style="margin-right: 4px">删除</a-button>
            <a-button type="primary" @click="showModal(record)">编辑</a-button>
            <!-- 模态框组件 -->
            <a-modal
              v-model:open="open"
              title="用户编辑"
              :confirm-loading="confirmLoading"
              @ok="handleOk"
            >
              <!-- 表单组件 -->
              <a-form :model="formState" name="basic" autocomplete="off">
                <a-form-item name="userName">
                  <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
                </a-form-item>
                <a-form-item name="userAvatar">
                  <a-input v-model:value="formState.userAvatar" placeholder="请输入头像URL" />
                </a-form-item>
                <a-form-item name="userProfile">
                  <a-input v-model:value="formState.userProfile" placeholder="请输入简介" />
                </a-form-item>
                <a-form-item name="userRole">
                  <a-select v-model:value="formState.userRole" placeholder="请选择用户角色">
                    <a-select-option value="user">普通用户</a-select-option>
                    <a-select-option value="admin">管理员</a-select-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </a-modal>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { listUserVoByPage, deleteUser, updateUser } from '@/api/userController'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { onMounted, reactive, computed, ref } from 'vue'

// 表格列
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 查询条件参数
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 2,
})

// 模态框状态
const open = ref<boolean>(false)
const confirmLoading = ref<boolean>(false)
// 编辑表单初始状态
const formState = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
})
// 编辑用户信息
const showModal = (record: API.UserVO) => {
  open.value = true
  formState.id = record.id
  formState.userName = record.userName || ''
  formState.userAvatar = record.userAvatar || ''
  formState.userProfile = record.userProfile || ''
  formState.userRole = record.userRole || ''
}
// 提交编辑表单
const handleOk = async () => {
  confirmLoading.value = true
  try {
    const res = await updateUser(formState)
    if (res.data.code === 0) {
      message.success('编辑成功')
      open.value = false
      fetchData()
    } else {
      message.error('编辑失败：' + res.data.message)
    }
  } finally {
    confirmLoading.value = false
  }
}

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({ ...searchParams })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败：' + res.data.message)
  }
}

// 处理表格分页、排序、筛选等变化
const doTableChange = (page: any) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 查询用户数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除用户
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id: Number(id) })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败：' + res.data.message)
  }
}

// 初始化数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}
</style>
