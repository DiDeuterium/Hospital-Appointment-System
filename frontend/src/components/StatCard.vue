<script setup>
import AppIcon from './AppIcon.vue'

defineProps({
  label: String,
  value: [String, Number],
  unit: String,
  icon: String,
  // 图标背景配色，默认与主色一致，可选 success/warning/danger/info
  tone: { type: String, default: 'brand' }
})
</script>

<template>
  <div class="stat-card">
    <div class="stat-card__main">
      <div class="stat-card__label">{{ label }}</div>
      <div class="stat-card__value">
        {{ value }}<span v-if="unit" class="stat-card__unit">{{ unit }}</span>
      </div>
      <div v-if="$slots.trend" class="stat-card__trend"><slot name="trend" /></div>
    </div>
    <div v-if="icon || $slots.icon" class="stat-card__icon" :class="`tone-${tone}`">
      <slot name="icon">
        <AppIcon :name="icon" :size="20" />
      </slot>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-5);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--app-sp-4);
  transition: border-color var(--app-transition-fast), box-shadow var(--app-transition-fast);
}
.stat-card:hover {
  border-color: var(--app-border);
  box-shadow: var(--app-shadow-sm);
}

.stat-card__main { flex: 1; min-width: 0; }
.stat-card__label {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin-bottom: var(--app-sp-2);
}
.stat-card__value {
  font-size: var(--app-fs-display);
  font-weight: 600;
  color: var(--app-text-1);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.stat-card__unit {
  font-size: var(--app-fs-body);
  color: var(--app-text-3);
  font-weight: 400;
  margin-left: 4px;
}
.stat-card__trend {
  margin-top: var(--app-sp-2);
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
}

.stat-card__icon {
  width: 40px; height: 40px;
  border-radius: var(--app-radius-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.tone-brand   { background: var(--app-brand-50);    color: var(--app-brand-600); }
.tone-success { background: var(--app-success-bg);  color: var(--app-success-text); }
.tone-warning { background: var(--app-warning-bg);  color: var(--app-warning-text); }
.tone-danger  { background: var(--app-danger-bg);   color: var(--app-danger-text); }
.tone-info    { background: var(--app-info-bg);     color: var(--app-info-text); }
</style>
