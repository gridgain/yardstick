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
    echo "Usage: benchmark-drivers-start.sh [PROPERTIES_FILE_PATH]"
    echo "Script that starts BenchmarkDriver on local machine."
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

if [ "${DRIVER_HOSTS}" == "" ]; then
    DRIVER_HOSTS="localhost"
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
    pkill -9 -f "Dyardstick.driver"

    IFS=',' read -ra hosts0 <<< "${DRIVER_HOSTS}"
    for host_name in "${hosts0[@]}";
    do
        `ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} pkill -9 -f "Dyardstick.driver"`
    done
}

trap "cleanup; exit" SIGHUP SIGINT SIGTERM SIGQUIT SIGKILL

# Define logs directory.
LOGS_DIR=${SCRIPT_DIR}/../${LOGS_BASE}/logs_drivers

if [[ "${OUTPUT_FOLDER}" == "" ]] && [[ ${CONFIG} != *'-of '* ]] && [[ ${CONFIG} != *'--outputFolder '* ]]; then
    folder=results-$(date +"%Y%m%d-%H%M%S")

    OUTPUT_FOLDER="--outputFolder ${folder}"
fi

# JVM options.
JVM_OPTS=${JVM_OPTS}" -Dyardstick.driver"

CUR_DIR=$(pwd)

cntr=0

drvNum=$((`echo ${DRIVER_HOSTS} | tr ',' '\n' | wc -l`))

IFS=',' read -ra hosts0 <<< "${DRIVER_HOSTS}"
for host_name in "${hosts0[@]}";
do
    if ((${drvNum} > 1)); then
        outFol=${OUTPUT_FOLDER}"/"${cntr}"-"${host_name}

        if [[ ${CONFIG} != *'-hn '* ]] && [[ ${CONFIG} != *'--hostName '* ]]; then
            host_name0="--hostName ${host_name}"
        fi
    else
        outFol=${OUTPUT_FOLDER}
    fi

    now=`date +'%H%M%S'`

    cfg="${outFol} ${host_name0} ${CONFIG}"

    suffix=`echo "${cfg}" | tail -c 60 | sed 's/ *$//g'`

    echo "<"$(date +"%H:%M:%S")"><yardstick> Starting driver config '..."${suffix}"' on "${host_name}""

    file_log=${LOGS_DIR}"/"${now}"_"${cntr}"_"${host_name}".log"

    ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} mkdir -p ${LOGS_DIR}

    ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} \
        "MAIN_CLASS='org.yardstickframework.BenchmarkDriverStartUp'" "JVM_OPTS='${JVM_OPTS}'" "CP='${CP}'" \
        "CUR_DIR='${CUR_DIR}'" "PROPS_ENV0='${PROPS_ENV}'" \
        "nohup ${SCRIPT_DIR}/benchmark-bootstrap.sh ${cfg} "--config" ${CONFIG_INCLUDE} > ${file_log} 2>& 1 &"

    ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} "HOST_NAME='${host_name}'" \
        ${SCRIPT_DIR}/benchmark-wait-driver-up.sh

    echo "<"$(date +"%H:%M:%S")"><yardstick> Driver is started on "${host_name}

    cntr=$((1 + $cntr))
done

for host_name in "${hosts0[@]}";
do
    ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} ${SCRIPT_DIR}/benchmark-wait-driver-finish.sh

    echo "<"$(date +"%H:%M:%S")"><yardstick> Driver is stopped on "${host_name}

    # Create marker file denoting that subfolders contain results from multiple drivers.
    if ((${drvNum} > 1)); then
        ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} touch ${CUR_DIR}/${OUTPUT_FOLDER#--outputFolder }"/.multiple-drivers"
    fi
done
