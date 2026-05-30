-- ============================================
-- 医院预约挂号系统 — 数据库升级方案
-- 主题：排班发布、患者预约、模拟支付、排班变更审核、公告通知
-- ============================================

DROP TABLE IF EXISTS schedule_change_request;
DROP TABLE IF EXISTS payment_record;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS sys_notice;
DROP TABLE IF EXISTS admin_user;
DROP TABLE IF EXISTS department;

-- ================== 1. 基础信息模块 ==================

-- 1. 科室表
CREATE TABLE department (
    dept_id     INT          AUTO_INCREMENT PRIMARY KEY COMMENT '科室编号',
    dept_name   VARCHAR(50)  NOT NULL UNIQUE            COMMENT '科室名称',
    location    VARCHAR(100)                            COMMENT '门诊位置',
    description TEXT                                    COMMENT '科室简介',
    status      TINYINT      NOT NULL DEFAULT 1         COMMENT '状态: 1正常 0停用',
    CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室信息表';

-- 2. 医生表
CREATE TABLE doctor (
    doc_id      INT          AUTO_INCREMENT PRIMARY KEY            COMMENT '医生编号',
    doc_name    VARCHAR(50)  NOT NULL                              COMMENT '医生姓名',
    gender      CHAR(1)      NOT NULL CHECK (gender IN ('M', 'F')) COMMENT '性别',
    title       VARCHAR(20)                                        COMMENT '职称',
    dept_id     INT          NOT NULL                              COMMENT '所属科室编号',
    password    VARCHAR(255) NOT NULL                              COMMENT '登录密码(BCrypt哈希)',
    avatar_url  VARCHAR(255)                                       COMMENT '医生头像URL',
    specialty   VARCHAR(500)                                       COMMENT '擅长领域',
    status      TINYINT      NOT NULL DEFAULT 1                    COMMENT '状态: 1正常 0停用',
    CHECK (status IN (0, 1)),
    FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生信息表';

-- 3. 患者表
CREATE TABLE patient (
    patient_id INT           AUTO_INCREMENT PRIMARY KEY   COMMENT '患者流水号',
    id_card    CHAR(18)      NOT NULL UNIQUE              COMMENT '身份证号',
    real_name  VARCHAR(50)   NOT NULL                     COMMENT '真实姓名',
    gender     CHAR(1)       CHECK (gender IN ('M', 'F')) COMMENT '性别',
    phone      VARCHAR(20)   NOT NULL                     COMMENT '联系电话',
    password   VARCHAR(255)  NOT NULL                     COMMENT '登录密码(BCrypt哈希)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者信息表';

-- 4. 管理员表
CREATE TABLE admin_user (
    admin_id    INT          AUTO_INCREMENT PRIMARY KEY COMMENT '管理员编号',
    username    VARCHAR(50)  NOT NULL UNIQUE            COMMENT '登录账号',
    real_name   VARCHAR(50)  NOT NULL                   COMMENT '管理员姓名',
    password    VARCHAR(255) NOT NULL                   COMMENT '登录密码(BCrypt哈希)',
    status      TINYINT      NOT NULL DEFAULT 1         COMMENT '状态: 1正常 0停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- ================== 2. 排班与预约模块 ==================

-- 5. 排班表
CREATE TABLE schedule (
    schedule_id INT           AUTO_INCREMENT PRIMARY KEY                 COMMENT '排班流水号',
    doc_id      INT           NOT NULL                                   COMMENT '出诊医生编号',
    work_date   DATE          NOT NULL                                   COMMENT '出诊日期',
    shift       VARCHAR(10)   NOT NULL CHECK (shift IN ('上午', '下午', '夜诊')) COMMENT '时段',
    total_quota INT           NOT NULL CHECK (total_quota > 0)           COMMENT '总号源数',
    rest_quota  INT           NOT NULL                                   COMMENT '剩余号源数',
    fee         DECIMAL(8, 2) NOT NULL DEFAULT 0.00                      COMMENT '挂号费',
    status      TINYINT       NOT NULL DEFAULT 1                         COMMENT '出诊状态: 1正常 0停诊',
    UNIQUE (doc_id, work_date, shift),
    CHECK (fee >= 0),
    CHECK (status IN (0, 1)),
    CHECK (rest_quota >= 0 AND rest_quota <= total_quota),
    FOREIGN KEY (doc_id) REFERENCES doctor(doc_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生排班表';

-- 6. 挂号预约表
CREATE TABLE appointment (
    appt_id       INT          AUTO_INCREMENT PRIMARY KEY COMMENT '预约单号',
    patient_id    INT          NOT NULL                   COMMENT '预约患者ID',
    schedule_id   INT          NOT NULL                   COMMENT '对应排班ID',
    queue_number  INT          NOT NULL                   COMMENT '就诊排队号',
    status        TINYINT      NOT NULL DEFAULT 1         COMMENT '状态: 1已预约 2已取消 3已就诊 4已过期',
    cancel_reason VARCHAR(255)                            COMMENT '取消原因',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE (schedule_id, queue_number),
    CHECK (queue_number > 0),
    CHECK (status IN (1, 2, 3, 4)),
    FOREIGN KEY (patient_id)  REFERENCES patient(patient_id)   ON DELETE RESTRICT,
    FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挂号预约记录表';

-- 7. 挂号费模拟支付记录表
CREATE TABLE payment_record (
    payment_id  INT           AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录编号',
    appt_id     INT           NOT NULL UNIQUE            COMMENT '关联预约单号',
    amount      DECIMAL(8, 2) NOT NULL                   COMMENT '挂号费金额',
    pay_status  TINYINT       NOT NULL DEFAULT 0         COMMENT '支付状态: 0待支付 1已支付 2已关闭',
    pay_method  VARCHAR(20)   NOT NULL DEFAULT '模拟支付' COMMENT '支付方式',
    pay_time    DATETIME                                 COMMENT '支付时间',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHECK (amount >= 0),
    CHECK (pay_status IN (0, 1, 2)),
    FOREIGN KEY (appt_id) REFERENCES appointment(appt_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挂号费模拟支付记录表';

-- ================== 3. 排班变更审核模块 ==================

-- 8. 排班变更申请表
CREATE TABLE schedule_change_request (
    request_id         INT          AUTO_INCREMENT PRIMARY KEY COMMENT '申请编号',
    schedule_id        INT          NOT NULL                   COMMENT '关联排班ID',
    change_type        TINYINT      NOT NULL                   COMMENT '变更类型: 1停诊 2修改排班',
    target_work_date   DATE                                    COMMENT '目标出诊日期(修改排班时使用)',
    target_shift       VARCHAR(10)                             COMMENT '目标时段(修改排班时使用)',
    target_total_quota INT                                     COMMENT '目标总号源数(修改排班时使用)',
    reason             VARCHAR(255) NOT NULL                   COMMENT '申请原因',
    status             TINYINT      NOT NULL DEFAULT 1         COMMENT '审核状态: 1待审核 2已通过 3已驳回 4已撤回',
    apply_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
    audit_admin_id     INT                                     COMMENT '实际审核管理员ID',
    audit_time         DATETIME                                COMMENT '审核时间',
    audit_remark       VARCHAR(255)                            COMMENT '审核意见',
    CHECK (change_type IN (1, 2)),
    CHECK (target_shift IS NULL OR target_shift IN ('上午', '下午', '夜诊')),
    CHECK (target_total_quota IS NULL OR target_total_quota > 0),
    CHECK (status IN (1, 2, 3, 4)),
    FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id) ON DELETE RESTRICT,
    FOREIGN KEY (audit_admin_id) REFERENCES admin_user(admin_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班变更申请表';

-- ================== 4. 公告模块 ==================

-- 9. 系统公告表
CREATE TABLE sys_notice (
    notice_id    INT          AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    title        VARCHAR(100) NOT NULL                   COMMENT '公告标题',
    content      TEXT         NOT NULL                   COMMENT '公告正文',
    is_top       TINYINT      NOT NULL DEFAULT 0         COMMENT '是否置顶: 1是 0否',
    status       TINYINT      NOT NULL DEFAULT 1         COMMENT '状态: 1发布 0下线',
    publish_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    admin_id     INT                                     COMMENT '发布管理员ID',
    CHECK (is_top IN (0, 1)),
    CHECK (status IN (0, 1)),
    FOREIGN KEY (admin_id) REFERENCES admin_user(admin_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ================== 性能索引 ==================

CREATE INDEX idx_doctor_dept_id        ON doctor(dept_id);
CREATE INDEX idx_doctor_status         ON doctor(status);
CREATE INDEX idx_schedule_doc_id       ON schedule(doc_id);
CREATE INDEX idx_schedule_date         ON schedule(work_date);
CREATE INDEX idx_schedule_query        ON schedule(work_date, doc_id, status);
CREATE INDEX idx_appt_patient          ON appointment(patient_id, status);
CREATE INDEX idx_appt_schedule         ON appointment(schedule_id);
CREATE INDEX idx_appt_status           ON appointment(status);
CREATE INDEX idx_payment_status        ON payment_record(pay_status, create_time);
CREATE INDEX idx_change_request_status ON schedule_change_request(status, apply_time);
CREATE INDEX idx_notice_status         ON sys_notice(status, is_top, publish_time);
