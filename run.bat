@echo off

start "assAccountant" javaw -classpath "%~dp0\bin" -Xms16m -Xmx1024m com.asofterspace.assAccountant.Main %*

pause
