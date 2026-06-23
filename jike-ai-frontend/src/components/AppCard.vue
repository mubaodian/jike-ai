<template>
  <div class="app-card">
    <!-- 应用封面 -->
    <div class="card-cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
      <div v-else class="cover-placeholder">
        <span>无封面</span>
      </div>
      <!-- 悬停按钮覆盖层 -->
      <div class="card-overlay">
        <a-space>
          <a-button type="primary" size="small" @click.stop="emit('view-chat', app.id!)">
            查看对话
          </a-button>
          <a-button v-if="app.deployKey" size="small" @click.stop="emit('view-work', app.deployKey!)">
            查看作品
          </a-button>
        </a-space>
      </div>
    </div>
    <!-- 卡片底部信息 -->
    <div class="card-footer">
      <div class="user-avatar">
        <a-avatar v-if="app.user?.userAvatar" :src="app.user.userAvatar" :size="40" />
        <a-avatar v-else :size="40">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info">
        <div class="app-name">{{ app.appName }}</div>
        <div class="user-name">{{ app.user?.userName }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  app: API.AppVO
}>()

const emit = defineEmits<{
  (e: 'view-chat', appId: number): void
  (e: 'view-work', deployKey: string): void
}>()
</script>

<style scoped>
.app-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(60, 100, 180, 0.08);
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.app-card:hover {
  box-shadow: 0 8px 28px rgba(60, 100, 180, 0.15);
  transform: translateY(-3px);
}

.app-card:hover .card-overlay {
  opacity: 1;
}

.card-cover {
  width: 100%;
  height: 150px;
  overflow: hidden;
  background: #eef2f8;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dce8f7 0%, #e8edf8 100%);
  color: #aabbd4;
  font-size: 13px;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(20, 40, 80, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.card-footer {
  padding: 12px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-grow: 1;
}

.user-avatar {
  flex-shrink: 0;
}

.app-info {
  flex-grow: 1;
  min-width: 0;
}

.app-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a2a4a;
  margin-bottom: 3px;
  word-break: break-word;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-name {
  font-size: 12px;
  color: #9aabcc;
  word-break: break-word;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
