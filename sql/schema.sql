-- ============================================
-- 医院预约挂号系统 — 数据库建表脚本
-- 版本: 1.1
-- 日期: 2026-05-07
-- 数据库: MySQL 8.0+
-- 设计人: 范骐鸣
-- 说明: 包含全部 5 张表, 含完整约束(PK/FK/UNIQUE/CHECK/NOT NULL/DEFAULT)与性能索引
--       所有主键统一使用 INT AUTO_INCREMENT
-- ============================================

CREATE DATABASE IF NOT EXISTS hospital_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hospital_db;

-- 清除旧表(按外键依赖逆序删除, 确保脚本可重复执行)
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS department;

-- ============================================
-- 1. 科室表
-- ============================================
CREATE TABLE department (
    dept_id     INT          AUTO_INCREMENT PRIMARY KEY COMMENT '科室编号 (自增)',
    dept_name   VARCHAR(50)  NOT NULL UNIQUE         COMMENT '科室名称',
    location    VARCHAR(100)                         COMMENT '门诊位置',
    description TEXT                                 COMMENT '科室简介'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. 医生表
-- ============================================
CREATE TABLE doctor (
    doc_id   INT          AUTO_INCREMENT PRIMARY KEY                COMMENT '医生编号 (自增)',
    doc_name VARCHAR(50)  NOT NULL                                  COMMENT '医生姓名',
    gender   CHAR(1)      NOT NULL CHECK (gender IN ('M', 'F'))    COMMENT '性别',
    title    VARCHAR(20)                                            COMMENT '职称',
    dept_id  INT          NOT NULL                                  COMMENT '所属科室编号',
    password VARCHAR(255) NOT NULL                                  COMMENT '登录密码(BCrypt哈希)',
    FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. 患者表
-- ============================================
CREATE TABLE patient (
    patient_id INT           AUTO_INCREMENT PRIMARY KEY  COMMENT '患者流水号(代理主键)',
    id_card    CHAR(18)      NOT NULL UNIQUE             COMMENT '身份证号(业务主键)',
    real_name  VARCHAR(50)   NOT NULL                    COMMENT '真实姓名',
    gender     CHAR(1)       CHECK (gender IN ('M', 'F')) COMMENT '性别',
    phone      VARCHAR(20)   NOT NULL                    COMMENT '联系电话',
    password   VARCHAR(255)  NOT NULL                    COMMENT '登录密码(BCrypt哈希)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. 排班表
-- ============================================
CREATE TABLE schedule (
    schedule_id INT          AUTO_INCREMENT PRIMARY KEY                 COMMENT '排班流水号',
    doc_id      INT          NOT NULL                                   COMMENT '出诊医生编号',
    work_date   DATE         NOT NULL                                   COMMENT '出诊日期',
    shift       VARCHAR(10)  NOT NULL CHECK (shift IN ('上午', '下午', '夜诊')) COMMENT '时段',
    total_quota INT          NOT NULL CHECK (total_quota > 0)          COMMENT '总号源数',
    rest_quota  INT          NOT NULL                                   COMMENT '剩余号源数',
    -- 复合唯一: 同一医生同一天同一时段最多一条排班
    UNIQUE (doc_id, work_date, shift),
    -- 防超卖: 剩余号源不能为负且不能超过总号源
    CHECK (rest_quota >= 0 AND rest_quota <= total_quota),
    FOREIGN KEY (doc_id) REFERENCES doctor(doc_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. 挂号记录表
-- ============================================
CREATE TABLE appointment (
    appt_id     INT       AUTO_INCREMENT PRIMARY KEY                              COMMENT '预约单号',
    patient_id  INT       NOT NULL                                                COMMENT '预约患者ID',
    schedule_id INT       NOT NULL                                                COMMENT '对应排班ID',
    status      INT       NOT NULL DEFAULT 1 CHECK (status IN (1, 2, 3))         COMMENT '状态: 1已预约 2已取消 3已就诊',
    create_time DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP                      COMMENT '订单创建时间',
    FOREIGN KEY (patient_id)  REFERENCES patient(patient_id)   ON DELETE RESTRICT,
    FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 性能索引 (设计依据见 数据库逻辑结构.md 第四节)
-- ============================================

-- 按科室查医生: 高频联表查询
CREATE INDEX idx_doctor_dept_id ON doctor(dept_id);

-- 按医生查排班 / 按日期查排班: 患者端高频筛选
CREATE INDEX idx_schedule_doc_id ON schedule(doc_id);
CREATE INDEX idx_schedule_date   ON schedule(work_date);

-- 按患者查预约 / 按排班查患者: 医生端+患者端高频查询
CREATE INDEX idx_appt_patient    ON appointment(patient_id);
CREATE INDEX idx_appt_schedule   ON appointment(schedule_id);

-- 按状态筛选预约: 取消/完成逻辑中频繁使用
CREATE INDEX idx_appt_status     ON appointment(status);

-- 说明: MySQL 8.0 不支持条件唯一索引, 因此 (patient_id, schedule_id) WHERE status=1
-- 的唯一约束无法在数据库层实现。重复挂号由后端 Application Service 层做业务判断:
--    SELECT COUNT(*) FROM appointment WHERE patient_id=? AND schedule_id=? AND status=1;
-- 若 COUNT > 0 则拒绝挂号并返回 "您已预约过该排班，请勿重复挂号"。
