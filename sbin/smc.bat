@echo off
bash -c "export SORCER_HOME2=`cygpath \"%SORCER_HOME%\"`; source $SORCER_HOME2/configs/minClasspath;export CLASSPATH=${CLASSPATH_WIN};smc %*"

