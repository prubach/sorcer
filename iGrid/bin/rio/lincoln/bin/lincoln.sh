#!/bin/bash

#/*
# 
# Copyright 2005 Sun Microsystems, Inc.
# Copyright 2005 GigaSpaces, Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# 	http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
#*/

# This script starts a non-persistent Lincoln using the start-lincoln.config configuration file found in
# $RIO_UTILS/configs.

# Check environment
if [ -f "./envcheck.sh" ] ; then
    ./envcheck.sh
    if [ $? != "0" ] ; then
        exit $? 
    fi
fi

if [ -z "$RIO_HOME" ] ; then
    echo "Set the RIO_HOME environment variable to point to the location where you have installed Rio"
    exit 1
fi

classpath="-cp $RIO_HOME/lib/boot.jar:$JINI_HOME/lib/start.jar"
launchTarget=com.sun.jini.start.ServiceStarter

$JAVA_HOME/bin/java -server $classpath -Djava.security.policy=$RIO_UTILS/policy/policy.all -Djava.protocol.handler.pkgs=net.jini.url -DRIO_UTILS=$RIO_UTILS -DRIO_HOME=$RIO_HOME -DJINI_HOME=$JINI_HOME $launchTarget $RIO_UTILS/configs/start-lincoln.config 
