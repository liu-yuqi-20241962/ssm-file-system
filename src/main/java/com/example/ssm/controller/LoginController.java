package com.example.ssm.controller;

import com.example.ssm.entity.SysUser;
import com.example.ssm.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录控制器。
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /** 根路径：跳转到文件列表（未登录时会被拦截器重定向到登录页） */
    @GetMapping("/")
    public String index() {
        return "redirect:/file/list";
    }

    /** 登录页 */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /** 登录提交：校验成功后把用户对象写入 session */
    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpSession session,
                          Model model) {
        SysUser user = userService.login(username, password);
        if (user == null) {
            model.addAttribute("error", "账号或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        return "redirect:/file/list";
    }

    /** 退出登录 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
