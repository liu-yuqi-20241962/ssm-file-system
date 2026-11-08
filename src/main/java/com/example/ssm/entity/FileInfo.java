package com.example.ssm.entity;

import java.util.Date;

/**
 * 文件元信息实体，对应 file_info 表。
 * 设计要点：磁盘上只存文件本体，数据库存元数据（原始文件名、存储路径、大小、归属人等）。
 * 原始文件名必须入库——落盘名是 UUID，用户下载时需要还原成原始中文名。
 */
public class FileInfo {

    /** 状态常量：0=已删除（逻辑删除） 1=正常 2=已冻结（上传者冻结下载） */
    public static final int STATUS_DELETED = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_FROZEN = 2;

    private Long id;
    private String originName;   // 用户提交的原始文件名（仅入库，不参与落盘路径）
    private String storeName;    // 落盘文件名：UUID + 原扩展名
    private String filePath;     // 相对存储路径，如 upload/zhangsan/2026/11
    private String ext;          // 扩展名（小写、不含点）
    private Long fileSize;       // 字节
    private String contentType;  // MIME 类型
    private Integer uploaderId;  // 上传人，越权校验依据
    private Date createTime;
    private Integer status;      // 1=正常 2=已冻结 0=已删除
    private Long downloadCount;  // 被下载次数（来自 download_log 计数，列表展示用）
    private String category;     // 文件分类：文档 / 图片 / 压缩包（按扩展名归类，用于分类存储与筛选）

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginName() { return originName; }
    public void setOriginName(String originName) { this.originName = originName; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getExt() { return ext; }
    public void setExt(String ext) { this.ext = ext; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Integer getUploaderId() { return uploaderId; }
    public void setUploaderId(Integer uploaderId) { this.uploaderId = uploaderId; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Long downloadCount) { this.downloadCount = downloadCount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
