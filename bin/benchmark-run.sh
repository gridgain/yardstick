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
# Script that starts BenchmarkDriver on local machine.
# This script expects first argument to be a path to run properties file which contains
# the list of remote nodes to start server on and the list of configurations.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "" ]; then
    echo $0", ERROR:"
    echo "Configuration file should be the first script argument."
    exit 1
fi

if [ ! -f $CONFIG_INCLUDE ]; then
    echo $0", ERROR:"
    echo "Configuration file not found."
    exit 1
fi

shift

CONFIG_TMP=`mktemp tmp.XXXXXXXX`

cp $CONFIG_INCLUDE $CONFIG_TMP
chmod +x $CONFIG_TMP

. $CONFIG_TMP
rm $CONFIG_TMP

if [ "${CONFIG}" == "" ]; then
    IFS=',' read -ra cfg <<< "${CONFIGS}"

    if ((${#cfg[@]} > 0)); then
        CONFIG=${cfg[0]}
    fi
else
    CONFIG="$CONFIG $*"
fi

if [ "${CONFIG}" == "" ]; then
    echo $0", ERROR:"
    echo "Config is not defined."
    exit 1
fi

if [[ ${CONFIG} = *' -of '* ]] || [[ ${CONFIG} = *' --outputFolder '* ]]; then
    CONFIG = ${CONFIG} --outputFolder results-$(date +"%Y-%m-%d_%H-%M-%S.%3N")
fi

# JVM options.
JVM_OPTS=${JVM_OPTS}" -Dyardstick.bench"

export CP
export JVM_OPTS
export MAIN_CLASS=org.yardstick.BenchmarkDriverStartUp

/bin/bash ${SCRIPT_DIR}/benchmark-bootstrap.sh ${CONFIG} "--config" ${CONFIG_INCLUDE}

echo "Benchmark execution finished."
