package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("patient")
public class Patient {
    @TableId(type = IdType.AUTO)
    private Integer patientId;
    private String idCard;
    private String realName;
    private String gender;
    private String phone;
    private String password;
}
