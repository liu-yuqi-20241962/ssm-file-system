package com.example.ssm.mapper;

import com.example.ssm.entity.SysUser;

/**
 * 用户数据访问接口。SQL 写在 UserMapper.xml（对应本接口 namespace）。
 */
public interface SysUserMapper {

    /**
     * 按登录账号查询用户（用于登录校验，返回含盐与哈希的完整记录）。
     */
    SysUser findByUsername(String username);
}
