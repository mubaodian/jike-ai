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
  background: transparent;
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
  padding: 0;
  margin: 0;
  width: 100%;
  background: transparent;
  flex: 1;
}

.footer {
  background: transparent;
  padding: 24px 0;
  text-align: center;
  border-top: 1px solid rgba(120, 100, 200, 0.1);
}

/* 清除 Ant Design Layout 默认背景色 */
:deep(.ant-layout) {
  background: transparent;
}

:deep(.ant-layout-content) {
  background: transparent;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer {
    padding: 16px 0;
  }
}
</style>
