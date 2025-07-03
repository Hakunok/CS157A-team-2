# Schema VCS

This folder contains files and scripts related to "version controlling" schema (and dummy 
population in the 
future).

### Setup

Set your database credentials in the gitignored  `config.env`.

### Usage

Start MySQL server before running scripts.
\
If not using bash or zsh, manually run migration scripts and update your `applied_migrations.txt`.

For bash and zsh users, run migrations with `migrate.sh`.
```bash
cd sql
./migrate.sh
```
\
For fresh setup use `schema.sql`.
```bash
mysql -u <DB_USER> -p<DB_PASSWORD> <DB_NAME> < schema.sql
```

### Housekeeping

Everytime we make changes to the database schema we'll add a new sql script in `migrations/`.
\
These will be numbered by versions (i.e. 001_foo.sql, 002_bar.sql, 003...)

We'll also maintain an up to date schema in `schema.sql`.

### Troubleshooting

* `ERROR: Missing DB credentials`
  * Make sure you set your DB credentials in `config.env`
* `ERROR: MySQL server is offline or unreachable`
  * Start the MySQL server
  * Check `which mysql` or `mysql --version`
* `ERROR: Failed to apply 123_foo.sql`
  * Check sql script for errors
* Script doesn't run
  * Try `chmod +x migrate.sh`