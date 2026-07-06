import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppGeneratePage from '@/pages/app/AppGeneratePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppDetailPage from '@/pages/app/AppDetailPage.vue'
import ChatHistoryManagePage from '@/pages/admin/ChatHistoryManagePage.vue'
import NotFoundPage from '@/pages/NotFoundPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/admin/chatHistoryManage',
      name: '对话管理',
      component: ChatHistoryManagePage,
    },
    {
      path: '/app/generate/:appId',
      name: '应用生成',
      component: AppGeneratePage,
    },
    {
      path: '/app/detail/:appId',
      name: '应用详情',
      component: AppDetailPage,
    },
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      component: NotFoundPage,
    },
  ],
})

export default router
