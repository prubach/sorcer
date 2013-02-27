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

# This script tests for the existence of environment variables JAVA_HOME, JINI_HOME and RIO_HOME

if [ -z "$JAVA_HOME" ] ; then
    echo "Set the JAVA_HOME environment variable to point to the location where you have installed Java"
    exit 1
fi

if [ -z "$JINI_HOME" ] ; then
    echo "Set the JINI_HOME environment variable to point to the location where you have installed Jini"
    exit 1
fi

if [ -z "$RIO_HOME" ] ; then
    echo "Set the RIO_HOME environment variable to point to the location where you have installed Rio"
    exit 1
fi

