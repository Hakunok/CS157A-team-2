### notes

right now our multi step db operations are not atomic transactions. we do these operations with different connections so right now it's not an atomic transaction. we'll change this going forward.

the functionalities we have right now are:
1. creating a new user account
2. requesting an author role
3. becoming an admin
4. selecting topic interests


### setup
Make a copy of `src/min/resources/db.properties.template` and name it `db.properties`, fill out
your correct credentials. This `db.properties` will be .gitignored.

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.username=root
db.password=password
db.database=database_name
```

see client README for instructions on setting up frontend dev env
