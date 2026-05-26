<script setup lang="ts">
import { Layout } from 'ant-design-vue'
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const { Header, Footer } = Layout
</script>

<template>
  <Layout class="basic-layout">
    <Header class="header">
      <GlobalHeader />
    </Header>
    <a-layout-content class="content">
      <RouterView />
    </a-layout-content>
    <Footer class="footer">
      <GlobalFooter />
    </Footer>
  </Layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 999;
}

.content {
  padding: 24px;
  margin: 16px auto 56px;
  width: 100%;
  max-width: 1200px;
}

.footer {
  background: #fff;
  padding: 24px 0;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }

  .footer {
    padding: 16px 0;
  }
}
</style>
