<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { myDoctorSchedules } from '@/api/doctor'
import { useUserStore } from '@/stores/user'
import { formatDate, weekdayCN } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import SectionCard from '@/components/SectionCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'

const router = useRouter()
const user = useUserStore()
const list = ref([])
const loading = ref(false)
const viewMode = ref('list') // 'list' | 'calendar'
const selectedMonth = ref(new Date())

const filters = ref({ workDate: '' })

// 日历数据
const calendarDays = computed(() => {
  const year = selectedMonth.value.getFullYear()
  const month = selectedMonth.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const startPad = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1 // 周一开头
  const daysInMonth = new Date(year, month + 1, 0).getDate()

  const days = []
  for (let i = 0; i < startPad; i++) days.push(null)
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const shifts = list.value.filter(s => s.workDate === dateStr)
    days.push({ date: dateStr, day: d, shifts, isToday: dateStr === formatDate(new Date()) })
  }
  return days
})

function prevMonth() {
  const d = new Date(selectedMonth.value)
  d.setMonth(d.getMonth() - 1)
  selectedMonth.value = d
}
function nextMonth() {
  const d = new Date(selectedMonth.value)
  d.setMonth(d.getMonth() + 1)
  selectedMonth.value = d
}

async function load() {
  const docId = user.profile?.docId
  if (!docId) { list.value = []; return }
  loading.value = true
  try {
    const params = {}
    if (filters.value.workDate) params.workDate = filters.value.workDate
    list.value = await myDoctorSchedules(docId, params)
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

function viewPatients(row) {
  router.push({ name: 'DoctorPatients', params: { scheduleId: row.scheduleId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="我的排班"
      :breadcrumbs="[{ label: '工作台', to: '/doctor' }, { label: '我的排班' }]"
    >
      <template #extra>
        <div class="view-toggle">
          <button
            class="view-toggle__btn"
            :class="{ 'is-active': viewMode === 'list' }"
            @click="viewMode = 'list'"
          >列表</button>
          <button
            class="view-toggle__btn"
            :class="{ 'is-active': viewMode === 'calendar' }"
            @click="viewMode = 'calendar'"
          >日历</button>
        </div>
      </template>
    </PageHeader>

    <!-- 日期筛选（仅列表模式） -->
    <div v-if="viewMode === 'list'" class="toolbar">
      <el-date-picker
        v-model="filters.workDate"
        type="date"
        value-format="YYYY-MM-DD"
        placeholder="按日期筛选"
        clearable
        size="large"
        @change="load"
      />
      <el-button size="large" @click="filters.workDate = ''; load()">清除</el-button>
    </div>

    <!-- 列表视图 -->
    <div v-if="viewMode === 'list'" v-loading="loading" class="schedule-list">
      <article
        v-for="s in list"
        :key="s.scheduleId"
        class="schedule-card"
        @click="viewPatients(s)"
      >
        <div class="schedule-card__head">
          <StatusTag type="primary">{{ s.shift }}</StatusTag>
          <span class="schedule-card__date">{{ s.workDate }} {{ weekdayCN(s.workDate) }}</span>
        </div>
        <div class="schedule-card__body">
          <div class="schedule-card__stat">
            <span class="schedule-card__lab">总号源</span>
            <span class="schedule-card__val">{{ s.totalQuota }}</span>
          </div>
          <div class="schedule-card__stat">
            <span class="schedule-card__lab">已约</span>
            <span class="schedule-card__val" style="color:var(--app-brand-600)">{{ s.appointedCount || 0 }}</span>
          </div>
          <div class="schedule-card__stat">
            <span class="schedule-card__lab">剩余</span>
            <span class="schedule-card__val" style="color:var(--app-success-text)">{{ s.restQuota }}</span>
          </div>
        </div>
      </article>
      <div v-if="!loading && !list.length" class="empty">暂无排班数据</div>
    </div>

    <!-- 日历视图 -->
    <SectionCard v-else class="section-gap">
      <div class="cal-head">
        <button class="cal-head__nav" @click="prevMonth"><AppIcon name="chevron-left" :size="16" /></button>
        <span class="cal-head__title">{{ selectedMonth.getFullYear() }} 年 {{ selectedMonth.getMonth() + 1 }} 月</span>
        <button class="cal-head__nav" @click="nextMonth"><AppIcon name="chevron-right" :size="16" /></button>
      </div>
      <div class="cal-grid">
        <div class="cal-grid__dow" v-for="d in ['一','二','三','四','五','六','日']" :key="d">{{ d }}</div>
        <div
          v-for="(day, idx) in calendarDays"
          :key="idx"
          class="cal-grid__day"
          :class="{ 'is-today': day?.isToday, 'is-empty': !day }"
        >
          <template v-if="day">
            <div class="cal-grid__num">{{ day.day }}</div>
            <div v-if="day.shifts.length" class="cal-grid__dots">
              <span
                v-for="s in day.shifts"
                :key="s.scheduleId"
                class="cal-grid__dot"
                :title="s.shift + ' ' + (s.appointedCount||0) + '/' + s.totalQuota"
              >{{ s.shift.slice(0,1) }}</span>
            </div>
          </template>
        </div>
      </div>
    </SectionCard>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }
.section-gap { margin-bottom: var(--app-sp-6); }

.view-toggle { display: flex; border: 1px solid var(--app-border); border-radius: var(--app-radius-md); overflow: hidden; }
.view-toggle__btn {
  border: none; background: var(--app-bg-elevated); padding: 6px var(--app-sp-4);
  font-size: var(--app-fs-caption); font-weight: 500; color: var(--app-text-2); cursor: pointer;
  transition: all var(--app-transition-fast);
}
.view-toggle__btn + .view-toggle__btn { border-left: 1px solid var(--app-border); }
.view-toggle__btn.is-active { background: var(--app-brand-500); color: #fff; }

.toolbar { display: flex; gap: var(--app-sp-3); margin-bottom: var(--app-sp-6); }

.schedule-list { display: flex; flex-direction: column; gap: var(--app-sp-3); }
.schedule-card {
  background: var(--app-bg-elevated); border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg); padding: var(--app-sp-5); cursor: pointer;
  transition: all var(--app-transition-fast);
}
.schedule-card:hover { border-color: var(--app-border); box-shadow: var(--app-shadow-sm); }
.schedule-card__head { display: flex; align-items: center; gap: var(--app-sp-3); margin-bottom: var(--app-sp-4); }
.schedule-card__date { font-size: var(--app-fs-caption); color: var(--app-text-3); }
.schedule-card__body { display: flex; gap: var(--app-sp-6); }
.schedule-card__stat { display: flex; flex-direction: column; gap: 2px; }
.schedule-card__lab { font-size: var(--app-fs-tiny); color: var(--app-text-3); }
.schedule-card__val { font-size: var(--app-fs-h3); font-weight: 600; color: var(--app-text-1); font-variant-numeric: tabular-nums; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }

/* 日历 */
.cal-head { display: flex; align-items: center; justify-content: center; gap: var(--app-sp-4); margin-bottom: var(--app-sp-5); }
.cal-head__nav {
  width: 32px; height: 32px; border: none; border-radius: var(--app-radius-md);
  background: var(--app-bg-subtle); color: var(--app-text-2); cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
}
.cal-head__nav:hover { background: var(--app-brand-100); color: var(--app-brand-600); }
.cal-head__title { font-size: var(--app-fs-h3); font-weight: 600; color: var(--app-text-1); }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
.cal-grid__dow { text-align: center; font-size: var(--app-fs-caption); color: var(--app-text-3); padding: var(--app-sp-2) 0; }
.cal-grid__day {
  aspect-ratio: 1; border-radius: var(--app-radius-md); padding: var(--app-sp-1);
  background: var(--app-bg-page); display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 4px;
}
.cal-grid__day.is-today { background: var(--app-brand-50); }
.cal-grid__day.is-empty { background: transparent; }
.cal-grid__num { font-size: var(--app-fs-caption); font-weight: 500; color: var(--app-text-2); font-variant-numeric: tabular-nums; }
.is-today .cal-grid__num { color: var(--app-brand-600); font-weight: 600; }
.cal-grid__dots { display: flex; gap: 2px; }
.cal-grid__dot {
  width: 20px; height: 18px; border-radius: var(--app-radius-sm);
  background: var(--app-brand-100); color: var(--app-brand-700);
  font-size: var(--app-fs-tiny); font-weight: 600;
  display: inline-flex; align-items: center; justify-content: center;
  cursor: default;
}
</style>
