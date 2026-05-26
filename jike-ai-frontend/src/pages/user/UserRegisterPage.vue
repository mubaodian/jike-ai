<template>
  <div id="userRegisterPage">
    <h2 class="title">即刻 AI 应用平台 - 用户注册</h2>
    <div class="desc">只需一句话，即刻生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        name="userAccount"
        :rules="[
          { required: true, message: '请输入用户名!' },
          { min: 4, message: '用户账号长度不能小于4位!' },
        ]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入用户账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码!' },
          { min: 8, message: '用户密码长度不能小于8位!' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入用户密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码!' },
          {
            validator: validatePasswordMatch,
            trigger: 'change',
          },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认用户密码" />
      </a-form-item>

      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">点击登录</RouterLink>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validatePasswordMatch = (_rule: any, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请确认密码!'))
  }
  if (value !== formState.userPassword) {
    return Promise.reject(new Error('两次输入密码不一致!'))
  }
  return Promise.resolve()
}

const handleSubmit = async (values: API.UserRegisterRequest) => {
  // 注册
  const res = await userRegister(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败：' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 480px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  text-align: right;
  color: #bbb;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
