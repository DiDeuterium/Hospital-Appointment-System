<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPatientsBySchedule } from '@/api/doctor'
import { APPT_STATUS_LABEL, APPT_STATUS_TAG_TYPE } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await listPatientsBySchedule(route.params.scheduleId)
  } catch (e) {} finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <el-button text @click="router.back()">← 返回排班</el-button>
      <h3 style="margin:0">排班 #{{ route.params.scheduleId }} 患者名册</h3>
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="apptId" label="预约号" width="100" />
      <el-table-column prop="realName" label="患者姓名" />
      <el-table-column prop="phone" label="手机号" width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="APPT_STATUS_TAG_TYPE[row.status] || 'info'">
            {{ APPT_STATUS_LABEL[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="挂号时间" width="180" />
    </el-table>
  </div>
</template>
