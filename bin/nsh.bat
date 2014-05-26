@echo off
pushd
SET mypath=%~dp0
SET SHOME_BIN=%mypath:~0,-1%
IF NOT DEFINED SORCER_HOME (
    IF EXIST "%SHOME_BIN%\sorcer-boot.bat" (
        SET "SORCER_HOME=%SHOME_BIN%\.."
    ) ELSE (
        ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
    )
)

IF NOT DEFINED SORCER_HOME ( 
  if exist "%CD%\common-run.bat" (
    call common-run.bat
  ) ELSE (
    call "%CD%\bin\common-run.bat"
  )
) ELSE (
  call "%SORCER_HOME%\bin\common-run.bat"
)
rem Use SORCER default if still not found
IF NOT DEFINED NSH_CONF SET "NSH_CONF=%SORCER_HOME%\configs\shell\configs\nsh-start.config"
rem Use the user nsh start-config file if exists.
IF EXIST "%HOMEDRIVE%%HOMEPATH%\.nsh\configs\nsh-start.config" SET "NSH_CONF=%HOMEDRIVE%%HOMEPATH%\.nsh\configs\nsh-start.config"

set STARTER_MAIN_CLASS=sorcer.tools.shell.NetworkShell
set SHELL_CLASS=sorcer.tools.shell.NetworkShell
rem echo java %JAVA_OPTS% -classpath "%SHELL_CLASSPATH%" -Djava.net.preferIPv4Stack=true -Djava.security.policy="%SORCER_HOME%\configs\shell\policy\shell.policy" -Djava.protocol.handler.pkgs="net.jini.url|sorcer.util.bdb|org.rioproject.url" -Djava.rmi.server.RMIClassLoaderSpi=sorcer.rio.rmi.SorcerResolvingLoader -Djava.rmi.server.useCodebaseOnly=false -Dprogram.name=NSH -Dsorcer.home="%SORCER_HOME%" -Dnsh.starter.config=%NSH_CONF% %STARTER_MAIN_CLASS% %*
CALL java %JAVA_OPTS% -classpath "%SHELL_CLASSPATH%" -Djava.security.policy="%SORCER_HOME%\configs\shell\policy\shell.policy" -Dprogram.name=NSH -Dnsh.starter.config="%NSH_CONF%" %STARTER_MAIN_CLASS% %*
rem --classpath "%CP%"
popd

