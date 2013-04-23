@echo off
::SORCER_HOME_CYG=cygdrive %SORCER_HOME%
::bash -c "export SORCER_HOME=`cygpath \"%SORCER_HOME%\"`; echo \"SORCER_HOME = $SORCER_HOME\";slp %*"
::bash -c "export SORCER_HOME=\"%SORCER_HOME%\"; echo \"SORCER_HOME = $SORCER_HOME\";slp %*"
bash -c "export SORCER_HOME2=`cygpath \"%SORCER_HOME%\"`; source $SORCER_HOME2/configs/minClasspath;export CLASSPATH=${CLASSPATH_WIN};spc %*"

