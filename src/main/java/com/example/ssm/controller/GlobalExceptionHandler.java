package com.example.ssm.controller;

import com.example.ssm.exception.FileAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理器：把业务异常转成友好提示页，不向用户暴露异常堆栈。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** 文件不存在 / 已删除 / 无权访问 */
    @ExceptionHandler(FileAccessException.class)
    public String handleFileAccess(FileAccessException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    /** 超出 multipart 容器上限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException e, Model model) {
        model.addAttribute("error", "上传文件过大（超过 10MB）");
        return "error";
    }

    /** 其他 multipart 解析异常 */
    @ExceptionHandler(MultipartException.class)
    public String handleMultipart(MultipartException e, Model model) {
        model.addAttribute("error", "上传失败：文件过大或请求格式不正确");
        return "error";
    }
}
