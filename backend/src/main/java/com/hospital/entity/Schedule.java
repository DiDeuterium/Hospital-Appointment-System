package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Integer scheduleId;
    private Integer docId;
    private LocalDate workDate;
    private String shift;
    private Integer totalQuota;
    private Integer restQuota;
    private BigDecimal fee;
    private Integer status;
}
