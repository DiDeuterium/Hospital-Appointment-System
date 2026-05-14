<script setup>
defineProps({
  title: String,
  subtitle: String,
  breadcrumbs: { type: Array, default: () => [] }
  // breadcrumbs 项形如：{ label: '首页', to: '/patient/home' }，最后一项不传 to
})
</script>

<template>
  <header class="page-header">
    <nav v-if="breadcrumbs.length" class="page-header__crumbs" aria-label="breadcrumb">
      <template v-for="(b, i) in breadcrumbs" :key="i">
        <router-link
          v-if="b.to && i < breadcrumbs.length - 1"
          :to="b.to"
          class="page-header__crumb"
        >{{ b.label }}</router-link>
        <span v-else class="page-header__crumb is-current">{{ b.label }}</span>
        <span v-if="i < breadcrumbs.length - 1" class="page-header__sep">/</span>
      </template>
    </nav>
    <div class="page-header__row">
      <div class="page-header__titles">
        <h1 v-if="title" class="page-header__title">{{ title }}</h1>
        <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.extra" class="page-header__extra">
        <slot name="extra" />
      </div>
    </div>
  </header>
</template>

<style scoped>
.page-header { margin-bottom: var(--app-sp-6); }

.page-header__crumbs {
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  margin-bottom: var(--app-sp-3);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.page-header__crumb {
  color: var(--app-text-3);
  transition: color var(--app-transition-fast);
}
.page-header__crumb:hover:not(.is-current) {
  color: var(--app-brand-600);
}
.page-header__crumb.is-current { color: var(--app-text-2); }
.page-header__sep {
  margin: 0 8px;
  color: var(--app-text-4);
}

.page-header__row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--app-sp-4);
  flex-wrap: wrap;
}
.page-header__title {
  font-size: var(--app-fs-h1);
  font-weight: 600;
  color: var(--app-text-1);
  margin: 0;
}
.page-header__subtitle {
  margin-top: 6px;
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
}
.page-header__extra {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
}
</style>
