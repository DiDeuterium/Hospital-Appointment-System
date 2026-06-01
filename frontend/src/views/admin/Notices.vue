<script setup>
import { onMounted, onBeforeUnmount, ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdminNotices, createNotice, updateNotice, offlineNotice } from '@/api/notice'
import { formatDateTime } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const list = ref([])
const loading = ref(false)
const statusFilter = ref('')

const blankForm = () => ({ noticeId: null, title: '', content: '', isTop: 0, status: 1 })
const dialog = reactive({ visible: false, isEdit: false, form: blankForm() })
const formRef = ref()
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }]
}

const filtered = computed(() => {
  if (statusFilter.value === '') return list.value
  return list.value.filter(n => String(n.status) === statusFilter.value)
})

async function load() {
  loading.value = true
  try {
    list.value = await listAdminNotices(statusFilter.value === '' ? {} : { status: statusFilter.value })
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

function openCreate() {
  dialog.isEdit = false
  dialog.form = blankForm()
  dialog.visible = true
}
function openEdit(row) {
  dialog.isEdit = true
  dialog.form = { ...row }
  dialog.visible = true
}

async function submit() {
  await formRef.value.validate()
  const payload = {
    title: dialog.form.title,
    content: dialog.form.content,
    isTop: dialog.form.isTop ? 1 : 0,
    status: dialog.form.status
  }
  try {
    if (dialog.isEdit) {
      await updateNotice(dialog.form.noticeId, payload)
      ElMessage.success('公告已更新')
    } else {
      await createNotice(payload)
      ElMessage.success('公告已发布')
    }
    dialog.visible = false
    load()
  } catch { /* 拦截器已弹错误 */ }
}

async function offline(row) {
  try {
    await ElMessageBox.confirm('确认下线该公告？下线后患者端不再展示。', '提示', { type: 'warning', lockScroll: false })
    await offlineNotice(row.noticeId)
    ElMessage.success('已下线')
    load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

const now = ref(new Date())
const clock = computed(() => {
  const d = now.value
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0') + ' ' + d.toTimeString().slice(0, 8)
})
let clockTimer = null

onMounted(() => {
  load()
  clockTimer = setInterval(() => { now.value = new Date() }, 1000)
})
onBeforeUnmount(() => { if (clockTimer) clearInterval(clockTimer) })
</script>

<template>
  <div class="page-container">
    <PageHeader title="公告管理" :subtitle="'共 ' + list.length + ' 条公告'">
      <template #extra>
        <time class="page-clock">{{ clock }}</time>
        <el-button size="large" type="primary" @click="openCreate">新增公告</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="1">已发布</el-radio-button>
        <el-radio-button value="0">已下线</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="table-wrap">
      <table class="nt-table" v-if="filtered.length">
        <thead>
          <tr><th>标题</th><th>置顶</th><th>状态</th><th>发布时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="n in filtered" :key="n.noticeId">
            <td class="nt-table__title">{{ n.title }}</td>
            <td>
              <StatusTag v-if="n.isTop" type="danger" size="small">置顶</StatusTag>
              <span v-else class="nt-table__muted">—</span>
            </td>
            <td>
              <StatusTag :type="n.status === 1 ? 'success' : 'info'" size="small">
                {{ n.status === 1 ? '已发布' : '已下线' }}
              </StatusTag>
            </td>
            <td class="nt-table__time">{{ formatDateTime(n.publishTime) }}</td>
            <td class="nt-table__actions">
              <el-button size="small" @click="openEdit(n)">编辑</el-button>
              <el-button size="small" type="warning" plain :disabled="n.status === 0" @click="offline(n)">下线</el-button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无公告</div>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑公告' : '新增公告'" width="560px" :lock-scroll="false">
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="dialog.form.title" maxlength="100" show-word-limit size="large" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="dialog.form.content" type="textarea" :rows="5" placeholder="公告正文" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="是否置顶">
            <el-switch v-model="dialog.form.isTop" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="发布状态">
            <el-radio-group v-model="dialog.form.status">
              <el-radio-button :value="1">发布</el-radio-button>
              <el-radio-button :value="0">下线</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>
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
.page-clock { font-size: var(--app-fs-body); color: var(--app-text-3); font-variant-numeric: tabular-nums; white-space: nowrap; }
.toolbar { display: flex; gap: var(--app-sp-3); margin-bottom: var(--app-sp-6); }

.table-wrap { background: var(--app-bg-elevated); border: 1px solid var(--app-border-light); border-radius: var(--app-radius-lg); overflow: hidden; }
.nt-table { width: 100%; border-collapse: collapse; font-size: var(--app-fs-body); }
.nt-table th { text-align: left; padding: var(--app-sp-3) var(--app-sp-5); background: var(--app-bg-subtle); color: var(--app-text-3); font-weight: 500; font-size: var(--app-fs-caption); border-bottom: 1px solid var(--app-border); }
.nt-table td { padding: var(--app-sp-3) var(--app-sp-5); border-bottom: 1px solid var(--app-border-light); color: var(--app-text-2); }
.nt-table tr:last-child td { border-bottom: none; }
.nt-table tr:hover td { background: var(--app-bg-hover); }
.nt-table__title { font-weight: 500; color: var(--app-text-1); }
.nt-table__time { color: var(--app-text-3); font-size: var(--app-fs-caption); white-space: nowrap; }
.nt-table__muted { color: var(--app-text-4); }
.nt-table__actions { white-space: nowrap; }
.empty { text-align: center; padding: var(--app-sp-8); color: var(--app-text-3); font-size: var(--app-fs-caption); }

.form-row { display: flex; gap: var(--app-sp-8); }
</style>
