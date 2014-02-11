@echo off
setlocal
set STARTER_MAIN_CLASS=sorcer.launcher.Sorcer

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


IF NOT "%1"=="" (
	set CONFIG=%*
) ELSE (
    set CONFIG=-Pmix
)

echo ##############################################################################
echo ##                       SORCER OS Booter                                
echo ##   SORCER_HOME: %SORCER_HOME%
echo ##   Webster URL: %WEBSTER_URL%
echo ##   
echo ##############################################################################
echo .
pushd %SORCER_HOME%\bin
call %SOS_START_CMD%
popd
endlocal
