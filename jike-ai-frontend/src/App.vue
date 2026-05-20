<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { health } from '@/api/healthController'

health()
  .then((response) => {
    console.log('后端健康检查成功:', response)
  })
  .catch((error) => {
    console.error('后端健康检查失败:', error)
  })
</script>

<template>
  <BasicLayout />
</template>

<style scoped></style>
