@echo off

echo    ------------------------------------
echo.
echo          XEService installation
echo.
echo    ------------------------------------
echo.

:addregister
echo 正在安装...
echo.
reg add HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Run /v XEService /d "%~dp0%XEService.exe"
if %errorlevel% equ 0 (
	goto startxex
)

:addstartup
set xpzh="%userprofile%\「开始」菜单\程序\启动\"
set xpen="%userprofile%\Start Menu\Programs\Startup\"
set win7="%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\"
REM set win7AllUsers="C:\ProgramData\Microsoft\Windows\Start Menu\Programs\StartUp\"
if exist %xpzh% (
	set pth="%xpzh%"
) else (
	if exist %xpen% (
		set pth="%xpen%"
	) else (
		if exist %win7% (
			set pth="%win7%"
		) else (
			echo 找不到启动文件夹，安装失败
			goto end
		)
	)
)
set pth=%pth%xextension.bat
echo @echo off > "%pth%"
echo %~d0 >> "%pth%"
echo cd "%~dp0" >> "%pth%"
echo start XEService.exe >> "%pth%"
if not exist "%pth%" (
	echo 无法写入启动脚本，安装失败
	goto end
)

:startxex
echo.
echo 正在启动...
echo.
%~d0
cd "%~dp0%"
start XEService.exe
if %errorlevel% equ 0 (
	echo 安装成功，XeXtension正在运行
) else (
	echo 启动XeXtension失败
)
echo.

:end
pause
