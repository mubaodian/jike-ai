<template>
  <div id="notFoundPage">
    <div class="error-container">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-description">抱歉，您访问的页面不存在</p>
      <a-button type="primary" size="large" @click="handleBackHome">
        返回首页
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const handleBackHome = () => {
  router.push('/')
}
</script>

<style scoped>
#notFoundPage {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-container {
  text-align: center;
  color: white;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.error-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.error-description {
  font-size: 16px;
  margin-bottom: 32px;
  opacity: 0.9;
}
</style>