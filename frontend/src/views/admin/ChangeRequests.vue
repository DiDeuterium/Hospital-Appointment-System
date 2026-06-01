<script setup>
import { onMounted, onBeforeUnmount, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listChangeRequests, approveChangeRequest } from '@/api/changeRequest'
import { listAdminSchedules } from '@/api/schedule'
import {
  CHANGE_TYPE, CHANGE_TYPE_LABEL,
  CHANGE_STATUS, CHANGE_STATUS_LABEL, CHANGE_STATUS_TAG_TYPE
} from '@/utils/constants'
import { formatDateTime } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const list = ref([])
const scheduleMap = ref({})
const loading = ref(false)
const tab = ref(String(CHANGE_STATUS.PENDING))
const actingId = ref(null)

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

function origin(scheduleId) {
  return scheduleMap.value[scheduleId] || null
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
  loading.value = true
  try {
    const [reqs, schedules] = await Promise.all([
      listChangeRequests({}),
      listAdminSchedules().catch(() => [])
    ])
    list.value = reqs || []
    const map = {}
    for (const s of (schedules || [])) map[s.scheduleId] = s
    scheduleMap.value = map
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

async function audit(r, approved) {
  const title = approved ? '通过申请' : '驳回申请'
  try {
    const { value } = await ElMessageBox.prompt(`请填写审核意见（${title}）`, title, {
      lockScroll: false,
      inputType: 'textarea',
      inputPlaceholder: '审核意见为必填',
      inputValidator: (v) => (v && v.trim() ? true : '请填写审核意见'),
      confirmButtonText: approved ? '通过' : '驳回',
      cancelButtonText: '取消'
    })
    actingId.value = r.requestId
    await approveChangeRequest(r.requestId, { approved: approved ? 1 : 0, remark: value.trim() })
    ElMessage.success(approved ? '已通过' : '已驳回')
    await load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  } finally {
    actingId.value = null
  }
}

const now = ref(new Date())
const clock = computed(() => {
  const d = now.value
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0') + ' ' + d.toTimeString().slice(0, 8)
})
let clockTimer = null

onMounted(() => {
  load()
  clockTimer = setInterval(() => { now.value = new Date() }, 1000)
})
onBeforeUnmount(() => { if (clockTimer) clearInterval(clockTimer) })
</script>

<template>
  <div class="page-container">
    <PageHeader title="排班变更审核" :subtitle="'共 ' + list.length + ' 条申请'">
      <template #extra>
        <time class="page-clock">{{ clock }}</time>
      </template>
    </PageHeader>

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

    <div v-loading="loading" class="table-wrap">
      <table class="cr-table" v-if="filtered.length">
        <thead>
          <tr><th>医生</th><th>原排班</th><th>变更类型</th><th>变更内容</th><th>原因</th><th>申请时间</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="r in filtered" :key="r.requestId">
            <td class="cr-table__name">{{ origin(r.scheduleId)?.docName || '—' }}</td>
            <td>
              <template v-if="origin(r.scheduleId)">
                {{ origin(r.scheduleId).workDate }} {{ origin(r.scheduleId).shift }}
              </template>
              <template v-else>排班 #{{ r.scheduleId }}</template>
            </td>
            <td>
              <StatusTag :type="r.changeType === CHANGE_TYPE.CANCEL ? 'warning' : 'primary'" size="small">
                {{ CHANGE_TYPE_LABEL[r.changeType] || '变更' }}
              </StatusTag>
            </td>
            <td class="cr-table__target">{{ targetText(r) }}</td>
            <td class="cr-table__reason">{{ r.reason }}</td>
            <td class="cr-table__time">{{ formatDateTime(r.applyTime) }}</td>
            <td>
              <StatusTag :type="CHANGE_STATUS_TAG_TYPE[r.status] || 'info'" size="small">
                {{ CHANGE_STATUS_LABEL[r.status] || '未知' }}
              </StatusTag>
            </td>
            <td class="cr-table__actions">
              <template v-if="r.status === CHANGE_STATUS.PENDING">
                <el-button size="small" type="primary" :loading="actingId === r.requestId" @click="audit(r, true)">通过</el-button>
                <el-button size="small" type="danger" plain :loading="actingId === r.requestId" @click="audit(r, false)">驳回</el-button>
              </template>
              <span v-else class="cr-table__remark">{{ r.auditRemark || '—' }}</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无申请</div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }
.page-clock { font-size: var(--app-fs-body); color: var(--app-text-3); font-variant-numeric: tabular-nums; white-space: nowrap; }

.tabs { display: flex; gap: var(--app-sp-2); margin-bottom: var(--app-sp-6); flex-wrap: wrap; }
.tabs__btn {
  border: 1px solid var(--app-border-light); background: var(--app-bg-elevated);
  border-radius: var(--app-radius-full); padding: var(--app-sp-2) var(--app-sp-4);
  font-size: var(--app-fs-caption); font-weight: 500; color: var(--app-text-2);
  cursor: pointer; transition: all var(--app-transition-fast);
}
.tabs__btn:hover { border-color: var(--app-brand-400); color: var(--app-brand-600); }
.tabs__btn.is-active { background: var(--app-brand-500); color: #fff; border-color: var(--app-brand-500); }

.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow-x: auto; }
.cr-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.cr-table th { text-align: left; padding: var(--app-sp-3) var(--app-sp-5); background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption); border-bottom: 1px solid var(--app-border); white-space: nowrap; }
.cr-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.cr-table tr:last-child td { border-bottom: none; }
.cr-table tr:hover td { background: var(--app-bg-hover); }
.cr-table__name { font-weight: 500; color: var(--app-text-1); white-space: nowrap; }
.cr-table__target { font-size: var(--app-fs-caption); }
.cr-table__reason { color: var(--app-text-3); font-size: var(--app-fs-caption); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cr-table__time { color: var(--app-text-3); font-size: var(--app-fs-caption); white-space: nowrap; }
.cr-table__actions { white-space: nowrap; }
.cr-table__remark { color: var(--app-text-3); font-size: var(--app-fs-caption); }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }
</style>
