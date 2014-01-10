@echo off
pushd
rem Workaround an issue with maven archetype crawl that creates the archetype-catalog.xml file inside the repository instead of the .m2 directory
call mvn archetype:crawl -Dcatalog="%HOMEDRIVE%%HOMEPATH%\.m2\archetype-catalog.xml"
popd
echo "Maven repository preparation finished! Press any key to continue!"
pause
