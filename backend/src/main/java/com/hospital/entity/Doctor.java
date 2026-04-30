package com.hospital.entity;

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
    @TableId
    private String docId;
    private String docName;
    private String gender;
    private String title;
    private String deptId;
    private String password;
}
