#!/bin/bash

set -e

SCRIPT_DIR="$(dirname "$0")"

# load db credentials from config.env
source "$SCRIPT_DIR/config.env"

# exit if missing db credentials
if [[ -z "$DB_USER" || -z "$DB_PASS" || -z "$DB_NAME" ]]; then
  echo "ERROR: Missing DB credentials"
  exit 1
fi

echo "USING: $DB_NAME"

# exit if mysql server is offline
if ! mysqladmin ping -u"$DB_USER" -p"$DB_PASS" > /dev/null 2>&1; then
  echo "ERROR: MySQL server is offline or unreachable"
  exit 1
fi

# get associated relative directory & file paths
MIGRATIONS_DIR="$SCRIPT_DIR/migrations"
APPLIED_FILE="$SCRIPT_DIR/applied_migrations.txt"
touch "$APPLIED_FILE"

# find sql migration scripts and attempt to apply delta
find "$MIGRATIONS_DIR" -type f -name "*.sql" | sort -V | while IFS= read -r file; do
  filename=$(basename "$file")

  if grep -Fxq "$filename" "$APPLIED_FILE"; then
    # skip if migrated
    echo "SKIP: $filename"
  else
    # attempt to apply migration script
    echo "APPLY: $filename"
    if mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$file"; then
      echo "$filename" >> "$APPLIED_FILE"
    else
      # exit on mysql error
      echo "ERROR: Failed to apply $filename"
      exit 1
    fi
  fi
done

echo "SUCCESS: Migrations applied"