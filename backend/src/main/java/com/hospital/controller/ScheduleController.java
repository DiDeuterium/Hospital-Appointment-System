package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.response.ScheduleVO;
import com.hospital.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Result<List<ScheduleVO>> list(
            @RequestParam String deptId,
            @RequestParam(required = false) LocalDate workDate,
            @RequestParam(required = false) String shift) {
        return Result.ok(scheduleService.list(deptId, workDate, shift));
    }
}
