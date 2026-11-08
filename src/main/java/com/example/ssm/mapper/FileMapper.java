package com.example.ssm.mapper;

import com.example.ssm.entity.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件数据访问接口。SQL 写在 FileMapper.xml（对应本接口 namespace）。
 */
public interface FileMapper {

    /**
     * 插入一条文件元信息记录。
     */
    int insert(FileInfo fileInfo);

    /**
     * 查询某个用户的文件（不含已删除），按上传时间倒序；
     * category 非空时只查该分类（文档/图片/压缩包），并带出每个文件的下载次数。
     */
    List<FileInfo> listByUploader(@Param("uploaderId") Integer uploaderId,
                                  @Param("category") String category);

    /**
     * 按主键查询单条文件记录（下载前取存储路径与归属人）。
     */
    FileInfo findById(Long id);

    /**
     * 更新文件状态（冻结/解冻/删除），必须同时匹配 uploaderId —— 只有上传者本人能操作。
     * 返回影响行数，0 表示文件不存在或不属于该用户。
     */
    int updateStatus(@Param("id") Long id,
                     @Param("uploaderId") Integer uploaderId,
                     @Param("status") Integer status);

    /**
     * 写入一条下载日志（F5：AOP 下载审计）。
     */
    int insertDownloadLog(@Param("fileId") Long fileId, @Param("downloaderId") Integer downloaderId);
}
