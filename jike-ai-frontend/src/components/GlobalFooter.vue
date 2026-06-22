<script setup lang="ts">
import { ref } from 'vue'

const currentYear = ref(new Date().getFullYear())
</script>

<template>
  <div class="global-footer">
    <p class="footer-text">
      即刻AI生成应用平台 by
      <a href="https://github.com/mubaodian" target="_blank" rel="noopener noreferrer">
        木宝典
      </a>
    </p>
    <p class="footer-copyright">© {{ currentYear }} All Rights Reserved</p>
  </div>
</template>

<style scoped>
.global-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: transparent;
}

.footer-text {
  margin: 0;
  font-size: 14px;
  color: rgba(60, 40, 120, 0.55);
}

.footer-text a {
  color: #7b6fcf;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-text a:hover {
  color: #5b4fbf;
}

.footer-copyright {
  margin: 0;
  font-size: 12px;
  color: rgba(60, 40, 120, 0.35);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-footer {
    gap: 6px;
  }

  .footer-text {
    font-size: 13px;
  }

  .footer-copyright {
    font-size: 11px;
  }
}
</style>
