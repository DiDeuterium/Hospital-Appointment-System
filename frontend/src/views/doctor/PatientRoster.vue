<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPatientsBySchedule } from '@/api/doctor'
import { APPT_STATUS, APPT_STATUS_LABEL, APPT_STATUS_TAG_TYPE } from '@/utils/constants'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'

const route = useRoute()
const router = useRouter()
const list = ref([])
const loading = ref(false)
const statusFilter = ref('')

const filtered = computed(() => {
  if (!statusFilter.value) return list.value
  return list.value.filter(p => String(p.status) === statusFilter.value)
})

async function load() {
  loading.value = true
  try {
    list.value = await listPatientsBySchedule(route.params.scheduleId)
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="患者名册"
      :subtitle="'排班 #' + route.params.scheduleId + ' · 共 ' + list.length + ' 人'"
      :breadcrumbs="[
        { label: '工作台', to: '/doctor' },
        { label: '我的排班', to: '/doctor/schedules' },
        { label: '患者名册' }
      ]"
    />

    <!-- 状态 Tab -->
    <div class="tabs">
      <button class="tabs__btn" :class="{ 'is-active': !statusFilter }" @click="statusFilter = ''">全部 ({{ list.length }})</button>
      <button
        class="tabs__btn"
        :class="{ 'is-active': statusFilter === String(APPT_STATUS.BOOKED) }"
        @click="statusFilter = String(APPT_STATUS.BOOKED)"
      >待就诊 ({{ list.filter(p => p.status === APPT_STATUS.BOOKED).length }})</button>
      <button
        class="tabs__btn"
        :class="{ 'is-active': statusFilter === String(APPT_STATUS.FINISHED) }"
        @click="statusFilter = String(APPT_STATUS.FINISHED)"
      >已就诊 ({{ list.filter(p => p.status === APPT_STATUS.FINISHED).length }})</button>
      <button
        class="tabs__btn"
        :class="{ 'is-active': statusFilter === String(APPT_STATUS.CANCELLED) }"
        @click="statusFilter = String(APPT_STATUS.CANCELLED)"
      >已取消 ({{ list.filter(p => p.status === APPT_STATUS.CANCELLED).length }})</button>
    </div>

    <!-- 患者表格 -->
    <div v-loading="loading" class="table-wrap">
      <table class="pt-table" v-if="filtered.length">
        <thead>
          <tr>
            <th>预约号</th>
            <th>姓名</th>
            <th>手机号</th>
            <th>状态</th>
            <th>挂号时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in filtered" :key="p.apptId">
            <td><span class="pt-table__appt">#{{ p.apptId }}</span></td>
            <td class="pt-table__name">{{ p.realName }}</td>
            <td>{{ (p.phone || '').replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</td>
            <td>
              <StatusTag :type="APPT_STATUS_TAG_TYPE[p.status] === 'primary' ? 'primary' : APPT_STATUS_TAG_TYPE[p.status] === 'success' ? 'success' : 'default'">
                {{ APPT_STATUS_LABEL[p.status] || '未知' }}
              </StatusTag>
            </td>
            <td class="pt-table__time">{{ p.createTime || '—' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无患者</div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }

.tabs { display: flex; gap: var(--app-sp-2); margin-bottom: var(--app-sp-6); }
.tabs__btn {
  border: 1px solid var(--app-border-light); background: var(--app-bg-elevated);
  border-radius: var(--app-radius-full); padding: var(--app-sp-2) var(--app-sp-4);
  font-size: var(--app-fs-caption); font-weight: 500; color: var(--app-text-2);
  cursor: pointer; transition: all var(--app-transition-fast);
}
.tabs__btn:hover { border-color: var(--app-brand-400); color: var(--app-brand-600); }
.tabs__btn.is-active { background: var(--app-brand-500); color: #fff; border-color: var(--app-brand-500); }

.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow: hidden; }
.pt-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.pt-table th {
  text-align: left; padding: var(--app-sp-3) var(--app-sp-5);
  background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption);
  border-bottom: 1px solid var(--app-border);
}
.pt-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.pt-table tr:last-child td { border-bottom: none; }
.pt-table tr:hover td { background: var(--app-bg-hover); }
.pt-table__appt { color: var(--app-brand-600); font-weight: 500; font-variant-numeric: tabular-nums; }
.pt-table__name { font-weight: 500; color: var(--app-text-1); }
.pt-table__time { color: var(--app-text-3); font-size: var(--app-fs-caption); white-space: nowrap; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }
</style>
