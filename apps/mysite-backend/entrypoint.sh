#!/bin/sh

# Read the database password from the secret file and export it as an environment variable
if [ -f /run/secrets/db-password ]; then
    export POSTGRES_PASSWORD=$(cat /run/secrets/db-password)
fi

# Execute the Java application
exec java -jar app.jar
