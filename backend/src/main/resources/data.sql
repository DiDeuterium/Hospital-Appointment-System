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

-- 医生 (12 条, 密码均为 123456 的 BCrypt 哈希)
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
('110101199001011234', '赵小明', 'M', '13800001111', '$2b$12$c3H9qliZ80KkuevWvfliS.HNvIePFjkJc3/keZFXk8zhMh4hmABsC'),
('110101199205202345', '钱小红', 'F', '13800002222', '$2b$12$68SJ.nD/kul/WOHWbsTBYu.abpywXR05bPBy2K8KZKjCGTFVFz4Qi'),
('310102198803153456', '孙大伟', 'M', '13900003333', '$2b$12$c3lGJp.zFcm713/jvd0.A.4HGydX.iAffVf4aNOpljDODODJvuAR2'),
('310102199511204567', '李美玲', 'F', '13900004444', '$2b$12$fkFex9GaT0AODKBWFwKEeeOtdGQyn9527ZbIS1YB2hlNDHjOk2NrK'),
('440103197807256789', '周文博', 'M', '13600005555', '$2b$12$O6wk2eIzv2DgWWw7sa3RxOsx5BF4wBTvKbuChJzR5HT6UpRRz8UmW'),
('440103199209108901', '吴丽华', 'F', '13600006666', '$2b$12$WGMCeaLSyfkcS9Wex059.O1YsiG7oZ4F5/m8OHByOPWJ0JgZdcb2O'),
('330106198512251112', '郑国强', 'M', '13700007777', '$2b$12$nH6D01tCFApBeUbtuwmymeQd5PGurR4GQmChJgTwJswVqvQ943hVS'),
('330106199608062223', '王秀兰', 'F', '13700008888', '$2b$12$XUf0hqmueZJ6vhkpGcVFQeR1YCpratr5RMifSP2Vzoc3sYHbHF6kO'),
('510104199103173334', '冯建军', 'M', '13500009999', '$2b$12$NbI6zelOVY9RmxULZrDlOOp/sR8qIgWIPb2jpjBAJP52czy25R9BS'),
('510104199812084445', '陈玉芬', 'F', '13500001110', '$2b$12$493iGnB/Fa2VvM3BU31UoekbUtsge3TfB7AqeHiu2bmmxcQ4cSvsa'),
('320105198706185556', '蒋明辉', 'M', '13300001111', '$2b$12$gv187YZz1TEGnLUcAa9/tO.n7ql8hUNwTY7hai3xynTeRrPqgBQl2'),
('320105199404096667', '沈秀英', 'F', '13300002222', '$2b$12$FGpK3CRSE/FFCAGFQlm11u33P64ZGwN4lkw2WGFCE47ohBKQ/Nd.O');

-- 排班 (16 条, 包含过去/今天/未来日期)
INSERT INTO schedule (doc_id, work_date, shift, total_quota, rest_quota) VALUES
(1,  '2026-05-01', '上午', 30,  0),
(1,  '2026-05-02', '下午', 20,  3),
(3,  '2026-05-03', '上午', 25,  0),
(1,  '2026-05-16', '上午', 30, 15),
(2,  '2026-05-16', '上午', 25,  0),
(3,  '2026-05-16', '下午', 20,  5),
(5,  '2026-05-16', '上午', 30, 20),
(7,  '2026-05-16', '上午', 15, 10),
(1,  '2026-05-17', '上午', 30, 30),
(2,  '2026-05-17', '下午', 20, 20),
(5,  '2026-05-18', '上午', 25, 25),
(6,  '2026-05-18', '下午', 20,  0),
(9,  '2026-05-19', '上午', 30, 10),
(10, '2026-05-20', '上午', 20, 20),
(12, '2026-05-21', '下午', 25, 25),
(11, '2026-05-22', '上午', 20, 20);

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
