#!/bin/sh

. ${SORCER_HOME}/bin/common-run

dir=`dirname $0`
jar=`find ${dir}/target -name ex0-deploy*jar`

boot_opstring $jar
