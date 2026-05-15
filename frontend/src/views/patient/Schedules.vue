<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listSchedules } from '@/api/schedule'
import { listDepartments } from '@/api/department'
import { stashSchedule } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import DateTabs from '@/components/DateTabs.vue'
import ScheduleCard from '@/components/ScheduleCard.vue'
import StepFlow from '@/components/StepFlow.vue'

const route = useRoute()
const router = useRouter()
const deptId = route.params.deptId
const list = ref([])
const deptName = ref('')
const loading = ref(false)

const filters = reactive({
  workDate: '',
  shift: ''
})

const steps = [
  { label: '选科室' },
  { label: '选医生' },
  { label: '选时段' },
  { label: '确认' },
  { label: '完成' }
]

async function load() {
  loading.value = true
  try {
    const [schedules, depts] = await Promise.all([
      listSchedules({ deptId, ...filters }),
      listDepartments()
    ])
    list.value = schedules
    const found = depts.find(d => d.deptId == deptId)
    if (found) {
      deptName.value = found.deptName
      // 给每条排班挂上 deptName
      list.value = list.value.map(s => ({ ...s, deptName: found.deptName || '' }))
    }
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

function doBook(schedule) {
  stashSchedule(schedule)
  router.push({ name: 'PatientBookConfirm' })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <StepFlow :steps="steps" :current="2" class="section-gap" />

    <PageHeader
      :title="deptName || '排班选号'"
      :subtitle="'共 ' + list.length + ' 个可预约号'"
      :breadcrumbs="[
        { label: '首页', to: '/patient/home' },
        { label: '选择科室', to: '/patient/departments' },
        { label: deptName || '选号' }
      ]"
    />

    <!-- 日期选项卡 -->
    <div class="section-gap">
      <DateTabs v-model="filters.workDate" @update:model-value="load" />
    </div>

    <!-- 时段切换 -->
    <div class="shift-bar">
      <el-radio-group v-model="filters.shift" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="上午">上午</el-radio-button>
        <el-radio-button value="下午">下午</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 排班卡片列表 -->
    <div v-loading="loading" class="schedule-list">
      <ScheduleCard
        v-for="s in list"
        :key="s.scheduleId"
        :schedule="s"
        class="schedule-list__item"
        @book="doBook"
      />
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.section-gap { margin-bottom: var(--app-sp-6); }

.shift-bar {
  margin-bottom: var(--app-sp-6);
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: var(--app-sp-4);
}
</style>
