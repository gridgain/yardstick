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
# Script that starts BenchmarkServer on remote machines.
# This script expects the argument to be a path to run properties file which contains
# the list of remote nodes to start server on and the list of configurations.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-servers-start.sh [PROPERTIES_FILE_PATH]"
    echo "Script that starts BenchmarkServer on remote machines."
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

function cleanup() {
    pkill -9 -f "Dyardstick.server"

    IFS=',' read -ra hosts0 <<< "${SERVER_HOSTS}"
    for host_name in "${hosts0[@]}";
    do
        `ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} pkill -9 -f "Dyardstick.server"`
    done
}

trap "cleanup; exit" SIGHUP SIGINT SIGTERM SIGQUIT SIGKILL

# Define logs directory.
LOGS_DIR=${LOGS_BASE}/logs_servers

if [ "${RESTARTERS_LOGS_DIR}" = "" ]; then
    RESTARTERS_LOGS_DIR=${LOGS_BASE}/logs_restarters
fi

if [[ "${RESTART_SERVERS}" != "" ]] && [[ "${RESTART_SERVERS}" != "true" ]]; then
    mkdir -p ${RESTARTERS_LOGS_DIR}
fi

CUR_DIR=$(pwd)

DS=""

id=0

IFS=',' read -ra hosts0 <<< "${SERVER_HOSTS}"
for host_name in "${hosts0[@]}";
do
    CONFIG_PRM="-id ${id} ${CONFIG}"

    suffix=`echo "${CONFIG}" | tail -c 60 | sed 's/ *$//g'`

    echo "<"$(date +"%H:%M:%S")"><yardstick> Starting server config '..."${suffix}"' on "${host_name}" with id=${id}"

    now=`date +'%H%M%S'`

    # Extract description.
    IFS=' ' read -ra cfg0 <<< "${CONFIG}"
    for cfg00 in "${cfg0[@]}";
    do
        if [[ ${found} == 'true' ]]; then
            found=""
            DS=${cfg00}
        fi

        if [[ ${cfg00} == '-ds' ]] || [[ ${cfg00} == '--descriptions' ]]; then
            found="true"
        fi
    done

    file_log=${LOGS_DIR}"/"${now}"-id"${id}"-"${host_name}"-"${DS}".log"

    if [[ ${JVM_OPTS} == *"PrintGC"* ]]
    then
        JVM_OPTS=${JVM_OPTS}" -Xloggc:${LOGS_DIR}/gc-${now0}-server-id${id}-${host_name}-${DS}.log"
    fi

    export JAVA_HOME=${JAVA_HOME}
    export MAIN_CLASS='org.yardstickframework.BenchmarkServerStartUp'
    export JVM_OPTS="${JVM_OPTS}${SERVER_JVM_OPTS} -Dyardstick.server${id}"
    export CP=${CP}
    export CUR_DIR=${CUR_DIR}
    export PROPS_ENV0=${PROPS_ENV}

    if [[ ${host_name} = "127.0.0.1" || ${host_name} = "localhost" ]]
    then
        mkdir -p ${LOGS_DIR}

        nohup ${SCRIPT_DIR}/benchmark-bootstrap.sh ${CONFIG_PRM} "--config" ${CONFIG_INCLUDE} "--logsFolder" ${LOGS_DIR} \
        "--remoteuser" ${REMOTE_USER} "--remoteHostName" ${host_name} > ${file_log} 2>& 1 &
    else
        ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} mkdir -p ${LOGS_DIR}

        ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} \
            "JAVA_HOME='${JAVA_HOME}'" \
            "MAIN_CLASS='${MAIN_CLASS}'" "JVM_OPTS='${JVM_OPTS}'" "CP='${CP}'" \
            "CUR_DIR='${CUR_DIR}'" "PROPS_ENV0='${PROPS_ENV}'" \
            "nohup ${SCRIPT_DIR}/benchmark-bootstrap.sh ${CONFIG_PRM} "--config" ${CONFIG_INCLUDE} "--logsFolder" ${LOGS_DIR} \
            "--remoteuser" ${REMOTE_USER} "--remoteHostName" ${host_name} > ${file_log} 2>& 1 &"
    fi



    # Start a restarter if needed.
    if [[ "${RESTART_SERVERS}" != "" ]] && [[ "${RESTART_SERVERS}" != "true" ]]; then
        IFS=',' read -ra hostsToRestart0 <<< "${RESTART_SERVERS}"
        for host2Timeout in "${hostsToRestart0[@]}";
        do
            IFS=':' read -ra hostToRestart <<< "${host2Timeout}"

            host_to_restart=${hostToRestart[0]}
            id_to_restart=${hostToRestart[1]}
            delay=${hostToRestart[2]}
            pause=${hostToRestart[3]}
            period=${hostToRestart[4]}

            if [[ "${host_to_restart}" = "${host_name}" ]] && [[ "${id_to_restart}" = "${id}" ]] ; then
                if [[ "${delay}" != "" ]] && [[ "${pause}" != "" ]] && [[ "${period}" != "" ]] ; then
                    file_log=${RESTARTERS_LOGS_DIR}"/"${now}"_id"${id}"_"${host_name}".log"

                    nohup ${SCRIPT_DIR}/benchmark-server-restarter-start.sh "${host_name}" "${id}" "${CONFIG_PRM}" \
                    "${delay}" "${pause}" "${period}" "${CONFIG_INCLUDE}" > ${file_log} 2>& 1 &

                    echo "<"$(date +"%H:%M:%S")"><yardstick> Server restarter is started for ${host_to_restart} \
                    with id=${id} and config '...${suffix}', warmup delay ${delay} sec., pause ${pause} sec. and period ${period} sec."
                else
                    echo "<"$(date +"%H:%M:%S")"><yardstick> Failed to start a server restarter for host \
                    ${host_to_restart} with id=${id}. Next params should not be empty: [warmup delay='${delay}', pause='${pause}', period='${period}']"
                fi
            fi
        done
    fi
    # End of restarter logic.

    id=$((1 + $id))
done
