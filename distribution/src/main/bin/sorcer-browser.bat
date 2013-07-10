@echo off
pushd
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
echo ##                       SORCER Browser                                
echo ##   SORCER_HOME: %SORCER_HOME%
echo ##   RIO_HOME   : %RIO_HOME%
echo ##   Webster URL: %WEBSTER_URL%
echo ##   
echo ##############################################################################
echo .


set STARTER_MAIN_CLASS=sorcer.ssb.SorcerServiceBrowser
set CONFIG=..\configs\browser\configs\ssb.config
set BROWSER_CLASSPATH=%JINI_CLASSPATH%;%LIB_DIR%\sorcer\browser.jar;%LIB_DIR%\sorcer\sos-env.jar;%LIB_DIR%\sorcer\sorcer-api.jar;%LIB_DIR%\sorcer\sos-util.jar;%LIB_DIR%\sorcer\sos-platform.jar;%LIB_DIR%\sorcer\sos-webster.jar;%LIB_DIR%\rio-resolver\resolver-api.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\logback-core.jar;%LIB_DIR%\commons\logback-classic.jar;%LIB_DIR%\rio\rio-platform.jar;%LIB_DIR%\commons\groovy-all.jar;%LIB_DIR%\commons\jsc-admin.jar
cd %SORCER_HOME%\bin
CALL java %JAVA_OPTS% -classpath %BROWSER_CLASSPATH% -Dssb.logFile=..\configs\browser\logs\browser.log -Dssb.logLen=300 -Djava.net.preferIPv4Stack=true -Djava.protocol.handler.pkgs=net.jini.url -Djava.security.policy=%SORCER_HOME%\configs\browser\policy\ssb.policy  -Djava.rmi.server.useCodebaseOnly=false -Dprogram.name=Browser %STARTER_MAIN_CLASS% %CONFIG% %*
popd
