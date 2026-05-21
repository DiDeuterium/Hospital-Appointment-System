<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listDoctorsByDept } from '@/api/department'
import { listDepartments } from '@/api/department'
import PageHeader from '@/components/PageHeader.vue'
import DoctorCard from '@/components/DoctorCard.vue'

const route = useRoute()
const router = useRouter()
const deptId = route.params.deptId
const list = ref([])
const deptName = ref('')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const [doctors, depts] = await Promise.all([
      listDoctorsByDept(deptId),
      listDepartments()
    ])
    list.value = doctors
    const found = depts.find(d => String(d.deptId) === String(deptId))
    deptName.value = found?.deptName || `科室 ${deptId}`
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

function viewDetail(doc) {
  router.push({ name: 'PatientDoctorDetail', params: { deptId, docId: doc.docId } })
}
function goSchedules() {
  router.push({ name: 'PatientSchedules', params: { deptId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      :title="deptName"
      :subtitle="'共 ' + list.length + ' 位医生'"
      :breadcrumbs="[{ label: '首页', to: '/patient/home' }, { label: '选择科室', to: '/patient/departments' }, { label: deptName }]"
    >
      <template #extra>
        <el-button type="primary" @click="goSchedules">查看本科室排班</el-button>
      </template>
    </PageHeader>

    <div v-loading="loading" class="doctors-grid">
      <el-row :gutter="16">
        <el-col
          v-for="d in list"
          :key="d.docId"
          :xs="24"
          :sm="12"
          :lg="8"
          style="margin-bottom:16px"
        >
          <DoctorCard
            :doctor="d"
            @view-detail="viewDetail"
          />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
</style>
