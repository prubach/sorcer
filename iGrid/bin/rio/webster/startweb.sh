#!/bin/bash

#/*
# 
# Copyright 2005 Sun Microsystems, Inc.
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

# This scripts starts a Webster process set the serve up code using the org.jini.rio.tools.webster.root system
# property

# Check environment
if [ -x "./envcheck.sh" ] ; then 
    ./envcheck.sh
     if [ $? != "0" ] ; then
         exit $?
     fi
fi

JINI_LIB=$JINI_HOME/lib
XML=$RIO_HOME/xml

$JAVA_HOME/bin/java -jar -Djava.protocol.handler.pkgs=net.jini.url -Dorg.jini.rio.tools.webster.debug -Dorg.jini.rio.tools.webster.root="$RIO_HOME/lib;$JINI_HOME;$JINI_LIB;$XML" -Dorg.jini.rio.tools.webster.port=9000 -Djava.security.policy=$RIO_HOME/configs/sorcer.policy $RIO_HOME/lib/webster.jar




