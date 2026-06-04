<script setup>
import { onMounted, onBeforeUnmount, ref, reactive, computed } from 'vue'
import { listPayments } from '@/api/payment'
import { PAY_STATUS, PAY_STATUS_LABEL, PAY_STATUS_TAG_TYPE } from '@/utils/constants'
import { formatDateTime, formatFee } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'

const list = ref([])
const loading = ref(false)
const filters = reactive({ payStatus: '', range: [] })

const payStatusOptions = [
  { value: PAY_STATUS.UNPAID, label: '待支付' },
  { value: PAY_STATUS.PAID, label: '已支付' },
  { value: PAY_STATUS.CLOSED, label: '已关闭' }
]

const totalPaid = computed(() =>
  list.value
    .filter(p => p.payStatus === PAY_STATUS.PAID)
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)
)

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filters.payStatus !== '' && filters.payStatus != null) params.payStatus = filters.payStatus
    if (filters.range && filters.range.length === 2) {
      params.startTime = filters.range[0]
      params.endTime = filters.range[1]
    }
    list.value = await listPayments(params)
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

function reset() {
  filters.payStatus = ''
  filters.range = []
  load()
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
    <PageHeader title="支付记录" :subtitle="'共 ' + list.length + ' 条记录 · 已支付合计 ' + formatFee(totalPaid)">
      <template #extra>
        <time class="page-clock">{{ clock }}</time>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-select v-model="filters.payStatus" placeholder="支付状态" clearable size="large" class="toolbar__sel" @change="load">
        <el-option v-for="o in payStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-date-picker
        v-model="filters.range"
        type="datetimerange"
        value-format="YYYY-MM-DDTHH:mm:ss"
        range-separator="至"
        start-placeholder="创建时间起"
        end-placeholder="创建时间止"
        size="large"
        @change="load"
      />
      <el-button size="large" @click="reset">清除</el-button>
    </div>

    <p class="note">
      <AppIcon name="info" :size="14" />
      支付记录按预约单号关联，需患者/医生明细可在「预约」相关页面按预约单号核对。
    </p>

    <div v-loading="loading" class="table-wrap">
      <table class="pay-table" v-if="list.length">
        <thead>
          <tr><th>预约单号</th><th>金额</th><th>支付状态</th><th>支付方式</th><th>支付时间</th><th>创建时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="p in list" :key="p.paymentId">
            <td><span class="pay-table__appt">#{{ p.apptId }}</span></td>
            <td class="pay-table__amount">{{ formatFee(p.amount) }}</td>
            <td>
              <StatusTag :type="PAY_STATUS_TAG_TYPE[p.payStatus] || 'warning'" size="small">
                {{ PAY_STATUS_LABEL[p.payStatus] ?? '待支付' }}
              </StatusTag>
            </td>
            <td>{{ p.payMethod || '模拟支付' }}</td>
            <td class="pay-table__time">{{ p.payTime ? formatDateTime(p.payTime) : '—' }}</td>
            <td class="pay-table__time">{{ formatDateTime(p.createTime) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无支付记录</div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }
.page-clock { font-size: var(--app-fs-body); color: var(--app-text-3); font-variant-numeric: tabular-nums; white-space: nowrap; }
.toolbar { display: flex; gap: var(--app-sp-3); margin-bottom: var(--app-sp-3); flex-wrap: wrap; align-items: center; }
.toolbar__sel { width: 160px; }
.note { display: flex; align-items: center; gap: 6px; font-size: var(--app-fs-caption); color: var(--app-text-3); margin: 0 0 var(--app-sp-5); }

.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow-x: auto; }
.pay-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.pay-table th { text-align: left; padding: var(--app-sp-3) var(--app-sp-5); background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption); border-bottom: 1px solid var(--app-border); white-space: nowrap; }
.pay-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.pay-table tr:last-child td { border-bottom: none; }
.pay-table tr:hover td { background: var(--app-bg-hover); }
.pay-table__appt { color: var(--app-brand-600); font-weight: 500; font-variant-numeric: tabular-nums; }
.pay-table__amount { color: var(--app-danger-text); font-weight: 600; font-variant-numeric: tabular-nums; }
.pay-table__time { color: var(--app-text-3); font-size: var(--app-fs-caption); white-space: nowrap; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }
</style>
