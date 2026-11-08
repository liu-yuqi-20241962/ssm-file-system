package com.example.ssm.entity;

import java.util.Date;

/**
 * 用户实体，对应 sys_user 表。
 */
public class SysUser {
    private Integer id;
    private String username;
    private String password;   // SHA-256 加盐后的十六进制串，绝不存明文
    private String salt;       // 盐值，与密码分开存储
    private String realName;
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
