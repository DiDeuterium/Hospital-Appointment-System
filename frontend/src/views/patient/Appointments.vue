<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyAppointments, cancelAppointment } from '@/api/appointment'
import { useUserStore } from '@/stores/user'
import { APPT_STATUS, APPT_STATUS_LABEL, APPT_STATUS_TAG_TYPE } from '@/utils/constants'
import { deptIcon, SHIFT_TIME_MAP } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const user = useUserStore()
const list = ref([])
const status = ref('')
const loading = ref(false)

const statusTabs = [
  { key: '',       label: '全部' },
  { key: String(APPT_STATUS.BOOKED),     label: '待就诊' },
  { key: String(APPT_STATUS.FINISHED),   label: '已完成' },
  { key: String(APPT_STATUS.CANCELLED),  label: '已取消' }
]

const counts = computed(() => {
  const map = {}
  for (const t of statusTabs) {
    map[t.key] = t.key
      ? list.value.filter(a => String(a.status) === t.key).length
      : list.value.length
  }
  return map
})

const tagTypeMap = {
  [APPT_STATUS.BOOKED]: 'primary',
  [APPT_STATUS.FINISHED]: 'success',
  [APPT_STATUS.CANCELLED]: 'default'
}

async function load() {
  const pid = user.profile?.patientId
  if (!pid) { list.value = []; return }
  loading.value = true
  try {
    const params = status.value ? { status: Number(status.value) } : {}
    list.value = await listMyAppointments(pid, params)
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
    await cancelAppointment(row.apptId)
    ElMessage.success('已取消')
    load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

function viewDetail(row) {
  sessionStorage.setItem('hospital:apptDetail', JSON.stringify(row))
  router.push({ name: 'PatientApptDetail', params: { apptId: row.apptId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="我的预约"
      :subtitle="'共 ' + list.length + ' 条预约记录'"
      :breadcrumbs="[{ label: '首页', to: '/patient/home' }, { label: '我的预约' }]"
    />

    <!-- 状态 Tab -->
    <div class="tabs">
      <button
        v-for="t in statusTabs"
        :key="t.key"
        type="button"
        class="tabs__btn"
        :class="{ 'is-active': status === t.key }"
        @click="status = t.key; load()"
      >
        {{ t.label }}
        <span class="tabs__count">{{ counts[t.key] || 0 }}</span>
      </button>
    </div>

    <!-- 卡片列表 -->
    <div v-loading="loading" class="appt-list">
      <template v-if="list.length">
        <article
          v-for="a in list"
          :key="a.apptId"
          class="appt-card"
          :class="{ 'is-cancelled': a.status === APPT_STATUS.CANCELLED }"
          @click="viewDetail(a)"
        >
          <div class="appt-card__head">
            <span class="appt-card__dept-icon">{{ deptIcon(a.deptName) }}</span>
            <div class="appt-card__title">
              <span class="appt-card__dept">{{ a.deptName }}</span>
              <span class="appt-card__doc">{{ a.docName }}</span>
            </div>
            <StatusTag :type="tagTypeMap[a.status] || 'info'">
              {{ APPT_STATUS_LABEL[a.status] || '未知' }}
            </StatusTag>
          </div>

          <div class="appt-card__info">
            <div class="appt-card__info-item">
              <AppIcon name="calendar" :size="14" />
              <span>{{ a.workDate }} {{ a.shift }}</span>
            </div>
            <div class="appt-card__info-item">
              <AppIcon name="clock" :size="14" />
              <span>{{ SHIFT_TIME_MAP[a.shift] || '' }}</span>
            </div>
            <div class="appt-card__info-item">
              <span class="appt-card__label">预约号</span>
              <span>#{{ a.apptId }}</span>
            </div>
          </div>

          <footer v-if="a.status === APPT_STATUS.BOOKED" class="appt-card__footer" @click.stop>
            <el-button size="small" plain @click="cancel(a)">取消预约</el-button>
          </footer>
        </article>
      </template>

      <EmptyState
        v-else-if="!loading"
        title="暂无预约记录"
        :description="status ? '当前筛选条件下没有预约' : '还没有预约过，去首页开始挂号吧'"
      >
        <template v-if="!status" #action>
          <el-button type="primary" @click="router.push('/patient/departments')">去挂号</el-button>
        </template>
      </EmptyState>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}

/* ---- Tab ---- */
.tabs {
  display: flex;
  gap: var(--app-sp-2);
  margin-bottom: var(--app-sp-6);
}
.tabs__btn {
  border: 1px solid var(--app-border-light);
  background: var(--app-bg-elevated);
  border-radius: var(--app-radius-full);
  padding: var(--app-sp-2) var(--app-sp-4);
  font-size: var(--app-fs-caption);
  font-weight: 500;
  color: var(--app-text-2);
  cursor: pointer;
  transition: all var(--app-transition-fast);
  display: flex;
  align-items: center;
  gap: 6px;
}
.tabs__btn:hover { border-color: var(--app-brand-400); color: var(--app-brand-600); }
.tabs__btn.is-active {
  background: var(--app-brand-500);
  color: #fff;
  border-color: var(--app-brand-500);
}
.tabs__count {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

/* ---- 卡片 ---- */
.appt-list {
  display: flex;
  flex-direction: column;
  gap: var(--app-sp-4);
}
.appt-card {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-5);
  cursor: pointer;
  transition: all var(--app-transition-fast);
}
.appt-card:hover {
  border-color: var(--app-border);
  box-shadow: var(--app-shadow-sm);
  transform: translateY(-1px);
}
.appt-card.is-cancelled {
  opacity: 0.6;
}
.appt-card.is-cancelled:hover { opacity: 0.8; }

.appt-card__head {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  margin-bottom: var(--app-sp-4);
}
.appt-card__dept-icon { font-size: 24px; flex-shrink: 0; }
.appt-card__title { flex: 1; display: flex; align-items: baseline; gap: var(--app-sp-2); min-width: 0; }
.appt-card__dept { font-size: var(--app-fs-h3); font-weight: 600; color: var(--app-text-1); }
.appt-card__doc  { font-size: var(--app-fs-caption); color: var(--app-text-3); }

.appt-card__info {
  display: flex;
  flex-wrap: wrap;
  gap: var(--app-sp-2) var(--app-sp-5);
  margin-bottom: var(--app-sp-3);
}
.appt-card__info-item {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  font-size: var(--app-fs-caption);
  color: var(--app-text-2);
}
.appt-card__label { color: var(--app-text-3); }
.appt-card__footer {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--app-sp-3);
  border-top: 1px solid var(--app-border-light);
}
</style>
