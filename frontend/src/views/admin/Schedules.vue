<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule
} from '@/api/schedule'
import { listDoctors } from '@/api/doctor'
import { SHIFT_OPTIONS } from '@/utils/constants'

const list = ref([])
const doctors = ref([])
const loading = ref(false)

const filters = reactive({ docId: '', workDate: '' })

const dialog = reactive({
  visible: false,
  isEdit: false,
  form: { scheduleId: null, docId: '', workDate: '', shift: '上午', totalQuota: 30 }
})
const formRef = ref()
const rules = {
  docId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  shift: [{ required: true, message: '请选择时段', trigger: 'change' }],
  totalQuota: [{ required: true, message: '请输入号源数', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    // deptId 必须传（后端 @RequestParam required=true），admin 查全院时传空字符串
    const params = { deptId: '' }
    if (filters.docId) params.docId = filters.docId
    if (filters.workDate) params.workDate = filters.workDate
    list.value = await listSchedules(params)
  } catch (e) {} finally {
    loading.value = false
  }
}

async function loadDoctors() {
  doctors.value = await listDoctors()
}

function docName(docId) {
  return doctors.value.find(d => d.docId === docId)?.docName || docId
}

function openCreate() {
  dialog.isEdit = false
  dialog.form = { scheduleId: null, docId: '', workDate: '', shift: '上午', totalQuota: 30 }
  dialog.visible = true
}
function openEdit(row) {
  dialog.isEdit = true
  dialog.form = { ...row }
  dialog.visible = true
}
async function submit() {
  await formRef.value.validate()
  try {
    if (dialog.isEdit) {
      const { scheduleId, ...rest } = dialog.form
      await updateSchedule(scheduleId, rest)
      ElMessage.success('修改成功')
    } else {
      const { scheduleId, ...rest } = dialog.form
      await createSchedule(rest)
      ElMessage.success('发布成功')
    }
    dialog.visible = false
    load()
  } catch (e) {}
}
async function remove(row) {
  await ElMessageBox.confirm('删除该排班？若已有患者预约将无法删除', '提示', { type: 'warning' })
  try {
    await deleteSchedule(row.scheduleId)
    ElMessage.success('已删除')
    load()
  } catch (e) {}
}

onMounted(() => {
  loadDoctors()
  load()
})
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h3 style="margin:0">排班发布</h3>
      <div style="display:flex;gap:8px;align-items:center">
        <el-select
          v-model="filters.docId"
          placeholder="按医生筛选"
          clearable
          style="width:180px"
          @change="load"
        >
          <el-option v-for="d in doctors" :key="d.docId" :label="d.docName" :value="d.docId" />
        </el-select>
        <el-date-picker
          v-model="filters.workDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="按日期筛选"
          clearable
          @change="load"
        />
        <el-button type="primary" @click="openCreate">发布排班</el-button>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="scheduleId" label="排班号" width="100" />
      <el-table-column label="医生">
        <template #default="{ row }">{{ row.docName || docName(row.docId) }}</template>
      </el-table-column>
      <el-table-column prop="workDate" label="日期" width="120" />
      <el-table-column prop="shift" label="时段" width="80" />
      <el-table-column prop="totalQuota" label="总号源" width="100" />
      <el-table-column prop="restQuota" label="剩余" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '修改排班' : '发布排班'"
      width="480px"
    >
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="90px">
        <el-form-item label="医生" prop="docId">
          <el-select v-model="dialog.form.docId" placeholder="选择医生" style="width:100%" :disabled="dialog.isEdit">
            <el-option v-for="d in doctors" :key="d.docId" :label="d.docName + ' (' + d.docId + ')'" :value="d.docId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="workDate">
          <el-date-picker
            v-model="dialog.form.workDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="时段" prop="shift">
          <el-radio-group v-model="dialog.form.shift">
            <el-radio v-for="s in SHIFT_OPTIONS" :key="s.value" :value="s.value">{{ s.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源总数" prop="totalQuota">
          <el-input-number v-model="dialog.form.totalQuota" :min="1" :max="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
