@echo off
pushd
set STARTER_MAIN_CLASS=sorcer.boot.ServiceStarter
set CONFIG=..\configs\sorcer-boot.config

IF defined SORCER_HOME ( 
  call %SORCER_HOME%\bin\common-run.bat
) ELSE (
  if exist %CD%\common-run.bat (
    call common-run.bat
  ) ELSE (
    call %CD%\bin\common-run.bat
  )
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
