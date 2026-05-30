-- ============================================
-- 医院预约挂号系统 — 升级版测试数据
-- 密码均为 BCrypt 哈希
-- ============================================

-- 1. 科室数据
INSERT INTO department (dept_name, location, description, status) VALUES
('心内科',   '门诊楼2层', '心血管疾病诊疗，含高血压、冠心病、心律失常等', 1),
('神经内科', '门诊楼2层', '神经系统疾病诊疗，含头痛、癫痫、帕金森等', 1),
('骨科',     '门诊楼3层', '骨骼关节疾病诊疗，含骨折、关节炎、颈椎病等', 1),
('儿科',     '门诊楼1层', '儿童常见病及多发病诊疗', 1),
('皮肤科',   '门诊楼5层', '皮肤病诊疗，含湿疹、痤疮、银屑病等', 1),
('眼科',     '门诊楼4层', '眼部疾病诊疗，含近视、白内障、青光眼等', 1),
('耳鼻喉科', '门诊楼4层', '耳鼻喉疾病诊疗', 1),
('消化内科', '门诊楼2层', '消化系统疾病诊疗，含胃炎、肠炎、肝病等', 1),
('呼吸内科', '门诊楼2层', '呼吸系统疾病诊疗，含哮喘、肺炎、慢阻肺等', 1),
('妇科',     '门诊楼3层', '妇科常见病及多发病诊疗', 1);

-- 2. 医生数据
-- avatar_url 仅保存图片访问路径，图片文件可放在后端静态资源或上传目录中。
INSERT INTO doctor (doc_name, gender, title, dept_id, password, avatar_url, specialty, status) VALUES
('张伟',   'M', '主任医师',   1, '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi', '/avatars/doctor-1.png',  '冠心病、高血压、心律失常综合诊疗', 1),
('李娜',   'F', '副主任医师', 1, '$2b$10$Y6eNyHtHCKy0stV..jLr9.Eq4C9mZsvLnEMu29WBPNCuOubYx4l9O', '/avatars/doctor-2.png',  '心力衰竭、心血管慢病管理', 1),
('王强',   'M', '主任医师',   2, '$2b$10$YKBK.65uhGul38tG5RLU/u61nSO9PjmBcc1m12Tfe5bxkBPOt4Du6', '/avatars/doctor-3.png',  '脑血管病、头痛、癫痫诊疗', 1),
('刘芳',   'F', '主治医师',   2, '$2b$10$.Hoon5Ehe4uAPZlDqMp.AuZ7DZQi4KzGkuL0E0ps1T4Y.z0ohkNzm', '/avatars/doctor-4.png',  '帕金森、睡眠障碍、神经康复', 1),
('陈刚',   'M', '主任医师',   3, '$2b$10$PFMDur4veCaeqMm6L9GWbe/7e54cuJO8p55bJGnFd3VWFLsc0vOpS', '/avatars/doctor-5.png',  '骨折、关节置换、脊柱疾病', 1),
('杨丽',   'F', '副主任医师', 3, '$2b$10$veDC4Kie5RrwMv8MrHcZTugQkU37/KvAX/1.QbAHVUHY1ex4L.YJC', '/avatars/doctor-6.png',  '颈肩腰腿痛、运动损伤', 1),
('赵敏',   'F', '主任医师',   4, '$2b$10$lsoP1laicZoNRdlvy6YZgulUXTbQAfFtaxXKNRXGFBM.Zvd9Qn6nm', '/avatars/doctor-7.png',  '儿童呼吸道感染、儿童保健', 1),
('孙磊',   'M', '主治医师',   5, '$2b$10$QZugyY6te.J8pVf0/e7Rz.5PA1rV2qgQIhQ3savvEPtt9volQlw26', '/avatars/doctor-8.png',  '湿疹、痤疮、皮肤过敏', 1),
('周杰',   'M', '主任医师',   6, '$2b$10$Z0.XBRZYz2qSfmu.Tc/BJeVEJOcs.TJA7Tm/j.WkYPa7huYEjMJ/q', '/avatars/doctor-9.png',  '白内障、青光眼、眼底病', 1),
('吴秀英', 'F', '副主任医师', 7, '$2b$10$4OWUyDWQR3JA1KK6jFGoXuIX9SWZnzywF3RBUI6h.cTYF9F80CP.G', '/avatars/doctor-10.png', '鼻炎、咽喉炎、听力疾病', 1),
('郑涛',   'M', '主治医师',   8, '$2b$10$G41IP6nA2NS8OG0MMzbpa.7ncsnaZn/9bjuL5Y8phBgBf8Oc23SL6', '/avatars/doctor-11.png', '胃炎、肠炎、消化道溃疡', 1),
('钱红',   'F', '主任医师',   9, '$2b$10$OGCCox9Izs6eSqTBjq64N.xd07w7HFn8io4yp6z.K5zhuF9Hc/jV6', '/avatars/doctor-12.png', '哮喘、肺炎、慢阻肺', 1),
('刘梅',   'F', '主任医师',  10, '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi', '/avatars/doctor-13.png', '妇科炎症、月经异常、围产咨询', 1);

-- 3. 患者数据
INSERT INTO patient (id_card, real_name, gender, phone, password) VALUES
('110101199001011234', '赵小明', 'M', '13800001111', '$2b$10$/alhAc1F2S6TE.yPV3Y37.qcaUwhcLP5CVwF./spiiChb0dB7KdeS'),
('110101199205202345', '钱小红', 'F', '13800002222', '$2b$10$0Eiyfl3YSmncbHSmLgXZtuEJrLbX6k16Rb3JVHCEmoNd5lQy5pJ5O'),
('310102198803153456', '孙大伟', 'M', '13900003333', '$2b$10$90y2eCkSK9uHw4XRcsJ0..ZVivw4rwsbadD3NOPysdSvaeT2BpXye'),
('310102199511204567', '李美玲', 'F', '13900004444', '$2b$10$1hg5.LJZ.vlC/0Dc0QiX5uFZi9o5w7fxphY2Htid4kv09xNgJfLma'),
('440103197807256789', '周文博', 'M', '13600005555', '$2b$10$mZ06NZiZ3A.vrG5rKF/g3uVaiuMl/ohqSC.X7XAmFKx2tjjIZNmbS'),
('440103199209108901', '吴丽华', 'F', '13600006666', '$2b$10$m7KUajd4.WF.JWvDAvAs0uRz1S0liB6WKx.ZNUOX94kKIYncBaQ46'),
('330106198512251112', '郑国强', 'M', '13700007777', '$2b$10$ZHVCj1SLin3Rt6/LWzouc.QZDCTaS.NmRmZJCq5ckZDS9B1ip3kMu'),
('330106199608062223', '王秀兰', 'F', '13700008888', '$2b$10$jLaaoN41LzNeOT5DVTmZ2OJRoJf/GoArIzrFBuDZubGR0vupwFqdm'),
('510104199103173334', '冯建军', 'M', '13500009999', '$2b$10$TB97rGxCpggj.6yRKmdol.pjdfaHhjP8xSM7Hgz/hDSaVatjNgWdy'),
('510104199812084445', '陈玉芬', 'F', '13500001110', '$2b$10$MjvA7KmskWzh9c/QzLnJ6u1cLKOY3tm/Pz6KiL2EZgq1qz4evQG/6'),
('320105198706185556', '蒋明辉', 'M', '13300001111', '$2b$10$.Xs3E.jI7jTD4yQtJxXsJeUe28xKx/sdZLCuDopfhyUbfZhHs2KE6'),
('320105199404096667', '沈秀英', 'F', '13300002222', '$2b$10$h2AC4.HsLyWKEKFR5rWTTOU.vxa8LdioeWwVPFk0iukZwYVkSYS3i'),
('420102199005051234', '林强',   'M', '13100001111', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199106062345', '宋佳',   'F', '13100002222', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199207073456', '唐明',   'M', '13100003333', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199308084567', '马莉',   'F', '13100004444', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199409095678', '董建国', 'M', '13100005555', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199510106789', '黄晓',   'F', '13100006666', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199611117890', '高强',   'M', '13100007777', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('420102199712128901', '彭静',   'F', '13100008888', '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi');

-- 4. 管理员数据
-- 明文密码均为 admin123。
INSERT INTO admin_user (username, real_name, password, status) VALUES
('admin1', '肖国扬', '$2b$10$tBWuKr.5vKva7hIEI.raF.NA1zgsixZg1oSIFmS6LedgqIfcLDdPi', 1),
('admin2', '狄东琛', '$2b$10$tBWuKr.5vKva7hIEI.raF.NA1zgsixZg1oSIFmS6LedgqIfcLDdPi', 1);

-- 5. 排班数据
-- 挂号费按职称设置：主任医师 50.00，副主任医师 35.00，主治医师 25.00。
INSERT INTO schedule (doc_id, work_date, shift, total_quota, rest_quota, fee, status) VALUES
(1,  '2026-06-02', '上午', 30, 28, 50.00, 1),
(1,  '2026-06-02', '下午', 20, 19, 50.00, 1),
(3,  '2026-06-02', '上午', 25, 24, 50.00, 1),
(1,  '2026-06-03', '上午', 30, 28, 50.00, 1),
(2,  '2026-06-03', '上午',  2,  0, 35.00, 1),
(3,  '2026-06-03', '下午', 20, 19, 50.00, 1),
(5,  '2026-06-03', '上午', 30, 29, 50.00, 1),
(7,  '2026-06-03', '上午', 15, 15, 50.00, 1),
(1,  '2026-06-04', '上午', 30, 28, 50.00, 1),
(2,  '2026-06-04', '下午', 20, 20, 35.00, 1),
(5,  '2026-06-05', '上午', 25, 25, 50.00, 1),
(6,  '2026-06-05', '下午', 20, 19, 35.00, 1),
(9,  '2026-06-06', '上午', 30, 27, 50.00, 1),
(10, '2026-06-06', '上午', 20, 20, 35.00, 1),
(12, '2026-06-07', '下午', 25, 25, 50.00, 1),
(11, '2026-06-07', '上午', 20, 20, 25.00, 1),
(4,  '2026-06-08', '上午', 50, 45, 25.00, 1),
(8,  '2026-06-08', '下午', 12,  2, 25.00, 1),
(2,  '2026-06-08', '上午', 10,  0, 35.00, 1),
(6,  '2026-06-08', '下午',  8,  0, 35.00, 1),
(3,  '2026-06-08', '夜诊', 15,  1, 50.00, 1),
(5,  '2026-06-02', '下午', 30, 28, 50.00, 1),
(7,  '2026-06-02', '上午', 25, 25, 50.00, 1),
(8,  '2026-06-02', '上午', 20, 20, 25.00, 1),
(9,  '2026-06-02', '下午', 30, 27, 50.00, 1),
(10, '2026-06-02', '上午', 20, 20, 35.00, 1),
(11, '2026-06-02', '上午', 25, 25, 25.00, 1),
(12, '2026-06-02', '下午', 30, 30, 50.00, 1),
(13, '2026-06-02', '上午', 30, 30, 50.00, 1),
(8,  '2026-06-03', '下午', 20, 20, 25.00, 1),
(9,  '2026-06-03', '下午', 30, 30, 50.00, 1),
(10, '2026-06-03', '下午', 20, 20, 35.00, 1),
(11, '2026-06-03', '上午', 25, 24, 25.00, 1),
(12, '2026-06-03', '上午', 30, 30, 50.00, 1),
(13, '2026-06-03', '上午', 30, 30, 50.00, 1),
(3,  '2026-06-04', '上午', 25, 25, 50.00, 1),
(5,  '2026-06-04', '下午', 30, 30, 50.00, 1),
(7,  '2026-06-04', '上午', 25, 25, 50.00, 1),
(8,  '2026-06-04', '上午', 20, 18, 25.00, 1),
(9,  '2026-06-04', '下午', 30, 30, 50.00, 1),
(10, '2026-06-04', '上午', 20, 20, 35.00, 1),
(11, '2026-06-04', '下午', 25, 25, 25.00, 1),
(12, '2026-06-04', '上午', 30, 30, 50.00, 1),
(13, '2026-06-04', '上午', 30, 30, 50.00, 1),
(1,  '2026-06-05', '上午', 30, 30, 50.00, 1),
(3,  '2026-06-05', '上午', 25, 23, 50.00, 1),
(7,  '2026-06-05', '下午', 25, 25, 50.00, 1),
(8,  '2026-06-05', '上午', 20, 20, 25.00, 1),
(9,  '2026-06-05', '下午', 30, 30, 50.00, 1),
(10, '2026-06-05', '上午', 20, 20, 35.00, 1),
(11, '2026-06-05', '下午', 25, 25, 25.00, 1),
(12, '2026-06-05', '上午', 30, 30, 50.00, 1),
(13, '2026-06-05', '上午', 30, 30, 50.00, 1),
(1,  '2026-06-06', '上午', 30, 30, 50.00, 1),
(3,  '2026-06-06', '下午', 25, 24, 50.00, 1),
(5,  '2026-06-06', '上午', 30, 30, 50.00, 1),
(7,  '2026-06-06', '上午', 25, 25, 50.00, 1),
(8,  '2026-06-06', '上午', 20, 20, 25.00, 1),
(11, '2026-06-06', '下午', 25, 25, 25.00, 1),
(12, '2026-06-06', '上午', 30, 30, 50.00, 1),
(13, '2026-06-06', '上午', 30, 30, 50.00, 1),
(1,  '2026-06-07', '下午', 30, 30, 50.00, 1),
(4,  '2026-06-07', '上午', 25, 25, 25.00, 1),
(5,  '2026-06-07', '上午', 30, 30, 50.00, 1),
(7,  '2026-06-07', '上午', 25, 25, 50.00, 1),
(8,  '2026-06-07', '下午', 20, 20, 25.00, 1),
(9,  '2026-06-07', '上午', 30, 29, 50.00, 1),
(10, '2026-06-07', '上午', 20, 20, 35.00, 1),
(13, '2026-06-07', '下午', 30, 30, 50.00, 1),
(7,  '2026-06-08', '下午', 25, 25, 50.00, 1),
(9,  '2026-06-08', '下午', 30, 30, 50.00, 1),
(10, '2026-06-08', '上午', 20, 18, 35.00, 1),
(11, '2026-06-08', '下午', 25, 25, 25.00, 1),
(12, '2026-06-08', '上午', 30, 30, 50.00, 1),
(13, '2026-06-08', '上午', 30, 30, 50.00, 1);

UPDATE schedule
SET work_date = CASE work_date
    WHEN '2026-05-18' THEN '2026-06-02'
    WHEN '2026-05-19' THEN '2026-06-03'
    WHEN '2026-05-20' THEN '2026-06-04'
    WHEN '2026-05-21' THEN '2026-06-05'
    WHEN '2026-05-22' THEN '2026-06-06'
    WHEN '2026-05-23' THEN '2026-06-07'
    WHEN '2026-05-24' THEN '2026-06-08'
    ELSE work_date
END;

-- 6. 预约数据
-- queue_number 按同一排班内的创建时间自动生成；取消预约补充取消原因。
INSERT INTO appointment (patient_id, schedule_id, queue_number, status, cancel_reason, create_time, update_time)
SELECT
    raw.patient_id,
    raw.schedule_id,
    ROW_NUMBER() OVER (PARTITION BY raw.schedule_id ORDER BY raw.create_time, raw.patient_id) AS queue_number,
    raw.status,
    CASE
        WHEN raw.status = 2 AND MOD(raw.patient_id + raw.schedule_id, 3) = 0 THEN '临时有事，取消本次预约'
        WHEN raw.status = 2 AND MOD(raw.patient_id + raw.schedule_id, 3) = 1 THEN '症状缓解，暂不就诊'
        WHEN raw.status = 2 THEN '改约其他时间'
        ELSE NULL
    END AS cancel_reason,
    raw.create_time,
    raw.create_time AS update_time
FROM (
    SELECT 1 patient_id, 1 schedule_id, 3 status, TIMESTAMP '2026-05-18 08:15:00' create_time UNION ALL
    SELECT 2, 1, 3, TIMESTAMP '2026-05-18 09:20:00' UNION ALL
    SELECT 3, 1, 2, TIMESTAMP '2026-05-18 10:00:00' UNION ALL
    SELECT 4, 2, 3, TIMESTAMP '2026-05-18 14:30:00' UNION ALL
    SELECT 5, 3, 3, TIMESTAMP '2026-05-18 08:00:00' UNION ALL
    SELECT 1, 4, 1, TIMESTAMP '2026-05-18 09:00:00' UNION ALL
    SELECT 2, 4, 1, TIMESTAMP '2026-05-18 09:30:00' UNION ALL
    SELECT 3, 5, 1, TIMESTAMP '2026-05-18 10:00:00' UNION ALL
    SELECT 4, 5, 1, TIMESTAMP '2026-05-18 11:00:00' UNION ALL
    SELECT 6, 6, 1, TIMESTAMP '2026-05-18 14:00:00' UNION ALL
    SELECT 7, 7, 1, TIMESTAMP '2026-05-18 15:30:00' UNION ALL
    SELECT 1, 9, 1, TIMESTAMP '2026-05-19 10:00:00' UNION ALL
    SELECT 8, 9, 1, TIMESTAMP '2026-05-19 11:00:00' UNION ALL
    SELECT 9, 12, 1, TIMESTAMP '2026-05-20 08:00:00' UNION ALL
    SELECT 10, 12, 2, TIMESTAMP '2026-05-20 09:00:00' UNION ALL
    SELECT 11, 13, 1, TIMESTAMP '2026-05-21 09:00:00' UNION ALL
    SELECT 1, 13, 1, TIMESTAMP '2026-05-21 09:30:00' UNION ALL
    SELECT 5, 13, 1, TIMESTAMP '2026-05-21 10:00:00' UNION ALL
    SELECT 1, 17, 1, TIMESTAMP '2026-05-22 08:00:00' UNION ALL
    SELECT 2, 17, 1, TIMESTAMP '2026-05-22 08:05:00' UNION ALL
    SELECT 3, 17, 1, TIMESTAMP '2026-05-22 08:10:00' UNION ALL
    SELECT 4, 17, 1, TIMESTAMP '2026-05-22 08:15:00' UNION ALL
    SELECT 5, 17, 1, TIMESTAMP '2026-05-22 08:20:00' UNION ALL
    SELECT 6, 18, 1, TIMESTAMP '2026-05-22 09:00:00' UNION ALL
    SELECT 7, 18, 1, TIMESTAMP '2026-05-22 09:05:00' UNION ALL
    SELECT 8, 18, 1, TIMESTAMP '2026-05-22 09:10:00' UNION ALL
    SELECT 9, 18, 1, TIMESTAMP '2026-05-22 09:15:00' UNION ALL
    SELECT 10, 18, 1, TIMESTAMP '2026-05-22 09:20:00' UNION ALL
    SELECT 11, 18, 1, TIMESTAMP '2026-05-22 09:25:00' UNION ALL
    SELECT 12, 18, 1, TIMESTAMP '2026-05-22 09:30:00' UNION ALL
    SELECT 13, 18, 1, TIMESTAMP '2026-05-22 09:35:00' UNION ALL
    SELECT 14, 18, 1, TIMESTAMP '2026-05-22 09:40:00' UNION ALL
    SELECT 15, 18, 1, TIMESTAMP '2026-05-22 09:45:00' UNION ALL
    SELECT 1, 19, 1, TIMESTAMP '2026-05-23 10:00:00' UNION ALL
    SELECT 3, 19, 1, TIMESTAMP '2026-05-23 10:05:00' UNION ALL
    SELECT 5, 19, 1, TIMESTAMP '2026-05-23 10:10:00' UNION ALL
    SELECT 7, 19, 1, TIMESTAMP '2026-05-23 10:15:00' UNION ALL
    SELECT 9, 19, 1, TIMESTAMP '2026-05-23 10:20:00' UNION ALL
    SELECT 11, 19, 1, TIMESTAMP '2026-05-23 10:25:00' UNION ALL
    SELECT 13, 19, 1, TIMESTAMP '2026-05-23 10:30:00' UNION ALL
    SELECT 15, 19, 1, TIMESTAMP '2026-05-23 10:35:00' UNION ALL
    SELECT 17, 19, 1, TIMESTAMP '2026-05-23 10:40:00' UNION ALL
    SELECT 19, 19, 1, TIMESTAMP '2026-05-23 10:45:00' UNION ALL
    SELECT 2, 20, 1, TIMESTAMP '2026-05-23 11:00:00' UNION ALL
    SELECT 4, 20, 1, TIMESTAMP '2026-05-23 11:05:00' UNION ALL
    SELECT 6, 20, 1, TIMESTAMP '2026-05-23 11:10:00' UNION ALL
    SELECT 8, 20, 1, TIMESTAMP '2026-05-23 11:15:00' UNION ALL
    SELECT 10, 20, 1, TIMESTAMP '2026-05-23 11:20:00' UNION ALL
    SELECT 12, 20, 1, TIMESTAMP '2026-05-23 11:25:00' UNION ALL
    SELECT 14, 20, 1, TIMESTAMP '2026-05-23 11:30:00' UNION ALL
    SELECT 16, 20, 1, TIMESTAMP '2026-05-23 11:35:00' UNION ALL
    SELECT 18, 20, 2, TIMESTAMP '2026-05-23 11:40:00' UNION ALL
    SELECT 20, 20, 2, TIMESTAMP '2026-05-23 11:45:00' UNION ALL
    SELECT 1, 20, 2, TIMESTAMP '2026-05-23 11:50:00' UNION ALL
    SELECT 2, 21, 1, TIMESTAMP '2026-05-23 14:00:00' UNION ALL
    SELECT 3, 21, 1, TIMESTAMP '2026-05-23 14:05:00' UNION ALL
    SELECT 4, 21, 1, TIMESTAMP '2026-05-23 14:10:00' UNION ALL
    SELECT 5, 21, 1, TIMESTAMP '2026-05-23 14:15:00' UNION ALL
    SELECT 7, 21, 1, TIMESTAMP '2026-05-23 14:20:00' UNION ALL
    SELECT 8, 21, 1, TIMESTAMP '2026-05-23 14:25:00' UNION ALL
    SELECT 9, 21, 1, TIMESTAMP '2026-05-23 14:30:00' UNION ALL
    SELECT 10, 21, 1, TIMESTAMP '2026-05-23 14:35:00' UNION ALL
    SELECT 12, 21, 1, TIMESTAMP '2026-05-23 14:40:00' UNION ALL
    SELECT 13, 21, 1, TIMESTAMP '2026-05-23 14:45:00' UNION ALL
    SELECT 15, 21, 1, TIMESTAMP '2026-05-23 14:50:00' UNION ALL
    SELECT 16, 21, 1, TIMESTAMP '2026-05-23 14:55:00' UNION ALL
    SELECT 18, 21, 1, TIMESTAMP '2026-05-23 15:00:00' UNION ALL
    SELECT 19, 21, 1, TIMESTAMP '2026-05-23 15:05:00' UNION ALL
    SELECT 6, 21, 2, TIMESTAMP '2026-05-23 15:10:00' UNION ALL
    SELECT 11, 21, 2, TIMESTAMP '2026-05-23 15:15:00' UNION ALL
    SELECT 5, 22, 3, TIMESTAMP '2026-05-17 09:00:00' UNION ALL
    SELECT 6, 22, 3, TIMESTAMP '2026-05-17 09:30:00' UNION ALL
    SELECT 8, 25, 3, TIMESTAMP '2026-05-17 10:00:00' UNION ALL
    SELECT 9, 25, 3, TIMESTAMP '2026-05-17 11:00:00' UNION ALL
    SELECT 10, 25, 3, TIMESTAMP '2026-05-17 14:00:00' UNION ALL
    SELECT 12, 33, 1, TIMESTAMP '2026-05-18 08:00:00' UNION ALL
    SELECT 15, 39, 1, TIMESTAMP '2026-05-19 09:00:00' UNION ALL
    SELECT 16, 39, 1, TIMESTAMP '2026-05-19 09:30:00' UNION ALL
    SELECT 18, 46, 1, TIMESTAMP '2026-05-20 08:30:00' UNION ALL
    SELECT 19, 46, 1, TIMESTAMP '2026-05-20 10:30:00' UNION ALL
    SELECT 2, 55, 1, TIMESTAMP '2026-05-21 11:00:00' UNION ALL
    SELECT 4, 67, 1, TIMESTAMP '2026-05-22 09:15:00' UNION ALL
    SELECT 7, 72, 1, TIMESTAMP '2026-05-23 08:15:00' UNION ALL
    SELECT 9, 72, 1, TIMESTAMP '2026-05-23 09:15:00'
) raw
ORDER BY raw.schedule_id, raw.create_time;

UPDATE appointment a
JOIN schedule s ON s.schedule_id = a.schedule_id
SET
    a.create_time = TIMESTAMP(DATE_SUB(s.work_date, INTERVAL 1 DAY)) + INTERVAL (8 * 60 + a.queue_number * 5) MINUTE,
    a.update_time = TIMESTAMP(DATE_SUB(s.work_date, INTERVAL 1 DAY)) + INTERVAL (8 * 60 + a.queue_number * 5) MINUTE;

-- 7. 挂号费模拟支付记录
-- 每条预约均已支付；金额来自对应排班，支付记录创建时间与预约创建时间一致。
INSERT INTO payment_record (appt_id, amount, pay_status, pay_method, pay_time, create_time)
SELECT
    a.appt_id,
    s.fee,
    1,
    CASE WHEN MOD(a.appt_id, 2) = 0 THEN '支付宝' ELSE '微信' END,
    a.create_time,
    a.create_time
FROM appointment a
JOIN schedule s ON s.schedule_id = a.schedule_id
ORDER BY a.appt_id;

-- 8. 排班变更申请数据
INSERT INTO schedule_change_request (
    schedule_id, change_type, target_work_date, target_shift, target_total_quota,
    reason, status, apply_time, audit_admin_id, audit_time, audit_remark
) VALUES
(10, 2, '2026-06-09', '上午', 24, '希望调整到上午门诊并增加号源', 3, '2026-06-01 16:00:00', 1, '2026-06-01 17:20:00', '目标时段已有其他工作安排，暂不通过'),
(11, 1, NULL, NULL, NULL, '临时参加院内培训，申请停诊', 4, '2026-06-02 08:30:00', NULL, NULL, '医生已自行撤回申请');

-- 9. 系统公告数据
INSERT INTO sys_notice (title, content, is_top, status, publish_time, admin_id) VALUES
('门诊预约须知', '请患者按预约时间提前十五分钟到院取号，过号后需重新排队。', 1, 1, '2026-06-01 08:00:00', 1),
('系统维护通知', '预约挂号系统将于本周六晚间进行短时维护，维护期间可能无法提交新预约。', 0, 1, '2026-06-02 18:30:00', 2);
