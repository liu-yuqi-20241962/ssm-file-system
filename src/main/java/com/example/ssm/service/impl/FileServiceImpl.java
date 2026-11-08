package com.example.ssm.service.impl;

import com.example.ssm.entity.FileInfo;
import com.example.ssm.exception.FileAccessException;
import com.example.ssm.mapper.FileMapper;
import com.example.ssm.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件服务实现。
 * 安全要点：
 *  1) 大小与类型校验都在服务端，不依赖前端；
 *  2) 落盘名用 UUID + 原扩展名，用户提交的原始文件名只入库，杜绝路径穿越与覆盖；
 *  3) 存储路径取自数据库，下载时绝不拼接用户输入；
 *  4) 下载前做归属校验，防止越权下载他人文件。
 */
@Service
public class FileServiceImpl implements FileService {

    /** 大小上限：10MB */
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    /** 扩展名白名单（小写、不含点） */
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "png", "doc", "docx", "zip");

    @Autowired
    private FileMapper fileMapper;

    /** 上传根目录，来自配置文件 config.properties，禁止硬编码 */
    @Value("${upload.root.dir}")
    private String uploadRootDir;

    @Override
    public List<FileInfo> listFiles(Integer uploaderId, String category) {
        return fileMapper.listByUploader(uploaderId, category);
    }

    @Override
    public void saveFile(MultipartFile file, Integer uploaderId, String username) {
        // 1. 空文件校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        // 2. 大小校验（服务端，上限 10MB）
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 10MB");
        }
        // 3. 类型校验（扩展名白名单，服务端）
        String originName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = extractExt(originName);
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("仅支持上传 pdf / jpg / png / doc / docx / zip 格式文件");
        }

        // 4. 安全重命名：UUID + 原扩展名
        String storeName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 5. 按日期 + 分类分目录：用户名/年/月/分类（相对上传根目录，如 zhangsan/2026/11/文档）
        LocalDate now = LocalDate.now();
        String category = mapExtToCategory(ext);
        String relativeDir = username + "/" + now.getYear() + "/"
                + String.format("%02d", now.getMonthValue()) + "/" + category;

        // 6. 落盘
        try {
            Path dir = Paths.get(uploadRootDir, relativeDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(storeName).toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("文件保存失败，请重试", e);
        }

        // 7. 元信息入库（磁盘存文件、数据库存元数据）
        FileInfo info = new FileInfo();
        info.setOriginName(originName);
        info.setStoreName(storeName);
        info.setFilePath(relativeDir);
        info.setExt(ext);
        info.setFileSize(file.getSize());
        info.setContentType(file.getContentType());
        info.setUploaderId(uploaderId);
        fileMapper.insert(info);
    }

    @Override
    public FileInfo downloadFile(Long fileId, Integer currentUserId) {
        if (fileId == null) {
            throw new FileAccessException("文件不存在或已删除");
        }
        FileInfo info = fileMapper.findById(fileId);
        if (info == null || info.getStatus() == null || info.getStatus() == FileInfo.STATUS_DELETED) {
            throw new FileAccessException("文件不存在或已删除");
        }
        // 上传者已冻结该文件的下载
        if (info.getStatus() == FileInfo.STATUS_FROZEN) {
            throw new FileAccessException("该文件已被上传者冻结，暂不能下载");
        }
        // 归属校验：只允许下载自己的文件
        if (!info.getUploaderId().equals(currentUserId)) {
            throw new FileAccessException("无权访问该文件");
        }
        return info;
    }

    @Override
    public File resolveDiskFile(FileInfo fileInfo) {
        // 路径完全来自数据库元信息，不拼接任何用户输入
        return Paths.get(uploadRootDir, fileInfo.getFilePath(), fileInfo.getStoreName()).toFile();
    }

    @Override
    public boolean freezeFile(Long fileId, Integer uploaderId) {
        if (fileId == null) {
            return false;
        }
        // 只有上传者本人能冻结，且只冻结正常状态的文件
        return fileMapper.updateStatus(fileId, uploaderId, FileInfo.STATUS_FROZEN) > 0;
    }

    @Override
    public boolean unfreezeFile(Long fileId, Integer uploaderId) {
        if (fileId == null) {
            return false;
        }
        // 只有上传者本人能解冻
        return fileMapper.updateStatus(fileId, uploaderId, FileInfo.STATUS_NORMAL) > 0;
    }

    /**
     * 按扩展名归类：pdf/doc/docx → 文档；jpg/png → 图片；zip → 压缩包。
     * 用于“按分类分目录存储 + 列表分类筛选”。
     */
    private String mapExtToCategory(String ext) {
        switch (ext) {
            case "pdf":
            case "doc":
            case "docx":
                return "文档";
            case "jpg":
            case "png":
                return "图片";
            default:
                return "压缩包"; // zip
        }
    }

    /**
     * 从文件名提取小写扩展名（不含点）；无扩展名或非法时返回 null。
     */
    private String extractExt(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
