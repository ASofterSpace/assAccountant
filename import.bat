@echo off

java -classpath "%~dp0\bin" -Xms16m -Xmx1024m com.asofterspace.accountant.AssAccountant drop_bank_statements import_bank_statements import

pause
