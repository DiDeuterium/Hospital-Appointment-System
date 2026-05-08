<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listDepartments } from '@/api/department'

const router = useRouter()
const keyword = ref('')
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await listDepartments(keyword.value ? { keyword: keyword.value } : {})
  } catch (e) {} finally {
    loading.value = false
  }
}

function pickDept(dept) {
  router.push({ name: 'PatientDoctors', params: { deptId: dept.deptId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h3 style="margin:0">选择科室</h3>
      <div>
        <el-input
          v-model="keyword"
          placeholder="搜索科室名称"
          clearable
          style="width:240px;margin-right:8px"
          @keyup.enter="load"
          @clear="load"
        />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>

    <el-row v-loading="loading" :gutter="16">
      <el-col v-for="d in list" :key="d.deptId" :span="8" style="margin-bottom:16px">
        <el-card shadow="hover" class="dept-card" @click="pickDept(d)">
          <div class="dept-name">{{ d.deptName }}</div>
          <div class="dept-loc">{{ d.location }}</div>
          <div class="dept-desc">{{ d.description }}</div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && !list.length" :span="24">
        <el-empty description="暂无科室" />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dept-card { cursor: pointer; transition: transform .15s; }
.dept-card:hover { transform: translateY(-2px); }
.dept-name { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 6px; }
.dept-loc { color: #909399; font-size: 13px; margin-bottom: 4px; }
.dept-desc { color: #606266; font-size: 13px; min-height: 38px; }
</style>
