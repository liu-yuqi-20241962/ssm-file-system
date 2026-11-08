<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>操作失败</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<div class="error-card">
    <h2>操作失败</h2>
    <div class="alert">${error}</div>
    <a class="btn" href="${pageContext.request.contextPath}/file/list">返回列表</a>
</div>
</body>
</html>
