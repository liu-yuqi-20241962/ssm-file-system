package com.example.ssm.exception;

/**
 * 文件访问异常：用于“文件不存在/已删除”与“无权访问”两种业务失败，
 * 由全局异常处理器转成友好提示，避免打印异常堆栈。
 */
public class FileAccessException extends RuntimeException {

    public FileAccessException(String message) {
        super(message);
    }
}
