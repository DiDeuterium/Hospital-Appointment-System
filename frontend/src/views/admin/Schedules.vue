<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdminSchedules, createSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'
import { listDoctors } from '@/api/doctor'
import { SHIFT_OPTIONS } from '@/utils/constants'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'

const list = ref([])
const doctors = ref([])
const loading = ref(false)
const filters = reactive({ docId: '', workDate: '' })

const dialog = reactive({
  visible: false, isEdit: false,
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
    const params = {}
    if (filters.docId) params.docId = filters.docId
    if (filters.workDate) params.workDate = filters.workDate
    list.value = await listAdminSchedules(params)
  } catch {} finally { loading.value = false }
}
async function loadDoctors() { doctors.value = await listDoctors({ deptId: '' }) }

function openCreate() {
  dialog.isEdit = false
  dialog.form = { scheduleId: null, docId: '', workDate: '', shift: '上午', totalQuota: 30 }
  dialog.visible = true
}
function openEdit(row) { dialog.isEdit = true; dialog.form = { ...row }; dialog.visible = true }

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
    dialog.visible = false; load()
  } catch {}
}
async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该排班？若已有预约将无法删除', '提示', { type: 'warning' })
    await deleteSchedule(row.scheduleId)
    ElMessage.success('已删除'); load()
  } catch {}
}

onMounted(() => { loadDoctors(); load() })
</script>

<template>
  <div class="page-container">
    <PageHeader title="排班管理" :subtitle="'共 ' + list.length + ' 条排班'">
      <template #extra>
        <el-button size="large" type="primary" @click="openCreate">发布排班</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-select v-model="filters.docId" placeholder="按医生筛选" clearable size="large" @change="load" class="toolbar__sel">
        <el-option v-for="d in doctors" :key="d.docId" :label="d.docName" :value="d.docId" />
      </el-select>
      <el-date-picker v-model="filters.workDate" type="date" value-format="YYYY-MM-DD" placeholder="按日期筛选" clearable size="large" @change="load" />
      <el-button size="large" @click="filters.docId = ''; filters.workDate = ''; load()">清除</el-button>
    </div>

    <div v-loading="loading" class="table-wrap">
      <table class="st-table" v-if="list.length">
        <thead>
          <tr><th>医生</th><th>日期</th><th>时段</th><th>总号源</th><th>剩余</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="s in list" :key="s.scheduleId">
            <td class="st-table__name">{{ s.docName }}</td>
            <td>{{ s.workDate }}</td>
            <td>{{ s.shift }}</td>
            <td>{{ s.totalQuota }}</td>
            <td>{{ s.restQuota }}</td>
            <td>
              <StatusTag :type="s.restQuota <= 0 ? 'danger' : s.restQuota < s.totalQuota * 0.3 ? 'warning' : 'success'" size="small">
                {{ s.restQuota <= 0 ? '约满' : s.restQuota < s.totalQuota * 0.3 ? '紧张' : '充足' }}
              </StatusTag>
            </td>
            <td class="st-table__actions">
              <el-button size="small" @click="openEdit(s)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(s)">删除</el-button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无排班</div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '修改排班' : '发布排班'" width="480px">
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-position="top">
        <el-form-item label="医生" prop="docId">
          <el-select v-model="dialog.form.docId" placeholder="选择医生" size="large" :disabled="dialog.isEdit" style="width:100%">
            <el-option v-for="d in doctors" :key="d.docId" :label="d.docName + ' (' + d.docId + ')'" :value="d.docId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="workDate">
          <el-date-picker v-model="dialog.form.workDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" size="large" style="width:100%" />
        </el-form-item>
        <el-form-item label="时段" prop="shift">
          <el-radio-group v-model="dialog.form.shift">
            <el-radio-button v-for="s in SHIFT_OPTIONS" :key="s.value" :value="s.value">{{ s.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源总数" prop="totalQuota">
          <el-input-number v-model="dialog.form.totalQuota" :min="1" :max="200" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="dialog.visible = false">取消</el-button>
        <el-button size="large" type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { max-width: var(--app-content-max-width); margin: 0 auto; padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12); }
.toolbar { display: flex; gap: var(--app-sp-3); margin-bottom: var(--app-sp-6); flex-wrap: wrap; }
.toolbar__sel { width: 220px; }

.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow: hidden; }
.st-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.st-table th { text-align: left; padding: var(--app-sp-3) var(--app-sp-5); background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption); border-bottom: 1px solid var(--app-border); }
.st-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.st-table tr:last-child td { border-bottom: none; }
.st-table tr:hover td { background: var(--app-bg-hover); }
.st-table__name { font-weight: 500; color: var(--app-text-1); }
.st-table__actions { white-space: nowrap; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }
</style>
