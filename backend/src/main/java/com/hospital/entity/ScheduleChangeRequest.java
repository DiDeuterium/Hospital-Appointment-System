package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("schedule_change_request")
public class ScheduleChangeRequest {
    public static final int TYPE_CANCEL = 1;
    public static final int TYPE_MODIFY = 2;

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_APPROVED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_WITHDRAWN = 4;

    @TableId(type = IdType.AUTO)
    private Integer requestId;
    private Integer scheduleId;
    private Integer changeType;
    private LocalDate targetWorkDate;
    private String targetShift;
    private Integer targetTotalQuota;
    private String reason;
    private Integer status;
    private LocalDateTime applyTime;
    private Integer auditAdminId;
    private LocalDateTime auditTime;
    private String auditRemark;
}
