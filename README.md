# 医院预约挂号系统 (Hospital Appointment System)

数据库课程设计项目，围绕"排班发布 → 患者查询 → 挂号预约 → 状态管理"闭环，构建一个核心的医院预约挂号数据库驱动系统。

**项目成员：** 狄东琛（后端）、肖国扬（前端）、范骐鸣（数据库/SQL）

## 系统功能

系统面向三类用户角色，各自拥有不同权限：

| 角色 | 功能 |
|------|------|
| **患者 (Patient)** | 注册/登录、浏览科室与医生、按日期/时段查询排班与号源、预约挂号、查看/取消预约、个人中心 |
| **医生 (Doctor)** | 登录、查看自己的排班、查看预约患者名册、完成就诊 |
| **管理员 (Admin)** | 登录、科室 CRUD、医生 CRUD、排班发布与管理、数据看板 |

### 核心业务流程

```
管理员发布排班 → 患者查询排班(剩余号源) → 选择时段预约 → 号源减1(事务)
                                                      ↓
                                              患者取消 → 号源回补(事务)
                                                      ↓
                                              医生完成就诊 → 状态更新
```

预约创建与取消均使用数据库事务（`@Transactional`）保证原子性，排班日期过期后预约自动标记为"已过期"。

## 技术栈

### 前端
| 技术 | 说明 |
|------|------|
| Vue 3.5 | Composition API |
| Vite 5 | 构建工具 |
| Element Plus 2.8 | UI 组件库（按需自动导入） |
| Vue Router 4 | 前端路由（按角色分组） |
| Pinia 2 | 状态管理 |
| Axios | HTTP 请求 |
| Sass | CSS 预处理 |

### 后端
| 技术 | 说明 |
|------|------|
| Java 17 | 运行环境 |
| Spring Boot 3.2.5 | 应用框架 |
| MyBatis-Plus 3.5.5 | ORM |
| MySQL 8.0 | 关系型数据库 |
| Maven | 项目构建 |
| SpringDoc OpenAPI 2.5 | API 文档（Swagger） |
| BCrypt | 密码哈希 |
| Spring Validation | 参数校验 |

## 数据库概况

共 5 张表，字符集 utf8mb4，引擎 InnoDB。

### ER 关系

```
Department (1) ──── (N) Doctor (1) ──── (N) Schedule (1) ──── (N) Appointment (N) ──── (1) Patient
```

- **科室 → 医生**：1:N（一个科室多名医生，一名医生归属一个科室）
- **医生 → 排班**：1:N（一名医生多条排班，一条排班属于一名医生）
- **患者 ↔ 排班**：M:N，通过 **Appointment（挂号记录）** 中间表拆解

### 表结构

| 表名 | 主键 | 核心字段 | 说明 |
|------|------|----------|------|
| `department` | dept_id (INT, AUTO) | dept_name (UNIQUE), location, description | 科室信息 |
| `doctor` | doc_id (INT, AUTO) | doc_name, gender (CHECK M/F), title, dept_id (FK), password (BCrypt) | 医生信息 |
| `patient` | patient_id (INT, AUTO) | id_card (UNIQUE, CHAR 18), real_name, gender, phone, password (BCrypt) | 患者信息 |
| `schedule` | schedule_id (INT, AUTO) | doc_id (FK), work_date, shift (CHECK 上午/下午/夜诊), total_quota, rest_quota | 排班号源 |
| `appointment` | appt_id (INT, AUTO) | patient_id (FK), schedule_id (FK), status (1=已预约/2=已取消/3=已就诊/4=已过期), create_time | 挂号记录 |

关键约束：`schedule` 表有 `CHECK (rest_quota >= 0 AND rest_quota <= total_quota)` 保证号源不超发；`appointment` 表在 `(patient_id, schedule_id)` 上应有唯一约束防止重复挂号。

## 运行方式

### 1. 环境要求

- **JDK 17** 及以上
- **Maven 3.8** 及以上
- **MySQL 8.0** 及以上（需提前运行）
- **Node.js 18** 及以上

### 2. 数据库准备

先在 MySQL 中创建数据库（表和数据由应用自动初始化，也可手动执行 SQL 脚本）：

```bash
# 手动建库（应用配置了 createDatabaseIfNotExist 可跳过）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hospital_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 3. 启动后端

```bash
cd backend

# 修改 src/main/resources/application.yml 中的数据库用户名/密码
# 默认为 root / 123456，根据本地环境调整

mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，Swagger 文档在 `http://localhost:8080/swagger-ui.html`。

管理员账号（配置在 application.yml 中）：`admin / admin123`。

> **注意：** `application.yml` 中 `spring.sql.init.mode` 当前为 `never`，首次启动需手动执行 `sql/schema.sql` 和 `sql/data.sql` 初始化数据。若改成 `always`，则每次启动删表重建并插入测试数据。

### 4. 启动前端

```bash
cd frontend

npm install
npm run dev
```

前端开发服务器在 `http://localhost:5173`，API 请求自动代理到 `http://localhost:8080`。

### 5. 测试账号

| 角色 | 用户名/ID | 密码 |
|------|-----------|------|
| 管理员 | admin | admin123 |
| 医生 | 1～13（doc_id） | 123456 |
| 患者 | 1～20（patient_id） | 123456 |

## 仓库主要文件

```
├── README.md                         # 项目入口说明（本文件）
├── .gitignore
├── sql/                              # 数据库脚本（独立维护版）
│   ├── schema.sql                    #   建表 DDL
│   ├── data.sql                      #   测试数据（10科室/13医生/20患者/75排班/84预约）
│   └── queries.sql                   #   核心查询 SQL 示例
├── docs/                             # 项目文档
│   ├── 项目需求说明.md                #   需求定义、角色、实体、关系、功能
│   ├── 关系模式设计.md                #   ER 图→关系模式、范式分析
│   ├── 数据库逻辑结构.md              #   表定义、约束、索引方案
│   ├── 数据库设计课堂实践方案.md       #   课程项目说明书
│   ├── 开发指导文档.md                #   团队分工、API 契约、任务清单
│   ├── 前端技术文档.md                #   前端架构与设计决策
│   ├── 前端UI设计.md                  #   UI 设计方案与线框图
│   ├── 数据库测试与问题解决报告.md     #   故障排查实录
│   ├── 自我测试说明.md                #   SQL 测试用例
│   ├── 医生信息表.md / 患者信息表.md   #   测试数据明细
│   └── ER图.png                      #   ER 图
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/hospital/        #   源码（按 controller/service/mapper/entity/dto 分层）
│       └── resources/
│           ├── application.yml       #   应用配置（数据库、端口、管理员账号）
│           ├── schema.sql            #   建表脚本（应用启动执行版）
│           └── data.sql              #   测试数据（应用启动执行版）
└── frontend/                         # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    ├── .env.development / .env.production
    ├── index.html
    └── src/
        ├── main.js / App.vue         #   入口
        ├── api/                      #   按模块拆分的 API 封装 + Axios 实例
        ├── router/                   #   路由表（按角色分组）+ 权限守卫
        ├── stores/                   #   Pinia 用户状态
        ├── layouts/                  #   布局组件（患者顶部导航、B端侧边栏）
        ├── views/                    #   页面（按角色分 patient/doctor/admin）
        ├── components/               #   通用组件
        ├── utils/                    #   工具函数（常量、校验、存储）
        └── styles/                   #   全局样式与 CSS Token
```

## API 接口概览

所有接口统一返回 `{ code, message, data }`，认证方式为请求头 `Authorization: Bearer <token>`。

### 公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/patients/register` | 患者注册 |
| POST | `/api/patients/login` | 患者登录 |
| POST | `/api/doctors/login` | 医生登录 |
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/departments` | 科室列表（支持关键词搜索） |
| GET | `/api/departments/{deptId}` | 科室详情 |
| GET | `/api/departments/{deptId}/doctors` | 科室下医生 |
| GET | `/api/schedules` | 排班查询（按科室/日期/时段） |

### 需认证接口

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| POST | `/api/appointments` | 患者 | 预约挂号（事务） |
| PUT | `/api/appointments/{id}/cancel` | 患者 | 取消预约（事务） |
| PUT | `/api/appointments/{id}/finish` | 医生 | 完成就诊 |
| GET | `/api/appointments/patients/{id}` | 患者 | 我的预约 |
| GET | `/api/doctors/{id}/schedules` | 医生 | 我的排班 |
| GET | `/api/doctors/schedules/{id}/patients` | 医生 | 排班患者名单 |
| CRUD | `/api/admin/departments` | 管理员 | 科室管理 |
| CRUD | `/api/admin/doctors` | 管理员 | 医生管理 |
| CRUD | `/api/admin/schedules` | 管理员 | 排班管理 |

## 认证机制

基于内存 Token 的认证方式：登录时服务端生成 UUID Token 存入 `ConcurrentHashMap`，客户端后续请求在 `Authorization` 头携带 `Bearer <token>`，由 `TokenInterceptor` 校验。Token 在服务重启后全部失效，需重新登录。密码使用 BCrypt 哈希存储。
