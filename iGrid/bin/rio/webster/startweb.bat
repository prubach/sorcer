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

rem This scripts starts a Webster process set the serve up code using the org.jini.rio.tools.webster.root system
rem property

set JINI_LIB=%JINI_HOME%\lib
set JINI_LIB_DL=%JINI_HOME%\lib-dl
set RIO_SUBS_LIB=%RIO_SUBSTRATES_HOME%\lib
set RIO_UTILS_LIB=%RIO_UTILS%\lib
set SORCER_LIB=%SORCER_HOME%\lib

start "Rio Webster" /min "%JAVA_HOME%\bin\java" -hotspot -jar -Djava.protocol.handler.pkgs=net.jini.url -Dorg.jini.rio.tools.webster.debug -Dorg.jini.rio.tools.webster.root=%RIO_HOME%\lib;%JINI_HOME%;%JINI_LIB%;%JINI_LIB_DL%;%RIO_SUBS_LIB%;%RIO_UTILS_LIB%;%SORCER_LIB%; -Dorg.jini.rio.tools.webster.port=9000 -Djava.security.policy=%RIO_HOME%\policy\sorcer.policy %RIO_HOME%\lib\webster.jar
