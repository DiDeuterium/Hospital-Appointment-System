<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { myDoctorSchedules } from '@/api/doctor'
import { submitChangeRequest } from '@/api/changeRequest'
import { useUserStore } from '@/stores/user'
import { formatDate, weekdayCN } from '@/utils/booking'
import { SHIFT_OPTIONS, CHANGE_TYPE, SCHEDULE_STATUS } from '@/utils/constants'
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

// 排班变更申请弹窗
const dialog = reactive({
  visible: false,
  submitting: false,
  schedule: null,
  form: { changeType: CHANGE_TYPE.CANCEL, targetWorkDate: '', targetShift: '', targetTotalQuota: null, reason: '' }
})

function isStopped(s) {
  return s.status === SCHEDULE_STATUS.STOPPED
}

function openChange(row, type) {
  dialog.schedule = row
  dialog.form = {
    changeType: type,
    targetWorkDate: '',
    targetShift: '',
    targetTotalQuota: row.totalQuota,
    reason: ''
  }
  dialog.visible = true
}

async function submitChange() {
  const f = dialog.form
  if (!f.reason || !f.reason.trim()) {
    ElMessage.warning('请填写申请原因')
    return
  }
  if (f.changeType === CHANGE_TYPE.MODIFY && !f.targetWorkDate && !f.targetShift && f.targetTotalQuota == null) {
    ElMessage.warning('修改排班申请至少填写一项目标内容')
    return
  }
  dialog.submitting = true
  try {
    const payload = { changeType: f.changeType, reason: f.reason.trim() }
    if (f.changeType === CHANGE_TYPE.MODIFY) {
      payload.targetWorkDate = f.targetWorkDate || null
      payload.targetShift = f.targetShift || null
      payload.targetTotalQuota = f.targetTotalQuota ?? null
    }
    await submitChangeRequest(dialog.schedule.scheduleId, payload)
    ElMessage.success('申请提交成功，等待管理员审核')
    dialog.visible = false
    load()
  } catch { /* 拦截器已弹错误 */ } finally {
    dialog.submitting = false
  }
}

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
        <div class="head-extra">
          <el-button @click="router.push('/doctor/change-requests')">
            <AppIcon name="file-text" :size="14" style="margin-right:4px" />我的申请
          </el-button>
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
      >
        <div class="schedule-card__head">
          <StatusTag type="primary">{{ s.shift }}</StatusTag>
          <StatusTag :type="isStopped(s) ? 'danger' : 'success'" size="small">
            {{ isStopped(s) ? '停诊' : '正常' }}
          </StatusTag>
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
        <footer class="schedule-card__footer">
          <el-button size="small" @click="viewPatients(s)">查看名册</el-button>
          <el-button size="small" plain :disabled="isStopped(s)" @click="openChange(s, CHANGE_TYPE.CANCEL)">申请停诊</el-button>
          <el-button size="small" plain :disabled="isStopped(s)" @click="openChange(s, CHANGE_TYPE.MODIFY)">申请改排班</el-button>
        </footer>
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

    <!-- 排班变更申请弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.form.changeType === CHANGE_TYPE.CANCEL ? '申请停诊' : '申请修改排班'"
      width="480px"
      :lock-scroll="false"
    >
      <div v-if="dialog.schedule" class="dialog-origin">
        原排班：{{ dialog.schedule.workDate }} {{ dialog.schedule.shift }} · 总号源 {{ dialog.schedule.totalQuota }} · 剩余 {{ dialog.schedule.restQuota }}
      </div>
      <el-form label-position="top">
        <el-form-item label="申请类型">
          <el-radio-group v-model="dialog.form.changeType">
            <el-radio-button :value="CHANGE_TYPE.CANCEL">停诊</el-radio-button>
            <el-radio-button :value="CHANGE_TYPE.MODIFY">修改排班</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <template v-if="dialog.form.changeType === CHANGE_TYPE.MODIFY">
          <p class="dialog-hint">以下三项至少填写一项；若该排班已有有效预约，仅可调整总号源。</p>
          <el-form-item label="目标日期">
            <el-date-picker v-model="dialog.form.targetWorkDate" type="date" value-format="YYYY-MM-DD" placeholder="不改则留空" size="large" style="width:100%" />
          </el-form-item>
          <el-form-item label="目标时段">
            <el-select v-model="dialog.form.targetShift" placeholder="不改则留空" clearable size="large" style="width:100%">
              <el-option v-for="s in SHIFT_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标总号源">
            <el-input-number v-model="dialog.form.targetTotalQuota" :min="1" :max="200" size="large" />
          </el-form-item>
        </template>

        <el-form-item label="申请原因" required>
          <el-input v-model="dialog.form.reason" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="请填写申请原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="dialog.visible = false">取消</el-button>
        <el-button size="large" type="primary" :loading="dialog.submitting" @click="submitChange">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }
.section-gap { margin-bottom: var(--app-sp-6); }

.view-toggle { display: flex; border: 1px solid var(--app-border); border-radius: var(--app-radius-md); overflow: hidden; }
.head-extra { display: flex; align-items: center; gap: var(--app-sp-3); }
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
  border-radius: var(--app-radius-lg); padding: var(--app-sp-5);
  transition: all var(--app-transition-fast);
}
.schedule-card:hover { border-color: var(--app-border); box-shadow: var(--app-shadow-sm); }
.schedule-card__head { display: flex; align-items: center; gap: var(--app-sp-3); margin-bottom: var(--app-sp-4); }
.schedule-card__date { font-size: var(--app-fs-caption); color: var(--app-text-3); }
.schedule-card__body { display: flex; gap: var(--app-sp-6); }
.schedule-card__stat { display: flex; flex-direction: column; gap: 2px; }
.schedule-card__lab { font-size: var(--app-fs-tiny); color: var(--app-text-3); }
.schedule-card__val { font-size: var(--app-fs-h3); font-weight: 600; color: var(--app-text-1); font-variant-numeric: tabular-nums; }
.schedule-card__footer { display: flex; justify-content: flex-end; gap: var(--app-sp-2); margin-top: var(--app-sp-4); padding-top: var(--app-sp-3); border-top: 1px solid var(--app-border-light); }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }

.dialog-origin { font-size: var(--app-fs-caption); color: var(--app-text-2); background: var(--app-bg-page); border-radius: var(--app-radius-md); padding: var(--app-sp-3); margin-bottom: var(--app-sp-4); }
.dialog-hint { font-size: var(--app-fs-tiny); color: var(--app-text-3); margin: 0 0 var(--app-sp-3); }

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
