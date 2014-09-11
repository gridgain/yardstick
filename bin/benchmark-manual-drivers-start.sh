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
# Script that starts BenchmarkDriver on a local machine.
# This script expects the argument to be a path to run properties file which contains
# list of configurations.
#

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-manual-drivers-start.sh [PROPERTIES_FILE_PATH]"
    echo "Script that starts BenchmarkDriver on a local machine."
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
}

trap "cleanup; exit" SIGHUP SIGINT SIGTERM SIGQUIT SIGKILL

# Define logs directory.
LOGS_BASE=logs-$(date +"%Y%m%d-%H%M%S")

LOGS_DIR=${SCRIPT_DIR}/../${LOGS_BASE}/logs_drivers

if [ ! -d "${LOGS_DIR}" ]; then
    mkdir -p ${LOGS_DIR}
fi

if [[ "${OUTPUT_FOLDER}" == "" ]] && [[ ${CONFIG} != *'-of '* ]] && [[ ${CONFIG} != *'--outputFolder '* ]]; then
    folder=results-$(date +"%Y%m%d-%H%M%S")

    OUTPUT_FOLDER="--outputFolder ${folder}"
fi

# JVM options.
JVM_OPTS=${JVM_OPTS}" -Dyardstick.driver"

CUR_DIR=$(pwd)

cntr=0

IFS=',' read -ra configs0 <<< "${CONFIGS}"
for cfg in "${configs0[@]}";
do
    now=`date +'%H%M%S'`

    cfgParams="${OUTPUT_FOLDER} ${cfg}"

    suffix=`echo "${cfgParams}" | tail -c 60 | sed 's/ *$//g'`

    file_log=${LOGS_DIR}"/"${now}"_"${cntr}"_driver.log"

    echo "<"$(date +"%H:%M:%S")"><yardstick> Starting driver config '..."${suffix}"'"
    echo "<"$(date +"%H:%M:%S")"><yardstick> Log file: "${file_log}

    MAIN_CLASS=org.yardstickframework.BenchmarkDriverStartUp JVM_OPTS=${JVM_OPTS} CP=${CP} \
    CUR_DIR=${CUR_DIR} PROPS_ENV0=${PROPS_ENV} \
    ${SCRIPT_DIR}/benchmark-bootstrap.sh ${cfgParams} --config ${CONFIG_INCLUDE} > ${file_log} 2>& 1 &

    HOST_NAME=localhost ${SCRIPT_DIR}/benchmark-wait-driver-up.sh

    echo "<"$(date +"%H:%M:%S")"><yardstick> Driver is started"

    HOST_NAME=localhost ${SCRIPT_DIR}/benchmark-wait-driver-finish.sh

    echo "<"$(date +"%H:%M:%S")"><yardstick> Driver is stopped"

    cntr=$((1 + $cntr))
done

echo "<"$(date +"%H:%M:%S")"><yardstick> All drivers are stopped"
