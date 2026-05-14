<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
function goHome() { router.replace('/') }
function goBack() {
  if (window.history.length > 1) router.back()
  else router.replace('/')
}
</script>

<template>
  <div class="error-page">
    <div class="error-page__inner">
      <div class="error-page__code">404</div>
      <h1 class="error-page__title">页面去看医生了…</h1>
      <p class="error-page__desc">您访问的页面不存在或已被移除，请检查地址或返回首页</p>
      <div class="error-page__actions">
        <el-button size="large" @click="goBack">返回上一步</el-button>
        <el-button type="primary" size="large" @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-page);
  padding: var(--app-sp-6);
}
.error-page__inner {
  text-align: center;
  max-width: 480px;
}
.error-page__code {
  font-size: 140px;
  font-weight: 700;
  color: var(--app-brand-100);
  line-height: 1;
  margin-bottom: var(--app-sp-5);
  font-variant-numeric: tabular-nums;
  letter-spacing: -2px;
}
.error-page__title {
  font-size: var(--app-fs-h1);
  color: var(--app-text-1);
  font-weight: 600;
  margin-bottom: var(--app-sp-3);
}
.error-page__desc {
  font-size: var(--app-fs-body);
  color: var(--app-text-3);
  margin-bottom: var(--app-sp-8);
  line-height: 1.6;
}
.error-page__actions {
  display: flex;
  gap: var(--app-sp-3);
  justify-content: center;
}
</style>
