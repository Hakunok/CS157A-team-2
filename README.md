# Team 02: aiRchive

## Stack
- **Frontend:** React
- **Backend:** Java 21, Jersey (JAX-RS 2.35), JDBC 9.3.0, Tomcat 9.0.105
- **Database:** MySQL 8.0.42
- **Architecture:** RESTful API with Repository-Service pattern

## Directory Structure

This project is divided into two separate applications (at least for development):
- `server/` - Java backend (JAX-RS, JDBC, Maven)
- `client/` - React frontend, not needed for backend development (you should probably open this in a different project or even VSCode)

## Setting up

### Backend
1. Open Eclipse
2. Go to File > Import > Maven > Existing Maven Projects
3. Select the path to the `server/` directory
   - Make sure to not open `client/` OR directly from the root (for some reason it keeps messing 
     up if i do that)
4. Eclipse will detect the `pom.xml` file and import the project as a Maven webapp
5. Set up Apache Tomcat in Eclipse
    - Go to Servers tab -> New > Server -> Select Tomcat version (9.0.105)
    - Add your importer `server` project to the server

### DB related
1. Install MySQL 8.0+ (8.0.42) and create the schema using `server/sql/schema.sql`.
2. Set up your JDBC connection parameters
   - Copy `server/src/main/resources/db.properties.template` to `server/src/main/resources/db.properties`.
   - Fill out the fields with your correct credentials (this is gitignored)
3. Make sure your MySQL server is running whenever you deploy the application

### Frontend

i'll write this soon



## References & Resources

### Java & Design Patterns
- [Repository: A Domain-Driven Design Pattern](https://www.umlboard.com/design-patterns/repository.html)

### JDBC & MySQL
- Dr. Ching-Seh Wu’s Lecture Slides
- [MySQL 8.4 Reference Manual](https://dev.mysql.com/doc/refman/8.4/en/)
- [MySQL TIMESTAMPDIFF() Function](https://www.w3resource.com/mysql/date-and-time-functions/mysql-timestampdiff-function.php)
- [Java SQL (java.sql) API Docs](https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html)

### RESTful API Design
- [Best Practices for RESTful API Design (Microsoft)](https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design)
- [JAX-RS Overview (Oracle)](https://www.oracle.com/technical-resources/articles/java/jax-rs.html)
- [Jersey 2.47 API Documentation](https://eclipse-ee4j.github.io/jersey.github.io/apidocs/latest/jersey/index.html)
- [HTTP Status Codes for REST](https://restfulapi.net/http-status-codes/)
- [JAX-RS @QueryParam example](https://mkyong.com/webservices/jax-rs/jax-rs-queryparam-example/)

### Recommendation Systems
- [Types of Recommendation Systems](https://marutitech.medium.com/what-are-the-types-of-recommendation-systems-3487cbafa7c9)

---
