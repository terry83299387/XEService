@echo off
%~d0
cd "%~dp0%"
java -Djava.ext.dirs=libs;ext -Dlog4j.configurationFile=resources\log4j2.xml xextension.Main
pause
