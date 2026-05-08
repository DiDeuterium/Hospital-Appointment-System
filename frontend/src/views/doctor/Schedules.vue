<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { myDoctorSchedules } from '@/api/doctor'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const workDate = ref('')
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const params = workDate.value ? { workDate: workDate.value } : {}
    list.value = await myDoctorSchedules(user.profile?.docId, params)
  } catch (e) {} finally {
    loading.value = false
  }
}

function viewPatients(row) {
  router.push({ name: 'DoctorPatients', params: { scheduleId: row.scheduleId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h3 style="margin:0">我的排班</h3>
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker
            v-model="workDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="按日期筛选"
            clearable
            @change="load"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="workDate" label="日期" width="140" />
      <el-table-column prop="shift" label="时段" width="100" />
      <el-table-column prop="totalQuota" label="总号源" width="100" />
      <el-table-column prop="restQuota" label="剩余" width="100" />
      <el-table-column prop="appointedCount" label="已约" width="100" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewPatients(row)">患者名册</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
