<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>登录 - 资料上传下载系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body class="login-body">
<div class="login-card">
    <h2>资料上传下载系统</h2>
    <c:if test="${not empty error}">
        <div class="alert">${error}</div>
    </c:if>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="form-item">
            <label>账号</label>
            <input type="text" name="username" required placeholder="请输入账号">
        </div>
        <div class="form-item">
            <label>密码</label>
            <input type="password" name="password" required placeholder="请输入密码">
        </div>
        <button type="submit" class="btn" style="width:100%">登 录</button>
    </form>
    <p class="hint">测试账号：zhangsan / 123456 或 lisi / 123456</p>
</div>
</body>
</html>
