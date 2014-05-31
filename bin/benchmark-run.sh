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
# This script expects the argument to be a path to run properties file which contains
# the list of remote nodes to start server on and the list of configurations.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-run.sh [PROPERTIES_FILE_PATH]"
    echo "Script that starts BenchmarkDriver on local machine."
    exit 1
fi

if [ "${CONFIG_INCLUDE}" == "" ]; then
    CONFIG_INCLUDE=${SCRIPT_DIR}/../config/benchmark.properties
    echo "Using default properties file: config/benchmark.properties"
fi

if [ ! -f $CONFIG_INCLUDE ]; then
    echo "ERROR: Properties file is not found."
    echo "Type \"--help\" for usage."
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
    echo "ERROR: Configurations (CONFIGS) are not defined in properties file."
    echo "Type \"--help\" for usage."
    exit 1
fi

if [[ ${CONFIG} != *'-of '* ]] && [[ ${CONFIG} != *'--outputFolder '* ]]; then
    folder=results-$(date +"%Y%m%d-%H%M%S")

    CONFIG="--outputFolder ${folder} ${CONFIG}"
fi

# JVM options.
JVM_OPTS=${JVM_OPTS}" -Dyardstick.bench"

export CP
export JVM_OPTS
export MAIN_CLASS=org.yardstickframework.BenchmarkDriverStartUp

/bin/bash ${SCRIPT_DIR}/benchmark-bootstrap.sh ${CONFIG} "--config" ${CONFIG_INCLUDE}

echo "<"$(date +"%H:%M:%S")"><yardstick> Benchmark execution finished."
