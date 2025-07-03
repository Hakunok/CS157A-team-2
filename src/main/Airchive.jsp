<%@ page import="java.sql.*"%>
<html>
<head>
  <title>Airchive</title>
</head>
<body>
<h1>JDBC for Airchive</h1>

<table border="1">
  <tr>
    <td>adminID</td>
    <td>adminName</td>
    <td>adminEmail</td>
    <td>authorID</td>
    <td>authorName</td>
    <td>readerID</td>
    <td>readerName</td>
  </tr>
    <%
     String db = "Tran";
        String user; // assumes database name is the same as username
          user = "root";
        String password = "666452";
        try {
            java.sql.Connection con;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tran?autoReconnect=true&useSSL=false", "root", "666452");
            out.println(db + " database successfully opened.<br/><br/>");

            out.println("Initial entries in table \"customers\": <br/>");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tran.customers");
            while (rs.next()) {
         out.println("<tr>" + "<td>" +  rs.getInt(1) + "</td>"+ "<td>" +    rs.getString(2) + "</td>"+   "<td>" + rs.getString(3) + "</td>"  + "</tr>");
            }
            rs.close();
            stmt.close();
            con.close();
        } catch(SQLException e) {
            out.println("SQLException caught: " + e.getMessage());
        }
    %>
</body>
</html>