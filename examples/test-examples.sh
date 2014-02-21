#!/bin/bash

EX_DIR=$SORCER_HOME/examples
LOG_DIR=/tmp/logs/

_mkdir () {
	if [ ! -e  $1 ]; then
		mkdir $1
	fi
}

stopSorcer ( ) {
  for p in `jps | grep Sorcer | cut -d " " -f 1`; do
    kill -9 $p > /dev/null
    if [ "$USE_RIO" == "1" ]; then
        if [ -f $SORCER_HOME/logs/all-$p.log ]; then
            mv $SORCER_HOME/logs/all-$p.log $1/
        fi
    fi

  done
}

startSorcer ( ) {
if [ "$USE_RIO" == "1" ]; then
  $SORCER_HOME/bin/sorcer-boot -Prio 2>&1 > $1 &
  sleep 32
  #$SORCER_HOME/bin/rio deploy $SORCER_HOME/configs/SorcerBoot.groovy > $1_deploy &
  #sleep 13
else
  $SORCER_HOME/bin/sorcer-boot 2>&1 > $1 &
  #ant -f $SORCER_HOME/bin/sorcer-boot.xml > $1 &
  sleep 15
fi
}

restartSorcer ( ) {
    if [ "$USE_RIO" != "1" ]; then
      stopSorcer  $2
      startSorcer $1
    fi
}

showExceptions ( ) {
  cd $LOG_DIR
  grep -R -n -A 5 Exception: > exceptions.txt
  grep -R -n -B 10 FAILURE >> exceptions.txt
  cat exceptions.txt
}

cleanLogs ( ) {
  rm -rf $LOG_DIR
  mkdir $LOG_DIR  
}

ex0 ( ) {
  EX=ex0
  _mkdir $LOG_DIR/$EX
  restartSorcer $LOG_DIR/$EX/socer-$EX$1.log $LOG_DIR/$EX/
  cd $SORCER_HOME
  if [ "$1" == "rio" ]; then
    $SORCER_HOME/bin/rio deploy $SORCER_HOME/examples/ex0/ex0-cfg/src/main/resources/opstring.groovy
  else
    $SORCER_HOME/bin/sorcer-boot :ex0-cfg > $LOG_DIR/$EX/ex0-prv-run.log &
    #$SORCER_HOME/bin/sorcer-boot :ex0-cfg 2>&1 > $LOG_DIR/$EX/ex0-prv-run.log &
  fi
  sleep 8
  cd $EX_DIR/$EX/$EX-req/
  ant -f run.xml > $LOG_DIR/$EX/req$1.log
  ./run.ntl >> $LOG_DIR/$EX/req$1.log
  cd $SORCER_HOME
}

ex6 ( ) {
  TYPE=$1
  EX=ex6
  _mkdir $LOG_DIR/$EX
  restartSorcer $LOG_DIR/$EX/socer-$EX-$TYPE.log $LOG_DIR/$EX/

  cd $SORCER_HOME
  if [ "$1" == "rio" ]; then
    $SORCER_HOME/bin/rio deploy $SORCER_HOME/examples/ex6/ex6-cfg-all/src/main/resources/AllEx6Boot.groovy
  elif [ "$1" == "prov" ]; then
    $SORCER_HOME/bin/sorcer-boot :ex6-cfg-adder 2>&1 > $LOG_DIR/$EX/adder-arithmetic.log &
    $SORCER_HOME/bin/sorcer-boot :ex6-cfg-multiplier 2>&1 > $LOG_DIR/$EX/multiplier-arithmetic.log &
    $SORCER_HOME/bin/sorcer-boot :ex6-cfg-subtractor 2>&1 > $LOG_DIR/$EX/subtractor-arithmetic.log &
    $SORCER_HOME/bin/sorcer-boot :ex6-cfg-divider 2>&1 > $LOG_DIR/$EX/divider-arithmetic.log &
  else
      $SORCER_HOME/bin/sorcer-boot :ex6-cfg-$TYPE > $LOG_DIR/$EX/$TYPE-arithmetic.log &
  fi
  cd $EX_DIR/$EX/$EX-req/
  ant -f arithmetic-ter-run.xml > $LOG_DIR/$EX/$TYPE-arithmetic-ter-run.log &
  sleep 8
  mvn -Dmaven.test.skip=false -DskipTests=false test > $LOG_DIR/$EX/$TYPE-req.log 
  ant -f f5-req-run.xml > $LOG_DIR/$EX/$TYPE-f5-req.log
  ant -f f5a-req-run.xml >> $LOG_DIR/$EX/$TYPE-f5a-req.log
  ant -f f5m-req-run.xml >> $LOG_DIR/$EX/$TYPE-f5m-req.log
  ant -f f5pull-req-run.xml >> $LOG_DIR/$EX/$TYPE-f5pull-req.log
  ant -f f5xP-req-run.xml >> $LOG_DIR/$EX/$TYPE-f5xP-req.log
  ant -f f5xS-req-run.xml >> $LOG_DIR/$EX/$TYPE-f5xS-req.log
  ant -f f1-req-run.xml > $LOG_DIR/$EX/$TYPE-f1-req.log
  ant -f f1-PAR-pull-run.xml >> $LOG_DIR/$EX/$TYPE-f1-PAR-req.log
  ant -f f1-SEQ-pull-run.xml >> $LOG_DIR/$EX/$TYPE-f1-SEQ-req.log
  nsh f1.ntl > $LOG_DIR/$EX/$TYPE-ntl-req.log
  nsh f1a.ntl > $LOG_DIR/$EX/$TYPE-ntl-a-req.log
}

if [ "$1" == "exc" ]; then
  showExceptions
  exit
fi

if [ "$1" == "rio" ]; then
  USE_RIO=1
fi

cleanLogs
startSorcer $LOG_DIR/sorcer.log
ex0
if [ "$1" == "rio" ]; then
  ex0 rio
fi
ex6 all
ex6 one-bean
ex6 prov
if [ "$1" == "rio" ]; then
  ex6 rio
fi

stopSorcer
showExceptions
