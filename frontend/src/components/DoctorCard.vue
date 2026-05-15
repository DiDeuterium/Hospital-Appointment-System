<script setup>
import StatusTag from './StatusTag.vue'
import AppIcon from './AppIcon.vue'
import { genderEmoji } from '@/utils/booking'
import { GENDER_LABEL } from '@/utils/constants'

defineProps({
  doctor: { type: Object, required: true }
})
defineEmits(['view-detail', 'view-schedules'])
</script>

<template>
  <article class="doctor-card">
    <div class="doctor-card__head">
      <div class="doctor-card__avatar">{{ genderEmoji(doctor.gender) }}</div>
      <div class="doctor-card__main">
        <div class="doctor-card__name-row">
          <h3 class="doctor-card__name">{{ doctor.docName }}</h3>
          <StatusTag v-if="doctor.title" type="primary" size="small">{{ doctor.title }}</StatusTag>
        </div>
        <div class="doctor-card__meta">
          <span>工号 {{ doctor.docId }}</span>
          <span class="doctor-card__sep">·</span>
          <span>{{ GENDER_LABEL[doctor.gender] || '—' }}</span>
        </div>
      </div>
    </div>

    <p class="doctor-card__bio">
      <slot name="bio">擅长方向暂未填充（系统预留字段）</slot>
    </p>

    <footer class="doctor-card__footer">
      <el-button @click="$emit('view-detail', doctor)">
        <AppIcon name="user" :size="14" style="margin-right:4px" />
        医生详情
      </el-button>
      <el-button type="primary" @click="$emit('view-schedules', doctor)">
        查看排班
        <AppIcon name="arrow-right" :size="14" style="margin-left:4px" />
      </el-button>
    </footer>
  </article>
</template>

<style scoped>
.doctor-card {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-5);
  display: flex;
  flex-direction: column;
  gap: var(--app-sp-4);
  transition: all var(--app-transition-fast);
}
.doctor-card:hover {
  border-color: var(--app-border);
  box-shadow: var(--app-shadow-sm);
  transform: translateY(-1px);
}

.doctor-card__head {
  display: flex;
  align-items: center;
  gap: var(--app-sp-4);
}
.doctor-card__avatar {
  width: 56px; height: 56px;
  border-radius: 50%;
  background: var(--app-brand-50);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}
.doctor-card__main { flex: 1; min-width: 0; }
.doctor-card__name-row {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  margin-bottom: 4px;
}
.doctor-card__name {
  font-size: var(--app-fs-h3);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0;
}
.doctor-card__meta {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  display: flex;
  align-items: center;
  gap: 6px;
}
.doctor-card__sep { color: var(--app-text-4); }

.doctor-card__bio {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  line-height: var(--app-lh-relaxed);
  margin: 0;
  min-height: 32px;
}

.doctor-card__footer {
  display: flex;
  gap: var(--app-sp-2);
}
.doctor-card__footer .el-button {
  flex: 1;
}
</style>
