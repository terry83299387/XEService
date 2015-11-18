@echo off

reg delete HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Run /v XEService /f

taskkill /f /im XEService.exe

pause
