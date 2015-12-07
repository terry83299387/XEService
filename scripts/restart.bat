@echo off
taskkill /f /im XEService.exe

%~d0
cd "%~dp0%"
start XEService.exe
pause
