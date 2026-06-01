<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listDoctorsByDept, listDepartments } from '@/api/department'
import { listSchedules } from '@/api/schedule'
import { genderEmoji, weekdayCN, formatDate, SHIFT_TIME_MAP, deptIcon, stashSchedule, formatFee } from '@/utils/booking'
import { GENDER_LABEL } from '@/utils/constants'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import QuotaBar from '@/components/QuotaBar.vue'
import SectionCard from '@/components/SectionCard.vue'

const route = useRoute()
const router = useRouter()
const deptId = route.params.deptId
const docId = route.params.docId

const doctor = ref(null)
const deptName = ref('')
const schedules = ref([])
const loading = ref(false)
const imgError = ref(false)

const SHIFTS = ['上午', '下午', '夜诊']

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

// 按"日期 × 时段(上午/下午/夜诊)"组织网格数据
const grid = computed(() => {
  return dateRange.value.map(d => ({
    date: d,
    weekday: weekdayCN(d),
    cells: SHIFTS.map(sh => schedules.value.find(s => s.workDate === d && s.shift === sh) || null)
  }))
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
    const found = doctors.find(d => String(d.docId) === String(docId))
    if (found) doctor.value = found
    const dept = depts.find(d => String(d.deptId) === String(deptId))
    deptName.value = dept?.deptName || `科室 ${deptId}`
    // 只保留本医生的排班
    schedules.value = (allSchedules || []).filter(s => String(s.docId) === String(docId))
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
      <div class="profile__avatar">
        <img
          v-if="doctor.avatarUrl && !imgError"
          :src="doctor.avatarUrl"
          :alt="doctor.docName"
          class="profile__img"
          @error="imgError = true"
        />
        <span v-else>{{ genderEmoji(doctor.gender) }}</span>
      </div>
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
          <span class="profile__bio-label">擅长领域：</span>{{ doctor.specialty || '暂未填写' }}
        </p>
      </div>
      <div class="profile__badge">{{ deptIcon(deptName) }}</div>
    </section>

    <!-- 近 7 天可预约排班（日历式网格，固定展示今天起未来 7 天） -->
    <SectionCard title="近 7 天排班" class="section-gap">

      <div v-loading="loading" class="cal">
        <div class="cal__header">
          <span class="cal__date-col" />
          <span class="cal__day-label" v-for="sh in SHIFTS" :key="sh">{{ sh }}</span>
        </div>
        <div class="cal__row" v-for="g in grid" :key="g.date">
          <div class="cal__date-col">
            <div class="cal__date">{{ g.date.slice(5) }}</div>
            <div class="cal__wd">{{ g.weekday }}</div>
          </div>
          <div class="cal__col" v-for="(cell, idx) in g.cells" :key="idx">
            <div
              v-if="cell"
              class="cal__cell"
              :class="{ 'is-today': g.date === dateRange[0] }"
              @click="gotoBook(cell)"
            >
              <QuotaBar :total="cell.totalQuota" :rest="cell.restQuota" />
              <div class="cal__fee">{{ formatFee(cell.fee) }}</div>
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
  overflow: hidden;
}
.profile__img { width: 100%; height: 100%; object-fit: cover; }
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
.profile__bio-label { color: var(--app-text-2); font-weight: 500; }
.profile__badge {
  font-size: 52px;
  flex-shrink: 0;
}

/* ---- 日历排版 ---- */
.cal__header {
  display: grid;
  grid-template-columns: 72px repeat(3, 1fr);
  gap: var(--app-sp-3);
  align-items: center;
  margin-bottom: var(--app-sp-4);
}
.cal__day-label {
  text-align: center;
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
}
.cal__row {
  display: grid;
  grid-template-columns: 72px repeat(3, 1fr);
  align-items: center;
  gap: var(--app-sp-3);
  margin-bottom: var(--app-sp-2);
}
.cal__date-col {
  text-align: center;
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
.cal__fee {
  margin-top: var(--app-sp-2);
  font-size: var(--app-fs-tiny);
  color: var(--app-danger-text);
  font-weight: 600;
  text-align: center;
}
.cal__cell.is-today {
  border-color: var(--app-brand-400);
  background: var(--app-brand-50);
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
