@echo off
REM Builds the project (first run only) and starts the Library Management System.
cd /d "%~dp0"
if not exist target\library-management-system.jar (
    call mvn -q package || exit /b 1
)
java -jar target\library-management-system.jar
