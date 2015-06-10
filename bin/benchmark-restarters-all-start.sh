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
# Script that starts server restarters at separate processes.
# This script expects the argument to be a path to run properties file which contains
# the list of remote nodes to be restarted by timeout on remote hosts and configuration.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-restarters-all-start.sh [PROPERTIES_FILE_PATH]"
    echo "Script that starts server restarters at separate processes on current machine with specified configuration."
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

# Define user to establish remote ssh session.
if [ "${REMOTE_USER}" == "" ]; then
    REMOTE_USER=$(whoami)
fi

if [ "${SERVER_HOSTS}" == "" ]; then
    echo "ERROR: Benchmark hosts (SERVER_HOSTS) is not defined in properties file."
    echo "Type \"--help\" for usage."
    exit 1
fi

if [ "${REMOTE_USER}" == "" ]; then
    echo "ERROR: Remote user (REMOTE_USER) is not defined in properties file."
    echo "Type \"--help\" for usage."
    exit 1
fi

if [ "${CONFIG}" == "" ]; then
    echo "ERROR: Config (CONFIG) is not defined. Restarter should be run with specified config."
    echo "Type \"--help\" for usage."
    exit 1
fi

if [ "${RESTARTERS_LOGS_DIR}" = "" ]; then
    RESTARTERS_LOGS_DIR=${SCRIPT_DIR}/../${LOGS_BASE}/logs_restarters
fi

mkdir -p ${RESTARTERS_LOGS_DIR}

##
## Main.
##
cntr=0

IFS=',' read -ra hostsToRestart0 <<< "${RESTART_SERVERS}"
for host2Timeout in "${hostsToRestart0[@]}";
do
    IFS=':' read -ra hostToRestart <<< "${host2Timeout}"

    host_to_restart=${hostToRestart[0]}
    delay=${hostToRestart[1]}
    period=${hostToRestart[2]}

    host_is_valid=false

    IFS=',' read -ra hosts0 <<< "${SERVER_HOSTS}"
    for host in "${hosts0[@]}";
    do
        if [ ${host_to_restart} = ${host} ]; then
            host_is_valid=true
        fi
    done

    if [ ${host_is_valid} = true ];
    then
        now=`date +'%H%M%S'`

        file_log=${RESTARTERS_LOGS_DIR}"/"${now}"_"${cntr}"_"${host_to_restart}".log"

        suffix=`echo "${CONFIG}" | tail -c 60 | sed 's/ *$//g'`

        nohup ${SCRIPT_DIR}/benchmark-restarter-start.sh "${host_to_restart}" "${CONFIG}" "${delay}" "${period}" "${CONFIG_INCLUDE}" > ${file_log} 2>& 1 &

        echo "<"$(date +"%H:%M:%S")"><yardstick> Server restarter is started for ${host_to_restart} with config '...${suffix}', warmup delay ${delay} sec. and period ${period} sec."

        cntr=$((1 + $cntr))
    else
        echo "<"$(date +"%H:%M:%S")"><yardstick> Error: Server restarter for '${host_to_restart}' cannot be started. Invalid host name."
    fi
done
