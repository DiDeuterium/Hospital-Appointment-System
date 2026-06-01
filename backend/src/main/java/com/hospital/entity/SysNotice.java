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
@TableName("sys_notice")
public class SysNotice {
    @TableId(type = IdType.AUTO)
    private Integer noticeId;
    private String title;
    private String content;
    private Integer isTop;
    private Integer status;
    private LocalDateTime publishTime;
    private Integer adminId;
}
