-- =============================================================
-- 测试账号数据
-- 密码均为 123456，采用 SHA-256(密码 + 盐) 计算，与 PasswordUtil 保持一致。
--   zhangsan: 880f3e82359804f51d9f769ed4791544c301619fd3550e0326e77961cb917056
--   lisi:     a13d8a1ff17c7f4a6c5a5e3c1de47b8704be38db9dfe0da4e5b9b688b3b95e3a
-- =============================================================
USE ssm_file_db;

-- 保证客户端以 UTF-8 发送中文，防止导入后姓名乱码
SET NAMES utf8mb4;

INSERT INTO sys_user (username, password, salt, real_name) VALUES
('zhangsan', '880f3e82359804f51d9f769ed4791544c301619fd3550e0326e77961cb917056', 'a1b2c3d4', '张三'),
('lisi',     'a13d8a1ff17c7f4a6c5a5e3c1de47b8704be38db9dfe0da4e5b9b688b3b95e3a', 'e5f6a7b8', '李四');
