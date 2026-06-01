<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listNotices } from '@/api/notice'
import { formatDateTime } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const loading = ref(false)

// 置顶优先，其次按发布时间倒序
const sorted = computed(() =>
  [...list.value].sort((a, b) => {
    if ((b.isTop || 0) !== (a.isTop || 0)) return (b.isTop || 0) - (a.isTop || 0)
    return String(b.publishTime || '').localeCompare(String(a.publishTime || ''))
  })
)

async function load() {
  loading.value = true
  try {
    list.value = await listNotices()
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

function viewDetail(n) {
  router.push({ name: 'PatientNoticeDetail', params: { noticeId: n.noticeId } })
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="系统公告"
      :subtitle="'共 ' + list.length + ' 条公告'"
      :breadcrumbs="[{ label: '首页', to: '/patient/home' }, { label: '系统公告' }]"
    />

    <div v-loading="loading" class="notice-list">
      <template v-if="sorted.length">
        <article
          v-for="n in sorted"
          :key="n.noticeId"
          class="notice-card"
          @click="viewDetail(n)"
        >
          <div class="notice-card__head">
            <StatusTag v-if="n.isTop" type="danger" size="small">置顶</StatusTag>
            <h3 class="notice-card__title">{{ n.title }}</h3>
          </div>
          <p class="notice-card__excerpt">{{ (n.content || '').slice(0, 80) }}</p>
          <div class="notice-card__time">{{ formatDateTime(n.publishTime) }}</div>
        </article>
      </template>
      <EmptyState v-else-if="!loading" title="暂无公告" description="目前没有已发布的系统公告" />
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.notice-list { display: flex; flex-direction: column; gap: var(--app-sp-4); }
.notice-card {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-lg);
  padding: var(--app-sp-5);
  cursor: pointer;
  transition: all var(--app-transition-fast);
}
.notice-card:hover { border-color: var(--app-border); box-shadow: var(--app-shadow-sm); transform: translateY(-1px); }
.notice-card__head { display: flex; align-items: center; gap: var(--app-sp-2); margin-bottom: var(--app-sp-2); }
.notice-card__title { font-size: var(--app-fs-h3); font-weight: 600; color: var(--app-text-1); margin: 0; }
.notice-card__excerpt {
  font-size: var(--app-fs-caption); color: var(--app-text-3); margin: 0 0 var(--app-sp-3);
  line-height: var(--app-lh-relaxed);
}
.notice-card__time { font-size: var(--app-fs-tiny); color: var(--app-text-4); }
</style>
