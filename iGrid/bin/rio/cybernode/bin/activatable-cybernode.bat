@echo off

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

rem This script starts a persistent and activatable Cybernode using the start-activatable-cybernode.config
rem configuration file found in %RIO_HOME%\configs.


if exist envcheck.bat call envcheck.bat

if errorlevel 1 goto end

setlocal

set RIO_LOG_DIR="%USERPROFILE%"\.rio\logs\
set PATH=%PATH%;"%USERPROFILE%"\.rio\native

set classpath=-cp %RIO_HOME%\lib\boot.jar;%JINI_HOME%\lib\start.jar;
set launchTarget=com.sun.jini.start.ServiceStarter

%JAVA_HOME%\bin\java -server %classpath% -Djava.protocol.handler.pkgs=net.jini.url -Djava.security.policy=%RIO_HOME%\policy\sorcer.policy -DRIO_HOME=%RIO_HOME% -DJINI_HOME=%JINI_HOME% -DRIO_LOG_DIR=%RIO_LOG_DIR% %launchTarget% %RIO_HOME%\configs\start-activatable-cybernode.config

endlocal

:end
