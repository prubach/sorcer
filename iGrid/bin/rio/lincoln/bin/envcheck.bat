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

if "%JAVA_HOME%" == "" goto noJavaHome
if "%JINI_HOME%" == "" goto noJiniHome
if "%RIO_HOME%" == "" goto noRioHome
goto end

:noJavaHome
echo Set the JAVA_HOME environment variable to point to the location where you have installed Java
goto exitWithError

:noJiniHome
echo Set the JINI_HOME environment variable to point to the location where you have installed Jini
goto exitWithError

:noRioHome
echo Set the RIO_HOME environment variable to point to the location where you have installed Rio
goto exitWithError


:exitWithError
exit /B 1

:end
exit /B 0
