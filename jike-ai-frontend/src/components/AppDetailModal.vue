<template>
  <a-modal
    v-model:visible="visibleModel"
    title="应用详情"
    :footer="null"
    width="400px"
  >
    <div class="app-detail-content">
      <!-- 应用基础信息 -->
      <div class="detail-section">
        <div class="detail-item">
          <span class="detail-label">创建者</span>
          <div class="creator-info">
            <a-avatar shape="circle" v-if="appUserInfo?.userAvatar" :src="appUserInfo.userAvatar" :size="32" />
            <a-avatar shape="circle" v-else :size="32">
              {{ appUserInfo?.userName?.charAt(0) || 'U' }}
            </a-avatar>
            <span class="creator-name">{{ appUserInfo?.userName }}</span>
          </div>
        </div>
        <div class="detail-item">
          <span class="detail-label">创建时间</span>
          <span class="detail-value">{{ createTime }}</span>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="isOwnApp || isAdmin" class="action-section">
        <a-space>
          <a-button type="primary" @click="emit('edit')">修改</a-button>
          <a-popconfirm
            title="删除应用"
            description="确定要删除此应用吗？删除后无法恢复。"
            ok-text="确定"
            cancel-text="取消"
            @confirm="emit('delete')"
          >
            <a-button danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  visible: boolean
  appUserInfo?: API.UserVO
  createTime?: string
  isOwnApp: boolean
  isAdmin: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}>()

const visibleModel = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
})
</script>

<style scoped>
.app-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.detail-value {
  font-size: 14px;
  color: #000;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.creator-name {
  font-size: 14px;
  color: #000;
}

.action-section {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
