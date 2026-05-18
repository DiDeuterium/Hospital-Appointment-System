<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'
import PageHeader from '@/components/PageHeader.vue'
import AppIcon from '@/components/AppIcon.vue'

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
  try { list.value = await listDepartments(keyword.value ? { keyword: keyword.value } : {}) } catch {} finally { loading.value = false }
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
      await updateDepartment(dialog.form.deptId, dialog.form)
      ElMessage.success('修改成功')
    } else {
      await createDepartment(dialog.form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } catch {}
}
async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该科室？若有医生关联将无法删除', '提示', { type: 'warning', lockScroll: false })
    await deleteDepartment(row.deptId)
    ElMessage.success('已删除')
    load()
  } catch {}
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader title="科室管理" :subtitle="'共 ' + list.length + ' 个科室'" />

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索科室名称" clearable size="large" @keyup.enter="load" @clear="load" class="toolbar__search">
        <template #prefix><AppIcon name="search" :size="16" style="color:var(--app-text-4)" /></template>
      </el-input>
      <el-button size="large" @click="load">查询</el-button>
      <el-button size="large" type="primary" @click="openCreate">新增科室</el-button>
    </div>

    <div v-loading="loading" class="table-wrap">
      <table class="dt-table" v-if="list.length">
        <thead>
          <tr><th>编号</th><th>名称</th><th>位置</th><th>简介</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="d in list" :key="d.deptId">
            <td>{{ d.deptId }}</td>
            <td class="dt-table__name">{{ d.deptName }}</td>
            <td>{{ d.location || '—' }}</td>
            <td class="dt-table__desc">{{ (d.description || '').slice(0, 40) || '—' }}</td>
            <td class="dt-table__actions">
              <el-button size="small" @click="openEdit(d)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(d)">删除</el-button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无科室</div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '修改科室' : '新增科室'" width="480px" :lock-scroll="false">
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-position="top">
        <el-form-item label="科室编号" prop="deptId">
          <el-input v-model="dialog.form.deptId" :disabled="dialog.isEdit" size="large" />
        </el-form-item>
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="dialog.form.deptName" size="large" />
        </el-form-item>
        <el-form-item label="位置"><el-input v-model="dialog.form.location" size="large" /></el-form-item>
        <el-form-item label="简介">
          <el-input v-model="dialog.form.description" type="textarea" :rows="3" />
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
.toolbar { display: flex; gap: var(--app-sp-3); margin-bottom: var(--app-sp-6); align-items: center; }
.toolbar__search { max-width: 300px; }

.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow: hidden; }
.dt-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.dt-table th { text-align: left; padding: var(--app-sp-3) var(--app-sp-5); background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption); border-bottom: 1px solid var(--app-border); }
.dt-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.dt-table tr:last-child td { border-bottom: none; }
.dt-table tr:hover td { background: var(--app-bg-hover); }
.dt-table__name { font-weight: 500; color: var(--app-text-1); }
.dt-table__desc { color: var(--app-text-3); font-size: var(--app-fs-caption); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dt-table__actions { white-space: nowrap; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }
</style>
