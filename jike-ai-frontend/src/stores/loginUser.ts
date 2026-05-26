import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })
  // 获取当前登录用户信息
  async function fetchLoginUser() {
    const res = await getCurrentUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  // 更新登录用户信息
  function setLoginUser(newLoginuser: any) {
    loginUser.value = newLoginuser
  }
  return { loginUser, fetchLoginUser, setLoginUser }
})
