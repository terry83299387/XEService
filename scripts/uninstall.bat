@echo off

reg delete HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Run /v XEService /f

set scriptfilename=xextension.bat
set xpzh="%userprofile%\����ʼ���˵�\����\����\"
set xpen="%userprofile%\Start Menu\Programs\Startup\"
set win7="%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\"
if exist %xpzh%%scriptfilename% (rm -f %xpzh%%scriptfilename%) else (
	if exist %xpen%%scriptfilename% (rm -f %xpen%%scriptfilename%) else (
		if exist %win7%%scriptfilename% (rm -f %win7%%scriptfilename%)
	)
)

taskkill /f /im XEService.exe

pause
