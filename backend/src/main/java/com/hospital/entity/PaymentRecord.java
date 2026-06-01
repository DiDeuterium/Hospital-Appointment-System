package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Integer paymentId;
    private Integer apptId;
    private BigDecimal amount;
    private Integer payStatus;
    private String payMethod;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
