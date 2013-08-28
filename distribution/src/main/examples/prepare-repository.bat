@echo off
pushd
IF defined SORCER_HOME ( 
	set LIB_DIR=%SORCER_HOME%\lib    
) ELSE (
	set SORCER_HOME=%CD%\..
	set LIB_DIR=%CD%\..\lib
	rem call %SORCER_HOME%\bin\common-run.bat
)

echo ##############################################################################
echo ##                       SORCER Examples - prepare repository                                
echo ##   SORCER_HOME: %SORCER_HOME%
echo ##   
echo ##############################################################################
echo .

set INST_CLASSPATH=%LIB_DIR%\sorcer\sos-env.jar;%LIB_DIR%\sorcer\sos-util.jar;%LIB_DIR%\commons\slf4j-api.jar;%LIB_DIR%\commons\slf4j-simple.jar;%LIB_DIR%\commons\xercesImpl.jar;%LIB_DIR%\commons\xml-apis.jar;%LIB_DIR%\commons\commons-io.jar
call java -classpath "%INST_CLASSPATH%" sorcer.installer.Installer
rem Workaround an issue with maven archetype crawl that creates the archetype-catalog.xml file inside the repository instead of the .m2 directory
call mvn archetype:crawl -Dcatalog="%HOMEDRIVE%%HOMEPATH%\.m2\archetype-catalog.xml"

popd
echo "Maven repository preparation finished! Press any key to continue!"
pause
