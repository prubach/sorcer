#!/bin/sh

. ${SORCER_HOME}/bin/common-run

main="`dirname $0`/../ex0-deployment"

POLICY_FILE=${main}/policy/prv.policy

boot_opstring `find ${main}/target -name ex0-deployment*jar`
