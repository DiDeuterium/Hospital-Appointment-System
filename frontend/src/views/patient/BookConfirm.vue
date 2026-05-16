<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAppointment } from '@/api/appointment'
import { useUserStore } from '@/stores/user'
import { peekSchedule, stashResult, SHIFT_TIME_MAP } from '@/utils/booking'
import StatusTag from '@/components/StatusTag.vue'
import StepFlow from '@/components/StepFlow.vue'
import SectionCard from '@/components/SectionCard.vue'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)

const schedule = ref(peekSchedule())
const infoExpired = computed(() => !schedule.value)

const steps = [
  { label: '选科室' },
  { label: '选医生' },
  { label: '选时段' },
  { label: '确认' },
  { label: '完成' }
]

const notices = [
  '预约成功后请提前 15 分钟到院取号',
  '同一时段每位患者仅可预约一次',
  '如需取消，请至少在就诊前 2 小时操作'
]

async function doBook() {
  if (!schedule.value) return
  loading.value = true
  try {
    const pid = user.profile?.patientId
    if (!pid) {
      ElMessage.warning('无法获取就诊人信息，请重新登录')
      return
    }
    const res = await createAppointment({
      patientId: pid,
      scheduleId: schedule.value.scheduleId
    })
    stashResult(res)
    router.replace({ name: 'PatientBookSuccess' })
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <!-- 信息过期 -->
    <SectionCard v-if="infoExpired">
      <div class="expired">
        <h2 class="expired__title">挂号信息已过期</h2>
        <p class="expired__desc">请从排班列表重新选择。</p>
        <el-button size="large" type="primary" @click="router.replace({ name: 'PatientHome' })">返回首页</el-button>
      </div>
    </SectionCard>

    <!-- 正常流程 -->
    <template v-else>
      <StepFlow :steps="steps" :current="3" class="section-gap" />

      <SectionCard title="核对预约信息">
        <div class="info-table">
          <div class="info-table__row">
            <span class="info-table__label">科室</span>
            <span class="info-table__value">{{ schedule.deptName }}</span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">医生</span>
            <span class="info-table__value">{{ schedule.docName }} <StatusTag size="small">{{ schedule.title }}</StatusTag></span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">日期</span>
            <span class="info-table__value">{{ schedule.workDate }}</span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">时段</span>
            <span class="info-table__value">{{ schedule.shift }}（{{ SHIFT_TIME_MAP[schedule.shift] || '' }}）</span>
          </div>
          <div class="info-table__row">
            <span class="info-table__label">就诊人</span>
            <span class="info-table__value">{{ user.displayName }}</span>
          </div>
        </div>
      </SectionCard>

      <SectionCard title="温馨提示" class="section-gap">
        <ul class="tips">
          <li v-for="t in notices" :key="t" class="tips__item">
            <span class="tips__dot" />
            {{ t }}
          </li>
        </ul>
      </SectionCard>

      <div class="actions">
        <el-button size="large" @click="router.back()">取 消</el-button>
        <el-button size="large" type="primary" :loading="loading" @click="doBook">确认挂号</el-button>
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
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
}

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

.expired {
  text-align: center;
  padding: var(--app-sp-12) 0;
}
.expired__title {
  font-size: var(--app-fs-h2);
  color: var(--app-text-1);
  margin-bottom: var(--app-sp-3);
}
.expired__desc {
  font-size: var(--app-fs-body);
  color: var(--app-text-3);
  margin-bottom: var(--app-sp-6);
}
</style>
