<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSchedules } from '@/api/schedule'
import { createAppointment } from '@/api/appointment'
import { useUserStore } from '@/stores/user'
import { SHIFT_OPTIONS } from '@/utils/constants'

const route = useRoute()
const user = useUserStore()
const list = ref([])
const loading = ref(false)

const filters = reactive({
  workDate: '',
  shift: ''
})

async function load() {
  loading.value = true
  try {
    const params = { deptId: route.params.deptId }
    if (filters.workDate) params.workDate = filters.workDate
    if (filters.shift) params.shift = filters.shift
    list.value = await listSchedules(params)
  } catch (e) {} finally {
    loading.value = false
  }
}

async function book(row) {
  if (row.restQuota <= 0) {
    ElMessage.warning('该排班已约满')
    return
  }
  await ElMessageBox.confirm(
    '确认预约 ' + row.workDate + ' ' + row.shift + ' ' + row.docName + ' 医生吗？',
    '挂号确认',
    { type: 'warning' }
  )
  try {
    await createAppointment({
      patientId: user.profile?.patientId,
      scheduleId: row.scheduleId
    })
    ElMessage.success('挂号成功')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <el-form inline :model="filters">
        <el-form-item label="日期">
          <el-date-picker
            v-model="filters.workDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            clearable
          />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="filters.shift" placeholder="全部" clearable style="width:140px">
            <el-option v-for="s in SHIFT_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="workDate" label="日期" width="120" />
      <el-table-column prop="shift" label="时段" width="80" />
      <el-table-column prop="docName" label="医生" width="120" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="deptName" label="科室" />
      <el-table-column label="号源" width="160">
        <template #default="{ row }">
          <span :class="row.restQuota > 0 ? 'q-ok' : 'q-no'">
            {{ row.restQuota }} / {{ row.totalQuota }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            :disabled="row.restQuota <= 0"
            @click="book(row)"
          >挂号</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.q-ok { color: var(--el-color-success); font-weight: 600; }
.q-no { color: var(--el-color-info); }
</style>
