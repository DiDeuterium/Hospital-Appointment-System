<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyAppointments, cancelAppointment } from '@/api/appointment'
import { useUserStore } from '@/stores/user'
import {
  APPT_STATUS,
  APPT_STATUS_LABEL,
  APPT_STATUS_TAG_TYPE
} from '@/utils/constants'

const user = useUserStore()
const list = ref([])
const status = ref('')
const loading = ref(false)

async function load() {
  const pid = user.profile?.patientId
  if (!pid) {
    list.value = []
    return
  }
  loading.value = true
  try {
    const params = status.value ? { status: status.value } : {}
    list.value = await listMyAppointments(pid, params)
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

async function cancel(row) {
  await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
  try {
    await cancelAppointment(row.apptId)
    ElMessage.success('已取消')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h3 style="margin:0">我的预约</h3>
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button :value="APPT_STATUS.BOOKED">已预约</el-radio-button>
        <el-radio-button :value="APPT_STATUS.CANCELLED">已取消</el-radio-button>
        <el-radio-button :value="APPT_STATUS.FINISHED">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="apptId" label="预约号" width="100" />
      <el-table-column prop="deptName" label="科室" />
      <el-table-column prop="docName" label="医生" />
      <el-table-column prop="workDate" label="日期" width="120" />
      <el-table-column prop="shift" label="时段" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="APPT_STATUS_TAG_TYPE[row.status] || 'info'">
            {{ APPT_STATUS_LABEL[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="挂号时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            type="danger"
            size="small"
            :disabled="row.status !== APPT_STATUS.BOOKED"
            @click="cancel(row)"
          >取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
