package com.example.ssm.aop;

import com.example.ssm.mapper.FileMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 下载日志切面（F5，选做）。
 * 在“下载文件”服务方法正常返回（即通过归属校验、确认为有效下载）后，
 * 把文件 ID、下载人、下载时间写入 download_log 表，实现下载审计。
 */
@Aspect
@Component
public class DownloadLogAspect {

    @Autowired
    private FileMapper fileMapper;

    /**
     * 拦截 FileService.downloadFile：仅当正常返回（下载成功）时记录日志。
     * 入参通过 JoinPoint 获取：args[0]=文件ID，args[1]=下载人ID。
     */
    @AfterReturning("execution(* com.example.ssm.service.FileService.downloadFile(..))")
    public void recordDownload(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2 && args[0] != null) {
            Long fileId = (Long) args[0];
            Integer downloaderId = (Integer) args[1];
            fileMapper.insertDownloadLog(fileId, downloaderId);
        }
    }
}
