@echo off
pushd
SET mypath=%~dp0
SET SHOME_BIN=%mypath:~0,-1%
IF NOT DEFINED SORCER_HOME (
    IF EXIST "%SHOME_BIN%\sorcer-boot.bat" (
        SET "SORCER_HOME=%SHOME_BIN%\.."
    ) ELSE (
        ECHO Problem setting SORCER_HOME, please set this variable and point it to the main SORCER installation directory!
    )
)
IF defined SORCER_HOME ( 
  call "%SORCER_HOME%\bin\common-run.bat"
) ELSE (
  if exist "%CD%\common-run.bat" (
    call common-run.bat
  ) ELSE (
    call "%CD%\bin\common-run.bat"
  )
)


rem Workaround an issue with maven archetype crawl that creates the archetype-catalog.xml file inside the repository instead of the .m2 directory
if not exist "%SORCER_HOME%\logs\sorcer_archetype_installed_user_%USERNAME%.tmp" (
    call mvn archetype:crawl -Dcatalog="%HOMEDRIVE%%HOMEPATH%\.m2\archetype-catalog.xml"
    copy /y NUL "%SORCER_HOME%\logs\sorcer_archetype_installed_user_%USERNAME%.tmp" >NUL
)

CALL mvn archetype:generate -DarchetypeGroupId=org.sorcersoft.sorcer -DarchetypeArtifactId=sorcer-provider
popd
