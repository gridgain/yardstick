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
# Script that starts BenchmarkServers on a local machine.
# This script expects first argument to be a path to run properties file.
# Second argument is optional and defines number of starting servers.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-manual-servers-start.sh [PROPERTIES_FILE_PATH]"
    echo "Script that starts BenchmarkServers on a local machine."
    exit 1
fi

if [ "${CONFIG_INCLUDE}" == "" ]; then
    CONFIG_INCLUDE=${SCRIPT_DIR}/../config/benchmark.properties
    echo "<"$(date +"%H:%M:%S")"><yardstick> Using default properties file: config/benchmark.properties"
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

if [ -z "$1" ]; then
    if [ "${SERVER_HOSTS}" == "" ]; then
        SERVER_NODES=1
    else
        srv_num=0

        IFS=',' read -ra hosts <<< "${SERVER_HOSTS}"
        for host in "${hosts[@]}";
        do
            if [ "${host}" == "localhost" ] || [ "${host}" == "127.0.0.1" ]; then
                srv_num=$((1 + $srv_num))
            else
                ip=$(hostname --ip-address)

                host_name=$(hostname)

                if [ "${host}" == "${ip}" ] || [ "${host}" == "${host_name}" ]; then
                    srv_num=$((1 + $srv_num))
                fi
            fi
        done

        SERVER_NODES=${srv_num}
    fi
else
    SERVER_NODES=$1
fi

if ((${SERVER_NODES} < 1)); then
    echo "ERROR: Servers number is should be greater than 0:" ${SERVER_NODES}
    echo "Type \"--help\" for usage."
    exit 1
fi

function cleanup() {
    pkill -9 -f "Dyardstick.server"
}

trap "cleanup; exit" SIGHUP SIGINT SIGTERM SIGQUIT SIGKILL

# Define logs directory.
LOGS_BASE=logs-$(date +"%Y%m%d-%H%M%S")

LOGS_DIR=${SCRIPT_DIR}/../${LOGS_BASE}/logs_servers

if [ ! -d "${LOGS_DIR}" ]; then
    mkdir -p ${LOGS_DIR}
fi

# JVM options.
JVM_OPTS=${JVM_OPTS}" -Dyardstick.server"

CUR_DIR=$(pwd)

for i in $(eval echo {1..$SERVER_NODES});
do
    suffix=`echo "${CONFIG}" | tail -c 60 | sed 's/ *$//g'`

    file_log=${LOGS_DIR}"/"${i}"_server.log"

    echo "<"$(date +"%H:%M:%S")"><yardstick> Starting server config '..."${suffix}"'"
    echo "<"$(date +"%H:%M:%S")"><yardstick> Log file: "${file_log}

    MAIN_CLASS=org.yardstickframework.BenchmarkServerStartUp JVM_OPTS=${JVM_OPTS} CP=${CP} \
    CUR_DIR=${CUR_DIR} PROPS_ENV0=${PROPS_ENV} \
    nohup ${SCRIPT_DIR}/benchmark-bootstrap.sh ${CONFIG} --config ${CONFIG_INCLUDE} > ${file_log} 2>& 1 &
done
