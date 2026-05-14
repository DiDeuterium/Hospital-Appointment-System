<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment
} from '@/api/department'

const list = ref([])
const loading = ref(false)
const keyword = ref('')

const dialog = reactive({
  visible: false,
  isEdit: false,
  form: { deptId: '', deptName: '', location: '', description: '' }
})
const formRef = ref()

const rules = {
  deptId: [{ required: true, message: '请输入科室编号', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = await listDepartments(keyword.value ? { keyword: keyword.value } : {})
  } catch (e) {} finally {
    loading.value = false
  }
}

function openCreate() {
  dialog.isEdit = false
  dialog.form = { deptId: '', deptName: '', location: '', description: '' }
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
      const { deptId, ...rest } = dialog.form
      await updateDepartment(deptId, rest)
      ElMessage.success('修改成功')
    } else {
      await createDepartment(dialog.form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } catch (e) {}
}

async function remove(row) {
  await ElMessageBox.confirm('删除该科室？若有医生关联将无法删除', '提示', { type: 'warning' })
  try {
    await deleteDepartment(row.deptId)
    ElMessage.success('已删除')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h3 style="margin:0">科室管理</h3>
      <div>
        <el-input
          v-model="keyword"
          placeholder="搜索科室名称"
          clearable
          style="width:240px;margin-right:8px"
          @keyup.enter="load"
          @clear="load"
        />
        <el-button @click="load">查询</el-button>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="deptId" label="编号" width="120" />
      <el-table-column prop="deptName" label="名称" width="160" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="description" label="简介" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '修改科室' : '新增科室'"
      width="480px"
    >
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="90px">
        <el-form-item label="科室编号" prop="deptId">
          <el-input v-model="dialog.form.deptId" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="dialog.form.deptName" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="dialog.form.location" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="dialog.form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
