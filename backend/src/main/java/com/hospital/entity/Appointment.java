package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("appointment")
public class Appointment {
    public static final int STATUS_BOOKED = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_EXPIRED = 4;

    @TableId(type = IdType.AUTO)
    private Integer apptId;
    private Integer patientId;
    private Integer scheduleId;
    private Integer status;
    private LocalDateTime createTime;
}
