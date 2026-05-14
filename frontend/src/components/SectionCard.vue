<script setup>
defineProps({
  title: String,
  // 是否带边框（默认带）；首页 hero 类容器可设为 false 走自定义样式
  bordered: { type: Boolean, default: true },
  // 内容区是否需要内边距（关一些紧凑场景，如纯表格）
  bodyPadding: { type: Boolean, default: true }
})
</script>

<template>
  <section class="section-card" :class="{ 'is-bordered': bordered }">
    <header v-if="title || $slots.extra" class="section-card__head">
      <h2 v-if="title" class="section-card__title">{{ title }}</h2>
      <div v-if="$slots.extra" class="section-card__extra">
        <slot name="extra" />
      </div>
    </header>
    <div class="section-card__body" :class="{ 'no-padding': !bodyPadding }">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.section-card {
  background: var(--app-bg-elevated);
  border-radius: var(--app-radius-lg);
  overflow: hidden;
}
.section-card.is-bordered {
  border: 1px solid var(--app-border-light);
}

.section-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--app-sp-4) var(--app-sp-5);
  border-bottom: 1px solid var(--app-border-light);
  gap: var(--app-sp-3);
}
.section-card__title {
  font-size: var(--app-fs-h3);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0;
}
.section-card__extra {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
}

.section-card__body { padding: var(--app-sp-5); }
.section-card__body.no-padding { padding: 0; }
</style>
