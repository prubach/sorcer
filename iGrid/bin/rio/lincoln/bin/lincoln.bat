@echo off

@rem /*
@rem 
@rem Copyright 2005 Sun Microsystems, Inc.
@rem Copyright 2005 GigaSpaces, Inc.
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

rem This script starts a non-persistent Lincoln using the start-lincoln.config configuration file found in
rem %RIO_UTILS%\configs. 

if exist envcheck.bat call envcheck.bat

if errorlevel 1 goto end

if "%RIO_HOME" == "" goto homeEnvFailure

setlocal

set classpath=-cp %RIO_HOME%\lib\boot.jar;%JINI_HOME%\lib\start.jar;
set launchTarget=com.sun.jini.start.ServiceStarter

"%JAVA_HOME%\bin\java" -server %classpath% -Djava.security.policy=%RIO_UTILS%\policy\sorcer.policy -Djava.protocol.handler.pkgs=net.jini.url -DRIO_UTILS=%RIO_UTILS% -DRIO_HOME=%RIO_HOME% -DJINI_HOME=%JINI_HOME% %launchTarget% %RIO_UTILS%\configs\start-lincoln.config 

endlocal

:homeEnvFailure
echo "Set the RIO_HOME environment variable to point to the location where you have installed Rio 

:end

