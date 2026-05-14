<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, required: true },
  rest: { type: Number, required: true },
  showText: { type: Boolean, default: true }
})

const percent = computed(() => {
  if (props.total <= 0) return 0
  return Math.max(0, Math.min(100, (props.rest / props.total) * 100))
})

const level = computed(() => {
  if (props.rest <= 0) return 'full'
  if (percent.value <= 30) return 'tight'
  return 'enough'
})

const levelText = computed(() => ({
  enough: '充足',
  tight: '紧张',
  full: '约满'
}[level.value]))
</script>

<template>
  <div class="quota-bar" :class="`is-${level}`">
    <div class="quota-bar__track">
      <div class="quota-bar__fill" :style="{ width: percent + '%' }" />
    </div>
    <div v-if="showText" class="quota-bar__text">
      <span class="quota-bar__count">{{ rest }} / {{ total }}</span>
      <span class="quota-bar__label">{{ levelText }}</span>
    </div>
  </div>
</template>

<style scoped>
.quota-bar { display: flex; flex-direction: column; gap: 6px; }

.quota-bar__track {
  height: 6px;
  background: var(--app-bg-subtle);
  border-radius: var(--app-radius-full);
  overflow: hidden;
}
.quota-bar__fill {
  height: 100%;
  border-radius: var(--app-radius-full);
  transition: width var(--app-transition-base);
}

.quota-bar__text {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--app-fs-caption);
}
.quota-bar__count {
  color: var(--app-text-2);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}
.quota-bar__label { font-weight: 500; }

.is-enough .quota-bar__fill  { background: var(--app-accent-500); }
.is-enough .quota-bar__label { color: var(--app-success-text); }

.is-tight .quota-bar__fill  { background: var(--app-warning); }
.is-tight .quota-bar__label { color: var(--app-warning-text); }

.is-full .quota-bar__fill  { background: var(--app-text-4); }
.is-full .quota-bar__label { color: var(--app-text-3); }
</style>
