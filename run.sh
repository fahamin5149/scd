#!/bin/sh
# Builds the project (first run only) and starts the Library Management System.
cd "$(dirname "$0")"
if [ ! -f target/library-management-system.jar ]; then
    mvn -q package || exit 1
fi
java -jar target/library-management-system.jar
