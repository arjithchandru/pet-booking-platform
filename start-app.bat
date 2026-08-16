@echo off
title PetCare Booking Platform - Pre-flight & Launcher
echo ========================================================
echo   PetCare Booking Platform - Build, Test & Launch
echo ========================================================
echo.

:: 1. Navigate to script root directory
cd /d "%~dp0"

:: 2. Start PostgreSQL Docker Container (Required for Integration Tests)
echo [1/5] Starting PostgreSQL Container for tests & app...
docker compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start Docker. Please ensure Docker Desktop is running.
    pause
    exit /b %errorlevel%
)
echo PostgreSQL is up.
echo.

:: 3. Run Automated Test Suite
echo [2/5] Running Backend Test Suite (Unit & Integration Tests)...
cd backend
call mvn test "-Dsurefire.useFile=false"
if %errorlevel% neq 0 (
    echo.
    echo ========================================================
    echo [TEST FAILURE] One or more automated tests failed!
    echo Application startup aborted to prevent unstable execution.
    echo ========================================================
    cd ..
    pause
    exit /b 1
)
cd ..
echo.
echo [OK] All tests passed successfully!
echo.

:: 4. Launch Spring Boot Backend in a separate window
echo [3/5] Starting Spring Boot Backend API (port 8080)...
start "PetCare - Backend (Port 8080)" cmd /k "cd /d "%~dp0backend" && mvn spring-boot:run"

:: 5. Launch React / Vite Frontend in a separate window
echo [4/5] Starting Vite Dev Server (port 5173)...
start "PetCare - Frontend (Port 5173)" cmd /k "cd /d "%~dp0frontend" && npm run dev"

:: 6. Open Browser
echo [5/5] Launching Browser...
timeout /t 5 /nobreak >nul
start http://localhost:5173

echo.
echo ========================================================
echo   Services are running and verified!
echo   - Backend:  http://localhost:8080/swagger-ui.html
echo   - Frontend: http://localhost:5173
echo ========================================================
echo.
pause