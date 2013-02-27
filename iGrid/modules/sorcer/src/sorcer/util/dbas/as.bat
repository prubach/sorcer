@echo off 
REM !!! Start ApplicationServer !!! 
set java_dir=www\java\jdk1.1.5\bin
set as_dir=www\java\mbpl\dbas

cd w:\

java mbpl.dbas.ApplicationServer -dir %as_dir%

