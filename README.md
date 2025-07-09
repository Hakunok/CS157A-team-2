### setup
Make a copy of `src/min/resources/db.properties.template` and name it `db.properties`, fill out
your correct credentials. This `db.properties` will be .gitignored.

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.username=root
db.password=password
db.database=database_name
```