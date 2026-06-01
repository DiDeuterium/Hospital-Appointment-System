<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyChangeRequests, withdrawChangeRequest } from '@/api/changeRequest'
import { myDoctorSchedules } from '@/api/doctor'
import { useUserStore } from '@/stores/user'
import {
  CHANGE_TYPE, CHANGE_TYPE_LABEL,
  CHANGE_STATUS, CHANGE_STATUS_LABEL, CHANGE_STATUS_TAG_TYPE
} from '@/utils/constants'
import { formatDateTime } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const user = useUserStore()
const list = ref([])
const scheduleMap = ref({})
const loading = ref(false)
const tab = ref('')
const withdrawingId = ref(null)

const statusTabs = [
  { key: '', label: '全部' },
  { key: String(CHANGE_STATUS.PENDING), label: '待审核' },
  { key: String(CHANGE_STATUS.APPROVED), label: '已通过' },
  { key: String(CHANGE_STATUS.REJECTED), label: '已驳回' },
  { key: String(CHANGE_STATUS.WITHDRAWN), label: '已撤回' }
]

const filtered = computed(() => {
  if (!tab.value) return list.value
  return list.value.filter(r => String(r.status) === tab.value)
})

function originText(scheduleId) {
  const s = scheduleMap.value[scheduleId]
  return s ? `${s.workDate} ${s.shift}` : `排班 #${scheduleId}`
}

function targetText(r) {
  if (r.changeType === CHANGE_TYPE.CANCEL) return '申请停诊'
  const parts = []
  if (r.targetWorkDate) parts.push(`日期→${r.targetWorkDate}`)
  if (r.targetShift) parts.push(`时段→${r.targetShift}`)
  if (r.targetTotalQuota != null) parts.push(`总号源→${r.targetTotalQuota}`)
  return parts.length ? parts.join('，') : '修改排班'
}

async function load() {
  const docId = user.profile?.docId
  if (!docId) { list.value = []; return }
  loading.value = true
  try {
    const [reqs, schedules] = await Promise.all([
      listMyChangeRequests({}),
      myDoctorSchedules(docId, {}).catch(() => [])
    ])
    list.value = reqs || []
    const map = {}
    for (const s of (schedules || [])) map[s.scheduleId] = s
    scheduleMap.value = map
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

async function withdraw(r) {
  try {
    await ElMessageBox.confirm('确认撤回该申请？', '提示', { type: 'warning', lockScroll: false })
    withdrawingId.value = r.requestId
    await withdrawChangeRequest(r.requestId)
    ElMessage.success('已撤回')
    await load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  } finally {
    withdrawingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="我的申请"
      :subtitle="'共 ' + list.length + ' 条排班变更申请'"
      :breadcrumbs="[{ label: '工作台', to: '/doctor' }, { label: '我的申请' }]"
    />

    <div class="tabs">
      <button
        v-for="t in statusTabs"
        :key="t.key"
        type="button"
        class="tabs__btn"
        :class="{ 'is-active': tab === t.key }"
        @click="tab = t.key"
      >{{ t.label }}</button>
    </div>

    <div v-loading="loading" class="req-list">
      <template v-if="filtered.length">
        <article v-for="r in filtered" :key="r.requestId" class="req-card">
          <div class="req-card__head">
            <StatusTag :type="r.changeType === CHANGE_TYPE.CANCEL ? 'warning' : 'primary'" size="small">
              {{ CHANGE_TYPE_LABEL[r.changeType] || '变更' }}
            </StatusTag>
            <span class="req-card__origin">原排班：{{ originText(r.scheduleId) }}</span>
            <StatusTag :type="CHANGE_STATUS_TAG_TYPE[r.status] || 'info'">
              {{ CHANGE_STATUS_LABEL[r.status] || '未知' }}
            </StatusTag>
          </div>

          <div class="req-card__body">
            <div class="req-card__line"><span class="req-card__label">变更内容</span>{{ targetText(r) }}</div>
            <div class="req-card__line"><span class="req-card__label">申请原因</span>{{ r.reason }}</div>
            <div class="req-card__line"><span class="req-card__label">申请时间</span>{{ formatDateTime(r.applyTime) }}</div>
            <div v-if="r.auditTime" class="req-card__line">
              <span class="req-card__label">审核意见</span>{{ r.auditRemark || '—' }}（{{ formatDateTime(r.auditTime) }}）
            </div>
          </div>

          <footer v-if="r.status === CHANGE_STATUS.PENDING" class="req-card__footer">
            <el-button size="small" type="danger" plain :loading="withdrawingId === r.requestId" @click="withdraw(r)">
              撤回申请
            </el-button>
          </footer>
        </article>
      </template>
      <EmptyState v-else-if="!loading" title="暂无申请" description="你还没有提交过排班变更申请，可在「我的排班」中发起" />
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }

.tabs { display: flex; gap: var(--app-sp-2); margin-bottom: var(--app-sp-6); flex-wrap: wrap; }
.tabs__btn {
  border: 1px solid var(--app-border-light); background: var(--app-bg-elevated);
  border-radius: var(--app-radius-full); padding: var(--app-sp-2) var(--app-sp-4);
  font-size: var(--app-fs-caption); font-weight: 500; color: var(--app-text-2);
  cursor: pointer; transition: all var(--app-transition-fast);
}
.tabs__btn:hover { border-color: var(--app-brand-400); color: var(--app-brand-600); }
.tabs__btn.is-active { background: var(--app-brand-500); color: #fff; border-color: var(--app-brand-500); }

.req-list { display: flex; flex-direction: column; gap: var(--app-sp-4); }
.req-card {
  background: var(--app-bg-elevated); border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg); padding: var(--app-sp-5);
}
.req-card__head { display: flex; align-items: center; gap: var(--app-sp-3); margin-bottom: var(--app-sp-4); }
.req-card__origin { flex: 1; font-size: var(--app-fs-body); font-weight: 600; color: var(--app-text-1); }
.req-card__body { display: flex; flex-direction: column; gap: var(--app-sp-2); }
.req-card__line { font-size: var(--app-fs-caption); color: var(--app-text-2); }
.req-card__label { display: inline-block; width: 64px; color: var(--app-text-3); }
.req-card__footer { display: flex; justify-content: flex-end; padding-top: var(--app-sp-3); margin-top: var(--app-sp-3); border-top: 1px solid var(--app-border-light); }
</style>
