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
# Script that starts BenchmarkServer on remote machines, starts BenchmarkDriver with specified configuration
# on local machine and after the benchmark finishes it stops remote BenchmarkServers.
# NOTE: This script requires some environment variables to be defined.
#

# Define user to establish remote ssh session.
REMOTE_USER=$(whoami)

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

# Define logs directory.
LOGS_DIR=${SCRIPT_DIR}../logs

if [ "${BDRIVER}" == "" ]; then
    echo $0", ERROR:"
    echo "BenchmarkDriver is not defined."
    exit 1
fi

if [ "${REMOTE_USER}" == "" ]; then
    echo $0", ERROR:"
    echo "Remote user is not defined."
    exit 1
fi

if [ "${BCONFIG}" == "" ]; then
    echo $0", ERROR:"
    echo "Config is not defined."
    exit 1
fi

# JVM options.
JVM_OPTS="-XX:+PrintGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="${SCRIPT_DIR}

function cleanup() {
    pkill -9 -f "Dyardstick.bench"

    IFS=',' read -ra hosts0 <<<"${BHOSTS}"
    for host_name in "${hosts0[@]}";
    do
        ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} pkill -9 -f "Dyardstick.bench"
    done
}

trap "cleanup; exit" SIGHUP SIGINT SIGTERM SIGQUIT SIGKILL

if [ "${BSERVER}" != "" ] && [ "${BHOSTS}" != "" ]; then
    if [ ! -d "${LOGS_DIR}" ]; then
        mkdir -p ${LOGS_DIR}
    fi

    cntr=0

    VM_OPTS=${JVM_OPTS}" -Dyardstick.bench"

    IFS=',' read -ra hosts0 <<< "$BHOSTS"
    for host_name in "${hosts0[@]}";
    do
        echo "<<<"
        echo "<<< Starting config '"${BCONFIG}"' on "${host_name}" >>>"
        echo "<<<"

        file_log=${LOGS_DIR}"/"${cntr}"_"${host_name}".log"

        ssh -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} "JVM_OPTS='${VM_OPTS}'" \
            ${SCRIPT_DIR}/benchmark-run.sh ${BCONFIG} "-n" ${BSERVER} > ${file_log} 2>& 1 &

        cntr=$((1 + $cntr))
    done

    sleep 3s
fi

echo "<<<"
echo "<<< Starting benchmark >>>"
echo "<<<"

/bin/bash ${SCRIPT_DIR}/benchmark-run.sh ${BCONFIG} "-n" ${BDRIVER}

cleanup
