<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listMyAppointments } from '@/api/appointment'
import { listDepartments } from '@/api/department'
import { APPT_STATUS, APPT_STATUS_LABEL } from '@/utils/constants'
import { deptIcon } from '@/utils/booking'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'
import StatCard from '@/components/StatCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import SectionCard from '@/components/SectionCard.vue'

const router = useRouter()
const user = useUserStore()

const recentAppt = ref(null)
const hotDepts = ref([])
const loading = ref(false)

const shortcuts = [
  { label: '科室挂号', icon: 'building', to: '/patient/departments' },
  { label: '我的预约', icon: 'calendar-check', to: '/patient/appointments' },
  { label: '预约管理', icon: 'file-text', to: '/patient/appointments' }
]

const tips = [
  '预约成功后请提前 15 分钟到院取号',
  '取消预约请至少在就诊前 2 小时操作',
  '如有发热、咳嗽等症状，请前往发热门诊'
]

onMounted(async () => {
  loading.value = true
  try {
    const pid = user.profile?.patientId
    const [depts, appts] = await Promise.all([
      listDepartments(),
      pid ? listMyAppointments(pid, { status: APPT_STATUS.BOOKED }).catch(() => []) : []
    ])
    hotDepts.value = depts.slice(0, 4)
    recentAppt.value = Array.isArray(appts) && appts.length ? appts[0] : null
  } catch { /* 后端没起时静默 */ } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <!-- 欢迎横幅 -->
    <section class="hero">
      <div class="hero__body">
        <h1 class="hero__greeting">你好，{{ user.displayName || '用户' }}</h1>
        <p class="hero__date">欢迎使用在线预约挂号服务</p>
      </div>
      <div class="hero__cta">
        <el-button size="large" type="primary" @click="router.push('/patient/departments')">
          立即挂号
          <AppIcon name="arrow-right" :size="16" style="margin-left:4px" />
        </el-button>
      </div>
    </section>

    <!-- 我的预约提醒 -->
    <SectionCard v-if="recentAppt" title="最近预约" class="section-gap">
      <template #extra>
        <router-link to="/patient/appointments">查看全部</router-link>
      </template>
      <div class="reminder">
        <div class="reminder__main">
          <div class="reminder__dept">
            <span class="reminder__dept-icon">{{ deptIcon(recentAppt.deptName) }}</span>
            <span>{{ recentAppt.deptName }} · {{ recentAppt.docName }}</span>
          </div>
          <div class="reminder__info">
            <span>{{ recentAppt.workDate }} {{ recentAppt.shift }}</span>
            <span class="reminder__sep">·</span>
            <span>预约号 #{{ recentAppt.apptId }}</span>
          </div>
        </div>
        <StatusTag type="primary">{{ APPT_STATUS_LABEL[recentAppt.status] || '未知' }}</StatusTag>
      </div>
    </SectionCard>

    <!-- 最近预约为空时温和提示 -->
    <SectionCard v-else title="我的预约" class="section-gap">
      <EmptyState title="暂无预约" description="您还没有预约记录，点击下方按钮开始挂号" :style="{ padding: 'var(--app-sp-8) 0' }">
        <template #action>
          <el-button type="primary" @click="router.push('/patient/departments')">去挂号</el-button>
        </template>
      </EmptyState>
    </SectionCard>

    <!-- 快捷入口 -->
    <SectionCard class="section-gap" :bordered="false" :body-padding="false">
      <div class="shortcuts">
        <button
          v-for="s in shortcuts"
          :key="s.label"
          type="button"
          class="shortcuts__item"
          @click="router.push(s.to)"
        >
          <span class="shortcuts__icon">
            <AppIcon :name="s.icon" :size="22" />
          </span>
          <span class="shortcuts__label">{{ s.label }}</span>
        </button>
      </div>
    </SectionCard>

    <!-- 热门科室 -->
    <SectionCard title="热门科室" class="section-gap">
      <template #extra>
        <router-link to="/patient/departments">查看全部</router-link>
      </template>
      <div v-loading="loading" class="hot-depts">
        <el-row :gutter="12">
          <el-col v-for="d in hotDepts" :key="d.deptId" :span="12" style="margin-bottom:12px">
            <div class="hot-depts__card" @click="router.push({ name: 'PatientDoctors', params: { deptId: d.deptId } })">
              <div class="hot-depts__head">
                <span class="hot-depts__icon">{{ deptIcon(d.deptName) }}</span>
                <h3 class="hot-depts__name">{{ d.deptName }}</h3>
              </div>
              <p class="hot-depts__loc">{{ d.location || '' }}</p>
              <div class="hot-depts__go">
                立即查看
                <AppIcon name="chevron-right" :size="14" />
              </div>
            </div>
          </el-col>
        </el-row>
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
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.section-gap { margin-bottom: var(--app-sp-6); }

/* ---- 欢迎横幅 ---- */
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--app-sp-8);
  margin-bottom: var(--app-sp-6);
  background: linear-gradient(105deg, var(--app-brand-50) 0%, var(--app-brand-100) 100%);
  border-radius: var(--app-radius-xl);
  gap: var(--app-sp-6);
}
.hero__greeting {
  font-size: var(--app-fs-display);
  font-weight: 600;
  color: var(--app-text-1);
  line-height: 1.3;
}
.hero__date {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin-top: var(--app-sp-2);
}
.hero__cta { flex-shrink: 0; }

/* ---- 预约提醒 ---- */
.reminder {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--app-sp-4);
}
.reminder__main {
  display: flex;
  flex-direction: column;
  gap: var(--app-sp-2);
}
.reminder__dept {
  font-size: var(--app-fs-h3);
  font-weight: 600;
  color: var(--app-text-1);
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
}
.reminder__dept-icon { font-size: 20px; flex-shrink: 0; }
.reminder__info {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
}
.reminder__sep { color: var(--app-text-4); }

/* ---- 快捷入口 ---- */
.shortcuts {
  display: flex;
  justify-content: space-around;
  padding: var(--app-sp-5);
}
.shortcuts__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--app-sp-2);
  border: none;
  background: none;
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-4) var(--app-sp-5);
  cursor: pointer;
  transition: all var(--app-transition-fast);
  color: var(--app-text-2);
}
.shortcuts__item:hover {
  background: var(--app-brand-50);
  color: var(--app-brand-600);
}
.shortcuts__icon {
  width: 48px; height: 48px;
  border-radius: var(--app-radius-md);
  background: var(--app-bg-subtle);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background var(--app-transition-fast);
}
.shortcuts__item:hover .shortcuts__icon {
  background: var(--app-brand-100);
}
.shortcuts__label {
  font-size: var(--app-fs-caption);
  font-weight: 500;
}

/* ---- 热门科室 ---- */
.hot-depts__card {
  background: var(--app-bg-page);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-md);
  padding: var(--app-sp-4);
  cursor: pointer;
  transition: all var(--app-transition-fast);
}
.hot-depts__card:hover {
  border-color: var(--app-brand-300);
  box-shadow: var(--app-shadow-sm);
}
.hot-depts__head {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  margin-bottom: var(--app-sp-2);
}
.hot-depts__icon { font-size: 20px; }
.hot-depts__name {
  font-size: var(--app-fs-body);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0;
}
.hot-depts__loc {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin: 0 0 var(--app-sp-3);
}
.hot-depts__go {
  font-size: var(--app-fs-caption);
  color: var(--app-brand-600);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ---- 就诊须知 ---- */
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

/* ---- 响应式 ---- */
@media (max-width: 640px) {
  .hero {
    flex-direction: column;
    text-align: center;
  }
  .reminder { flex-direction: column; align-items: flex-start; }
}
</style>
