@echo off
setlocal enabledelayedexpansion

REM ========================================================
REM CareFirst Healthcare Management System - Share with Friends
REM Windows Batch + PowerShell Tunnel Launcher
REM ========================================================

set APP_PORT=8080
set HEALTH_URL=http://localhost:%APP_PORT%/health
set TARGET_DIR=target
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"
set APP_LOG=%TARGET_DIR%\app.log
set TUNNEL_LOG=%TARGET_DIR%\tunnel.log

echo =======================================================
echo 🏥 CareFirst Healthcare System - Friend Sharing Tunnel
echo =======================================================

REM 1. Check for cloudflared or ngrok
where cloudflared >nul 2>nul
if %errorlevel% equ 0 (
    set TUNNEL_TOOL=cloudflared
) else (
    where ngrok >nul 2>nul
    if %errorlevel% equ 0 (
        set TUNNEL_TOOL=ngrok
    ) else (
        echo ❌ Neither 'cloudflared' nor 'ngrok' was found in your PATH.
        echo.
        echo 👉 Install cloudflared in 5 seconds via winget:
        echo    winget install Cloudflare.cloudflared
        echo.
        echo 👉 Or install ngrok from: https://ngrok.com/download
        pause
        exit /b 1
    )
)

REM 2. Check if application is already healthy
powershell -Command "try { $r = Invoke-WebRequest -Uri '%HEALTH_URL%' -TimeoutSec 2 -UseBasicParsing; exit 0 } catch { exit 1 }"
if %errorlevel% equ 0 (
    echo ✅ Application is already running and healthy on port %APP_PORT%.
) else (
    echo 🚀 Starting CareFirst Healthcare Management System (mvn compile exec:java)...
    start /b "" cmd /c "mvn compile exec:java > %APP_LOG% 2>&1"

    echo ⏳ Waiting for application to initialize on %HEALTH_URL%...
    set HEALTHY=0
    for /l %%i in (1,1,45) do (
        powershell -Command "try { $r = Invoke-WebRequest -Uri '%HEALTH_URL%' -TimeoutSec 2 -UseBasicParsing; exit 0 } catch { exit 1 }"
        if !errorlevel! equ 0 (
            set HEALTHY=1
            goto app_ready
        )
        <nul set /p=.
        timeout /t 1 /nobreak >nul
    )
    :app_ready
    echo.
    if !HEALTHY! neq 1 (
        echo ❌ Application failed to become healthy within 45 seconds.
        echo Please check %APP_LOG%
        pause
        exit /b 1
    )
    echo ✅ CareFirst app is ready!
)

REM 3. Launch tunnel
echo 🌐 Launching secure HTTPS tunnel via !TUNNEL_TOOL!...
del /f /q "%TUNNEL_LOG%" 2>nul

if "!TUNNEL_TOOL!"=="cloudflared" (
    start /b "" cmd /c "cloudflared tunnel --url http://localhost:%APP_PORT% > %TUNNEL_LOG% 2>&1"
    
    echo ⏳ Fetching public trycloudflare.com link...
    set PUBLIC_URL=
    for /l %%i in (1,1,20) do (
        timeout /t 1 /nobreak >nul
        if exist "%TUNNEL_LOG%" (
            for /f "tokens=*" %%a in ('powershell -Command "Get-Content '%TUNNEL_LOG%' | Select-String -Pattern 'https://[a-zA-Z0-9-]+\.trycloudflare\.com' | ForEach-Object { $_.Matches.Value } | Select-Object -First 1"') do (
                set PUBLIC_URL=%%a
            )
            if defined PUBLIC_URL goto print_link
        )
    )

    :print_link
    if not defined PUBLIC_URL (
        echo ⚠️ Cloudflare tunnel started, check %TUNNEL_LOG% for the link.
    ) else (
        echo.
        echo =======================================================
        echo 🎉 YOUR TEMPORARY PUBLIC LINK IS LIVE!
        echo =======================================================
        echo.
        echo 🔗 Share this link with friends:
        echo    👉 !PUBLIC_URL!
        echo.
        echo 🔑 Demo Logins:
        echo    👑 Admin:   admin@carefirst.com         / Admin@123
        echo    🩺 Doctor:  doctor.sarah@carefirst.com  / Doctor@123
        echo    👤 Patient: john.doe@example.com        / Patient@123
        echo.
        echo 💡 Press [Ctrl + C] anytime to stop.
        echo =======================================================
    )
) else (
    start /b "" cmd /c "ngrok http %APP_PORT% > %TUNNEL_LOG% 2>&1"
    timeout /t 3 /nobreak >nul
    echo 🔗 Checking ngrok dashboard: http://127.0.0.1:4040
)

pause
