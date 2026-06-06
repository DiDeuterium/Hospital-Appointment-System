-- ============================================
-- 医院预约挂号系统 — 升级版核心 SQL 查询
-- 版本: 2.0
-- 日期: 2026-06-06
-- 说明: 面向数据库设计课程展示，覆盖科室/医生、排班号源、
--       预约支付、医生工作量、调班审核、公告与数据一致性。
--       示例 ID 与日期基于当前 sql/data.sql。
-- ============================================

USE hospital_db;

-- ============================================================
-- 一、基础查询
-- ============================================================

-- Q1: 查询正常启用的科室列表
-- 用途: 患者端选择科室；停用科室不应出现在可挂号入口。
SELECT
    dept_id,
    dept_name,
    location,
    description,
    status
FROM department
WHERE status = 1
ORDER BY dept_id;

-- Q2: 查询某科室下正常启用的医生
-- 示例: 心内科 dept_id=1。
SELECT
    d.doc_id,
    d.doc_name,
    d.gender,
    d.title,
    d.avatar_url,
    d.specialty,
    d.status,
    dept.dept_name
FROM doctor d
JOIN department dept ON dept.dept_id = d.dept_id
WHERE d.dept_id = 1
  AND d.status = 1
  AND dept.status = 1
ORDER BY d.doc_id;

-- Q3: 按身份证号查询患者信息
-- 用途: 患者登录、注册查重、个人中心资料读取。
SELECT
    patient_id,
    id_card,
    real_name,
    gender,
    phone
FROM patient
WHERE id_card = '110101199001011234';

-- Q4: 查询管理员账号状态
-- 用途: 管理员登录前验证账号是否启用。
SELECT
    admin_id,
    username,
    real_name,
    status,
    create_time
FROM admin_user
ORDER BY admin_id;

-- ============================================================
-- 二、排班与号源查询
-- ============================================================

-- Q5: 查询某科室某日期的全部排班
-- 示例: 心内科 dept_id=1 在 2026-06-03 的排班，包含已约满排班。
SELECT
    s.schedule_id,
    s.work_date,
    s.shift,
    s.total_quota,
    s.rest_quota,
    s.fee,
    s.status AS schedule_status,
    d.doc_id,
    d.doc_name,
    d.title,
    dept.dept_name
FROM schedule s
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE dept.dept_id = 1
  AND s.work_date = '2026-06-03'
ORDER BY s.shift, s.schedule_id;

-- Q6: 查询患者可预约排班
-- 用途: 过滤停用科室、停用医生、停诊排班和已约满排班。
SELECT
    schedule_id,
    doc_id,
    doc_name,
    title,
    dept_id,
    dept_name,
    work_date,
    shift,
    total_quota,
    rest_quota,
    fee
FROM v_available_schedule
WHERE dept_id = 1
  AND work_date = '2026-06-03'
ORDER BY work_date, shift, schedule_id;

-- Q7: 查询未来一周各科室可预约号源汇总
-- 用途: 首页展示各科室剩余号源与最早可约日期。
SELECT
    dept.dept_id,
    dept.dept_name,
    COUNT(DISTINCT s.schedule_id) AS available_schedule_count,
    SUM(s.rest_quota) AS total_rest_quota,
    MIN(s.work_date) AS earliest_work_date,
    MAX(s.work_date) AS latest_work_date
FROM schedule s
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE s.work_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 6 DAY)
  AND s.status = 1
  AND s.rest_quota > 0
  AND d.status = 1
  AND dept.status = 1
GROUP BY dept.dept_id, dept.dept_name
ORDER BY total_rest_quota DESC, dept.dept_id;

-- Q8: 查询已约满排班
-- 用途: 验证满号排班不会出现在 v_available_schedule 中。
SELECT
    s.schedule_id,
    s.work_date,
    s.shift,
    s.total_quota,
    s.rest_quota,
    d.doc_name,
    dept.dept_name
FROM schedule s
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE s.rest_quota = 0
ORDER BY s.work_date, s.shift, s.schedule_id;

-- Q9: 查询指定医生的排班日历
-- 示例: 张伟 doc_id=1。
SELECT
    s.schedule_id,
    s.work_date,
    s.shift,
    s.total_quota,
    s.rest_quota,
    s.total_quota - s.rest_quota AS used_quota,
    s.fee,
    s.status
FROM schedule s
WHERE s.doc_id = 1
ORDER BY s.work_date, s.shift;

-- ============================================================
-- 三、预约与支付查询
-- ============================================================

-- Q10: 查询某患者的预约详情
-- 用途: “我的预约”列表，包含科室、医生、排队号与支付状态。
SELECT
    appt_id,
    patient_id,
    patient_name,
    dept_name,
    doc_name,
    title,
    work_date,
    shift,
    queue_number,
    appointment_status,
    cancel_reason,
    amount,
    pay_status,
    pay_method,
    pay_time,
    create_time
FROM v_appointment_detail
WHERE patient_id = 1
ORDER BY work_date DESC, shift, appt_id DESC;

-- Q11: 查询某个排班的患者名册
-- 用途: 医生端按排队号查看当天患者。
SELECT
    a.schedule_id,
    a.queue_number,
    a.appt_id,
    p.patient_id,
    p.real_name,
    p.gender,
    p.phone,
    a.status AS appointment_status,
    pr.pay_status
FROM appointment a
JOIN patient p ON p.patient_id = a.patient_id
LEFT JOIN payment_record pr ON pr.appt_id = a.appt_id
WHERE a.schedule_id = 17
ORDER BY a.queue_number;

-- Q12: 查询待支付预约
-- 用途: 查找创建预约后尚未完成模拟支付的数据。
SELECT
    a.appt_id,
    p.real_name AS patient_name,
    d.doc_name,
    dept.dept_name,
    s.work_date,
    s.shift,
    pr.amount,
    pr.pay_status,
    pr.create_time
FROM payment_record pr
JOIN appointment a ON a.appt_id = pr.appt_id
JOIN patient p ON p.patient_id = a.patient_id
JOIN schedule s ON s.schedule_id = a.schedule_id
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE pr.pay_status = 0
ORDER BY pr.create_time DESC;

-- Q13: 按支付状态统计支付记录
-- pay_status: 0待支付 1已支付 2已关闭 3已退款。
SELECT
    pay_status,
    COUNT(*) AS record_count,
    SUM(amount) AS total_amount,
    MIN(create_time) AS first_create_time,
    MAX(create_time) AS last_create_time
FROM payment_record
GROUP BY pay_status
ORDER BY pay_status;

-- Q14: 统计各科室挂号收入
-- 只统计已支付记录 pay_status=1。
SELECT
    dept.dept_id,
    dept.dept_name,
    COUNT(pr.payment_id) AS paid_count,
    SUM(pr.amount) AS paid_amount
FROM payment_record pr
JOIN appointment a ON a.appt_id = pr.appt_id
JOIN schedule s ON s.schedule_id = a.schedule_id
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE pr.pay_status = 1
GROUP BY dept.dept_id, dept.dept_name
ORDER BY paid_amount DESC, dept.dept_id;

-- Q15: 查询已取消且已退款的预约
-- 用途: 验证“取消预约 + 自动退款”业务结果。
SELECT
    a.appt_id,
    p.real_name AS patient_name,
    dept.dept_name,
    d.doc_name,
    s.work_date,
    s.shift,
    a.cancel_reason,
    pr.amount,
    pr.pay_status
FROM appointment a
JOIN payment_record pr ON pr.appt_id = a.appt_id
JOIN patient p ON p.patient_id = a.patient_id
JOIN schedule s ON s.schedule_id = a.schedule_id
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE a.status = 2
  AND pr.pay_status = 3
ORDER BY a.update_time DESC;

-- ============================================================
-- 四、统计分析查询
-- ============================================================

-- Q16: 医生工作量统计
-- 统计每位医生的预约、取消、完成就诊数量。
SELECT
    d.doc_id,
    d.doc_name,
    d.title,
    dept.dept_name,
    COUNT(a.appt_id) AS total_appointments,
    SUM(CASE WHEN a.status = 1 THEN 1 ELSE 0 END) AS booked_count,
    SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) AS cancelled_count,
    SUM(CASE WHEN a.status = 3 THEN 1 ELSE 0 END) AS finished_count,
    SUM(CASE WHEN a.status = 4 THEN 1 ELSE 0 END) AS expired_count
FROM doctor d
JOIN department dept ON dept.dept_id = d.dept_id
LEFT JOIN schedule s ON s.doc_id = d.doc_id
LEFT JOIN appointment a ON a.schedule_id = s.schedule_id
GROUP BY d.doc_id, d.doc_name, d.title, dept.dept_name
ORDER BY total_appointments DESC, d.doc_id;

-- Q17: 科室号源利用率统计
-- used_quota = total_quota - rest_quota。
SELECT
    dept.dept_id,
    dept.dept_name,
    SUM(s.total_quota) AS total_quota,
    SUM(s.rest_quota) AS rest_quota,
    SUM(s.total_quota - s.rest_quota) AS used_quota,
    ROUND(SUM(s.total_quota - s.rest_quota) / SUM(s.total_quota) * 100, 2) AS usage_rate_percent
FROM department dept
JOIN doctor d ON d.dept_id = dept.dept_id
JOIN schedule s ON s.doc_id = d.doc_id
GROUP BY dept.dept_id, dept.dept_name
ORDER BY usage_rate_percent DESC, dept.dept_id;

-- Q18: 每日预约趋势
-- 按排班日期统计预约数量和有效预约数量。
SELECT
    s.work_date,
    COUNT(a.appt_id) AS total_appointments,
    SUM(CASE WHEN a.status IN (1, 3, 4) THEN 1 ELSE 0 END) AS active_or_done_count,
    SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) AS cancelled_count
FROM schedule s
LEFT JOIN appointment a ON a.schedule_id = s.schedule_id
GROUP BY s.work_date
ORDER BY s.work_date;

-- Q19: 患者预约频次排名
-- 用途: 识别预约次数较多的患者。
SELECT
    p.patient_id,
    p.real_name,
    p.phone,
    COUNT(a.appt_id) AS appointment_count,
    SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) AS cancelled_count
FROM patient p
LEFT JOIN appointment a ON a.patient_id = p.patient_id
GROUP BY p.patient_id, p.real_name, p.phone
ORDER BY appointment_count DESC, p.patient_id
LIMIT 10;

-- ============================================================
-- 五、排班变更与公告查询
-- ============================================================

-- Q20: 查询排班变更申请列表
-- change_type: 1停诊 2修改排班；status: 1待审核 2已通过 3已驳回 4已撤回。
SELECT
    r.request_id,
    r.change_type,
    r.status,
    r.reason,
    r.target_work_date,
    r.target_shift,
    r.target_total_quota,
    r.apply_time,
    r.audit_time,
    r.audit_remark,
    s.schedule_id,
    s.work_date AS current_work_date,
    s.shift AS current_shift,
    s.total_quota AS current_total_quota,
    d.doc_name,
    dept.dept_name,
    au.real_name AS audit_admin_name
FROM schedule_change_request r
JOIN schedule s ON s.schedule_id = r.schedule_id
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
LEFT JOIN admin_user au ON au.admin_id = r.audit_admin_id
ORDER BY r.status, r.apply_time DESC;

-- Q21: 查询待审核变更申请
-- 用途: 管理员审核列表。
SELECT
    r.request_id,
    r.change_type,
    r.reason,
    r.apply_time,
    s.schedule_id,
    s.work_date,
    s.shift,
    s.total_quota,
    s.rest_quota,
    d.doc_id,
    d.doc_name,
    dept.dept_name
FROM schedule_change_request r
JOIN schedule s ON s.schedule_id = r.schedule_id
JOIN doctor d ON d.doc_id = s.doc_id
JOIN department dept ON dept.dept_id = d.dept_id
WHERE r.status = 1
ORDER BY r.apply_time;

-- Q22: 查询已发布公告
-- 用途: 患者端公告列表，置顶优先。
SELECT
    n.notice_id,
    n.title,
    n.content,
    n.is_top,
    n.publish_time,
    au.real_name AS publisher
FROM sys_notice n
LEFT JOIN admin_user au ON au.admin_id = n.admin_id
WHERE n.status = 1
ORDER BY n.is_top DESC, n.publish_time DESC;

-- ============================================================
-- 六、数据一致性检查查询
-- ============================================================

-- Q23: 检查预约是否都有支付记录
-- 正常结果应为 0 行。
SELECT
    a.appt_id,
    a.patient_id,
    a.schedule_id
FROM appointment a
LEFT JOIN payment_record pr ON pr.appt_id = a.appt_id
WHERE pr.payment_id IS NULL;

-- Q24: 检查一条预约是否出现多条支付记录
-- 正常结果应为 0 行。
SELECT
    appt_id,
    COUNT(*) AS payment_count
FROM payment_record
GROUP BY appt_id
HAVING COUNT(*) > 1;

-- Q25: 检查同一患者是否重复有效预约同一排班
-- 正常结果应为 0 行。
SELECT
    patient_id,
    schedule_id,
    COUNT(*) AS active_count
FROM appointment
WHERE status = 1
GROUP BY patient_id, schedule_id
HAVING COUNT(*) > 1;

-- Q26: 检查排班号源与预约数量是否一致
-- used_count 统计 status=1已预约、3已就诊、4已过期；
-- 正常结果应为 0 行。
SELECT
    s.schedule_id,
    s.total_quota,
    s.rest_quota,
    COUNT(CASE WHEN a.status IN (1, 3, 4) THEN 1 END) AS used_count,
    s.total_quota - s.rest_quota AS expected_used
FROM schedule s
LEFT JOIN appointment a ON a.schedule_id = s.schedule_id
GROUP BY s.schedule_id, s.total_quota, s.rest_quota
HAVING used_count <> expected_used;

-- Q27: 检查排队号是否连续
-- 如果一个排班内最大排队号不等于预约数，说明中间可能存在缺号。
SELECT
    schedule_id,
    COUNT(*) AS appointment_count,
    MAX(queue_number) AS max_queue_number
FROM appointment
GROUP BY schedule_id
HAVING COUNT(*) <> MAX(queue_number);
