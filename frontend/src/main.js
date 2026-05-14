import { createApp } from 'vue'
import { createPinia } from 'pinia'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

import 'element-plus/dist/index.css'
import './styles/index.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// 全局注册 Element Plus 图标：路由 meta.icon 与模板里 <component :is="..."/> 可直接按名称使用
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.mount('#app')
