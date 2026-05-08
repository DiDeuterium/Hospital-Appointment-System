<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDoctors, createDoctor, updateDoctor, deleteDoctor } from '@/api/doctor'
import { listDepartments } from '@/api/department'
import { GENDER_OPTIONS, GENDER_LABEL } from '@/utils/constants'

const list = ref([])
const depts = ref([])
const loading = ref(false)
const filterDeptId = ref('')

const dialog = reactive({
  visible: false,
  isEdit: false,
  form: {
    docId: '', docName: '', gender: 'M',
    title: '', deptId: '', password: ''
  }
})
const formRef = ref()
const rules = {
  docId: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  docName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const params = filterDeptId.value ? { deptId: filterDeptId.value } : {}
    list.value = await listDoctors(params)
  } catch (e) {} finally {
    loading.value = false
  }
}

async function loadDepts() {
  depts.value = await listDepartments()
}

function openCreate() {
  dialog.isEdit = false
  dialog.form = { docId: '', docName: '', gender: 'M', title: '', deptId: '', password: '' }
  dialog.visible = true
}
function openEdit(row) {
  dialog.isEdit = true
  dialog.form = { ...row, password: '' }
  dialog.visible = true
}
async function submit() {
  await formRef.value.validate()
  try {
    if (dialog.isEdit) {
      const { docId, ...rest } = dialog.form
      // 编辑时若密码留空则不传
      if (!rest.password) delete rest.password
      await updateDoctor(docId, rest)
      ElMessage.success('修改成功')
    } else {
      await createDoctor(dialog.form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } catch (e) {}
}
async function remove(row) {
  await ElMessageBox.confirm('删除该医生？若有排班关联将无法删除', '提示', { type: 'warning' })
  try {
    await deleteDoctor(row.docId)
    ElMessage.success('已删除')
    load()
  } catch (e) {}
}

function deptName(deptId) {
  return depts.value.find(d => d.deptId === deptId)?.deptName || deptId
}

onMounted(() => {
  loadDepts()
  load()
})
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h3 style="margin:0">医生管理</h3>
      <div>
        <el-select
          v-model="filterDeptId"
          placeholder="按科室筛选"
          clearable
          style="width:200px;margin-right:8px"
          @change="load"
        >
          <el-option v-for="d in depts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="docId" label="工号" width="120" />
      <el-table-column prop="docName" label="姓名" width="120" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ GENDER_LABEL[row.gender] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="title" label="职称" width="140" />
      <el-table-column label="科室">
        <template #default="{ row }">{{ deptName(row.deptId) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '修改医生' : '新增医生'"
      width="500px"
    >
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="90px">
        <el-form-item label="工号" prop="docId">
          <el-input v-model="dialog.form.docId" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="docName">
          <el-input v-model="dialog.form.docName" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="dialog.form.gender">
            <el-radio v-for="g in GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="dialog.form.title" />
        </el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="dialog.form.deptId" placeholder="选择科室" style="width:100%">
            <el-option v-for="d in depts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="dialog.isEdit ? '重置密码' : '初始密码'">
          <el-input
            v-model="dialog.form.password"
            type="password"
            show-password
            :placeholder="dialog.isEdit ? '留空则不修改' : '请输入初始密码'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
