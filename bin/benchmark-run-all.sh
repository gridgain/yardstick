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
# Script that starts BenchmarkServers on remote machines, runs BenchmarkDriver and stops the servers on remote machines.
# This procedure is performed for all configurations defined in run properties file.
# This script expects the argument to be a path to run properties file which contains
# the list of remote nodes to start server on and the list of configurations.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-run-all.sh [PROPERTIES_FILE_PATH]"
    echo "Script that executes BenchmarkDriver locally and BenchmarkServers on remote machines."
    exit 1
fi

if [ "${CONFIG_INCLUDE}" == "" ]; then
    CONFIG_INCLUDE=${SCRIPT_DIR}/../config/benchmark.properties
    echo "<"$(date +"%H:%M:%S")"><yardstick> Using default properties file: config/benchmark.properties"
fi

if [ ! -f $CONFIG_INCLUDE ]; then
    echo "ERROR: Properties file not found."
    echo "Type \"--help\" for usage."
    exit 1
fi

CONFIG_TMP=`mktemp tmp.XXXXXXXX`

cp $CONFIG_INCLUDE $CONFIG_TMP
chmod +x $CONFIG_TMP

. $CONFIG_TMP
rm $CONFIG_TMP

if [ "${CONFIGS}" == "" ]; then
    echo "ERROR: Configurations (CONFIGS) are not defined in properties file."
    echo "Type \"--help\" for usage."
    exit 1
fi

folder=results-$(date +"%Y%m%d-%H%M%S")

export LOGS_BASE=logs-$(date +"%Y%m%d-%H%M%S")

if [ -z "$RESTART_SERVERS" ]; then
    /bin/bash ${SCRIPT_DIR}/benchmark-servers-start.sh ${CONFIG_INCLUDE}

    sleep 3s
fi

IFS=',' read -ra configs0 <<< "${CONFIGS}"
for cfg in "${configs0[@]}";
do
    CONFIG=${cfg}

    if [[ ${CONFIG} != *'-of '* ]] && [[ ${CONFIG} != *'--outputFolder '* ]]; then
        OUTPUT_FOLDER="--outputFolder ${folder}"
    fi

    export CONFIG
    export OUTPUT_FOLDER

    if [ -n "$RESTART_SERVERS" ]; then
        /bin/bash ${SCRIPT_DIR}/benchmark-servers-start.sh ${CONFIG_INCLUDE}

        sleep 3s
    fi

    /bin/bash ${SCRIPT_DIR}/benchmark-drivers-start.sh ${CONFIG_INCLUDE}

    if [ -n "$RESTART_SERVERS" ]; then
        /bin/bash ${SCRIPT_DIR}/benchmark-servers-stop.sh ${CONFIG_INCLUDE}

        sleep 1s
    fi
done

if [ -z "$RESTART_SERVERS" ]; then
    /bin/bash ${SCRIPT_DIR}/benchmark-servers-stop.sh ${CONFIG_INCLUDE}

    sleep 1s
fi
