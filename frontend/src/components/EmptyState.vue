<script setup>
defineProps({
  title: { type: String, default: '暂无数据' },
  description: String,
  // 可选：自定义图标，传 inline SVG 或文字均可（用默认 slot 也行）
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <slot name="icon">
        <!-- 默认是一个简洁的"空文件夹"图标 -->
        <svg width="56" height="56" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M8 18a2 2 0 0 1 2-2h14l4 4h22a2 2 0 0 1 2 2v24a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2z" />
          <path d="M8 26h48" opacity="0.5" />
        </svg>
      </slot>
    </div>
    <h3 class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--app-sp-12) var(--app-sp-6);
  color: var(--app-text-3);
}
.empty-state__icon {
  color: var(--app-border-strong);
  margin-bottom: var(--app-sp-4);
}
.empty-state__title {
  font-size: var(--app-fs-body);
  font-weight: 500;
  color: var(--app-text-2);
  margin: 0 0 var(--app-sp-2);
}
.empty-state__desc {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin: 0;
  text-align: center;
  max-width: 320px;
}
.empty-state__action { margin-top: var(--app-sp-5); }
</style>
