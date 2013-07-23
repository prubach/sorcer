IF NOT DEFINED SORCER_HOME (
    IF EXIST %CD%\bin\sorcer-boot.bat (
        SET SORCER_HOME=%CD%
    ) ELSE (
        IF EXIST %CD%\sorcer-boot.bat (
            SET SORCER_HOME=%CD%\..
        ) ELSE (
            ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
        )
    )
)
IF NOT DEFINED RIO_HOME SET RIO_HOME=%SORCER_HOME%\lib\rio
rem read sorcer.env to get the local repo location
FOR /F "tokens=1,2 delims==" %%G IN (%SORCER_HOME%\configs\sorcer.env) DO (set %%G=%%H)
set MVN_REPO=%sorcer.local.repo.location%

set USER_HOME=%HOMEDRIVE%%HOMEPATH%

SETLOCAL EnableDelayedExpansion
set MVN_REPO=!MVN_REPO;${user.home}=%USER_HOME%!
set MVN_REPO=%MVN_REPO;/=\%
rem echo %MVN_REPO%
IF NOT DEFINED MVN_REPO SET MVN_REPO=%HOMEDRIVE%%HOMEPATH%\.m2\repository
ENDLOCAL & SET MVN_REPO=%MVN_REPO%

set LIB_DIR=%SORCER_HOME%\lib

IF EXIST %LIB_DIR%\sorcer\sos-env.jar (
   rem Distro
   set "JINI_CLASSPATH=%LIB_DIR%\jini\jsk-platform.jar;%LIB_DIR%\jini\jsk-lib.jar;%LIB_DIR%\river\start.jar;%LIB_DIR%\jini-lookup\serviceui.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\logback-classic.jar;%LIB_DIR%\commons\logback-core.jar"
   set "BOOT_CLASSPATH=%LIB_DIR%\jini\jsk-platform.jar;%LIB_DIR%\jini\jsk-lib.jar;%LIB_DIR%\river\start.jar;%LIB_DIR%\jini-lookup\serviceui.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\logback-classic.jar;%LIB_DIR%\commons\logback-core.jar;%LIB_DIR%\sorcer\sorcer-api.jar;%LIB_DIR%\sorcer\sos-env.jar;%LIB_DIR%\sorcer\sos-util.jar;%LIB_DIR%\sorcer\sos-boot.jar;%LIB_DIR%\sorcer\sos-webster.jar;%LIB_DIR%\sorcer\sos-rio-start.jar;%LIB_DIR%\jini\jsk-resources.jar;%LIB_DIR%\rio\rio-platform.jar;%LIB_DIR%\rio\rio-logging-support.jar;%LIB_DIR%\rio\rio-start.jar;%LIB_DIR%\rio-resolver\resolver-api.jar;%LIB_DIR%\rio\start.jar;%LIB_DIR%\commons\groovy-all.jar;%LIB_DIR%\commons\guava.jar;%LIB_DIR%\commons\commons-lang3.jar;%LIB_DIR%\..\configs"
   set "SHELL_CLASSPATH=%LIB_DIR%\sorcer\sorcer-api.jar;%LIB_DIR%\sorcer\sos-shell.jar;%LIB_DIR%\sorcer\sos-netlet.jar;%LIB_DIR%\sorcer\sos-env.jar;%LIB_DIR%\sorcer\sos-api.jar;%LIB_DIR%\sorcer\sos-util.jar;%LIB_DIR%\sorcer\sos-boot.jar;%LIB_DIR%\sorcer\sos-platform.jar;%LIB_DIR%\sorcer\sos-webster.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\logback-core.jar;%LIB_DIR%\commons\logback-classic.jar;%LIB_DIR%\commons\groovy-all.jar;%LIB_DIR%\jini\jsk-platform.jar;%LIB_DIR%\jini\jsk-lib.jar;%LIB_DIR%\jini-lookup\serviceui.jar"
) ELSE (
   rem Maven
   FOR /F "tokens=1,2 delims==" %%G IN (%SORCER_HOME%\configs\versions.properties) DO (set %%G=%%H)
   set "JINI_CLASSPATH=%MVN_REPO%\net\jini\jsk-platform\%v.jini%\jsk-platform-%v.jini%.jar;%MVN_REPO%\net\jini\jsk-lib\%v.jini%\jsk-lib-%v.jini%.jar;%MVN_REPO%\org\apache\river\start\%v.jini%\start-%v.jini%.jar;%MVN_REPO%\net\jini\lookup\serviceui\%v.jini%\serviceui-%v.jini%.jar;%MVN_REPO%\org\slf4j\slf4j-api\1.7.5\slf4j-api-1.7.5.jar;%MVN_REPO%\ch\qos\logback\logback-core\1.0.11\logback-core-1.0.11.jar;%MVN_REPO%\ch\qos\logback\logback-classic\1.0.11\logback-classic-1.0.11.jar"
   set "BOOT_CLASSPATH=%MVN_REPO%\net\jini\jsk-platform\%v.jini%\jsk-platform-%v.jini%.jar;%MVN_REPO%\net\jini\jsk-lib\%v.jini%\jsk-lib-%v.jini%.jar;%MVN_REPO%\org\apache\river\start\%v.jini%\start-%v.jini%.jar;%MVN_REPO%\net\jini\lookup\serviceui\%v.jini%\serviceui-%v.jini%.jar;%MVN_REPO%\org\slf4j\slf4j-api\1.7.5\slf4j-api-1.7.5.jar;%MVN_REPO%\ch\qos\logback\logback-core\1.0.11\logback-core-1.0.11.jar;%MVN_REPO%\ch\qos\logback\logback-classic\1.0.11\logback-classic-1.0.11.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-boot\%v.sorcer%\sos-boot-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-env\%v.sorcer%\sos-env-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-api\%v.sorcer%\sorcer-api-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-util\%v.sorcer%\sos-util-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-webster\%v.sorcer%\sos-webster-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-rio-start\%v.sorcer%\sos-rio-start-%v.sorcer%.jar;%MVN_REPO%\net\jini\jsk-resources\%v.jini%\jsk-resources-%v.jini%.jar;%MVN_REPO%\org\rioproject\resolver\resolver-api\%v.rio%\resolver-api-%v.rio%.jar;%MVN_REPO%\org\rioproject\rio-platform\%v.rio%\rio-platform-%v.rio%.jar;%MVN_REPO%\org\rioproject\rio-logging-support\%v.rio%\rio-logging-support-%v.rio%.jar;%MVN_REPO%\org\rioproject\rio-start\%v.rio%\rio-start-%v.rio%.jar;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar;%MVN_REPO%\com\google\guava\14.0.1\guava-14.0.1.jar;%MVN_REPO%\org\apache\commons\commons-lang3\3.1\commons-lang3-3.1.jar;%MVN_REPO%\com\google\collections\google-collections\1.0\google-collections-1.0.jar;%LIB_DIR%\..\configs"
   set "SHELL_CLASSPATH=%MVN_REPO%\org\sorcersoft\sorcer\sos-shell\%v.sorcer%\sos-shell-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sorcer-api\%v.sorcer%\sorcer-api-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-netlet\%v.sorcer%\sos-netlet-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-env\%v.sorcer%\sos-env-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-api\%v.sorcer%\sos-api-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-util\%v.sorcer%\sos-util-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-boot\%v.sorcer%\sos-boot-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-platform\%v.sorcer%\sos-platform-%v.sorcer%.jar;%MVN_REPO%\org\sorcersoft\sorcer\sos-webster\%v.sorcer%\sos-webster-%v.sorcer%.jar;%MVN_REPO%\org\slf4j\slf4j-api\1.7.5\slf4j-api-1.7.5.jar;%MVN_REPO%\ch\qos\logback\logback-classic\1.0.11\logback-classic-1.0.11.jar;%MVN_REPO%\ch\qos\logback\logback-core\1.0.11\logback-core-1.0.11.jar;%MVN_REPO%\org\codehaus\groovy\groovy-all\%v.groovy%\groovy-all-%v.groovy%.jar;%MVN_REPO%\net\jini\jsk-platform\%v.jini%\jsk-platform-%v.jini%.jar;%MVN_REPO%\net\jini\jsk-lib\%v.jini%\jsk-lib-%v.jini%.jar;%MVN_REPO%\net\jini\lookup\serviceui\%v.jini%\serviceui-%v.jini%.jar"
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

REM Turn on debugging if DEBUG is set in env
IF DEFINED DEBUG (
  SET JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000
) 

set SOS_START_CMD=java %JAVA_OPTS% -classpath "%BOOT_CLASSPATH%" -Djava.net.preferIPv4Stack=true -Djava.security.policy=%SORCER_HOME%/configs/sorcer.policy -Djava.protocol.handler.pkgs="net.jini.url|sorcer.util.bdb.sos|org.rioproject.url" -Djava.rmi.server.useCodebaseOnly=false -Dwebster.tmp.dir=%SORCER_HOME%/databases -Dprogram.name=SORCER -Dsorcer.home=%SORCER_HOME% %STARTER_MAIN_CLASS% %CONFIG%

rem ECHO %WEBSTER_URL%
rem ECHO %BOOT_CLASSPATH%
 
