package com.example.ssm.service;

import com.example.ssm.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 文件服务接口：负责上传落盘、元信息入库、列表查询、下载取文件。
 */
public interface FileService {

    /**
     * 查询某用户的文件（不含已删除）；category 非空时只查该分类（文档/图片/压缩包）。
     */
    List<FileInfo> listFiles(Integer uploaderId, String category);

    /**
     * 保存单个上传文件：服务端校验（大小、类型）→ 安全重命名（UUID）→ 按日期+分类分目录落盘 → 元信息入库。
     * 校验不通过时抛出 IllegalArgumentException（消息为友好提示）。
     */
    void saveFile(MultipartFile file, Integer uploaderId, String username);

    /**
     * 下载前取文件：校验归属与存在性，通过后返回元信息。
     * 不存在/已删除或无权访问时抛出 FileAccessException。
     */
    FileInfo downloadFile(Long fileId, Integer currentUserId);

    /**
     * 由元信息解析磁盘上的绝对路径（路径取自数据库，绝不拼接用户输入）。
     */
    File resolveDiskFile(FileInfo fileInfo);

    /**
     * 冻结某个文件的下载（仅上传者本人）。文件不存在/不属于该用户时返回 false。
     */
    boolean freezeFile(Long fileId, Integer uploaderId);

    /**
     * 解冻某个文件（仅上传者本人）。文件不存在/不属于该用户时返回 false。
     */
    boolean unfreezeFile(Long fileId, Integer uploaderId);
}
