<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listDoctorsByDept } from '@/api/department'
import { GENDER_LABEL } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await listDoctorsByDept(route.params.deptId)
  } catch (e) {} finally {
    loading.value = false
  }
}

function viewSchedules() {
  router.push({ name: 'PatientSchedules', params: { deptId: route.params.deptId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <el-button text @click="router.back()">← 返回科室</el-button>
      <el-button type="primary" @click="viewSchedules">查看排班号源</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="docId" label="工号" width="120" />
      <el-table-column prop="docName" label="姓名" width="120" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ GENDER_LABEL[row.gender] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="title" label="职称" />
    </el-table>
  </div>
</template>
