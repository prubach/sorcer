@rem pushd
@rem /*
@rem 
@rem Copyright 2005 Sun Microsystems, Inc.
@rem 
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem 
@rem 	http://www.apache.org/licenses/LICENSE-2.0
@rem 
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem 
@rem */
@echo off
SET mypath=%~dp0
SET SHOME_BIN=%mypath:~0,-1%
IF NOT DEFINED SORCER_HOME (
	echo sh not set
    IF EXIST "%SHOME_BIN%\sorcer-boot.bat" (
        SET SORCER_HOME=%SHOME_BIN%\..
    ) ELSE (
        ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
		EXIT
    )
)
IF DEFINED SORCER_HOME (
rem	SET FOUND=
rem	SET PROG=sorcer-boot.bat
rem	FOR %%i IN ("%PATH:;=";"%") DO IF EXIST %%i\%PROG% SET FOUND=%%i  
rem	IF NOT DEFINED FOUND (
		SET "PATH=%SORCER_HOME%\bin;%PATH%"
rem	)
)

SET FOUND=
SET PROG=mvn.bat
FOR %%i IN ("%PATH:;=";"%") DO IF EXIST %%i\%PROG% SET FOUND=%%i  
IF NOT DEFINED FOUND (
    SET M2_HOME=%SORCER_HOME%\lib\apache-maven
    SET "PATH=%M2_HOME%\bin;%PATH%"
)
SET FOUND=
SET PROG=ant.bat
FOR %%i IN ("%PATH:;=";"%") DO IF EXIST %%i\%PROG% SET FOUND=%%i  
IF NOT DEFINED FOUND (
    SET ANT_HOME=%SORCER_HOME%\lib\apache-ant
    SET "PATH=%ANT_HOME%\bin;%PATH%"
)

rem This script sets the environment needed to run commands in this 
rem Sorcer distribution.
rem
rem Instructions
rem -------------
rem Set JAVA_HOME to the location where Java is installed
rem Set SORCER_HOME to the directory where Sorcer is installed.
rem
rem Run this command file :
rem      > setenv.bat
echo ##############################################################################
echo ##                       SORCER Examples
echo ##   SORCER_HOME: %SORCER_HOME%
echo ##
echo ##   To build and run examples you have to have the following tools installed:
echo ##
echo ##   - Java Full JDK (http://java.oracle.com)
echo ##   - Apache Maven (http://maven.apache.org)
echo ##   - Apache Ant (http://ant.apache.org)
echo ##
echo ##   When you have them installed and available in your PATH:
echo ##   1. Before building examples for the first time please run:
echo ##      prepare-repository.bat
echo ##   2. Then run: mvn install to build the examples.
echo ##   3. If all builds succeed you are ready to go.
echo ##      You can start providers by executing:
echo ##      boot :ex0-cfg or
echo ##      boot ex0/ex0-cfg/target/ex0-cfg.jar
echo ##      or
echo ##      look for xml ant scripts to start providers
echo ##      and requestors.
echo ##   7. You can start them by running:
echo ##      ant -f script.xml
echo ##
echo ##############################################################################
echo .
call %windir%\System32\cmd.exe
popd
