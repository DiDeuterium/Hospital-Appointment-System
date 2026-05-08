# 医院预约挂号系统 — 前端

## 技术栈
- Vue 3.5 + Vite 5
- Element Plus（按需自动导入）
- Vue Router 4 + Pinia 2
- Axios（统一 baseURL `/api`，开发期通过 Vite proxy 转发到 `http://localhost:8080`）

## 启动
```bash
npm install
npm run dev          # 开发: http://localhost:5173
npm run build        # 生产构建
npm run preview      # 预览生产包
```

后端默认地址在 `.env.development` 中通过 `VITE_PROXY_TARGET` 配置，默认 `http://localhost:8080`。

## 目录结构
```
frontend/
├── public/                    静态资源（直出）
├── src/
│   ├── api/                   按业务模块拆分的接口封装
│   │   ├── request.js         axios 实例 + 拦截器
│   │   ├── auth.js            登录/注册
│   │   ├── department.js      科室
│   │   ├── doctor.js          医生
│   │   ├── schedule.js        排班
│   │   └── appointment.js     预约
│   ├── router/
│   │   ├── index.js           路由实例 + 全局守卫
│   │   └── routes/            按角色拆分的路由表
│   ├── stores/                Pinia (user.js)
│   ├── layouts/               顶部导航 + 侧边栏 + 内容区
│   ├── views/                 页面，按角色聚合
│   │   ├── login/  patient/  doctor/  admin/  error/
│   ├── utils/                 storage / constants / validators
│   ├── styles/index.scss
│   ├── App.vue
│   └── main.js
├── .env / .env.development / .env.production
├── vite.config.js
└── package.json
```

## 设计取舍
1. **按角色拆分路由**：`router/routes/{patient,doctor,admin}.js`，新增角色只需新增一个文件并在 `router/index.js` 注册。
2. **按业务模块拆分 API**：每个后端模块对应一个 `api/*.js`，函数名贴近接口路径，便于联调时定位。
3. **统一响应处理**：`request.js` 拦截 `{ code, message, data }`，业务码非 200 抛错并 ElMessage 提示，调用方只关注 `data`。
4. **角色权限**：路由 `meta.roles` 声明可访问角色，全局守卫读 Pinia user store 校验。401 自动跳登录。
5. **登录态持久化**：token + 用户信息写入 `localStorage`，刷新不丢登录态。
6. **Element Plus 按需自动导入**：`unplugin-auto-import` + `unplugin-vue-components`，组件无需手动 import。
7. **侧边栏由路由元信息驱动**：`meta.title` / `meta.icon` / `meta.hidden` 控制，新增菜单只需配路由。

## 与后端接口对接
- Base URL: `/api`
- 请求头：登录后自动携带 `token`
- 响应：`{ code: 200, message, data }`
- 错误消息直接展示给用户（中文、清晰）

完整接口对应关系见 `src/api/*.js` 中的注释，与 `docs/开发指导文档.md` 一致。
