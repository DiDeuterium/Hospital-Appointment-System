<script setup>
import { ref, computed, watch } from 'vue'
import AppIcon from './AppIcon.vue'
import { formatDate, weekdayCN } from '@/utils/booking'

const props = defineProps({
  modelValue: String,           // YYYY-MM-DD 当前选中
  days: { type: Number, default: 7 }  // 显示几天
})
const emit = defineEmits(['update:modelValue'])

// 起始日期（左上滑动）
const baseDate = ref(new Date())

const dateList = computed(() => {
  const list = []
  const start = new Date(baseDate.value)
  start.setHours(0, 0, 0, 0)
  for (let i = 0; i < props.days; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    const value = formatDate(d)
    const today = formatDate(new Date())
    list.push({
      value,
      day: d.getDate(),
      month: d.getMonth() + 1,
      weekday: weekdayCN(d),
      isToday: value === today,
      isWeekend: d.getDay() === 0 || d.getDay() === 6
    })
  }
  return list
})

function pick(value) { emit('update:modelValue', value) }
function prev() {
  const d = new Date(baseDate.value)
  d.setDate(d.getDate() - props.days)
  baseDate.value = d
}
function next() {
  const d = new Date(baseDate.value)
  d.setDate(d.getDate() + props.days)
  baseDate.value = d
}

// 首次进入若 modelValue 为空，默认选今天
watch(() => props.modelValue, (v) => {
  if (!v) emit('update:modelValue', formatDate(new Date()))
}, { immediate: true })
</script>

<template>
  <div class="date-tabs">
    <button type="button" class="date-tabs__nav" @click="prev" aria-label="上一周">
      <AppIcon name="chevron-left" :size="16" />
    </button>
    <div class="date-tabs__list">
      <button
        v-for="d in dateList"
        :key="d.value"
        type="button"
        class="date-tabs__item"
        :class="{
          'is-active': modelValue === d.value,
          'is-today': d.isToday,
          'is-weekend': d.isWeekend
        }"
        @click="pick(d.value)"
      >
        <div class="date-tabs__weekday">{{ d.isToday ? '今天' : d.weekday }}</div>
        <div class="date-tabs__date">{{ d.month }}/{{ d.day }}</div>
      </button>
    </div>
    <button type="button" class="date-tabs__nav" @click="next" aria-label="下一周">
      <AppIcon name="chevron-right" :size="16" />
    </button>
  </div>
</template>

<style scoped>
.date-tabs {
  display: flex;
  align-items: stretch;
  gap: var(--app-sp-2);
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-2);
}

.date-tabs__nav {
  width: 32px;
  border: none;
  background: transparent;
  border-radius: var(--app-radius-md);
  cursor: pointer;
  color: var(--app-text-3);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--app-transition-fast);
  flex-shrink: 0;
}
.date-tabs__nav:hover {
  background: var(--app-bg-subtle);
  color: var(--app-text-1);
}

.date-tabs__list {
  display: flex;
  flex: 1;
  gap: 4px;
  overflow-x: auto;
}
.date-tabs__list::-webkit-scrollbar { display: none; }

.date-tabs__item {
  flex: 1;
  min-width: 64px;
  padding: var(--app-sp-2) var(--app-sp-3);
  border: 1px solid transparent;
  background: transparent;
  border-radius: var(--app-radius-md);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  transition: all var(--app-transition-fast);
}
.date-tabs__item:hover { background: var(--app-bg-subtle); }
.date-tabs__item.is-weekend .date-tabs__date { color: var(--app-text-3); }

.date-tabs__item.is-active {
  background: var(--app-brand-500);
  border-color: var(--app-brand-500);
}
.date-tabs__item.is-active .date-tabs__weekday,
.date-tabs__item.is-active .date-tabs__date { color: #fff; }

.date-tabs__weekday {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  line-height: 1.3;
}
.date-tabs__date {
  font-size: var(--app-fs-h3);
  font-weight: 600;
  color: var(--app-text-1);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
.date-tabs__item.is-today:not(.is-active) .date-tabs__weekday {
  color: var(--app-brand-600);
  font-weight: 500;
}
</style>
