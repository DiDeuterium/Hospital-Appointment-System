<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listDoctorsByDept, listDepartments } from '@/api/department'
import { listSchedules } from '@/api/schedule'
import { genderEmoji, weekdayCN, formatDate, SHIFT_TIME_MAP, deptIcon, stashSchedule } from '@/utils/booking'
import { GENDER_LABEL } from '@/utils/constants'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import QuotaBar from '@/components/QuotaBar.vue'
import SectionCard from '@/components/SectionCard.vue'
import DateTabs from '@/components/DateTabs.vue'

const route = useRoute()
const router = useRouter()
const deptId = route.params.deptId
const docId = route.params.docId

const doctor = ref(null)
const deptName = ref('')
const schedules = ref([])
const loading = ref(false)
const selectedDate = ref(formatDate(new Date()))

// 近 7 天日期列表（用于构造网格并留满 7 天）
const dateRange = computed(() => {
  const list = []
  const start = new Date()
  start.setHours(0, 0, 0, 0)
  for (let i = 0; i < 7; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    list.push(formatDate(d))
  }
  return list
})

// 按"日期+时段"组织网格数据
const grid = computed(() => {
  return dateRange.value.map(d => {
    const am = schedules.value.find(s => s.workDate === d && s.shift === '上午')
    const pm = schedules.value.find(s => s.workDate === d && s.shift === '下午')
    return { date: d, weekday: weekdayCN(d), am, pm }
  })
})

function gotoBook(schedule) {
  if (!schedule || schedule.restQuota <= 0) return
  stashSchedule({ ...schedule, deptName: deptName.value })
  router.push({ name: 'PatientBookConfirm' })
}

async function load() {
  loading.value = true
  try {
    // 获取当前科室的医生信息 + 全科室排班（按 deptId 过滤）
    const [doctors, allSchedules, depts] = await Promise.all([
      listDoctorsByDept(deptId),
      listSchedules({ deptId }),
      listDepartments()
    ])
    const found = doctors.find(d => d.docId == docId)
    if (found) doctor.value = found
    const dept = depts.find(d => d.deptId == deptId)
    deptName.value = dept?.deptName || `科室 ${deptId}`
    // 只保留本医生的排班
    schedules.value = (allSchedules || []).filter(s => s.docId == docId)
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      :title="doctor ? doctor.docName : '医生详情'"
      :breadcrumbs="[
        { label: '首页', to: '/patient/home' },
        { label: '选择科室', to: '/patient/departments' },
        { label: deptName || '...', to: { name: 'PatientDoctors', params: { deptId } } },
        { label: doctor ? doctor.docName : '' }
      ]"
    />

    <!-- 医生信息卡 -->
    <section v-if="doctor" class="profile">
      <div class="profile__avatar">{{ genderEmoji(doctor.gender) }}</div>
      <div class="profile__body">
        <div class="profile__name-row">
          <h1 class="profile__name">{{ doctor.docName }}</h1>
          <StatusTag v-if="doctor.title" type="primary">{{ doctor.title }}</StatusTag>
        </div>
        <div class="profile__meta">
          <span>{{ deptName }}</span>
          <span class="profile__sep">·</span>
          <span>{{ GENDER_LABEL[doctor.gender] || '—' }}</span>
          <span class="profile__sep">·</span>
          <span>工号 {{ doctor.docId }}</span>
        </div>
        <p class="profile__bio">
          擅长方向暂未填充（系统预留字段）。医生信息与排班数据由管理员维护，请以现场公示为准。
        </p>
      </div>
      <div class="profile__badge">{{ deptIcon(deptName) }}</div>
    </section>

    <!-- 近 7 天可预约排班（日历式网格） -->
    <SectionCard title="近 7 天排班" class="section-gap">
      <template #extra>
        <DateTabs v-model="selectedDate" />
      </template>

      <div v-loading="loading" class="cal">
        <div class="cal__header">
          <span class="cal__day-label">上午</span>
          <span class="cal__day-label" />
          <span class="cal__day-label">下午</span>
        </div>
        <div class="cal__row" v-for="g in grid" :key="g.date">
          <div class="cal__col">
            <div
              v-if="g.am"
              class="cal__cell"
              :class="{ 'is-selected': selectedDate === g.date }"
              @click="gotoBook(g.am)"
            >
              <QuotaBar :total="g.am.totalQuota" :rest="g.am.restQuota" />
            </div>
            <div v-else class="cal__cell is-empty">—</div>
          </div>
          <div class="cal__date-col">
            <div class="cal__date">{{ g.date.slice(5) }}</div>
            <div class="cal__wd">{{ g.weekday }}</div>
          </div>
          <div class="cal__col">
            <div
              v-if="g.pm"
              class="cal__cell"
              :class="{ 'is-selected': selectedDate === g.date }"
              @click="gotoBook(g.pm)"
            >
              <QuotaBar :total="g.pm.totalQuota" :rest="g.pm.restQuota" />
            </div>
            <div v-else class="cal__cell is-empty">—</div>
          </div>
        </div>
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

/* ---- 医生信息卡 ---- */
.profile {
  display: flex;
  align-items: flex-start;
  gap: var(--app-sp-6);
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-xl);
  padding: var(--app-sp-8);
  margin-bottom: var(--app-sp-6);
}
.profile__avatar {
  width: 88px; height: 88px;
  border-radius: 50%;
  background: var(--app-brand-50);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
}
.profile__body { flex: 1; min-width: 0; }
.profile__name-row {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  margin-bottom: var(--app-sp-2);
}
.profile__name {
  font-size: var(--app-fs-h1);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0;
}
.profile__meta {
  font-size: var(--app-fs-body);
  color: var(--app-text-3);
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  margin-bottom: var(--app-sp-4);
  flex-wrap: wrap;
}
.profile__sep { color: var(--app-text-4); }
.profile__bio {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  line-height: var(--app-lh-relaxed);
  margin: 0;
}
.profile__badge {
  font-size: 52px;
  flex-shrink: 0;
}

/* ---- 日历排版 ---- */
.cal__header {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  margin-bottom: var(--app-sp-4);
  justify-content: center;
}
.cal__day-label {
  flex: 1;
  text-align: center;
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
}
.cal__row {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  margin-bottom: var(--app-sp-2);
}
.cal__col { flex: 1; }
.cal__date-col {
  width: 72px;
  text-align: center;
  flex-shrink: 0;
}
.cal__date {
  font-size: var(--app-fs-body);
  font-weight: 600;
  color: var(--app-text-1);
}
.cal__wd {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
}
.cal__cell {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-md);
  padding: var(--app-sp-3);
  cursor: pointer;
  transition: all var(--app-transition-fast);
}
.cal__cell:hover:not(.is-empty) {
  border-color: var(--app-brand-400);
  box-shadow: var(--app-shadow-sm);
}
.cal__cell.is-empty {
  text-align: center;
  color: var(--app-text-4);
  padding: var(--app-sp-4);
}
</style>
