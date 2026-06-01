package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.SysNotice;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.SysNoticeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysNoticeService {

    private final SysNoticeMapper sysNoticeMapper;

    public SysNoticeService(SysNoticeMapper sysNoticeMapper) {
        this.sysNoticeMapper = sysNoticeMapper;
    }

    public List<SysNotice> listPublished() {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, 1)
               .orderByDesc(SysNotice::getIsTop)
               .orderByDesc(SysNotice::getPublishTime);
        return sysNoticeMapper.selectList(wrapper);
    }

    public SysNotice getById(Integer noticeId) {
        SysNotice notice = sysNoticeMapper.selectById(noticeId);
        if (notice == null || notice.getStatus() == 0) {
            throw new BusinessException(404, "公告不存在");
        }
        return notice;
    }

    // Admin methods
    public List<SysNotice> listAll(Integer status) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getIsTop)
               .orderByDesc(SysNotice::getPublishTime);
        return sysNoticeMapper.selectList(wrapper);
    }

    public void add(String title, String content, Integer isTop, Integer status, Integer adminId) {
        SysNotice notice = new SysNotice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsTop(isTop != null ? isTop : 0);
        notice.setStatus(status != null ? status : 1);
        notice.setAdminId(adminId);
        sysNoticeMapper.insert(notice);
    }

    public void update(Integer noticeId, String title, String content, Integer isTop, Integer status) {
        SysNotice notice = sysNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        if (title != null) notice.setTitle(title);
        if (content != null) notice.setContent(content);
        if (isTop != null) notice.setIsTop(isTop);
        if (status != null) notice.setStatus(status);
        sysNoticeMapper.updateById(notice);
    }

    public void offline(Integer noticeId) {
        SysNotice notice = sysNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        notice.setStatus(0);
        sysNoticeMapper.updateById(notice);
    }
}
