<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { myDoctorSchedules } from '@/api/doctor'
import { formatDate, weekdayCN } from '@/utils/booking'
import StatCard from '@/components/StatCard.vue'
import SectionCard from '@/components/SectionCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'

const router = useRouter()
const user = useUserStore()
const todaySchedules = ref([])
const weekStats = ref({ shifts: 0, patients: 0 })
const loading = ref(false)

async function load() {
  const docId = user.profile?.docId
  if (!docId) return
  loading.value = true
  try {
    const today = formatDate(new Date())
    const all = await myDoctorSchedules(docId, {})
    // 今日排班
    todaySchedules.value = (all || []).filter(s => s.workDate === today)
    // 本周统计
    weekStats.value = {
      shifts: (all || []).length,
      patients: (all || []).reduce((sum, s) => sum + (s.appointedCount || 0), 0)
    }
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <!-- 欢迎横幅 -->
    <section class="hero">
      <div class="hero__body">
        <h1 class="hero__greeting">{{ user.displayName || '医生' }}，早上好</h1>
        <p class="hero__date">今天是 {{ formatDate(new Date()) }} {{ weekdayCN(new Date()) }}</p>
        <p class="hero__summary" v-if="todaySchedules.length">
          您今日有 {{ todaySchedules.length }} 个排班，待接诊 {{ todaySchedules.reduce((s, i) => s + (i.appointedCount || 0), 0) }} 人
        </p>
      </div>
    </section>

    <!-- 统计卡 -->
    <div class="stats">
      <StatCard label="今日排班" :value="todaySchedules.length" unit="班次" icon="calendar" tone="brand" />
      <StatCard
        label="今日已约"
        :value="todaySchedules.reduce((s, i) => s + (i.appointedCount || 0), 0)"
        unit="患者"
        icon="users"
        tone="success"
      />
      <StatCard label="本周累计" :value="weekStats.shifts" unit="班次" icon="calendar-check" tone="info" />
    </div>

    <!-- 今日排班 -->
    <SectionCard title="今日排班" class="section-gap">
      <template v-if="todaySchedules.length">
        <div
          v-for="s in todaySchedules"
          :key="s.scheduleId"
          class="shift-item"
          @click="router.push({ name: 'DoctorPatients', params: { scheduleId: s.scheduleId } })"
        >
          <div class="shift-item__main">
            <StatusTag type="primary">{{ s.shift }}</StatusTag>
            <div class="shift-item__info">
              <span>已约 {{ s.appointedCount || 0 }} / {{ s.totalQuota }}</span>
              <span class="shift-item__sep">·</span>
              <span>剩余 {{ s.restQuota }}</span>
            </div>
          </div>
          <AppIcon name="chevron-right" :size="16" class="shift-item__arrow" />
        </div>
      </template>
      <div v-else class="shift-empty">今日暂无排班</div>
    </SectionCard>

    <!-- 快捷入口 -->
    <SectionCard title="快捷操作" class="section-gap" :bordered="false" :body-padding="false">
      <div class="shortcuts">
        <button class="shortcuts__item" @click="router.push('/doctor/schedules')">
          <span class="shortcuts__icon"><AppIcon name="calendar" :size="22" /></span>
          <span class="shortcuts__label">全部排班</span>
        </button>
      </div>
    </SectionCard>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.section-gap { margin-bottom: var(--app-sp-6); }

.hero {
  padding: var(--app-sp-8);
  margin-bottom: var(--app-sp-6);
  background: linear-gradient(105deg, var(--app-brand-50) 0%, var(--app-brand-100) 100%);
  border-radius: var(--app-radius-xl);
}
.hero__greeting { font-size: var(--app-fs-display); font-weight: 600; color: var(--app-text-1); }
.hero__date { font-size: var(--app-fs-caption); color: var(--app-text-3); margin-top: var(--app-sp-2); }
.hero__summary { font-size: var(--app-fs-body); color: var(--app-text-2); margin-top: var(--app-sp-3); }

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--app-sp-4);
  margin-bottom: var(--app-sp-6);
}
@media (max-width: 640px) { .stats { grid-template-columns: 1fr; } }

.shift-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--app-sp-4); border-radius: var(--app-radius-md);
  background: var(--app-bg-page); cursor: pointer;
  transition: all var(--app-transition-fast);
}
.shift-item:hover { background: var(--app-brand-50); }
.shift-item + .shift-item { margin-top: var(--app-sp-3); }
.shift-item__main { display: flex; align-items: center; gap: var(--app-sp-3); }
.shift-item__info { font-size: var(--app-fs-caption); color: var(--app-text-2); display: flex; align-items: center; gap: var(--app-sp-2); }
.shift-item__sep { color: var(--app-text-4); }
.shift-item__arrow { color: var(--app-text-3); flex-shrink: 0; }
.shift-empty { text-align: center; padding: var(--app-sp-8) 0; color: var(--app-text-3); font-size: var(--app-fs-caption); }

.shortcuts { display: flex; padding: var(--app-sp-5); }
.shortcuts__item {
  display: flex; flex-direction: column; align-items: center; gap: var(--app-sp-2);
  border: none; background: none; border-radius: var(--app-radius-lg); padding: var(--app-sp-4) var(--app-sp-5);
  cursor: pointer; transition: all var(--app-transition-fast); color: var(--app-text-2);
}
.shortcuts__item:hover { background: var(--app-brand-50); color: var(--app-brand-600); }
.shortcuts__icon {
  width: 48px; height: 48px; border-radius: var(--app-radius-md);
  background: var(--app-bg-subtle); display: inline-flex; align-items: center; justify-content: center;
}
.shortcuts__item:hover .shortcuts__icon { background: var(--app-brand-100); }
.shortcuts__label { font-size: var(--app-fs-caption); font-weight: 500; }
</style>
