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

check_help() {
    [ "${ARG}" == "-h" ] || [ "${ARG}" == "--help" ]
}

check_driver_help() {
    [ "${ARG}" == "-hd" ] || [ "${ARG}" == "--help-driver" ]
}

help() {
    check_config

    echo "Usage: benchmark-run.sh [--help] [properties-file-name]"
    echo "Script that starts BenchmarkDriver on local machine."
    echo

    CONFIG_TMP=`mktemp tmp.XXXXXXXX`

    cp $CONFIG_INCLUDE $CONFIG_TMP
    chmod +x $CONFIG_TMP

    . $CONFIG_TMP
    rm $CONFIG_TMP

    if [ "${CONFIGS}" != "" ]; then
        echo "Benchmark drivers:"

        IFS=',' read -ra configs0 <<< "${CONFIGS}"
        for cfg in "${configs0[@]}";
        do
            s=`echo ${cfg} | grep -o '\-dn \+\w* *'`
            echo "    "${s/\-dn /}
        done

        echo
        echo "See 'benchmark-run.sh --help-driver <benchamrk-driver-name>' for more information on a specific benchmark driver."
    fi
}                                   	

check_config() {
    if [ "${CONFIG_INCLUDE}" == "" ]; then
        CONFIG_INCLUDE=${SCRIPT_DIR}/../config/benchmark.properties
        echo "Using default properties file: config/benchmark.properties"
    fi

    if [ ! -f $CONFIG_INCLUDE ]; then
        echo "ERROR: Properties file is not found: "${CONFIG_INCLUDE}
        echo "Type \"--help\" for usage."
        exit 1
    fi
}

run_driver_help() {
    export MAIN_CLASS=org.yardstick.BenchmarkDriverStartUp

    /bin/bash ${SCRIPT_DIR}/benchmark-bootstrap.sh "-dn "${DRIVER_NAME}" --help"
}

ARG=$1

if check_driver_help; then
    DRIVER_NAME=$2

    if [ "${DRIVER_NAME}" == "" ]; then 
        echo "Benchmark driver name is not defined to get help of."
        echo "Type \"--help\" for usage."

        exit 1
    fi   

    echo ${DRIVER_NAME}

    run_driver_help

    exit 1
fi

if check_help; then
    CONFIG_INCLUDE=$2

    help

    exit 1
else
   ARG=$2

   if check_help; then
       CONFIG_INCLUDE=$1
 
       help

       exit 1
   fi
fi

CONFIG_INCLUDE=$1

check_config

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
fi

if [ "${CONFIG}" == "" ]; then
    echo "ERROR: Configurations (CONFIGS) are not defined in properties file."
    echo "Type \"--help\" for usage."
    exit 1
fi

if [[ ${CONFIG} != *' -of '* ]] && [[ ${CONFIG} != *' --outputFolder '* ]]; then
    CONFIG=${CONFIG}" --outputFolder results_"$(date +"%Y-%m-%d_%H-%M-%S")
fi

# JVM options.
JVM_OPTS=${JVM_OPTS}" -Dyardstick.bench"

export CP
export JVM_OPTS
export MAIN_CLASS=org.yardstick.BenchmarkDriverStartUp

/bin/bash ${SCRIPT_DIR}/benchmark-bootstrap.sh ${CONFIG} "--config" ${CONFIG_INCLUDE}

echo "Benchmark execution finished."