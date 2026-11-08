-- =============================================================
-- 基于 SSM 的资料上传下载系统 · 数据库初始化脚本
-- 数据库：ssm_file_db
-- 表：sys_user(用户)、file_info(文件元信息)、download_log(下载日志)
-- 说明：密码采用 SHA-256 加盐存储，禁止明文；文件实体落盘、数据库只存元数据。
-- =============================================================

DROP DATABASE IF EXISTS ssm_file_db;
CREATE DATABASE ssm_file_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ssm_file_db;

-- -------------------------------------------------------------
-- 1. 用户表
-- -------------------------------------------------------------
CREATE TABLE sys_user (
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID(主键,自增)',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(64)  NOT NULL COMMENT '密码(SHA-256加盐后的十六进制)',
    salt        VARCHAR(32)  NOT NULL COMMENT '盐值(每个用户随机)',
    real_name   VARCHAR(50)  COMMENT '真实姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. 文件元信息表
--    uploader_id 建立索引：按“当前登录用户”查询其上传记录是高频操作，
--    索引可避免全表扫描；同时 uploader_id 也是越权校验的依据。
-- -------------------------------------------------------------
CREATE TABLE file_info (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID(主键,自增)',
    origin_name  VARCHAR(255) NOT NULL COMMENT '用户提交的原始文件名(仅入库,不用于落盘)',
    store_name   VARCHAR(255) NOT NULL COMMENT '落盘文件名(UUID+原扩展名)',
    file_path    VARCHAR(500) NOT NULL COMMENT '相对存储路径(按日期分目录)',
    ext          VARCHAR(20)  COMMENT '扩展名(小写,不含点)',
    file_size    BIGINT       COMMENT '文件大小(字节)',
    content_type VARCHAR(100) COMMENT 'MIME 内容类型',
    uploader_id  INT          NOT NULL COMMENT '上传人ID(关联 sys_user.id)',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    status       TINYINT DEFAULT 1 COMMENT '状态: 1=正常 2=已冻结(上传者冻结下载) 0=已删除(逻辑删除)',
    KEY idx_uploader (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元信息表';

-- -------------------------------------------------------------
-- 3. 下载日志表（对应选做功能 F5：AOP 下载审计）
-- -------------------------------------------------------------
CREATE TABLE download_log (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    file_id       BIGINT NOT NULL COMMENT '被下载的文件ID',
    downloader_id INT    COMMENT '下载人ID(关联 sys_user.id)',
    download_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
    KEY idx_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载日志表';
