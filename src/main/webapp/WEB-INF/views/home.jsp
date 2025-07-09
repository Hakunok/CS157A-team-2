<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home - aiRchive</title>
</head>
<body>

<nav>
    <a href="<c:url value='/home'/>">Home</a> |

    <c:if test="${not empty loggedInUser}">
        <a href="<c:url value='/signout'/>">Sign Out</a>
    </c:if>

    <c:if test="${empty loggedInUser}">
        <a href="<c:url value='/signin'/>">Sign In</a> |
        <a href="<c:url value='/signup'/>">Sign Up</a>
    </c:if>
</nav>

<main>
    <h1>Welcome to aiRchive!</h1>

    <c:if test="${not empty loggedInUser}">
        <h2>Welcome back, ${loggedInUser.firstName}!</h2>
    </c:if>
</main>