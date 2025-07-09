<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In - aiRchive</title>
</head>
<body>

<main>
    <h1>Sign in to aiRchive</h1>

    <div style="font-weight: bold;">
        <c:if test="${not empty error}">
            <span style="color:red;">${error}</span>
        </c:if>
        <c:if test="${not empty success}">
            <span style="color:green;">${success}</span>
        </c:if>
    </div>

    <form action="signin" method="post" id="signin-form" data-context-path="${pageContext.request.contextPath}">
        <div>
            <label for="usernameOrEmail">Username or email address</label>
            <input type="text" id="usernameOrEmail" name="usernameOrEmail" required>
        </div>
        <br>
        <div>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <br>
        <button type="submit" disabled>Sign In</button>
    </form>
</main>

<script src="${pageContext.request.contextPath}/assets/js/signin.js"></script>

</body>
</html>