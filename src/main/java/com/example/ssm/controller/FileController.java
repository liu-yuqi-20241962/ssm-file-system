package com.example.ssm.controller;

import com.example.ssm.entity.FileInfo;
import com.example.ssm.entity.SysUser;
import com.example.ssm.exception.FileAccessException;
import com.example.ssm.service.FileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件控制器：列表、上传、下载。
 */
@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /** 提交列表：只显示当前登录用户自己的记录；category 非空时按分类筛选 */
    @GetMapping("/list")
    public String list(@RequestParam(value = "category", required = false) String category,
                       HttpSession session, Model model) {
        SysUser user = (SysUser) session.getAttribute("loginUser");
        model.addAttribute("files", fileService.listFiles(user.getId(), category));
        model.addAttribute("currentCategory", category);
        return "list";
    }

    /** 上传页 */
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    /**
     * 上传提交（支持一次选多个文件）：逐个做服务端校验与落盘，
     * 全部成功跳转列表页并提示；存在失败时回到上传页并汇总提示。
     */
    @PostMapping("/upload")
    public String doUpload(@RequestParam("files") MultipartFile[] files,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        SysUser user = (SysUser) session.getAttribute("loginUser");
        if (files == null || files.length == 0) {
            model.addAttribute("error", "请选择要上传的文件");
            return "upload";
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue; // 忽略空项
            }
            try {
                fileService.saveFile(file, user.getId(), user.getUsername());
                successCount++;
            } catch (IllegalArgumentException e) {
                errors.add((file.getOriginalFilename() == null ? "文件" : file.getOriginalFilename())
                        + "：" + e.getMessage());
            }
        }

        if (successCount == 0) {
            model.addAttribute("error", String.join("；", errors));
            return "upload";
        }
        String msg = "成功上传 " + successCount + " 个文件";
        if (!errors.isEmpty()) {
            msg += "；" + errors.size() + " 个失败：" + String.join("；", errors);
        }
        redirectAttributes.addFlashAttribute("success", msg);
        return "redirect:/file/list";
    }

    /** 冻结下载：仅上传者本人可操作自己的文件 */
    @PostMapping("/freeze")
    public String freeze(@RequestParam("fileId") Long fileId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        SysUser user = (SysUser) session.getAttribute("loginUser");
        boolean ok = fileService.freezeFile(fileId, user.getId());
        redirectAttributes.addFlashAttribute(ok ? "success" : "error",
                ok ? "文件已冻结，他人将无法下载" : "操作失败：文件不存在或无权操作");
        return "redirect:/file/list";
    }

    /** 解冻：恢复文件的正常下载 */
    @PostMapping("/unfreeze")
    public String unfreeze(@RequestParam("fileId") Long fileId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        SysUser user = (SysUser) session.getAttribute("loginUser");
        boolean ok = fileService.unfreezeFile(fileId, user.getId());
        redirectAttributes.addFlashAttribute(ok ? "success" : "error",
                ok ? "文件已解冻，恢复下载" : "操作失败：文件不存在或无权操作");
        return "redirect:/file/list";
    }

    /**
     * 下载：按 fileId 从数据库取存储路径（绝不拼接用户输入），
     * 归属校验通过后以 attachment 方式返回，正确处理中文文件名。
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("fileId") Long fileId,
                                             HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("loginUser");
        // 归属校验 + 存在性校验；通过后 AOP 切面会自动写入下载日志
        FileInfo info = fileService.downloadFile(fileId, user.getId());

        File diskFile = fileService.resolveDiskFile(info);
        if (!diskFile.exists()) {
            throw new FileAccessException("文件不存在或已删除");
        }

        Resource resource = new FileSystemResource(diskFile);
        // 中文文件名：filename*=UTF-8'' 百分号编码；同时给 ASCII 回退名
        String disposition = "attachment; filename=\"" + info.getStoreName()
                + "\"; filename*=UTF-8''" + encodeRfc5987(info.getOriginName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(info.getFileSize())
                .body(resource);
    }

    /**
     * RFC 5987 文件名编码：对 UTF-8 字节做百分号编码（保留字母数字与 - _ . ~）。
     */
    private String encodeRfc5987(String filename) {
        StringBuilder sb = new StringBuilder();
        for (byte b : filename.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append((char) c);
            } else {
                sb.append('%').append(String.format("%02X", c));
            }
        }
        return sb.toString();
    }
}
