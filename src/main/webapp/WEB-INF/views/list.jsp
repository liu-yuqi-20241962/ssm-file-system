<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>我的提交记录</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<div class="container">
    <header>
        <h2>我的提交记录</h2>
        <div class="user-box">
            <span>欢迎，${sessionScope.loginUser.realName}（${sessionScope.loginUser.username}）</span>
            <a class="btn" href="${pageContext.request.contextPath}/file/upload">上传文件</a>
            <a class="btn btn-plain" href="${pageContext.request.contextPath}/logout">退出登录</a>
        </div>
    </header>

    <c:if test="${not empty success}">
        <div class="success"><c:out value="${success}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert"><c:out value="${error}"/></div>
    </c:if>

    <div class="filter-bar card">
        <span>分类筛选：</span>
        <a href="${pageContext.request.contextPath}/file/list" class="chip ${empty currentCategory ? 'chip-on' : ''}">全部</a>
        <c:forEach items="${['文档','图片','压缩包']}" var="cat">
            <a href="${pageContext.request.contextPath}/file/list?category=${cat}"
               class="chip ${cat == currentCategory ? 'chip-on' : ''}">${cat}</a>
        </c:forEach>
    </div>

    <c:choose>
        <c:when test="${empty files}">
            <div class="empty">暂无提交记录，点击右上角「上传文件」开始提交作业</div>
        </c:when>
        <c:otherwise>
            <div class="card">
                <table>
                    <thead>
                    <tr>
                        <th>文件名</th>
                        <th>分类</th>
                        <th>大小</th>
                        <th>上传时间</th>
                        <th>下载次数</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${files}" var="f">
                        <tr>
                            <td><c:out value="${f.originName}"/></td>
                            <td><span class="tag"><c:out value="${f.category}"/></span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${f.fileSize >= 1048576}">
                                        <fmt:formatNumber value="${f.fileSize / 1048576}" pattern="0.0"/> MB
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${f.fileSize / 1024}" pattern="0.0"/> KB
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><fmt:formatDate value="${f.createTime}" pattern="yyyy-MM-dd HH:mm"/></td>
                            <td>${f.downloadCount} 次</td>
                            <td>
                                <c:choose>
                                    <c:when test="${f.status == 2}"><span class="frozen">已冻结</span></c:when>
                                    <c:otherwise><span class="ok">正常</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${f.status == 2}">
                                        <form action="${pageContext.request.contextPath}/file/unfreeze" method="post" class="inline-form">
                                            <input type="hidden" name="fileId" value="${f.id}">
                                            <button type="submit" class="btn-mini">解冻</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/file/download?fileId=${f.id}">下载</a>
                                        <form action="${pageContext.request.contextPath}/file/freeze" method="post" class="inline-form">
                                            <input type="hidden" name="fileId" value="${f.id}">
                                            <button type="submit" class="btn-mini btn-mini-plain">冻结</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
