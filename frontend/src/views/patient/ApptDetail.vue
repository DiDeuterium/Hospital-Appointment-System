<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelAppointment, payAppointment } from '@/api/appointment'
import {
  APPT_STATUS, APPT_STATUS_LABEL,
  PAY_STATUS, PAY_STATUS_LABEL, PAY_STATUS_TAG_TYPE
} from '@/utils/constants'
import { SHIFT_TIME_MAP, deptIcon, formatDateTime, formatFee } from '@/utils/booking'
import { useUserStore } from '@/stores/user'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import SectionCard from '@/components/SectionCard.vue'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

// 从 sessionStorage 恢复（从 Appointments 列表点进来时携带）
const appt = ref(null)
const cancelLoading = ref(false)
const payLoading = ref(false)

onMounted(() => {
  try {
    const raw = sessionStorage.getItem('hospital:apptDetail')
    if (raw) appt.value = JSON.parse(raw)
  } catch { /* 解析失败 */ }
})

const tagTypeMap = {
  [APPT_STATUS.BOOKED]: 'primary',
  [APPT_STATUS.FINISHED]: 'success',
  [APPT_STATUS.EXPIRED]: 'warning',
  [APPT_STATUS.CANCELLED]: 'default'
}

// 就地同步 sessionStorage，保证返回列表再进来数据一致
function syncStash() {
  try { sessionStorage.setItem('hospital:apptDetail', JSON.stringify(appt.value)) } catch { /* ignore */ }
}

async function doCancel() {
  if (!appt.value) return
  try {
    const { value } = await ElMessageBox.prompt('确认取消该预约？可填写取消原因（选填）', '取消预约', {
      type: 'warning',
      lockScroll: false,
      inputPlaceholder: '取消原因（选填）',
      inputType: 'textarea',
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想'
    })
    cancelLoading.value = true
    await cancelAppointment(appt.value.apptId, value)
    ElMessage.success('已取消')
    appt.value = {
      ...appt.value,
      status: APPT_STATUS.CANCELLED,
      cancelReason: value || appt.value.cancelReason,
      updateTime: new Date().toISOString()
    }
    syncStash()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  } finally {
    cancelLoading.value = false
  }
}

async function doPay() {
  if (!appt.value) return
  payLoading.value = true
  try {
    await payAppointment(appt.value.apptId)
    ElMessage.success('挂号费支付成功')
    appt.value = { ...appt.value, payStatus: PAY_STATUS.PAID, payTime: new Date().toISOString() }
    syncStash()
  } catch { /* 拦截器已弹错误 */ } finally {
    payLoading.value = false
  }
}

const tips = [
  '请于就诊当天提前 15 分钟到院',
  '出示身份证至自助机取号',
  '如需取消，可在"我的预约"中操作，请提前至少 2 小时'
]
</script>

<template>
  <div class="page-container">
    <template v-if="appt">
      <PageHeader
        :title="appt.deptName || '预约详情'"
        :breadcrumbs="[
          { label: '首页', to: '/patient/home' },
          { label: '我的预约', to: '/patient/appointments' },
          { label: '#' + appt.apptId }
        ]"
      >
        <template #extra>
          <StatusTag :type="tagTypeMap[appt.status] || 'info'" size="large">
            {{ APPT_STATUS_LABEL[appt.status] || '未知' }}
          </StatusTag>
        </template>
      </PageHeader>

      <!-- 核心信息卡 -->
      <SectionCard class="section-gap">
        <div class="hero">
          <span class="hero__icon">{{ deptIcon(appt.deptName) }}</span>
          <div class="hero__main">
            <div class="hero__dept">{{ appt.deptName }} · {{ appt.docName }}</div>
            <div class="hero__appt">预约号 #{{ appt.apptId }}</div>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-grid__item">
            <div class="info-grid__label">就诊日期</div>
            <div class="info-grid__value">{{ appt.workDate }}</div>
          </div>
          <div class="info-grid__item">
            <div class="info-grid__label">就诊时段</div>
            <div class="info-grid__value">{{ appt.shift }}（{{ SHIFT_TIME_MAP[appt.shift] || '' }}）</div>
          </div>
          <div v-if="appt.queueNumber != null" class="info-grid__item">
            <div class="info-grid__label">就诊排队号</div>
            <div class="info-grid__value info-grid__queue">{{ appt.queueNumber }} 号</div>
          </div>
          <div class="info-grid__item">
            <div class="info-grid__label">挂号费</div>
            <div class="info-grid__value info-grid__fee">{{ formatFee(appt.fee ?? appt.amount) }}</div>
          </div>
          <div class="info-grid__item">
            <div class="info-grid__label">支付状态</div>
            <div class="info-grid__value">
              <StatusTag :type="PAY_STATUS_TAG_TYPE[appt.payStatus] || 'warning'">
                {{ PAY_STATUS_LABEL[appt.payStatus] ?? '待支付' }}
              </StatusTag>
            </div>
          </div>
          <div class="info-grid__item">
            <div class="info-grid__label">挂号时间</div>
            <div class="info-grid__value">{{ formatDateTime(appt.createTime) }}</div>
          </div>
          <div class="info-grid__item">
            <div class="info-grid__label">就诊人</div>
            <div class="info-grid__value">{{ user.displayName }}</div>
          </div>
        </div>

        <!-- 取消原因 / 更新时间（已取消时展示） -->
        <div v-if="appt.status === APPT_STATUS.CANCELLED" class="cancel-info">
          <div class="cancel-info__row">
            <span class="cancel-info__label">取消原因</span>
            <span class="cancel-info__value">{{ appt.cancelReason || '未填写' }}</span>
          </div>
          <div v-if="appt.updateTime" class="cancel-info__row">
            <span class="cancel-info__label">更新时间</span>
            <span class="cancel-info__value">{{ formatDateTime(appt.updateTime) }}</span>
          </div>
        </div>

        <!-- 凭证二维码占位 -->
        <div class="qrcode-placeholder">
          <div class="qrcode-placeholder__box">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3z"/>
              <rect x="4" y="4" width="5" height="5" fill="currentColor" />
              <rect x="15" y="4" width="5" height="5" fill="currentColor" />
              <rect x="4" y="15" width="5" height="5" fill="currentColor" />
            </svg>
          </div>
          <div class="qrcode-placeholder__text">就诊凭证二维码</div>
          <div class="qrcode-placeholder__hint">（系统预留，本期不实现）</div>
        </div>
      </SectionCard>

      <!-- 就诊须知 -->
      <SectionCard title="就诊温馨提示" class="section-gap">
        <ul class="tips">
          <li v-for="t in tips" :key="t" class="tips__item">
            <span class="tips__dot" />
            {{ t }}
          </li>
        </ul>
      </SectionCard>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button size="large" @click="router.back()">返回列表</el-button>
        <el-button
          v-if="appt.status === APPT_STATUS.BOOKED && appt.payStatus === PAY_STATUS.UNPAID"
          size="large"
          type="primary"
          :loading="payLoading"
          @click="doPay"
        >模拟支付</el-button>
        <el-button
          v-if="appt.status === APPT_STATUS.BOOKED"
          size="large"
          type="danger"
          :loading="cancelLoading"
          @click="doCancel"
        >取消预约</el-button>
      </div>
    </template>

    <!-- 未传入预约信息 -->
    <div v-else class="empty-page">
      <h2 class="empty-page__title">未找到预约信息</h2>
      <p class="empty-page__desc">请从"我的预约"列表进入此页面</p>
      <el-button type="primary" @click="router.replace({ name: 'PatientAppointments' })">返回我的预约</el-button>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.section-gap { margin-bottom: var(--app-sp-6); }

/* ---- 英雄区 ---- */
.hero {
  display: flex;
  align-items: center;
  gap: var(--app-sp-4);
  margin-bottom: var(--app-sp-6);
}
.hero__icon { font-size: 40px; flex-shrink: 0; }
.hero__dept { font-size: var(--app-fs-h2); font-weight: 600; color: var(--app-text-1); }
.hero__appt { font-size: var(--app-fs-caption); color: var(--app-text-3); margin-top: 4px; }

/* ---- 信息网格 ---- */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--app-sp-4);
  margin-bottom: var(--app-sp-6);
}
.info-grid__item {
  background: var(--app-bg-page);
  border-radius: var(--app-radius-md);
  padding: var(--app-sp-3) var(--app-sp-4);
}
.info-grid__label { font-size: var(--app-fs-caption); color: var(--app-text-3); margin-bottom: 4px; }
.info-grid__value { font-size: var(--app-fs-body); color: var(--app-text-1); font-weight: 500; }
.info-grid__queue { color: var(--app-brand-600); }
.info-grid__fee { color: var(--app-danger-text); }

/* ---- 取消信息 ---- */
.cancel-info {
  background: var(--app-bg-page);
  border-radius: var(--app-radius-md);
  padding: var(--app-sp-4);
  margin-bottom: var(--app-sp-6);
}
.cancel-info__row { display: flex; gap: var(--app-sp-3); font-size: var(--app-fs-caption); }
.cancel-info__row + .cancel-info__row { margin-top: var(--app-sp-2); }
.cancel-info__label { color: var(--app-text-3); flex-shrink: 0; width: 64px; }
.cancel-info__value { color: var(--app-text-1); }

/* ---- 二维码占位 ---- */
.qrcode-placeholder {
  text-align: center;
  padding: var(--app-sp-6) 0 var(--app-sp-4);
  border-top: 1px solid var(--app-border-light);
}
.qrcode-placeholder__box {
  width: 120px; height: 120px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-page);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius-md);
  margin-bottom: var(--app-sp-3);
  color: var(--app-text-4);
}
.qrcode-placeholder__text { font-size: var(--app-fs-caption); color: var(--app-text-2); }
.qrcode-placeholder__hint { font-size: var(--app-fs-tiny); color: var(--app-text-4); margin-top: 4px; }

/* ---- 须知 ---- */
.tips { list-style: none; padding: 0; margin: 0; }
.tips__item {
  display: flex; align-items: center; gap: var(--app-sp-2); padding: 10px 0;
  color: var(--app-text-2); font-size: var(--app-fs-caption);
}
.tips__item + .tips__item { border-top: 1px solid var(--app-border-light); }
.tips__dot { width: 5px; height: 5px; border-radius: 50%; background: var(--app-brand-500); flex-shrink: 0; }

/* ---- 按钮 ---- */
.actions { display: flex; gap: var(--app-sp-4); justify-content: center; margin-top: var(--app-sp-8); }

/* ---- 空页 ---- */
.empty-page { text-align: center; padding: var(--app-sp-12) 0; }
.empty-page__title { font-size: var(--app-fs-h2); color: var(--app-text-1); margin-bottom: var(--app-sp-3); }
.empty-page__desc { font-size: var(--app-fs-body); color: var(--app-text-3); margin-bottom: var(--app-sp-6); }
</style>
