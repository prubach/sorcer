@echo off
pushd
IF defined SORCER_HOME ( 
  call "%SORCER_HOME%\bin\common-run.bat"
) ELSE (
  if exist "%CD%\common-run.bat" (
    call common-run.bat
  ) ELSE (
    call "%CD%\bin\common-run.bat"
  )
)

echo ##############################################################################
echo ##                       SORCER Browser                                
echo ##   SORCER_HOME: %SORCER_HOME%
echo ##   RIO_HOME   : %RIO_HOME%
echo ##   Webster URL: %WEBSTER_URL%
echo ##   
echo ##############################################################################
echo .


set STARTER_MAIN_CLASS=sorcer.ssb.SorcerServiceBrowser
set CONFIG=..\configs\browser\configs\ssb.config
rem set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%LIB_DIR%\jini-lookup\jmx-lookup.jar
rem set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%LIB_DIR%\commons\jsc-admin.jar
cd "%SORCER_HOME%\bin"
CALL java %JAVA_OPTS% -classpath "%BROWSER_CLASSPATH%" -Dssb.logFile=..\configs\browser\logs\browser.log -Dssb.logLen=300 -Djava.protocol.handler.pkgs="net.jini.url|sorcer.util.url|org.rioproject.url" -Djava.security.policy="%SORCER_HOME%\configs\browser\policy\ssb.policy" -Dwebster.internal="true" -Dwebster.tmp.dir="%SORCER_HOME%\databases" -Dprogram.name=Browser %STARTER_MAIN_CLASS% %CONFIG% %*
popd
