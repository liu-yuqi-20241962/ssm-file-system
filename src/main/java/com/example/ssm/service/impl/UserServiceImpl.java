package com.example.ssm.service.impl;

import com.example.ssm.entity.SysUser;
import com.example.ssm.mapper.SysUserMapper;
import com.example.ssm.service.UserService;
import com.example.ssm.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现：基于数据库的账号 + SHA-256 加盐密码校验。
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public SysUser login(String username, String rawPassword) {
        if (username == null || rawPassword == null || username.trim().isEmpty()) {
            return null;
        }
        SysUser user = sysUserMapper.findByUsername(username.trim());
        if (user == null) {
            return null;
        }
        // 用库中该用户的盐重新计算哈希比对，全程不比较明文
        if (!PasswordUtil.verify(rawPassword, user.getSalt(), user.getPassword())) {
            return null;
        }
        return user;
    }
}
