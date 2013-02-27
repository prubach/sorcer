@echo on

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

rem This script starts an instance of the Operational String Monitor using a configuration file. 

if exist envcheck.bat call envcheck.bat

if errorlevel 1 goto end

setlocal

set classpath=-cp %RIO_UTILS%\lib\opmon.jar;%RIO_HOME%\lib\rio.jar;%JINI_HOME%\lib\jsk-platform.jar;%JINI_HOME%\lib\jsk-lib.jar;
set launchTarget=org.jini.rio.utilities.opmon.OpMonitor

start "Opmon" /min "%JAVA_HOME%\bin\java" -hotspot %classpath% -Djava.protocol.handler.pkgs=net.jini.url %launchTarget% %IGRID_HOME%\bin\rio\opmon\configs\utils.config

endlocal

:end
