package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
