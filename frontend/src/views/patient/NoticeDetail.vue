<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNotice } from '@/api/notice'
import { formatDateTime } from '@/utils/booking'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import SectionCard from '@/components/SectionCard.vue'

const route = useRoute()
const router = useRouter()
const notice = ref(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    notice.value = await getNotice(route.params.noticeId)
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <PageHeader
      :title="notice ? notice.title : '公告详情'"
      :breadcrumbs="[
        { label: '首页', to: '/patient/home' },
        { label: '系统公告', to: '/patient/notices' },
        { label: notice ? notice.title : '详情' }
      ]"
    />

    <SectionCard v-loading="loading">
      <template v-if="notice">
        <div class="notice-head">
          <StatusTag v-if="notice.isTop" type="danger" size="small">置顶</StatusTag>
          <h1 class="notice-head__title">{{ notice.title }}</h1>
          <div class="notice-head__time">发布于 {{ formatDateTime(notice.publishTime) }}</div>
        </div>
        <div class="notice-body">{{ notice.content }}</div>
      </template>
      <div v-else-if="!loading" class="empty">
        <p>未找到该公告</p>
        <el-button type="primary" @click="router.replace({ name: 'PatientNotices' })">返回公告列表</el-button>
      </div>
    </SectionCard>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.notice-head { padding-bottom: var(--app-sp-4); border-bottom: 1px solid var(--app-border-light); margin-bottom: var(--app-sp-5); }
.notice-head__title { font-size: var(--app-fs-h1); font-weight: 600; color: var(--app-text-1); margin: var(--app-sp-2) 0; }
.notice-head__time { font-size: var(--app-fs-caption); color: var(--app-text-3); }
.notice-body {
  font-size: var(--app-fs-body);
  color: var(--app-text-1);
  line-height: var(--app-lh-relaxed);
  white-space: pre-wrap;
  word-break: break-word;
}
.empty { text-align: center; padding: var(--app-sp-10) 0; color: var(--app-text-3); }
.empty p { margin-bottom: var(--app-sp-4); }
</style>
