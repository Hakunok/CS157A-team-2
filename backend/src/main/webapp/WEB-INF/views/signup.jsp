<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up - aiRchive</title>
</head>
<body>

<main>
    <h1>Sign up to aiRchive</h1>

    <div id="form-error-message" style="color:red; font-weight: bold;">
        <c:if test="${not empty error}">
            ${error}
        </c:if>
    </div>

    <form action="signup" method="post" id="signup-form" data-context-path="${pageContext.request.contextPath}">
        <div>
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" required minlength="3" maxlength="20">
            <span id="username-validation-message"></span>
        </div>
        <br>
        <div>
            <label for="firstName">First Name:</label>
            <input type="text" id="firstName" name="firstName" required>
        </div>
        <br>
        <div>
            <label for="lastName">Last Name:</label>
            <input type="text" id="lastName" name="lastName" required>
        </div>
        <br>
        <div>
            <label for="email">Email:</label>
            <input type="email" id="email" name="email" required>
            <span id="email-validation-message"></span>
        </div>
        <br>
        <div>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required minlength="8">
        </div>
        <br>
        <div>
            <label for="confirmPassword">Confirm Password:</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required>
            <span id="password-match-message"></span>
        </div>
        <br>
        <button type="submit">Sign Up</button>
    </form>
</main>

<script src="${pageContext.request.contextPath}/assets/js/signup.js"> </script>

</body>
</html>