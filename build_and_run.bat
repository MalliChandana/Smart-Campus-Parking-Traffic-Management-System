@echo off
title Smart Campus Parking and Traffic Management System
echo ===============================================================================
echo     SMART CAMPUS PARKING AND TRAFFIC MANAGEMENT SYSTEM
echo ===============================================================================
echo.

if not exist "bin" mkdir "bin"

echo [1/2] Compiling Java source files with MySQL Connector/J...
javac -cp "lib/mysql-connector-j-8.3.0.jar;src" -d bin src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please check your JDK installation.
    pause
    exit /b %ERRORLEVEL%
)

echo [OK] Compilation successful.
echo.
echo [2/2] Launching Desktop GUI Application...
echo.
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" Main

pause
