<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { listDepartments } from '@/api/department'
import { deptIcon, DEPT_CATEGORIES } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import AppIcon from '@/components/AppIcon.vue'

const router = useRouter()
const keyword = ref('')
const list = ref([])
const loading = ref(false)
const activeCat = ref('all')

const filtered = reactive({
  get value() {
    let result = list.value
    if (activeCat.value !== 'all') {
      const cat = DEPT_CATEGORIES.find(c => c.key === activeCat.value)
      if (cat?.test) result = result.filter(d => cat.test(d.deptName))
    }
    if (keyword.value.trim()) {
      const kw = keyword.value.trim()
      result = result.filter(d => d.deptName.includes(kw))
    }
    return result
  }
})

async function load() {
  loading.value = true
  try { list.value = await listDepartments() } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

function goDept(dept) {
  router.push({ name: 'PatientDoctors', params: { deptId: dept.deptId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="选择就诊科室"
      :subtitle="'共 ' + list.length + ' 个科室，请选择您要预约的科室'"
      :breadcrumbs="[{ label: '首页', to: '/patient/home' }, { label: '选择科室' }]"
    />

    <!-- 搜索 + 分类 -->
    <div class="dept-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索科室名称"
        clearable
        size="large"
        class="dept-toolbar__search"
      >
        <template #prefix>
          <AppIcon name="search" :size="16" style="color:var(--app-text-4)" />
        </template>
      </el-input>
    </div>
    <div class="dept-cats">
      <button
        v-for="c in DEPT_CATEGORIES"
        :key="c.key"
        type="button"
        class="dept-cats__btn"
        :class="{ 'is-active': activeCat === c.key }"
        @click="activeCat = c.key"
      >{{ c.label }}</button>
    </div>

    <!-- 科室卡片网格 -->
    <div v-loading="loading" class="dept-grid">
      <el-row :gutter="16">
        <el-col
          v-for="d in filtered.value"
          :key="d.deptId"
          :xs="12"
          :sm="8"
          :md="6"
          style="margin-bottom:16px"
        >
          <div class="dept-card" @click="goDept(d)">
            <div class="dept-card__icon">{{ deptIcon(d.deptName) }}</div>
            <h3 class="dept-card__name">{{ d.deptName }}</h3>
            <p class="dept-card__loc">{{ d.location || '—' }}</p>
            <p class="dept-card__desc">{{ (d.description || '').slice(0, 36) || '暂无简介' }}</p>
            <div class="dept-card__go">
              立即查看
              <AppIcon name="chevron-right" :size="14" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}

.dept-toolbar {
  margin-bottom: var(--app-sp-4);
}
.dept-toolbar__search { max-width: 360px; }

.dept-cats {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: var(--app-sp-6);
}
.dept-cats__btn {
  border: none;
  background: var(--app-bg-elevated);
  color: var(--app-text-2);
  border: 1px solid var(--app-border-light);
  padding: 6px var(--app-sp-4);
  border-radius: var(--app-radius-full);
  font-size: var(--app-fs-caption);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--app-transition-fast);
}
.dept-cats__btn:hover {
  border-color: var(--app-brand-400);
  color: var(--app-brand-600);
}
.dept-cats__btn.is-active {
  background: var(--app-brand-500);
  color: #fff;
  border-color: var(--app-brand-500);
}

/* ---- 卡片 ---- */
.dept-card {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-5);
  cursor: pointer;
  transition: all var(--app-transition-fast);
  height: 100%;
}
.dept-card:hover {
  border-color: var(--app-brand-300);
  box-shadow: var(--app-shadow-sm);
  transform: translateY(-2px);
}
.dept-card__icon {
  font-size: 28px;
  margin-bottom: var(--app-sp-3);
  display: block;
}
.dept-card__name {
  font-size: var(--app-fs-h3);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0 0 var(--app-sp-2);
}
.dept-card__loc {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin: 0 0 4px;
}
.dept-card__desc {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin: 0 0 var(--app-sp-3);
  min-height: 32px;
}
.dept-card__go {
  font-size: var(--app-fs-caption);
  color: var(--app-brand-600);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
