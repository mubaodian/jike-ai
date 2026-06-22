<template>
  <div id="appDetailPage">
    <a-card title="应用信息" class="detail-card">
      <a-form
        ref="formRef"
        :model="formState"
        :rules="formRules"
        layout="vertical"
        @finish="handleSubmit"
      >
        <a-form-item label="应用ID" name="id">
          <a-input v-model:value="formState.id" disabled />
        </a-form-item>

        <a-form-item label="应用名称" name="appName">
          <a-input v-model:value="formState.appName" placeholder="请输入应用名称" />
        </a-form-item>

        <a-form-item label="应用描述" name="initPrompt">
          <a-textarea
            v-model:value="formState.initPrompt"
            placeholder="应用描述（仅展示，不可修改）"
            :rows="4"
            disabled
          />
        </a-form-item>

        <a-form-item label="创建时间" name="createTime">
          <a-input v-model:value="formState.createTime" disabled />
        </a-form-item>

        <a-form-item v-if="isAdmin" label="应用优先级" name="priority">
          <a-input-number v-model:value="formState.priority" placeholder="请输入优先级" />
        </a-form-item>

        <a-form-item v-if="isAdmin" label="应用封面" name="cover">
          <a-input v-model:value="formState.cover" placeholder="请输入封面URL" />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">保存</a-button>
            <a-button @click="handleBack">返回</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatTime } from '@/utils/time'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const isAdmin = ref(false)
const appOwnerId = ref<number>(0)

const formRef = ref()
const formState = reactive({
  id: '',
  appName: '',
  initPrompt: '',
  createTime: '',
  cover: '',
  priority: undefined,
})

const formRules = {
  appName: [
    { required: true, message: '请输入应用名称' },
    { min: 1, max: 100, message: '应用名称长度在1-100字符之间' },
  ],
}

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    // 直接使用字符串参数，避免精度丢失
    const appIdStr = route.params.appId as string

    // 校验：不能为空，只能由数字组成
    if (!appIdStr || !/^\d+$/.test(appIdStr)) {
      message.error('应用ID无效')
      return
    }

    const res = await getAppVoById({ id: appIdStr as unknown as number })

    if (res.data.code === 0 && res.data.data) {
      const app = res.data.data
      appOwnerId.value = app.userId || 0

      // 检查是否是管理员
      isAdmin.value = loginUserStore.loginUser.userRole === 'admin'

      // 检查权限：普通用户只能编辑自己的应用
      if (!isAdmin.value && loginUserStore.loginUser.id !== appOwnerId.value) {
        message.error('您没有权限编辑此应用')
        await router.push('/')
        return
      }

      Object.assign(formState, {
        id: app.id?.toString() || '',
        appName: app.appName || '',
        initPrompt: app.initPrompt || '',
        createTime: formatTime(app.createTime),
        cover: app.cover || '',
        priority: app.priority || 0,
      })
    } else {
      // 业务错误处理
      const errorMsg = res.data.message || '获取应用信息失败'
      message.error(errorMsg)
      console.error('获取应用信息业务错误:', res.data)
    }
  } catch (error) {
    message.error('获取应用信息出错，请检查应用是否存在')
    console.error('获取应用信息错误:', error)
  }
}

// 提交表单
const handleSubmit = async (values: any) => {
  loading.value = true

  try {
    const appId = formState.id as unknown as number

    if (isAdmin.value) {
      // 管理员可以修改名称、封面、优先级
      const updateData: any = {
        id: appId,
        appName: formState.appName,
      }

      // 只有当 cover 不为空时才添加
      if (formState.cover) {
        updateData.cover = formState.cover
      }

      // 只有当 priority 不为 undefined 和 0 时才添加
      if (formState.priority !== undefined && formState.priority !== 0) {
        updateData.priority = formState.priority
      }

      const res = await updateAppByAdmin(updateData)

      if (res.data.code === 0) {
        message.success('应用信息修改成功')
        await router.push('/')
      } else {
        message.error(res.data.message || '修改失败')
      }
    } else {
      // 普通用户只能修改名称
      const res = await updateApp({
        id: appId,
        appName: formState.appName,
      })

      if (res.data.code === 0) {
        message.success('应用信息修改成功')
        await router.push('/')
      } else {
        message.error(res.data.message || '修改失败')
      }
    }
  } catch (error) {
    message.error('修改应用信息出错')
    console.error('修改应用信息错误:', error)
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/')
}

onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
#appDetailPage {
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.detail-card {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

:deep(.ant-card-head-title) {
  font-size: 18px;
  font-weight: bold;
}
</style>
