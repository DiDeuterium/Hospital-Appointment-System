package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Select("""
        SELECT s.*, d.doc_name, d.title, d.dept_id, dp.dept_name
        FROM schedule s
        JOIN doctor d ON s.doc_id = d.doc_id
        JOIN department dp ON d.dept_id = dp.dept_id
        WHERE dp.dept_id = #{deptId}
          AND s.work_date >= CURDATE()
        ORDER BY s.work_date, s.shift
        """)
    List<Schedule> selectSchedulesWithDetails(@Param("deptId") String deptId);

    /**
     * 定向更新剩余号源，避免 updateById 全字段 UPDATE 时
     * docId(String) vs doc_id(INT) 类型不匹配导致参数错位
     */
    @Update("UPDATE schedule SET rest_quota = #{restQuota} WHERE schedule_id = #{scheduleId}")
    int updateRestQuota(@Param("scheduleId") Integer scheduleId, @Param("restQuota") Integer restQuota);
}
