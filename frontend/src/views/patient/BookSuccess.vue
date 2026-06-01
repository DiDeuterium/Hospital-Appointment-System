<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { peekResult, clearBooking, formatDateTime, formatFee } from '@/utils/booking'
import { payAppointment } from '@/api/appointment'
import { PAY_STATUS, PAY_STATUS_LABEL, PAY_STATUS_TAG_TYPE } from '@/utils/constants'
import StatusTag from '@/components/StatusTag.vue'
import StepFlow from '@/components/StepFlow.vue'
import SectionCard from '@/components/SectionCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const result = ref(peekResult())
const paying = ref(false)

const expired = computed(() => !result.value)

const steps = [
  { label: '选科室' },
  { label: '选医生' },
  { label: '选时段' },
  { label: '确认' },
  { label: '完成' }
]

async function doPay() {
  if (!result.value) return
  paying.value = true
  try {
    await payAppointment(result.value.apptId)
    result.value = { ...result.value, payStatus: PAY_STATUS.PAID, payTime: new Date().toISOString() }
    ElMessage.success('挂号费支付成功')
  } catch { /* 拦截器已弹错误 */ } finally {
    paying.value = false
  }
}

function goHome() { clearBooking(); router.replace({ name: 'PatientHome' }) }
function goAppts() { clearBooking(); router.push({ name: 'PatientAppointments' }) }
</script>

<template>
  <div class="page-container">
    <template v-if="expired">
      <EmptyState title="暂无挂号记录" description="请从排班列表选择后完成挂号为查看成功页。">
        <template #action>
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </template>
      </EmptyState>
    </template>

    <template v-else>
      <StepFlow :steps="steps" :current="4" class="section-gap" />

      <!-- 大对勾 -->
      <div class="mark">
        <svg width="56" height="56" viewBox="0 0 80 80">
          <circle cx="40" cy="40" r="40" fill="#10B981" />
          <path d="M24 40l12 12 20-24" stroke="#fff" stroke-width="5" fill="none" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <h1 class="mark__title">预约挂号成功</h1>
        <p class="mark__appt" v-if="result?.queueNumber != null">
          您的就诊排队号是 <strong>{{ result.queueNumber }}</strong> 号
        </p>
        <p class="mark__appt" v-else>您的预约号是 <strong>#{{ result?.apptId }}</strong></p>
      </div>

      <SectionCard class="section-gap">
        <div class="info-table">
          <div class="info-table__row">
            <span class="info-table__label">状态</span>
            <span class="info-table__value"><StatusTag type="success">待就诊</StatusTag></span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">预约单号</span>
            <span class="info-table__value">#{{ result?.apptId }}</span>
          </div>
          <div class="info-table__row" v-if="result?.queueNumber != null">
            <span class="info-table__label">排队号</span>
            <span class="info-table__value">{{ result.queueNumber }} 号</span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">挂号费</span>
            <span class="info-table__value info-table__fee">{{ formatFee(result?.fee ?? result?.amount) }}</span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">支付状态</span>
            <span class="info-table__value">
              <StatusTag :type="PAY_STATUS_TAG_TYPE[result?.payStatus] || 'warning'">
                {{ PAY_STATUS_LABEL[result?.payStatus] ?? '待支付' }}
              </StatusTag>
              <el-button
                v-if="result?.payStatus === PAY_STATUS.UNPAID"
                type="primary"
                size="small"
                :loading="paying"
                style="margin-left: 12px"
                @click="doPay"
              >模拟支付</el-button>
            </span>
          </div>
          <div class="info-table__row" v-if="result?.createTime">
            <span class="info-table__label">挂号时间</span>
            <span class="info-table__value">{{ formatDateTime(result.createTime) }}</span>
          </div>
        </div>
      </SectionCard>

      <SectionCard title="温馨提示" class="section-gap">
        <ul class="tips">
          <li class="tips__item"><span class="tips__dot" />请于就诊当天提前 15 分钟到院</li>
          <li class="tips__item"><span class="tips__dot" />出示身份证至自助机取号</li>
          <li class="tips__item"><span class="tips__dot" />如需取消，可在"我的预约"中操作</li>
        </ul>
      </SectionCard>

      <div class="actions">
        <el-button size="large" @click="goHome">返回首页</el-button>
        <el-button size="large" type="primary" @click="goAppts">查看我的预约</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.section-gap { margin-bottom: var(--app-sp-6); }

/* ---- 成功标记 ---- */
.mark {
  text-align: center;
  margin-bottom: var(--app-sp-8);
}
.mark__title {
  font-size: var(--app-fs-h1);
  color: var(--app-text-1);
  margin: var(--app-sp-5) 0 var(--app-sp-3);
  font-weight: 600;
}
.mark__appt {
  font-size: var(--app-fs-body);
  color: var(--app-text-3);
}
.mark__appt strong {
  color: var(--app-brand-600);
  font-size: var(--app-fs-h3);
}

.info-table__row {
  display: flex;
  padding: var(--app-sp-3) 0;
}
.info-table__row + .info-table__row { border-top: 1px solid var(--app-border-light); }
.info-table__label {
  width: 100px;
  font-size: var(--app-fs-body);
  color: var(--app-text-3);
  flex-shrink: 0;
}
.info-table__value {
  font-size: var(--app-fs-body);
  color: var(--app-text-1);
  font-weight: 500;
}
.info-table__fee { color: var(--app-danger-text); font-weight: 600; }

.tips { list-style: none; padding: 0; margin: 0; }
.tips__item {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  padding: 10px 0;
  color: var(--app-text-2);
  font-size: var(--app-fs-body);
}
.tips__item + .tips__item { border-top: 1px solid var(--app-border-light); }
.tips__dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--app-brand-500);
  flex-shrink: 0;
}

.actions {
  display: flex;
  gap: var(--app-sp-4);
  justify-content: center;
  margin-top: var(--app-sp-8);
}
</style>
