# Hospital Appointment System — 后端

## 项目简介

医院预约挂号系统的后端服务，基于 Spring Boot 3.2 构建，提供 RESTful API 供前端调用。支持患者注册/登录、科室与医生查询、排班查看、预约挂号/取消，以及管理员后台管理（科室、医生、排班 CRUD）等核心功能。

**设计文档**：[数据库逻辑结构.md](../docs/数据库逻辑结构.md) | [数据库设计课堂实践方案.md](../docs/数据库设计课堂实践方案.md)

## 技术栈

| 组件 | 版本 / 选型 |
|------|-------------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0+ |
| 构建工具 | Maven |
| API 文档 | SpringDoc OpenAPI (Swagger) 2.5 |
| 密码加密 | BCrypt (spring-security-crypto) |
| 参数校验 | spring-boot-starter-validation |

## 项目结构

```
backend/
├── pom.xml                          # Maven 配置
└── src/main/
    ├── java/com/hospital/
    │   ├── HospitalApplication.java # 启动入口
    │   ├── config/                  # 配置类 (CORS、拦截器注册)
    │   ├── controller/              # 控制器层
    │   │   ├── AdminController      # /api/admin/*
    │   │   ├── AppointmentController# /api/appointments/*
    │   │   ├── DepartmentController # /api/departments/*
    │   │   ├── DoctorController     # /api/doctors/*
    │   │   ├── PatientController    # /api/patients/*
    │   │   └── ScheduleController   # /api/schedules/*
    │   ├── dto/                     # 数据传输对象
    │   │   ├── request/             # 请求体 DTO
    │   │   ├── response/            # 响应体 VO
    │   │   └── Result.java          # 统一响应格式
    │   ├── entity/                  # 数据库实体
    │   ├── exception/               # 全局异常处理
    │   ├── interceptor/             # Token 认证拦截器
    │   ├── mapper/                  # MyBatis-Plus Mapper
    │   └── service/                 # 业务逻辑层
    └── resources/
        └── application.yml          # 应用配置
```

应用启动时自动执行 `src/main/resources/` 下的 `schema.sql` 和 `data.sql`，无需手动建库。原始 SQL 脚本位于项目根目录下的 `sql/` 目录。

## 快速启动

### 1. 环境要求

- **JDK 17** 或更高
- **Maven 3.8**+
- **MySQL 8.0**+（确保服务正在运行即可，数据库和表由应用自动创建）

### 2. 修改配置

编辑 `src/main/resources/application.yml`，根据本地环境修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_db?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root        # 改为你的 MySQL 用户名
    password: root        # 改为你的 MySQL 密码
```

管理员账号也可在此配置：

```yaml
admin:
  username: admin
  password: admin123
```

### 3. 启动项目

```bash
cd backend

# 编译并启动（开发环境）
mvn spring-boot:run

# 或者先打包再运行
mvn clean package -DskipTests
java -jar target/hospital-appointment-1.0.0.jar
```

启动后访问：
- 服务地址：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## API 接口概览

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

### 公开接口（无需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/patients/register` | 患者注册 |
| POST | `/api/patients/login` | 患者登录 |
| POST | `/api/doctors/login` | 医生登录 |
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/departments` | 科室列表 |
| GET | `/api/departments/{deptId}` | 科室详情 |
| GET | `/api/departments/{deptId}/doctors` | 科室下医生列表 |
| GET | `/api/schedules` | 排班查询 |

### 需要认证（请求头 `Authorization: Bearer <token>`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/appointments` | 预约挂号 |
| PUT | `/api/appointments/{apptId}/cancel` | 取消预约 |
| GET | `/api/appointments/patients/{patientId}` | 患者预约记录 |
| GET | `/api/doctors/{docId}/schedules` | 医生查看自己的排班 |
| GET | `/api/doctors/schedules/{scheduleId}/patients` | 医生查看排班下的患者 |
| GET/POST/PUT/DELETE | `/api/admin/departments` | 管理员-科室管理 |
| GET/POST/PUT/DELETE | `/api/admin/doctors` | 管理员-医生管理 |
| GET/POST/PUT/DELETE | `/api/admin/schedules` | 管理员-排班管理 |

## 认证机制

采用 Token 认证方式：

1. 客户端调用登录接口，传入凭证
2. 服务端验证通过后返回 Token（`LoginResponse.token`）
3. 客户端在后续请求的 `Authorization` 头中携带 Token：`Bearer <token>`
4. `TokenInterceptor` 拦截 `/api/**` 路径（公开接口除外），校验 Token 有效性
5. Token 为内存存储，重启后所有 Token 失效，需重新登录

## FAQ

### Q: 如何修改服务端口？

编辑 `application.yml` 中的 `server.port`，默认为 8080。

### Q: 如何查看执行的 SQL？

MyBatis-Plus 已配置 `StdOutImpl` 日志实现，所有 SQL 会输出到控制台。如需关闭，删除 `application.yml` 中的 `mybatis-plus.configuration.log-impl` 配置。

### Q: 密码如何存储？

患者和医生的密码使用 BCrypt 哈希后存入数据库，不存储明文。管理员密码直接配置在 `application.yml` 中（明文，仅用于开发环境）。

### Q: 如何重置患者密码？

目前无自助重置密码功能。可以通过数据库直接更新 `patient` 表的 `password` 字段，值为 BCrypt 哈希结果。可以使用在线 BCrypt 工具生成哈希值。

### Q: Swagger 文档无法访问？

确认 SpringDoc 依赖已正确引入（`springdoc-openapi-starter-webmvc-ui`），启动后访问 `/swagger-ui.html`。如果被拦截器拦截，检查 `WebMvcConfig` 中 Swagger 相关路径是否在排除列表中。

### Q: 每次启动都会重置数据库吗？

是的。`spring.sql.init.mode: always` 会在每次启动时执行 `schema.sql`（删表重建）和 `data.sql`（插入测试数据），所以重启后所有数据会恢复到初始状态。如需保留数据，将 `application.yml` 中的 `spring.sql.init.mode` 改为 `never`，并手动执行 `sql/` 目录下的脚本。

### Q: 启动时提示数据库连接失败？

检查以下项目：
1. MySQL 服务是否运行
2. `application.yml` 中的数据库 URL、用户名、密码是否正确
3. MySQL 驱动是否与数据库版本兼容（MySQL 8.0+ 使用 `mysql-connector-j`）

### Q: IDEA 中 Lombok 报错？

确保已安装 Lombok 插件（Settings → Plugins → 搜索 Lombok），并在 Settings → Build → Compiler → Annotation Processors 中勾选 "Enable annotation processing"。
