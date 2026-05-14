<script setup>
defineProps({
  type: {
    type: String,
    default: 'default'
    // default | primary | success | warning | danger | info
  },
  dot: Boolean,
  size: {
    type: String,
    default: 'normal'
    // small | normal | large
  }
})
</script>

<template>
  <span class="status-tag" :class="[`is-${type}`, `is-${size}`]">
    <span v-if="dot" class="status-tag__dot" />
    <slot />
  </span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 var(--app-sp-2);
  height: 22px;
  border-radius: var(--app-radius-sm);
  font-size: var(--app-fs-caption);
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
}
.is-small  { height: 18px; font-size: var(--app-fs-tiny); padding: 0 6px; }
.is-large  { height: 26px; font-size: var(--app-fs-body); padding: 0 var(--app-sp-3); }

.status-tag__dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: currentColor;
  flex-shrink: 0;
}

.is-default { background: var(--app-bg-subtle); color: var(--app-text-3); }
.is-primary { background: var(--app-brand-50);   color: var(--app-brand-700); }
.is-success { background: var(--app-success-bg); color: var(--app-success-text); }
.is-warning { background: var(--app-warning-bg); color: var(--app-warning-text); }
.is-danger  { background: var(--app-danger-bg);  color: var(--app-danger-text); }
.is-info    { background: var(--app-info-bg);    color: var(--app-info-text); }
</style>
