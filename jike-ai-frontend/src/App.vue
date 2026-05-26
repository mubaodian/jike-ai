<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const loginUserStore = useLoginUserStore()
// 初始化时获取登录用户信息
loginUserStore.fetchLoginUser()
</script>

<template>
  <BasicLayout />
</template>

<style scoped></style>
