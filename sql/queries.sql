-- ============================================
-- 医院预约挂号系统 — 核心 SQL 查询
-- 版本: 1.1
-- 日期: 2026-05-07
-- 设计人: 范骐鸣
-- 说明: 3个基础查询 + 2个多表连接 + 1个聚合 + 1个业务统计
--       基于开发指导文档 附录A
--       所有 ID 统一为 INT, 示例中使用的具体数值请对照 data.sql
-- ============================================

USE hospital_db;

-- ============================================================
-- 一、基础查询 (3个)
-- ============================================================

-- -----------------------------------------------------------
-- Q1: 查询所有科室列表
-- 用途: 患者端首页科室导航 / 管理员科室管理页
-- -----------------------------------------------------------
SELECT dept_id, dept_name, location, description
FROM department
ORDER BY dept_id;

-- -----------------------------------------------------------
-- Q2: 查询某科室下的所有医生
-- 用途: 患者按科室选医生 (级联查询第二步)
-- 示例: 查心内科(1)的医生
-- -----------------------------------------------------------
SELECT doc_id, doc_name, gender, title
FROM doctor
WHERE dept_id = 1
ORDER BY doc_id;

-- -----------------------------------------------------------
-- Q3: 查询某患者的基本信息
-- 用途: 患者个人中心 / 医生查看患者详情
-- 示例: 查 patient_id=1 的患者
-- -----------------------------------------------------------
SELECT patient_id, id_card, real_name, gender, phone
FROM patient
WHERE patient_id = 1;

-- 按身份证号查 (注册时查重 / 登录时查询)
SELECT patient_id, id_card, real_name, gender, phone, password
FROM patient
WHERE id_card = '110101199001011234';


-- ============================================================
-- 二、多表连接查询 (2个)
-- ============================================================

-- -----------------------------------------------------------
-- Q4: 查询某日期某科室的所有排班及对应医生信息
-- 用途: 患者按科室+日期浏览号源 (核心挂号前查询)
-- 示例: 查心内科(1)在 2026-05-07 的排班
-- 联表: schedule → doctor → department
-- -----------------------------------------------------------
SELECT
    s.schedule_id,
    s.work_date,
    s.shift,
    s.total_quota,
    s.rest_quota,
    d.doc_id,
    d.doc_name,
    d.title,
    dp.dept_name
FROM schedule s
JOIN doctor d     ON s.doc_id  = d.doc_id
JOIN department dp ON d.dept_id = dp.dept_id
WHERE dp.dept_id  = 1
  AND s.work_date = '2026-05-07'
ORDER BY s.shift, d.doc_id;

-- -----------------------------------------------------------
-- Q5: 查询某患者的所有预约记录(含科室/医生/日期/时段/状态)
-- 用途: "我的预约"列表页
-- 示例: 查 patient_id=1 的预约列表
-- 联表: appointment → schedule → doctor → department
-- -----------------------------------------------------------
SELECT
    a.appt_id,
    a.status,
    a.create_time,
    s.schedule_id,
    s.work_date,
    s.shift,
    d.doc_id,
    d.doc_name,
    d.title,
    dp.dept_id,
    dp.dept_name,
    dp.location
FROM appointment a
JOIN schedule   s  ON a.schedule_id = s.schedule_id
JOIN doctor     d  ON s.doc_id      = d.doc_id
JOIN department dp ON d.dept_id     = dp.dept_id
WHERE a.patient_id = 1
ORDER BY a.create_time DESC;

-- 变体: 按状态筛选 (如仅看"已预约")
SELECT
    a.appt_id,
    a.status,
    a.create_time,
    s.work_date,
    s.shift,
    d.doc_name,
    dp.dept_name
FROM appointment a
JOIN schedule   s  ON a.schedule_id = s.schedule_id
JOIN doctor     d  ON s.doc_id      = d.doc_id
JOIN department dp ON d.dept_id     = dp.dept_id
WHERE a.patient_id = 1
  AND a.status = 1
ORDER BY s.work_date, s.shift;


-- ============================================================
-- 三、聚合查询 (1个)
-- ============================================================

-- -----------------------------------------------------------
-- Q6: 统计每位医生的预约患者数量
-- 用途: 管理员统计报表 / 医生工作量分析
-- 统计每位医生当前有效的预约数(status=1)
-- -----------------------------------------------------------
SELECT
    d.doc_id,
    d.doc_name,
    d.title,
    dp.dept_name,
    COUNT(DISTINCT a.patient_id) AS patient_count,       -- 不同患者数
    COUNT(a.appt_id)             AS total_appointments   -- 总预约单数
FROM doctor d
JOIN department dp ON d.dept_id = dp.dept_id
LEFT JOIN schedule s  ON d.doc_id  = s.doc_id
LEFT JOIN appointment a ON s.schedule_id = a.schedule_id AND a.status = 1
GROUP BY d.doc_id, d.doc_name, d.title, dp.dept_name
ORDER BY total_appointments DESC;

-- 变体: 统计所有状态 (含已取消/已完成)
SELECT
    d.doc_id,
    d.doc_name,
    dp.dept_name,
    SUM(CASE WHEN a.status = 1 THEN 1 ELSE 0 END) AS booked,
    SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) AS cancelled,
    SUM(CASE WHEN a.status = 3 THEN 1 ELSE 0 END) AS completed,
    COUNT(a.appt_id) AS total
FROM doctor d
JOIN department dp ON d.dept_id = dp.dept_id
LEFT JOIN schedule s    ON d.doc_id  = s.doc_id
LEFT JOIN appointment a ON s.schedule_id = a.schedule_id
GROUP BY d.doc_id, d.doc_name, dp.dept_name
ORDER BY total DESC;


-- ============================================================
-- 四、业务查询 (1个)
-- ============================================================

-- -----------------------------------------------------------
-- Q7: 查询未来7天有剩余号源的科室及其可约号源数
-- 用途: 患者端首页 "可挂号科室" 快捷入口
-- 7天范围: 今天 2026-05-07 到 2026-05-13
-- -----------------------------------------------------------
SELECT
    dp.dept_id,
    dp.dept_name,
    dp.location,
    COUNT(DISTINCT s.schedule_id) AS available_schedule_count, -- 可约排班数量
    SUM(s.rest_quota)             AS total_available_quota,   -- 总可约号源数
    MIN(s.work_date)              AS earliest_date,           -- 最早可约日期
    MAX(s.work_date)              AS latest_date              -- 最晚可约日期
FROM department dp
JOIN doctor   d  ON dp.dept_id = d.dept_id
JOIN schedule s  ON d.doc_id   = s.doc_id
WHERE s.work_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 6 DAY)
  AND s.rest_quota > 0
GROUP BY dp.dept_id, dp.dept_name, dp.location
ORDER BY total_available_quota DESC;

-- 变体: 展开显示每个科室下每个排班的具体号源
SELECT
    dp.dept_name,
    d.doc_name,
    s.work_date,
    s.shift,
    s.rest_quota,
    s.total_quota
FROM schedule s
JOIN doctor     d  ON s.doc_id  = d.doc_id
JOIN department dp ON d.dept_id = dp.dept_id
WHERE s.work_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 6 DAY)
  AND s.rest_quota > 0
ORDER BY dp.dept_id, s.work_date, s.shift;
