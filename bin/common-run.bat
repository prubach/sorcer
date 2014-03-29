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

FOR /F "tokens=1,2 delims==" %%G IN (%SORCER_HOME%\configs\versions.properties) DO (set %%G=%%H)

set LOG_CP=
set LOG_CP=%MVN_REPO%\org\slf4j\slf4j-api\%v.slf4j%\slf4j-api-%v.slf4j%.jar
set LOG_CP=%LOG_CP%;%MVN_REPO%\org\slf4j\jul-to-slf4j\%v.slf4j%\jul-to-slf4j-%v.slf4j%.jar
set LOG_CP=%LOG_CP%;%MVN_REPO%\ch\qos\logback\logback-core\%v.logback%\logback-core-%v.logback%.jar
set LOG_CP=%LOG_CP%;%MVN_REPO%\ch\qos\logback\logback-classic\%v.logback%\logback-classic-%v.logback%.jar

set JINI_BASE=
set JINI_BASE=%MVN_REPO%\net\jini\jsk-platform\%v.jini%\jsk-platform-%v.jini%.jar;
set JINI_BASE=%JINI_BASE%;%MVN_REPO%\net\jini\jsk-lib\%v.jini%\jsk-lib-%v.jini%.jar;
set JINI_BASE=%JINI_BASE%;%MVN_REPO%\net\jini\lookup\serviceui\%v.jini%\serviceui-%v.jini%.jar
set JINI_BASE=%JINI_BASE%;%MVN_REPO%\net\jini\jsk-resources\%v.jini%\jsk-resources-%v.jini%.jar
set JINI_START=
set JINI_START=%MVN_REPO%\org\apache\river\start\%v.jini%\start-%v.jini%.jar

set SORCER_COMMON=
set SORCER_COMMON=%MVN_REPO%\org\sorcersoft\sorcer\sorcer-api\%v.sorcer%\sorcer-api-%v.sorcer%.jar
set SORCER_COMMON=%SORCER_COMMON%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-resolver\%v.sorcer%\sorcer-resolver-%v.sorcer%.jar
set SORCER_COMMON=%SORCER_COMMON%;%MVN_REPO%\org\sorcersoft\sorcer\sos-boot\%v.sorcer%\sos-boot-%v.sorcer%.jar
set SORCER_COMMON=%SORCER_COMMON%;%MVN_REPO%\org\sorcersoft\sorcer\sos-util\%v.sorcer%\sos-util-%v.sorcer%.jar

set JINI_CLASSPATH=
set JINI_CLASSPATH=%JINI_BASE%;%JINI_START%;%LOG_CP%

set BOOT_CLASSPATH=
set BOOT_CLASSPATH=%JINI_CLASSPATH%;%SORCER_COMMON%
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-start\%v.sorcer%\sos-start-%v.sorcer%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-launcher-base\%v.sorcer%\sorcer-launcher-base-%v.sorcer%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-installer\%v.sorcer%\sorcer-installer-%v.sorcer%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-launcher\%v.sorcer%\sorcer-launcher-%v.sorcer%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-rio-start\%v.sorcer%\sorcer-rio-start-%v.sorcer%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-launcher\%v.sorcer%\sorcer-launcher-%v.sorcer%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\resolver\resolver-api\%v.rio%\resolver-api-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\resolver\resolver-aether\%v.rio%\resolver-aether-%v.rio%.jar
::set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\net\jini\jsk-resources\%v.jini%\jsk-resources-%v.jini%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-platform\%v.rio%\rio-platform-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-logging-support\%v.rio%\rio-logging-support-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-start\%v.rio%\rio-start-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-lib\%v.rio%\rio-lib-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-api\%v.rio%\rio-api-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-proxy\%v.rio%\rio-proxy-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\rioproject\monitor\monitor-api\%v.rio%\monitor-api-%v.rio%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\com\google\guava\guava\15.0\guava-15.0.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\apache\commons\commons-lang3\3.2.1\commons-lang3-3.2.1.jar
::set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\codehaus\plexus\plexus-utils\%v.plexus%\plexus-utils-%v.plexus%.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\commons-cli\commons-cli\1.2\commons-cli-1.2.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\commons-io\commons-io\2.4\commons-io-2.4.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\com\google\inject\guice\4.0-beta4\guice-4.0-beta4.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\aopalliance\aopalliance\1.0\aopalliance-1.0.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\javax\inject\javax.inject\1\javax.inject-1.jar
set BOOT_CLASSPATH=%BOOT_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sigar\sigar\%v.sigar%\sigar-%v.sigar%.jar

set SHELL_CLASSPATH=
set SHELL_CLASSPATH=%JINI_BASE%;%LOG_CP%;%SORCER_COMMON%
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\rioproject\resolver\resolver-api\%v.rio%\resolver-api-%v.rio%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-platform\%v.rio%\rio-platform-%v.rio%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-rio-start\%v.sorcer%\sorcer-rio-start-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-shell\%v.sorcer%\sos-shell-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-netlet\%v.sorcer%\sos-netlet-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-api\%v.sorcer%\sos-api-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-platform\%v.sorcer%\sos-platform-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-webster\%v.sorcer%\sos-webster-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\dbp-api\%v.sorcer%\dbp-api-%v.sorcer%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\codehaus\plexus\plexus-utils\%v.plexus%\plexus-utils-%v.plexus%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar
set SHELL_CLASSPATH=%SHELL_CLASSPATH%;%MVN_REPO%\commons-io\commons-io\2.4\commons-io-2.4.jar

set SORCER_RIO_CP=
set SORCER_RIO_CP=%SORCER_COMMON%;%JINI_BASE%
set SORCER_RIO_CP=%SORCER_RIO_CP%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-rio-start\%v.sorcer%\sorcer-rio-start-%v.sorcer%.jar
set SORCER_RIO_CP=%SORCER_RIO_CP%;%MVN_REPO%\org\apache\commons\commons-lang3\3.2.1\commons-lang3-3.2.1.jar

set BROWSER_CLASSPATH=
set BROWSER_CLASSPATH=%JINI_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-api\%v.sorcer%\sorcer-api-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-platform\%v.sorcer%\sos-platform-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-util\%v.sorcer%\sos-util-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\browser\%v.sorcer%\browser-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-webster\%v.sorcer%\sos-webster-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-netlet\%v.sorcer%\sos-netlet-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\dbp-api\%v.sorcer%\dbp-api-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sos-api\%v.sorcer%\sos-api-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-resolver\%v.sorcer%\sorcer-resolver-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-ui\%v.sorcer%\sorcer-ui-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\rioproject\resolver\resolver-api\%v.rio%\resolver-api-%v.rio%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-api\%v.rio%\rio-api-%v.rio%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\rioproject\rio-platform\%v.rio%\rio-platform-%v.rio%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-rio-start\%v.sorcer%\sorcer-rio-start-%v.sorcer%.jar
set BROWSER_CLASSPATH=%BROWSER_CLASSPATH%;%MVN_REPO%\org\codehaus\plexus\plexus-utils\%v.plexus%\plexus-utils-%v.plexus%.jar


set SORCER_RIOUI_CP=%SORCER_RIO_CP%;%MVN_REPO%\org\slf4j\jul-to-slf4j\%v.slf4j%\jul-to-slf4j-%v.slf4j%.jar
 
rem Determine webster url
IF "%provider.webster.interface%"=="${localhost}" (
   SET provider.webster.interface=%COMPUTERNAME%
)

IF DEFINED %SORCER_WEBSTER_INTERFACE% IF DEFINED %SORCER_WEBSTER_PORT% (
   SET WEBSTER_URL=http://%SORCER_WEBSTER_INTERFACE%:%SORCER_WEBSTER_PORT%
) ELSE
   SET WEBSTER_URL=http://%provider.webster.interface%:%provider.webster.port%
)

IF NOT DEFINED RIO_HOME SET RIO_HOME=%SORCER_HOME%\lib\rio
set JAVA_OPTS=
set JAVA_OPTS=%JAVA_OPTS% -Dsun.net.maxDatagramSockets=1024
set JAVA_OPTS=%JAVA_OPTS% -Dsorcer.env.file="%SORCER_HOME%"\configs\sorcer.env
rem set JAVA_OPTS=%JAVA_OPTS% -Djava.net.preferIPv4Stack=true
set JAVA_OPTS=%JAVA_OPTS% -Djava.security.policy="%SORCER_HOME%"\configs\sorcer.policy
set JAVA_OPTS=%JAVA_OPTS% "-Djava.protocol.handler.pkgs=net.jini.url|sorcer.util.bdb|org.rioproject.url"
set JAVA_OPTS=%JAVA_OPTS% -Djava.rmi.server.RMIClassLoaderSpi=sorcer.rio.rmi.SorcerResolvingLoader
set JAVA_OPTS=%JAVA_OPTS% -Djava.rmi.server.useCodebaseOnly=false
set JAVA_OPTS=%JAVA_OPTS% -Dwebster.tmp.dir="%SORCER_HOME%"\databases
set JAVA_OPTS=%JAVA_OPTS% -Dsorcer.home="%SORCER_HOME%"
set JAVA_OPTS=%JAVA_OPTS% -DRIO_HOME="%RIO_HOME%"
set JAVA_OPTS=%JAVA_OPTS% -Dorg.rioproject.resolver.jar="%MVN_REPO%"\org\rioproject\resolver\resolver-aether\%v.rio%\resolver-aether-%v.rio%.jar
set JAVA_OPTS=%JAVA_OPTS% -Dlogback.configurationFile="%SORCER_HOME%"\configs\logback.groovy

REM Turn on debugging if DEBUG is set in env
IF DEFINED DEBUG (
  SET JAVA_OPTS=%JAVA_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000
) 
rem echo %JAVA_OPTS%
rem echo %BOOT_CLASSPATH%
set SOS_START_CMD="%JAVACMD%" %JAVA_OPTS% -Dprogram.name=SORCER -classpath "%BOOT_CLASSPATH%" %STARTER_MAIN_CLASS% %CONFIG%

rem echo %SOS_START_CMD%
:: Call the Sorcer installer to install Sorcer jars to local repo
set INST_CP=
set INST_CP=%LIB_DIR%\sorcer\sorcer-installer.jar
set INST_CP=%INST_CP%;%LIB_DIR%\sorcer\sorcer-resolver.jar
set INST_CP=%INST_CP%;%LIB_DIR%\sorcer\sorcer-api.jar
set INST_CP=%INST_CP%;%LIB_DIR%\sorcer\sos-util.jar
set INST_CP=%INST_CP%;%LIB_DIR%\commons\slf4j-api.jar
set INST_CP=%INST_CP%;%LIB_DIR%\commons\slf4j-simple.jar
set INST_CP=%INST_CP%;%LIB_DIR%\commons\commons-io.jar
set INST_CP=%INST_CP%;%LIB_DIR%\commons\xercesImpl.jar
set INST_CP=%INST_CP%;%LIB_DIR%\commons\xml-apis.jar
set INST_CP=%INST_CP%;%LIB_DIR%\rio-resolver\resolver-aether.jar
set INST_CP=%INST_CP%;%LIB_DIR%\rio-resolver\resolver-api.jar
if exist "%LIB_DIR%\sorcer\sorcer-api.jar" if not exist "%SORCER_HOME%\logs\sorcer_jars_installed_user_%USERNAME%.tmp" (
	call "%JAVACMD%" -cp "%INST_CP%" sorcer.installer.Installer
)
rem echo %SOS_START_CMD%
rem ECHO %WEBSTER_URL%
rem ECHO %BOOT_CLASSPATH%
