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
# This script expects first argument to be a path to run properties file which contains
# the list of remote nodes to start server on, server class name and driver class name.
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

CONFIG_TMP=`tempfile`

cp $CONFIG_INCLUDE $CONFIG_TMP
chmod +x $CONFIG_TMP

. $CONFIG_TMP
rm $CONFIG_TMP

# Define user to establish remote ssh session.
if [ "${REMOTE_USER}" == "" ]; then
    REMOTE_USER=$(whoami)
fi

# Define logs directory.
LOGS_DIR=${SCRIPT_DIR}/../logs

if [ "${BSERVER}" == "" ]; then
    echo $0", ERROR:"
    echo "BenchmarkServer (BSERVER) is not defined."
    exit 1
fi

if [ "${BHOSTS}" == "" ]; then
    echo $0", ERROR:"
    echo "Benchmark hosts (BHOSTS) is not defined."
    exit 1
fi

if [ "${REMOTE_USER}" == "" ]; then
    echo $0", ERROR:"
    echo "Remote user (REMOTE_USER) is not defined."
    exit 1
fi

BCONFIG="$BCONFIG $*"

if [ "${BCONFIG}" == "" ]; then
    echo $0", ERROR:"
    echo "Config is not defined."
    exit 1
fi

# JVM options.
JVM_OPTS="-Dyardstick.bench"

function cleanup() {
    pkill -9 -f "Dyardstick.bench"

    IFS=',' read -ra hosts0 <<< "${BHOSTS}"
    for host_name in "${hosts0[@]}";
    do
        ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} pkill -9 -f "Dyardstick.bench"
    done
}

trap "cleanup; exit" SIGHUP SIGINT SIGTERM SIGQUIT SIGKILL

if [ ! -d "${LOGS_DIR}" ]; then
    mkdir -p ${LOGS_DIR}
fi

cntr=0

IFS=',' read -ra hosts0 <<< "${BHOSTS}"
for host_name in "${hosts0[@]}";
do
    echo "<<<"
    echo "<<< Starting config '"${BCONFIG}"' on "${host_name}" >>>"
    echo "<<<"

    file_log=${LOGS_DIR}"/"${cntr}"_"${host_name}".log"

    ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} "JVM_OPTS='${JVM_OPTS}'" \
        ${SCRIPT_DIR}/benchmark-bootstrap.sh ${BCONFIG} "--config" ${CONFIG_INCLUDE} "--name" ${BSERVER} > ${file_log} 2>& 1 &

    cntr=$((1 + $cntr))
done
