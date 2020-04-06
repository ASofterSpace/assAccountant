@echo off

java -classpath "%~dp0\bin" -Xms16m -Xmx1024m com.asofterspace.accountant.Main %*

pause
