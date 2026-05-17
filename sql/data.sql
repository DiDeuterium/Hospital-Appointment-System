-- ============================================
-- 医院预约挂号系统 — 测试数据 (Spring Boot 启动自动执行)
-- 密码为 BCrypt 哈希, 明文均为 "123456"
-- ============================================

-- 科室 (10 条)
INSERT INTO department (dept_name, location, description) VALUES
('心内科',   '门诊楼2层', '心血管疾病诊疗，含高血压、冠心病、心律失常等'),
('神经内科', '门诊楼2层', '神经系统疾病诊疗，含头痛、癫痫、帕金森等'),
('骨科',     '门诊楼3层', '骨骼关节疾病诊疗，含骨折、关节炎、颈椎病等'),
('儿科',     '门诊楼1层', '儿童常见病及多发病诊疗'),
('皮肤科',   '门诊楼5层', '皮肤病诊疗，含湿疹、痤疮、银屑病等'),
('眼科',     '门诊楼4层', '眼部疾病诊疗，含近视、白内障、青光眼等'),
('耳鼻喉科', '门诊楼4层', '耳鼻喉疾病诊疗'),
('消化内科', '门诊楼2层', '消化系统疾病诊疗，含胃炎、肠炎、肝病等'),
('呼吸内科', '门诊楼2层', '呼吸系统疾病诊疗，含哮喘、肺炎、慢阻肺等'),
('妇科',     '门诊楼3层', '妇科常见病及多发病诊疗');

-- ============================================
-- 2. 医生表 (12 条, ID 1-12)
--   密码统一为占位值, 实际使用前需替换为 BCrypt 哈希
--   dept_id: 1=心内科 2=神经内科 3=骨科 4=儿科 5=皮肤科 6=眼科 7=耳鼻喉科 8=消化内科 9=呼吸内科 10=妇科
-- ============================================
INSERT INTO doctor (doc_name, gender, title, dept_id, password) VALUES
('张伟',   'M', '主任医师',   1, '$2b$10$OxDoBhKyhGJ/sPWLBK3QOuVg1g0Cm7UIYPAG0LlsN5BAFgqEZdhOi'),
('李娜',   'F', '副主任医师', 1, '$2b$10$Y6eNyHtHCKy0stV..jLr9.Eq4C9mZsvLnEMu29WBPNCuOubYx4l9O'),
('王强',   'M', '主任医师',   2, '$2b$10$YKBK.65uhGul38tG5RLU/u61nSO9PjmBcc1m12Tfe5bxkBPOt4Du6'),
('刘芳',   'F', '主治医师',   2, '$2b$10$.Hoon5Ehe4uAPZlDqMp.AuZ7DZQi4KzGkuL0E0ps1T4Y.z0ohkNzm'),
('陈刚',   'M', '主任医师',   3, '$2b$10$PFMDur4veCaeqMm6L9GWbe/7e54cuJO8p55bJGnFd3VWFLsc0vOpS'),
('杨丽',   'F', '副主任医师', 3, '$2b$10$veDC4Kie5RrwMv8MrHcZTugQkU37/KvAX/1.QbAHVUHY1ex4L.YJC'),
('赵敏',   'F', '主任医师',   4, '$2b$10$lsoP1laicZoNRdlvy6YZgulUXTbQAfFtaxXKNRXGFBM.Zvd9Qn6nm'),
('孙磊',   'M', '主治医师',   5, '$2b$10$QZugyY6te.J8pVf0/e7Rz.5PA1rV2qgQIhQ3savvEPtt9volQlw26'),
('周杰',   'M', '主任医师',   6, '$2b$10$Z0.XBRZYz2qSfmu.Tc/BJeVEJOcs.TJA7Tm/j.WkYPa7huYEjMJ/q'),
('吴秀英', 'F', '副主任医师', 7, '$2b$10$4OWUyDWQR3JA1KK6jFGoXuIX9SWZnzywF3RBUI6h.cTYF9F80CP.G'),
('郑涛',   'M', '主治医师',   8, '$2b$10$G41IP6nA2NS8OG0MMzbpa.7ncsnaZn/9bjuL5Y8phBgBf8Oc23SL6'),
('钱红',   'F', '主任医师',   9, '$2b$10$OGCCox9Izs6eSqTBjq64N.xd07w7HFn8io4yp6z.K5zhuF9Hc/jV6');

-- 患者 (12 条, 密码均为 123456 的 BCrypt 哈希)
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
('320105199404096667', '沈秀英', 'F', '13300002222', '$2b$10$h2AC4.HsLyWKEKFR5rWTTOU.vxa8LdioeWwVPFk0iukZwYVkSYS3i');

-- ============================================
-- 4. 排班表 (16 条, ID 1-16)
--   日期以 2026-05-07 为"今天", 覆盖过去/今天/未来
--   场景: 有号源(rest>0) / 已约满(rest=0) / 不同时段
--   rest_quota = total_quota - count(status=1或3的挂号); status=2(已取消)不占号源
--   doc_id 映射: 1=张伟 2=李娜 3=王强 5=陈刚 6=杨丽 7=赵敏 9=周杰 10=吴秀英 11=郑涛 12=钱红
-- ============================================
INSERT INTO schedule (doc_id, work_date, shift, total_quota, rest_quota) VALUES
-- 过去日期 (已过期, status=3 已就诊仍占号源)
(1,  '2026-05-01', '上午', 30, 28),   -- 张伟, 2 个已就诊 (appt 1,2)
(1,  '2026-05-02', '下午', 20, 19),   -- 张伟, 1 个已就诊 (appt 4)
(3,  '2026-05-03', '上午', 25, 24),   -- 王强, 1 个已就诊 (appt 5)

-- 今天 2026-05-07 (周四)
(1,  '2026-05-07', '上午', 30, 28),   -- 张伟 有号源, 2 个 active (appt 6,7)
(2,  '2026-05-07', '上午',  2,  0),   -- 李娜 已约满, 2 个 active (appt 8,9)
(3,  '2026-05-07', '下午', 20, 19),   -- 王强 有号源, 1 个 active (appt 10)
(5,  '2026-05-07', '上午', 30, 29),   -- 陈刚 有号源, 1 个 active (appt 11)
(7,  '2026-05-07', '上午', 15, 15),   -- 赵敏 全空 (儿科)

-- 未来日期 (5月8日-5月13日)
(1,  '2026-05-08', '上午', 30, 28),   -- 张伟 2 个 active (appt 12,13)
(2,  '2026-05-08', '下午', 20, 20),   -- 李娜 全空
(5,  '2026-05-09', '上午', 25, 25),   -- 陈刚 全空
(6,  '2026-05-09', '下午', 20, 19),   -- 杨丽 1 个 active (appt 14; appt 15 已取消)
(9,  '2026-05-10', '上午', 30, 27),   -- 周杰 3 个 active (appt 16,17,18)
(10, '2026-05-11', '上午', 20, 20),   -- 吴秀英 全空
(12, '2026-05-12', '下午', 25, 25),   -- 钱红 全空
(11, '2026-05-13', '上午', 20, 20);   -- 郑涛 全空 (7天边界)

-- 挂号记录 (18 条)
INSERT INTO appointment (patient_id, schedule_id, status, create_time) VALUES
(1,  1,  3, '2026-04-30 08:15:00'),
(2,  1,  3, '2026-04-30 09:20:00'),
(3,  1,  2, '2026-05-01 10:00:00'),
(4,  2,  3, '2026-05-01 14:30:00'),
(5,  3,  3, '2026-05-02 08:00:00'),
(1,  4,  1, '2026-05-15 09:00:00'),
(2,  4,  1, '2026-05-15 09:30:00'),
(3,  5,  1, '2026-05-15 10:00:00'),
(4,  5,  1, '2026-05-15 11:00:00'),
(6,  6,  1, '2026-05-16 08:00:00'),
(7,  7,  1, '2026-05-16 07:30:00'),
(1,  9,  1, '2026-05-16 10:00:00'),
(8,  9,  1, '2026-05-16 11:00:00'),
(9, 12,  1, '2026-05-16 08:00:00'),
(10, 12, 2, '2026-05-15 15:00:00'),
(11, 13, 1, '2026-05-16 09:00:00'),
(1,  13, 1, '2026-05-16 09:30:00'),
(5,  13, 1, '2026-05-16 10:00:00');
