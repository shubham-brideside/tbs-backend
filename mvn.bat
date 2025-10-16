@echo off
REM Maven wrapper batch file for Windows
REM This file helps run Maven commands when Maven is not in PATH

if exist "mvnw.cmd" (
    call mvnw.cmd %*
) else (
    echo Maven wrapper not found. Please install Maven or use the Maven wrapper.
    echo Download Maven from: https://maven.apache.org/download.cgi
    pause
)
