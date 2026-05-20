<script setup lang="ts">
import { ref, watch } from 'vue'
import { Menu, Button, Space } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 菜单项配置
const menuItems = ref([
  {
    key: 'home',
    label: '首页',
    path: '/',
  },
  {
    key: 'about',
    label: '关于',
    path: '/about',
  },
])

// 当前选中的菜单项
const selectedKeys = ref<string[]>(['home'])

// 根据路由路径更新菜单选中状态
const updateSelectedKeys = () => {
  const currentPath = route.path
  const menuItem = menuItems.value.find((m) => m.path === currentPath)
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
  const menuItem = menuItems.value.find((m) => m.key === item.key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

// 处理登录
const handleLogin = () => {
  console.log('跳转到登录页面')
  // TODO: 实现登录逻辑
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
          menuItems.map((item) => ({
            key: item.key,
            label: item.label,
          }))
        "
      />
    </div>

    <!-- 右侧：用户操作 -->
    <div class="header-right">
      <Space>
        <Button type="primary" @click="handleLogin">登录</Button>
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
  color: #000;
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
