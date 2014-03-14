@echo off
pushd
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
rem Use SORCER default if still not found
IF NOT DEFINED NSH_CONF SET NSH_CONF="%SORCER_HOME%\configs\shell\configs\nsh-start.config"
rem Use the user nsh start-config file if exists.
IF EXIST "%HOMEDRIVE%%HOMEPATH%\.nsh\configs\nsh-start.config" SET NSH_CONF="%HOMEDRIVE%%HOMEPATH%\.nsh\configs\nsh-start.config"

set STARTER_MAIN_CLASS=sorcer.tools.shell.ShellStarter

rem set TTT=
rem IF "%1" == "-cp" set TTT=1
rem IF "%1" == "-classpath" set TTT=1
rem IF "%1" == "--classpath" set TTT=1
rem IF DEFINED TTT (
rem   set CP=%2
rem )

rem IF NOT DEFINED CP ( 
rem   IF DEFINED CLASSPATH ( 
rem    SET CP=%CLASSPATH%
rem  ) ELSE (
rem    SET CP=.
rem  )
rem rem)
rem echo %CP%
 
set SHELL_CLASS=sorcer.tools.shell.NetworkShell

CALL java %JAVA_OPTS% -classpath "%SHELL_CLASSPATH%" -Djava.net.preferIPv4Stack=true -Djava.security.policy="%SORCER_HOME%\configs\shell\policy\shell.policy" -Djava.protocol.handler.pkgs="net.jini.url|sorcer.util.bdb|org.rioproject.url" -Djava.rmi.server.RMIClassLoaderSpi=sorcer.rio.rmi.SorcerResolvingLoader -Djava.rmi.server.useCodebaseOnly=false -Dprogram.name=NSH -Dsorcer.home="%SORCER_HOME%" -Dnsh.starter.config="%NSH_CONF%" %STARTER_MAIN_CLASS% --main %SHELL_CLASS% --config "%NSH_CONF%" %*
rem --classpath "%CP%"
popd

