@echo off

echo    ------------------------------------
echo.
echo          XEService installation
echo.
echo    ------------------------------------
echo.

reg add HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Run /v XEService /d "%~dp0%XEService.exe"

%~d0
cd "%~dp0%"
start XEService.exe

pause
