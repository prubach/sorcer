:: Set Versions
set rioVersion=5.0-M4-S4
set groovyVersion=2.1.3

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe
goto endOfJavaHome

:noJavaHome
set JAVACMD=java.exe
:endOfJavaHome

SET mypath=%~dp0
SET SHOME_BIN=%mypath:~0,-1%
IF NOT DEFINED SORCER_HOME (
    IF EXIST %SHOME_BIN%\sorcer-boot.bat (
        SET SORCER_HOME=%SHOME_BIN%\..
    ) ELSE (
        ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
    )
)
IF NOT DEFINED SORCER_EXT SET SORCER_EXT=%SORCER_HOME%
IF NOT DEFINED RIO_HOME SET RIO_HOME=%SORCER_HOME%\lib\rio
rem read sorcer.env to get the local repo location
FOR /F "tokens=1,2 delims==" %%G IN (%SORCER_HOME%\configs\sorcer.env) DO (set %%G=%%H)
set MVN_REPO=%sorcer.local.repo.location%

set USER_HOME=%HOMEDRIVE%%HOMEPATH%

SETLOCAL EnableDelayedExpansion
set MVN_REPO=!MVN_REPO;${user.home}=%USER_HOME%!
set MVN_REPO=%MVN_REPO;/=\%
IF NOT DEFINED MVN_REPO SET MVN_REPO=%HOMEDRIVE%%HOMEPATH%\.m2\repository
ENDLOCAL & SET MVN_REPO=%MVN_REPO%

set LIB_DIR=%SORCER_HOME%\lib

IF EXIST %LIB_DIR%\sorcer\sorcer-api.jar (
   rem Distro
   set LOG_CP="%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\jul-to-slf4j.jar;%LIB_DIR%\commons\logback-classic.jar;%LIB_DIR%\commons\logback-core.jar"
   set JINI_BASE="%LIB_DIR%\jini\jsk-platform.jar;%LIB_DIR%\jini\jsk-lib.jar;%LIB_DIR%\jini-lookup\serviceui.jar"
   set JINI_START="%LIB_DIR%\river\start.jar"

   set JINI_CLASSPATH="%JINI_BASE%;%JINI_START%;%LOG_CP%"
   set BOOT_CLASSPATH="%JINI_CLASSPATH%"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\sorcer\sorcer-api.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\sorcer\sorcer-resolver.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\sorcer\sos-boot.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\sorcer\sos-util.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\sorcer\sos-webster.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\jini\jsk-resources.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\rio\rio-platform.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\rio\rio-logging-support.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\rio\rio-start.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\rio\rio-lib.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\rio-resolver\resolver-api.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\commons\groovy-all.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\commons\guava.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\commons\commons-lang3.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\commons\commons-io.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\..\configs"

   set SHELL_CLASSPATH="%JINI_BASE%;%LOG_CP%"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\rio\rio-platform.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\rio-resolver\resolver-api.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sorcer-api.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-shell.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-netlet.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-api.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-util.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-boot.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-platform.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\sorcer\sos-webster.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\commons\groovy-all.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\commons\commons-io.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%LIB_DIR%\commons\plexus-utils.jar"

   set SORCER_RIO_CP="%JINI_BASE%"
   set SORCER_RIO_CP="%SORCER_RIO_CP%%LIB_DIR%\sorcer\sorcer-api.jar"
   set SORCER_RIO_CP="%SORCER_RIO_CP%%LIB_DIR%\sorcer\sorcer-resolver.jar"
   set SORCER_RIO_CP="%SORCER_RIO_CP%;%LIB_DIR%\sorcer\sos-util.jar"
   set SORCER_RIO_CP="%SORCER_RIO_CP%;%LIB_DIR%\sorcer\sos-rio-start.jar"
   set SORCER_RIO_CP="%SORCER_RIO_CP%;%LIB_DIR%\sorcer\sos-boot.jar"
   set SORCER_RIO_CP="%SORCER_RIO_CP%;%LIB_DIR%\commons\commons-lang3.jar"

   set SORCER_RIOUI_CP="%SORCER_RIO_CP%"
) ELSE (
   rem Maven
   FOR /F "tokens=1,2 delims==" %%G IN (%SORCER_HOME%\configs\versions.properties) DO (set %%G=%%H)
   set LOG_CP="%MVN_REPO%\org\slf4j\slf4j-api\%v.slf4j%\slf4j-api-%v.slf4j%.jar"
   set LOG_CP="%LOG_CP%;%MVN_REPO%\org\slf4j\jul-to-slf4j\%v.slf4j%\jul-to-slf4j-%v.slf4j%.jar"
   set LOG_CP="%LOG_CP%;%MVN_REPO%\ch\qos\logback\logback-core\1.0.11\logback-core-1.0.11.jar"
   set LOG_CP="%LOG_CP%;%MVN_REPO%\ch\qos\logback\logback-classic\1.0.11\logback-classic-1.0.11.jar"
   set JINI_BASE="%MVN_REPO%\net\jini\jsk-platform\%v.jini%\jsk-platform-%v.jini%.jar;%MVN_REPO%\net\jini\jsk-lib\%v.jini%\jsk-lib-%v.jini%.jar;%MVN_REPO%\net\jini\lookup\serviceui\%v.jini%\serviceui-%v.jini%.jar"
   set JINI_START="%MVN_REPO%\org\apache\river\start\%v.jini%\start-%v.jini%.jar"

   set SORCER_COMMON="%MVN_REPO%\org\sorcersoft\sorcer\sorcer-api\%v.sorcer%\sorcer-api-%v.sorcer%.jar"
   set SORCER_COMMON="%SORCER_COMMON%\org\sorcersoft\sorcer\sorcer-resolver\%v.sorcer%\sorcer-resolver-%v.sorcer%.jar"
   set SORCER_COMMON="%SORCER_COMMON%;%MVN_REPO%\org\sorcersoft\sorcer\sos-boot\%v.sorcer%\sos-boot-%v.sorcer%.jar"
   set SORCER_COMMON="%SORCER_COMMON%;%MVN_REPO%\org\sorcersoft\sorcer\sos-util\%v.sorcer%\sos-util-%v.sorcer%.jar"

   set JINI_CLASSPATH="%JINI_BASE%;%JINI_START%;%LOG_CP%"

   set BOOT_CLASSPATH="%JINI_CLASSPATH%;%SORCER_COMMON%"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-webster\%v.sorcer%\sos-webster-%v.sorcer%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\net\jini\jsk-resources\%v.jini%\jsk-resources-%v.jini%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\resolver\resolver-api\%v.rio%\resolver-api-%v.rio%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-platform\%v.rio%\rio-platform-%v.rio%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-logging-support\%v.rio%\rio-logging-support-%v.rio%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-start\%v.rio%\rio-start-%v.rio%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-lib\%v.rio%\rio-lib-%v.rio%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\com\google\guava\guava\15.0\guava-15.0.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\org\apache\commons\commons-lang3\3.1\commons-lang3-3.1.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%MVN_REPO%\commons-io\commons-io\2.4\commons-io-2.4.jar"
   set BOOT_CLASSPATH="%BOOT_CLASSPATH%;%LIB_DIR%\..\configs"

   set SHELL_CLASSPATH="%JINI_BASE%;%LOG_CP%;%SORCER_COMMON%"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\rioproject\resolver\resolver-api\%v.rio%\resolver-api-%v.rio%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-platform\%v.rio%\rio-platform-%v.rio%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-shell\%v.sorcer%\sos-shell-%v.sorcer%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-netlet\%v.sorcer%\sos-netlet-%v.sorcer%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-api\%v.sorcer%\sos-api-%v.sorcer%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-platform\%v.sorcer%\sos-platform-%v.sorcer%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-webster\%v.sorcer%\sos-webster-%v.sorcer%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\codehaus\plexus\plexus-utils\%v.plexus%\plexus-utils-%v.plexus%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar"
   set SHELL_CLASSPATH="%SHELL_CLASSPATH%;%MVN_REPO%\commons-io\commons-io\2.4\commons-io-2.4.jar"

   set SORCER_RIO_CP="%SORCER_COMMON%;%JINI_BASE%"
   set SORCER_RIO_CP="%SORCER_RIO_CP%;%MVN_REPO%\org\sorcersoft\sorcer\sos-rio-start\%v.sorcer%\sos-rio-start-%v.sorcer%.jar"
   set SORCER_RIO_CP="%SORCER_RIO_CP%;%MVN_REPO%\org\apache\commons\commons-lang3\3.1\commons-lang3-3.1.jar"

   set SORCER_RIOUI_CP="%SORCER_RIO_CP%"
)
 
rem Determine webster url
IF "%provider.webster.interface%"=="${localhost}" (
   SET provider.webster.interface=%COMPUTERNAME%
)

IF DEFINED %SORCER_WEBSTER_INTERFACE% IF DEFINED %SORCER_WEBSTER_PORT% (
   SET WEBSTER_URL=http://%SORCER_WEBSTER_INTERFACE%:%SORCER_WEBSTER_PORT%
) ELSE
   SET WEBSTER_URL=http://%provider.webster.interface%:%provider.webster.port%
)

set JAVA_OPTS=-Dsun.net.maxDatagramSockets=1024

REM Turn on debugging if DEBUG is set in env
IF DEFINED DEBUG (
  SET JAVA_OPTS=%JAVA_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000
) 

set SOS_START_CMD=java %JAVA_OPTS% -classpath "%BOOT_CLASSPATH%" -Dlogback.configurationFile="%SORCER_HOME%/configs/rio/logging/logback.groovy" -Dsorcer.env.file="%SORCER_HOME%\configs\sorcer.env" -Djava.net.preferIPv4Stack=true -Djava.security.policy=%SORCER_HOME%/configs/sorcer.policy -Djava.protocol.handler.pkgs="net.jini.url|sorcer.util.bdb|org.rioproject.url" -Djava.rmi.server.RMIClassLoaderSpi=org.rioproject.rmi.ResolvingLoader -Djava.rmi.server.useCodebaseOnly=false -Dwebster.tmp.dir=%SORCER_HOME%/databases -Dprogram.name=SORCER -Dsorcer.home=%SORCER_HOME% -DRIO_HOME=%RIO_HOME% %STARTER_MAIN_CLASS% %CONFIG%


:: Call the Sorcer installer to install Sorcer jars to local repo
set SOS_INST_CP=-cp "%LIB_DIR%\sorcer\sos-boot.jar;%LIB_DIR%\sorcer\sorcer-api.jar;%LIB_DIR%\sorcer\sos-util.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\slf4j-simple.jar;%LIB_DIR%\commons\commons-io.jar;%LIB_DIR%\commons\xercesImpl.jar;%LIB_DIR%\commons\xml-apis.jar"
set RIO_INST=-cp "%LIB_DIR%\commons\groovy-all.jar"

if exist %LIB_DIR%\sorcer\sos-boot.jar if not exist "%SORCER_HOME%\logs\sorcer_jars_installed_user_%USERNAME%.tmp" (
    :: Call the install script, do not assume that Groovy has been installed.
    rem set GCP=-cp "%RIO_HOME%\lib\groovy-all-%groovyVersion%.jar"
	"%JAVACMD%" %RIO_INST% org.codehaus.groovy.tools.GroovyStarter --main groovy.ui.GroovyMain "%RIO_HOME%\..\..\configs\rio\install.groovy" "%JAVA_HOME%" "%RIO_HOME%"
    "%JAVACMD%" %SOS_INST_CP% sorcer.installer.Installer
	set SOS_INST=
	set RIO_INST=
)

rem ECHO %WEBSTER_URL%
rem ECHO %BOOT_CLASSPATH%
 
