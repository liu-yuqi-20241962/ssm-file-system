<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>上传作业文件</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<div class="container">
    <div class="form-card">
        <h2>上传作业文件</h2>
        <c:if test="${not empty error}">
            <div class="alert"><c:out value="${error}"/></div>
        </c:if>
        <form action="${pageContext.request.contextPath}/file/upload" method="post" enctype="multipart/form-data">
            <div class="file-input">
                <input type="file" name="files" multiple required>
            </div>
            <p class="hint">支持格式：pdf / jpg / png / doc / docx / zip，单个文件不超过 10MB，可按住 Ctrl 一次选择多个文件</p>
            <div style="margin-top:16px">
                <button type="submit" class="btn">上传</button>
                <a class="btn btn-plain" href="${pageContext.request.contextPath}/file/list">返回列表</a>
            </div>
        </form>
    </div>
</div>
</body>
</html>
