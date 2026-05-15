<script setup>
import { computed } from 'vue'
import StatusTag from './StatusTag.vue'
import QuotaBar from './QuotaBar.vue'
import AppIcon from './AppIcon.vue'
import { SHIFT_TIME_MAP } from '@/utils/booking'

const props = defineProps({
  schedule: { type: Object, required: true }
})
defineEmits(['book'])

const isFull = computed(() => props.schedule.restQuota <= 0)
const timeRange = computed(() => SHIFT_TIME_MAP[props.schedule.shift] || '')
</script>

<template>
  <article class="schedule-card" :class="{ 'is-full': isFull }">
    <div class="schedule-card__head">
      <div class="schedule-card__shift">
        <span class="schedule-card__shift-tag">{{ schedule.shift }}</span>
        <span class="schedule-card__time">{{ timeRange }}</span>
      </div>
      <StatusTag v-if="schedule.title" type="primary" size="small">{{ schedule.title }}</StatusTag>
    </div>

    <div class="schedule-card__doctor">
      <h3 class="schedule-card__name">{{ schedule.docName }}</h3>
      <span v-if="schedule.deptName" class="schedule-card__dept">{{ schedule.deptName }}</span>
    </div>

    <QuotaBar :total="schedule.totalQuota" :rest="schedule.restQuota" />

    <footer class="schedule-card__footer">
      <el-button
        type="primary"
        :disabled="isFull"
        @click="$emit('book', schedule)"
      >
        {{ isFull ? '已约满' : '立即挂号' }}
        <AppIcon v-if="!isFull" name="arrow-right" :size="14" style="margin-left:4px" />
      </el-button>
    </footer>
  </article>
</template>

<style scoped>
.schedule-card {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-5);
  display: flex;
  flex-direction: column;
  gap: var(--app-sp-4);
  transition: all var(--app-transition-fast);
}
.schedule-card:hover {
  border-color: var(--app-border);
  box-shadow: var(--app-shadow-sm);
}
.schedule-card.is-full {
  background: var(--app-bg-subtle);
  opacity: 0.85;
}
.schedule-card.is-full:hover {
  transform: none;
  box-shadow: none;
}

.schedule-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--app-sp-2);
}
.schedule-card__shift {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
}
.schedule-card__shift-tag {
  font-size: var(--app-fs-caption);
  font-weight: 600;
  color: var(--app-brand-700);
  background: var(--app-brand-50);
  padding: 2px 8px;
  border-radius: var(--app-radius-sm);
}
.schedule-card__time {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  font-variant-numeric: tabular-nums;
}

.schedule-card__doctor {
  display: flex;
  align-items: baseline;
  gap: var(--app-sp-2);
}
.schedule-card__name {
  font-size: var(--app-fs-h3);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0;
}
.schedule-card__dept {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
}

.schedule-card__footer {
  display: flex;
  justify-content: flex-end;
}
</style>
