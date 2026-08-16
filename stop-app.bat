@echo off
title PetCare Booking Platform Teardown
echo ========================================================
echo   Stopping PetCare Booking Platform Services
echo ========================================================
echo.

cd /d "%~dp0"

:: Stop Spring Boot Backend process (port 8080)
echo [1/3] Terminating Spring Boot Backend...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /f /pid %%a >nul 2>&1

:: Stop Node / Vite Frontend process (port 5173)
echo [2/3] Terminating Vite Dev Server...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":5173" ^| findstr "LISTENING"') do taskkill /f /pid %%a >nul 2>&1

:: Stop PostgreSQL Docker Container
echo [3/3] Stopping Docker Containers...
docker compose down

echo.
echo ========================================================
echo   All services stopped successfully.
echo ========================================================
echo.
pause