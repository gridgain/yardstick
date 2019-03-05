#!/bin/bash

#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

#
# Script that starts BenchmarkServers.
#

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

source "${SCRIPT_DIR}"/functions.sh

#
# Discover path to Java executable and check it's version.
#

ARGS=$*

CP=${CP}":${SCRIPT_DIR}/../libs/*"

#
# Assertions are disabled by default.
# If you want to enable them - set 'ENABLE_ASSERTIONS' flag to '1'.
#
ENABLE_ASSERTIONS="0"

#
# Set '-ea' options if assertions are enabled.
#
if [ "${ENABLE_ASSERTIONS}" = "1" ]; then
    JVM_OPTS="${JVM_OPTS} -ea"
fi

export JAVA

"$JAVA" ${JVM_OPTS} -cp ${CP} org.yardstickframework.runners.ServerRunner -sd $SCRIPT_DIR ${ARGS}
