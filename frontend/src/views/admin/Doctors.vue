<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDoctors, createDoctor, updateDoctor, deleteDoctor } from '@/api/doctor'
import { listDepartments } from '@/api/department'
import { GENDER_OPTIONS, GENDER_LABEL } from '@/utils/constants'
import { genderEmoji } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import AppIcon from '@/components/AppIcon.vue'

const list = ref([])
const depts = ref([])
const loading = ref(false)
const filterDeptId = ref('')
const viewMode = ref('table')

const dialog = reactive({
  visible: false, isEdit: false,
  form: { docId: '', docName: '', gender: 'M', title: '', deptId: '', password: '' }
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
    const params = { deptId: filterDeptId.value || '' }
    list.value = await listDoctors(params)
  } catch {} finally { loading.value = false }
}
async function loadDepts() { depts.value = await listDepartments() }

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
      if (!rest.password) delete rest.password
      await updateDoctor(docId, rest)
      ElMessage.success('修改成功')
    } else {
      await createDoctor(dialog.form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false; load()
  } catch {}
}
async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该医生？', '提示', { type: 'warning' })
    await deleteDoctor(row.docId)
    ElMessage.success('已删除'); load()
  } catch {}
}

function deptName(deptId) {
  return depts.value.find(d => String(d.deptId) === String(deptId))?.deptName || deptId
}

onMounted(() => { loadDepts(); load() })
</script>

<template>
  <div class="page-container">
    <PageHeader title="医生管理" :subtitle="'共 ' + list.length + ' 位医生'">
      <template #extra>
        <div class="view-toggle">
          <button class="view-toggle__btn" :class="{ 'is-active': viewMode === 'table' }" @click="viewMode = 'table'">表格</button>
          <button class="view-toggle__btn" :class="{ 'is-active': viewMode === 'card' }" @click="viewMode = 'card'">卡片</button>
        </div>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-select v-model="filterDeptId" placeholder="按科室筛选" clearable size="large" @change="load" class="toolbar__sel">
        <el-option v-for="d in depts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
      </el-select>
      <el-button size="large" type="primary" @click="openCreate">新增医生</el-button>
    </div>

    <!-- 表格视图 -->
    <div v-if="viewMode === 'table'" v-loading="loading" class="table-wrap">
      <table class="dc-table" v-if="list.length">
        <thead>
          <tr><th>工号</th><th>姓名</th><th>性别</th><th>职称</th><th>科室</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="d in list" :key="d.docId">
            <td>{{ d.docId }}</td>
            <td class="dc-table__name">{{ d.docName }}</td>
            <td>{{ GENDER_LABEL[d.gender] || '—' }}</td>
            <td>{{ d.title || '—' }}</td>
            <td>{{ deptName(d.deptId) }}</td>
            <td class="dc-table__actions">
              <el-button size="small" @click="openEdit(d)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(d)">删除</el-button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无医生</div>
    </div>

    <!-- 卡片视图 -->
    <div v-else v-loading="loading" class="card-grid">
      <article v-for="d in list" :key="d.docId" class="doc-card">
        <div class="doc-card__avatar">{{ genderEmoji(d.gender) }}</div>
        <h3 class="doc-card__name">{{ d.docName }}</h3>
        <div class="doc-card__meta">
          <span>{{ GENDER_LABEL[d.gender] }}</span>
          <span class="doc-card__sep">·</span>
          <span>{{ d.title || '—' }}</span>
        </div>
        <div class="doc-card__dept">{{ deptName(d.deptId) }}</div>
        <div class="doc-card__actions">
          <el-button size="small" @click="openEdit(d)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="remove(d)">删除</el-button>
        </div>
      </article>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '修改医生' : '新增医生'" width="500px">
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-position="top">
        <el-form-item label="工号" prop="docId">
          <el-input v-model="dialog.form.docId" :disabled="dialog.isEdit" size="large" />
        </el-form-item>
        <el-form-item label="姓名" prop="docName">
          <el-input v-model="dialog.form.docName" size="large" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="dialog.form.gender">
            <el-radio-button v-for="g in GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="职称"><el-input v-model="dialog.form.title" size="large" /></el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="dialog.form.deptId" placeholder="选择科室" size="large" style="width:100%">
            <el-option v-for="d in depts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="dialog.isEdit ? '重置密码' : '初始密码'">
          <el-input v-model="dialog.form.password" type="password" show-password size="large" :placeholder="dialog.isEdit ? '留空则不修改' : '请输入初始密码'" />
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
.toolbar__sel { width: 220px; }

.view-toggle { display: flex; border: 1px solid var(--app-border); border-radius: var(--app-radius-md); overflow: hidden; }
.view-toggle__btn {
  border: none; background: var(--app-bg-elevated); padding: var(--app-sp-2) var(--app-sp-4);
  font-size: var(--app-fs-caption); font-weight: 500; color: var(--app-text-2); cursor: pointer;
  transition: all var(--app-transition-fast);
}
.view-toggle__btn + .view-toggle__btn { border-left: 1px solid var(--app-border); }
.view-toggle__btn.is-active { background: var(--app-brand-500); color: #fff; }

/* 表格 */
.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow: hidden; }
.dc-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.dc-table th { text-align: left; padding: var(--app-sp-3) var(--app-sp-5); background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption); border-bottom: 1px solid var(--app-border); }
.dc-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.dc-table tr:last-child td { border-bottom: none; }
.dc-table tr:hover td { background: var(--app-bg-hover); }
.dc-table__name { font-weight: 500; color: var(--app-text-1); }
.dc-table__actions { white-space: nowrap; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }

/* 卡片 */
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: var(--app-sp-4); }
.doc-card {
  background: var(--app-bg-elevated); border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg); padding: var(--app-sp-5);
  display: flex; flex-direction: column; align-items: center; gap: var(--app-sp-2);
  transition: all var(--app-transition-fast);
}
.doc-card:hover { border-color: var(--app-border); box-shadow: var(--app-shadow-sm); }
.doc-card__avatar { font-size: 36px; width: 64px; height: 64px; border-radius: 50%; background: var(--app-brand-50); display: flex; align-items: center; justify-content: center; }
.doc-card__name { font-size: var(--app-fs-h3); font-weight: 600; color: var(--app-text-1); margin: 0; }
.doc-card__meta { font-size: var(--app-fs-caption); color: var(--app-text-3); display: flex; align-items: center; gap: var(--app-sp-2); }
.doc-card__sep { color: var(--app-text-4); }
.doc-card__dept { font-size: var(--app-fs-caption); color: var(--app-brand-600); }
.doc-card__actions { display: flex; gap: var(--app-sp-2); margin-top: var(--app-sp-2); }
</style>
