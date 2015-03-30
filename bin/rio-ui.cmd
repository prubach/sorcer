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

::
:: Launches the Rio UI
::

title Rio UI
set command_line=%*

if "%RIO_HOME%" == "" set RIO_HOME=%~dp0..\lib\rio
set rioVersion=5.1.4

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
set rioVersion=%v.rio%

set RIOUI_CLASSPATH=
set RIOUI_CLASSPATH="%SORCER_RIOUI_CP%;%RIO_HOME%\lib\rio-ui-%rioVersion%.jar"
set MAIN_CLASS=org.rioproject.tools.ui.Main
rem echo %RIOUI_CLASSPATH%

call "%JAVACMD%" %JAVA_OPTS% -cp %RIOUI_CLASSPATH% -DRIO_HOME="%RIO_HOME%" -Dorg.rioproject.tools.ui.logDir="%RIO_HOME%\..\..\logs" -Djava.security.policy="%RIO_HOME%\..\..\configs\rio\rio.policy" -Djava.rmi.server.useCodebaseOnly=false -Djava.protocol.handler.pkgs=org.rioproject.url %MAIN_CLASS% %command_line%
