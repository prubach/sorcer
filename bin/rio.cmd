@echo off
::
:: Copyright to the original author or authors.
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::      http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::

:: This script provides the command and control utility for starting
:: Rio services and the Rio command line interface

:: Use local variables
setLocal EnableDelayedExpansion

:: Sorcer basic jars added to classpath
SET mypath=%~dp0
SET SHOME_BIN=%mypath:~0,-1%
IF NOT DEFINED SORCER_HOME (
    IF EXIST "%SHOME_BIN%\sorcer-boot.bat" (
        SET "SORCER_HOME=%SHOME_BIN%\.."
    ) ELSE (
        ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
    )
)

IF defined SORCER_HOME (
  call "%SORCER_HOME%\bin\common-run.bat"
) ELSE (
  if exist "%CD%\common-run.bat" (
    call common-run.bat
  ) ELSE (
    call "%CD%\bin\common-run.bat"
  )
)

set SLF4J_CLASSPATH=%LOG_CP%;%MVN_REPO%\org\rioproject\rio-logging-support\%v.rio%\rio-logging-support-%v.rio%.jar
set rioVersion=%v.rio%

:: Set local variables
if "%RIO_HOME%" == "" set RIO_HOME=%~dp0..\rio-%rioVersion%

set RIO_LIB=%RIO_HOME%\lib



if "%JAVA_MEM_OPTIONS%" == "" set JAVA_MEM_OPTIONS="-XX:MaxPermSize=256m"

:: Parse command line
if "%1"=="" goto interactive
if "%1"=="start" goto start
if "%1"=="create-project" goto create-project

:interactive
:: set cliExt="%RIO_HOME%"\..\..\configs\rio\rio_cli.groovy
:: set cliExt=""
set command_line=%*
set launchTarget=org.rioproject.tools.cli.CLI
set classpath=-cp "%RIO_HOME%\lib\rio-cli-%rioVersion%.jar";"%SLF4J_CLASSPATH%";"%SORCER_RIO_CP%"
set props="-DRIO_HOME=%RIO_HOME%"
"%JAVACMD%" %JAVA_OPTS% %classpath% -Xms256m -Xmx256m -Djava.protocol.handler.pkgs=org.rioproject.url -DRIO_HOME="%RIO_HOME%" -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy="%RIO_HOME%\..\configs\rio\rio.policy" %launchTarget% %cliExt% %command_line%
goto end

:create-project
mvn archetype:generate -DarchetypeGroupId=org.rioproject -DarchetypeGroupId=org.rioproject -DarchetypeRepository=http://www.rio-project.org/maven2 -DarchetypeVersion=5.1
goto end

:start

:: Get the service starter
shift
if "%1"=="" goto noService
set service=%1
set starterConfig="%RIO_HOME%\..\configs\rio\start-%1.groovy"
if not exist "%starterConfig%" goto noStarter
shift

:: Call the install script, do not assume that Groovy has been installed.
set groovyClasspath=-cp "%RIO_HOME%\lib\groovy-all-%groovyVersion%.jar"
"%JAVA_HOME%\bin\java" %groovyClasspath% org.codehaus.groovy.tools.GroovyStarter --main groovy.ui.GroovyMain "%RIO_HOME%\..\configs\rio\install.groovy" "%JAVA_HOME%" "%RIO_HOME%"

:: Call the Sorcer installer to install Sorcer jars to local repo
set SOS_INST_CP=-cp "%LIB_DIR%\sorcer\lib\sos-boot-%v.sorcer%.jar;%LIB_DIR%\sorcer\lib\sos-util-%v.sorcer%.jar;%LIB_DIR%\common\slf4j-api-%v.slf4j%.jar;%LIB_DIR%\common\slf4j-simple-%v.slf4j%.jar;%LIB_DIR%\common\commons-io-2.4.jar;%LIB_DIR%\common\xercesImpl-2.6.2.jar;
:: %LIB_DIR%\common\xml-apis-1..jar"
if exist %LIB_DIR%\sorcer\lib\sorcer-api-%v.sorcer%.jar if not exist "%SORCER_HOME%\logs\sorcer_jars_installed.tmp" (
    "%JAVACMD%" %SOS_INST_CP% sorcer.installer.Installer
)

echo starter config [%starterConfig%]
set RIO_LOG_DIR="%RIO_HOME%"\..\..\logs\
if "%RIO_NATIVE_DIR%" == "" set RIO_NATIVE_DIR="%RIO_HOME%"\lib\native
set PATH=%PATH%;"%RIO_NATIVE_DIR%"

set classpath=-cp "%RIO_HOME%\lib\rio-start-%rioVersion%.jar";"%JAVA_HOME%\lib\tools.jar";"%RIO_HOME%\lib\groovy-all-%groovyVersion%.jar";"%SLF4J_CLASSPATH%";"%SORCER_RIO_CP%"
set agentpath=-javaagent:"%RIO_HOME%\lib\rio-start-%rioVersion%.jar"

set launchTarget=org.rioproject.start.ServiceStarter

set loggingConfig="%RIO_HOME%\..\..\configs\rio\logging\rio-logging.properties"

"%JAVA_HOME%\bin\java" -server %JAVA_MEM_OPTIONS% %classpath% %agentpath% -Djava.protocol.handler.pkgs=org.rioproject.url -Djava.rmi.server.useCodebaseOnly=false -Djava.util.logging.config.file=%loggingConfig% -Dorg.rioproject.service=%service% %USER_OPTS% -Djava.security.policy="%RIO_HOME%"\..\configs\rio\rio.policy -Djava.library.path=%RIO_NATIVE_DIR% -DRIO_HOME="%RIO_HOME%" -Dorg.rioproject.home="%RIO_HOME%" -DRIO_NATIVE_DIR=%RIO_NATIVE_DIR% -DRIO_LOG_DIR=%RIO_LOG_DIR% -Drio.script.mainClass=%launchTarget% %launchTarget% "%starterConfig%"
goto end

:noStarter
echo Cannot locate expected service starter file [start-%1.config] in [%RIO_HOME%\..\configs\rio], exiting"
goto exitWithError

:noService
echo "A service to start is required, exiting"

:exitWithError
exit /B 1

:end
endlocal
title Command Prompt
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
exit /B 0
