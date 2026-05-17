<script setup>
import { onMounted, ref } from 'vue'
import { listDepartments } from '@/api/department'
import { listDoctors } from '@/api/doctor'
import { listAdminSchedules } from '@/api/schedule'
import { deptIcon } from '@/utils/booking'
import StatCard from '@/components/StatCard.vue'
import SectionCard from '@/components/SectionCard.vue'

const stats = ref({ deptCount: 0, docCount: 0, todayAppts: 0, usageRate: 0 })
const deptDistribution = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const [depts, doctors, schedules] = await Promise.all([
      listDepartments({ keyword: '' }),
      listDoctors({ deptId: '' }),
      listAdminSchedules()
    ])

    // 基础统计
    const today = new Date().toISOString().slice(0, 10)
    const todaySchedules = (schedules || []).filter(s => s.workDate === today)
    const totalQuota = todaySchedules.reduce((s, i) => s + (i.totalQuota || 0), 0)
    const restQuota = todaySchedules.reduce((s, i) => s + (i.restQuota || 0), 0)
    const appointed = totalQuota - restQuota

    stats.value = {
      deptCount: (depts || []).length,
      docCount: (doctors || []).length,
      todayAppts: appointed,
      usageRate: totalQuota ? Math.round((appointed / totalQuota) * 100) : 0
    }

    // 科室挂号分布（按 schedules 里的 deptName 聚合）
    const map = {}
    for (const s of (schedules || [])) {
      const name = s.deptName || '未知'
      map[name] = (map[name] || 0) + ((s.totalQuota || 0) - (s.restQuota || 0))
    }
    deptDistribution.value = Object.entries(map)
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 8)
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <h1 class="page-title">数据看板</h1>

    <!-- 统计卡 -->
    <div class="stats">
      <StatCard label="科室总数" :value="stats.deptCount" unit="个" icon="building" tone="brand" />
      <StatCard label="医生总数" :value="stats.docCount" unit="名" icon="users" tone="success" />
      <StatCard label="今日挂号" :value="stats.todayAppts" unit="单" icon="calendar-check" tone="info" />
      <StatCard label="号源使用率" :value="stats.usageRate" unit="%" icon="dashboard" tone="warning" />
    </div>

    <!-- 科室分布 -->
    <SectionCard title="各科室已约挂号分布" class="section-gap" v-loading="loading">
      <div v-if="deptDistribution.length" class="bar-chart">
        <div v-for="d in deptDistribution" :key="d.name" class="bar-chart__row">
          <span class="bar-chart__label">
            <span class="bar-chart__icon">{{ deptIcon(d.name) }}</span>
            {{ d.name }}
          </span>
          <div class="bar-chart__track">
            <div
              class="bar-chart__fill"
              :style="{ width: Math.max(4, (d.count / deptDistribution[0].count) * 100) + '%' }"
            />
          </div>
          <span class="bar-chart__val">{{ d.count }}</span>
        </div>
      </div>
      <div v-else class="empty">暂无数据</div>
    </SectionCard>

    <!-- 待处理 -->
    <SectionCard title="系统状态" class="section-gap">
      <div class="status-list">
        <div class="status-list__item">
          <span class="status-list__dot" style="background:var(--app-success)" />
          系统运行正常，请定期发布医生排班
        </div>
        <div class="status-list__item">
          <span class="status-list__dot" style="background:var(--app-warning)" />
          已约满的排班请及时关注，必要时增开号源
        </div>
        <div class="status-list__item">
          <span class="status-list__dot" style="background:var(--app-info)" />
          新增科室或医生后，请及时发布排班计划
        </div>
      </div>
    </SectionCard>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }
.page-title { font-size: var(--app-fs-h1); font-weight: 600; color: var(--app-text-1); margin-bottom: var(--app-sp-6); }
.section-gap { margin-bottom: var(--app-sp-6); }

.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--app-sp-4); margin-bottom: var(--app-sp-6); }
@media (max-width: 960px) { .stats { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .stats { grid-template-columns: 1fr; } }

/* 柱状图 */
.bar-chart { display: flex; flex-direction: column; gap: var(--app-sp-3); }
.bar-chart__row { display: flex; align-items: center; gap: var(--app-sp-3); }
.bar-chart__label {
  width: 120px; flex-shrink: 0; font-size: var(--app-fs-caption); color: var(--app-text-2);
  display: flex; align-items: center; gap: var(--app-sp-2); overflow: hidden; white-space: nowrap;
}
.bar-chart__icon { font-size: 16px; flex-shrink: 0; }
.bar-chart__icon { font-size: 14px; }
.bar-chart__track { flex: 1; height: 24px; background: var(--app-bg-subtle); border-radius: var(--app-radius-sm); overflow: hidden; }
.bar-chart__fill {
  height: 100%; border-radius: var(--app-radius-sm);
  background: linear-gradient(90deg, var(--app-brand-400), var(--app-brand-500));
  transition: width var(--app-transition-slow);
  min-width: 4px;
}
.bar-chart__val {
  width: 32px; text-align: right; font-size: var(--app-fs-caption); font-weight: 600;
  color: var(--app-text-1); font-variant-numeric: tabular-nums;
}

.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }

.status-list { display: flex; flex-direction: column; gap: var(--app-sp-3); }
.status-list__item { display: flex; align-items: center; gap: var(--app-sp-3); font-size: var(--app-fs-caption); color: var(--app-text-2); padding: var(--app-sp-2) 0; }
.status-list__dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
</style>
