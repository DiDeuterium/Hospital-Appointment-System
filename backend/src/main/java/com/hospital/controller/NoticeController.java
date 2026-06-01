package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.entity.SysNotice;
import com.hospital.service.SysNoticeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final SysNoticeService sysNoticeService;

    public NoticeController(SysNoticeService sysNoticeService) {
        this.sysNoticeService = sysNoticeService;
    }

    @GetMapping
    public Result<List<SysNotice>> list() {
        return Result.ok(sysNoticeService.listPublished());
    }

    @GetMapping("/{noticeId}")
    public Result<SysNotice> getById(@PathVariable Integer noticeId) {
        return Result.ok(sysNoticeService.getById(noticeId));
    }
}
