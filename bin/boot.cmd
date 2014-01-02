@echo off
pushd
set STARTER_MAIN_CLASS=sorcer.boot.ServiceStarter
SET CONFIG=%1
SET mypath=%~dp0
SET SHOME_BIN=%mypath:~0,-1%
IF NOT DEFINED SORCER_HOME (
    IF EXIST %SHOME_BIN%\sorcer-boot.bat (
        SET SORCER_HOME=%SHOME_BIN%\..
    ) ELSE (
        ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
    )
)

IF defined SORCER_HOME ( 
  call %SORCER_HOME%\bin\common-run.bat
) ELSE (
  if exist %CD%\common-run.bat (
    call common-run.bat
  ) ELSE (
    call %CD%\bin\common-run.bat
  )
)

:: Call the Sorcer installer to install Sorcer jars to local repo
set SOS_INST_CP=-cp "%LIB_DIR%\sorcer\sos-env.jar;%LIB_DIR%\sorcer\sos-util.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\slf4j-simple.jar;%LIB_DIR%\commons\commons-io.jar;%LIB_DIR%\commons\xercesImpl.jar;%LIB_DIR%\commons\xml-apis.jar"
if not exist "%SORCER_HOME%\logs\sorcer_jars_installed.tmp" (
    "%JAVACMD%" %SOS_INST_CP% sorcer.installer.Installer
)

echo ##############################################################################
echo ##                       SORCER OS Booter                                
echo ##   SORCER_HOME: %SORCER_HOME%
echo ##   RIO_HOME   : %RIO_HOME%
echo ##   Webster URL: %WEBSTER_URL%
echo ##   
echo ##############################################################################
echo .
cd %SORCER_HOME%\bin
call %SOS_START_CMD%
popd
