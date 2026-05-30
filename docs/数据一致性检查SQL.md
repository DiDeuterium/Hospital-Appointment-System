# 数据一致性检查 SQL

以下 SQL 用于检查升级版测试数据是否和业务约束一致。每个查询如果结果为空，表示该项检查通过。

## 1. 挂号时间是否早于出诊日期

```sql
SELECT
    a.appt_id,
    a.create_time,
    s.schedule_id,
    s.work_date,
    s.shift
FROM appointment a
JOIN schedule s ON s.schedule_id = a.schedule_id
WHERE a.create_time >= TIMESTAMP(s.work_date);
```

## 2. 排班剩余号源是否和预约数量一致

这里按 `status IN (1, 3)` 占用号源，`2 已取消` 不占用。

```sql
SELECT
    s.schedule_id,
    s.total_quota,
    s.rest_quota,
    COUNT(a.appt_id) AS used_quota,
    s.total_quota - COUNT(a.appt_id) AS expected_rest_quota
FROM schedule s
LEFT JOIN appointment a
    ON a.schedule_id = s.schedule_id
   AND a.status IN (1, 3)
GROUP BY s.schedule_id, s.total_quota, s.rest_quota
HAVING s.rest_quota <> expected_rest_quota;
```

## 3. 每个预约是否都有且仅有一条支付记录

```sql
SELECT
    a.appt_id,
    COUNT(pr.payment_id) AS payment_count
FROM appointment a
LEFT JOIN payment_record pr ON pr.appt_id = a.appt_id
GROUP BY a.appt_id
HAVING payment_count <> 1;
```

## 4. 支付金额是否等于排班挂号费

```sql
SELECT
    pr.payment_id,
    pr.appt_id,
    pr.amount,
    s.fee
FROM payment_record pr
JOIN appointment a ON a.appt_id = pr.appt_id
JOIN schedule s ON s.schedule_id = a.schedule_id
WHERE pr.amount <> s.fee;
```

## 5. 同一排班下排队号是否连续从 1 开始

```sql
SELECT
    schedule_id,
    COUNT(*) AS appt_count,
    MIN(queue_number) AS min_queue,
    MAX(queue_number) AS max_queue,
    COUNT(DISTINCT queue_number) AS distinct_queue_count
FROM appointment
GROUP BY schedule_id
HAVING min_queue <> 1
    OR max_queue <> appt_count
    OR distinct_queue_count <> appt_count;
```
