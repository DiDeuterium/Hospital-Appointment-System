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
@TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Integer docId;
    private String docName;
    private String gender;
    private String title;
    private Integer deptId;
    private String password;
    private String avatarUrl;
    private String specialty;
    private Integer status;
}
