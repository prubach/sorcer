#!/bin/bash

RET=0
POS=0
while [ ${RET} -eq 0 ]; do
    mvn -Prun-its -Dtest=ArithmeticExertleterTest test
    RET=$?
    POS=`expr $POS + 1`
    echo "Run $POS"
done

kdialog --title syslog --passivepopup "TEST FAILED after $POS runs" 2
