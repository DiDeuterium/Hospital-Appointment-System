package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.AdminUser;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.AdminUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final LoginService loginService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminUserService(AdminUserMapper adminUserMapper, LoginService loginService) {
        this.adminUserMapper = adminUserMapper;
        this.loginService = loginService;
    }

    public LoginResponse login(String username, String password) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, username);
        AdminUser admin = adminUserMapper.selectOne(wrapper);
        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(403, "账号已停用");
        }
        if (!encoder.matches(password, admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = loginService.createToken("ADMIN", String.valueOf(admin.getAdminId()), admin.getRealName());
        return new LoginResponse(token, String.valueOf(admin.getAdminId()), admin.getRealName());
    }

    public AdminUser getById(Integer adminId) {
        return adminUserMapper.selectById(adminId);
    }
}
