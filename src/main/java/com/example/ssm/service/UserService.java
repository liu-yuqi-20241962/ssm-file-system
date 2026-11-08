package com.example.ssm.service;

import com.example.ssm.entity.SysUser;

/**
 * 用户服务接口。
 */
public interface UserService {

    /**
     * 登录校验：账号存在且密码（加盐哈希）比对通过时返回用户，否则返回 null。
     */
    SysUser login(String username, String rawPassword);
}
