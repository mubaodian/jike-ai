<script setup lang="ts">
import { ref, watch } from 'vue'
import { Menu, message, Space } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import { UserOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

// 菜单项配置
const originItems
 = ref([
  {
    key: 'home',
    label: '首页',
    path: '/',
  },
  {
    key: 'appManage',
    label: '应用管理',
    path: '/admin/appManage',
  },
  {
    key: 'chatHistoryManage',
    label: '对话管理',
    path: '/admin/chatHistoryManage',
  },
  {
    key: 'userManage',
    label: '用户管理',
    path: '/admin/userManage',
  },
])

// 当前选中的菜单项
const selectedKeys = ref<string[]>(['home'])

// 根据路由路径更新菜单选中状态
const updateSelectedKeys = () => {
  const currentPath = route.path
  const menuItem = originItems
  .value.find((m) => m.path === currentPath)
  if (menuItem) {
    selectedKeys.value = [menuItem.key]
  }
}

// 监听路由变化
watch(() => route.path, updateSelectedKeys)

// 初始化时更新菜单选中状态
updateSelectedKeys()

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (item) => {
  const menuItem = originItems
  .value.find((m) => m.key === item.key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

// 过滤菜单项（权限校验）
const filterMenuItems = () => {
  return originItems.value.filter((item) => {
    if (item.path.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if(!loginUser?.id || loginUser.userRole !== 'admin'){
        return false
      }
    }
    return true
  })
}

// 用户退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    // 清除登录用户信息
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  }else{
    message.error('退出登录失败：' + res.data.message)
  }
}
</script>

<template>
  <div class="global-header">
    <!-- 左侧：Logo 和网站标题 -->
    <div class="header-left">
      <img src="@/assets/logo.png" alt="Logo" class="logo" />
      <span class="site-title">即刻AI</span>
    </div>

    <!-- 中间：菜单 -->
    <div class="header-menu">
      <Menu
        :selected-keys="selectedKeys"
        mode="horizontal"
        @click="handleMenuClick"
        :items="
          filterMenuItems()
          .map((item) => ({
            key: item.key,
            label: item.label,
          }))
        "
      />
    </div>

    <!-- 右侧：用户操作 -->
    <div class="header-right">
      <Space>
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <a-space>
              <a-avatar
                shape="circle"
                v-if="loginUserStore.loginUser.userAvatar"
                :src="loginUserStore.loginUser.userAvatar"
              ></a-avatar>
              <a-avatar shape="circle" v-else>
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
              <span>{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </Space>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.logo {
  height: 40px;
  width: auto;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #4096ff;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  margin: 0 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 120px;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
    height: 56px;
  }

  .header-left {
    min-width: auto;
    gap: 8px;
  }

  .logo {
    height: 32px;
  }

  .site-title {
    font-size: 16px;
  }

  .header-menu {
    margin: 0 12px;
  }

  .header-right {
    min-width: auto;
    gap: 8px;
  }
}

@media (max-width: 576px) {
  .global-header {
    flex-wrap: wrap;
    height: auto;
    padding: 8px 12px;
  }

  .header-menu {
    width: 100%;
    margin: 8px 0;
    order: 3;
  }

  .site-title {
    display: none;
  }
}
</style>
