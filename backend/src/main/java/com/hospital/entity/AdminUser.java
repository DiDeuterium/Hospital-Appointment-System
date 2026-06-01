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
@TableName("admin_user")
public class AdminUser {
    @TableId(type = IdType.AUTO)
    private Integer adminId;
    private String username;
    private String realName;
    private String password;
    private Integer status;
    private LocalDateTime createTime;
}
