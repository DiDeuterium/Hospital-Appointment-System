<script setup>
defineProps({
  steps: { type: Array, required: true },     // [{ label: '...' }, ...]
  current: { type: Number, default: 0 }       // 0-based index of 当前步骤
})
</script>

<template>
  <div class="step-flow">
    <template v-for="(step, idx) in steps" :key="idx">
      <div
        class="step-flow__node"
        :class="{
          'is-done': idx < current,
          'is-current': idx === current
        }"
      >
        <div class="step-flow__circle">
          <svg
            v-if="idx < current"
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="3"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M20 6L9 17l-5-5" />
          </svg>
          <template v-else>{{ idx + 1 }}</template>
        </div>
        <div class="step-flow__label">{{ step.label }}</div>
      </div>
      <div
        v-if="idx < steps.length - 1"
        class="step-flow__bar"
        :class="{ 'is-done': idx < current }"
      />
    </template>
  </div>
</template>

<style scoped>
.step-flow {
  display: flex;
  align-items: flex-start;
  width: 100%;
}

.step-flow__node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--app-sp-2);
  flex-shrink: 0;
  min-width: 80px;
}
.step-flow__circle {
  width: 28px; height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: var(--app-fs-caption);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  border: 1.5px solid var(--app-border);
  color: var(--app-text-4);
  background: var(--app-bg-elevated);
  transition: all var(--app-transition-fast);
}
.step-flow__label {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  text-align: center;
  white-space: nowrap;
}

.is-current .step-flow__circle {
  border-color: var(--app-brand-500);
  color: var(--app-brand-600);
  background: var(--app-brand-50);
}
.is-current .step-flow__label {
  color: var(--app-text-1);
  font-weight: 500;
}

.is-done .step-flow__circle {
  background: var(--app-brand-500);
  border-color: var(--app-brand-500);
  color: #fff;
}
.is-done .step-flow__label { color: var(--app-text-2); }

.step-flow__bar {
  flex: 1;
  height: 2px;
  background: var(--app-border);
  margin: 13px var(--app-sp-2) 0;
  border-radius: var(--app-radius-full);
  min-width: 24px;
  transition: background var(--app-transition-base);
}
.step-flow__bar.is-done { background: var(--app-brand-500); }
</style>
