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
MAIN_DIR=$(cd ${SCRIPT_DIR}/../; pwd)

CONFIG_INCLUDE=$1

if [ "${CONFIG_INCLUDE}" == "-h" ] || [ "${CONFIG_INCLUDE}" == "--help" ]; then
    echo "Usage: benchmark-run-all.sh [PROPERTIES_FILE_PATH]"
    echo "By default, all the necessary files will be automatically uploaded from this host"
    echo "to every other remote host to the same path."
    echo "If you prefer to do it manually set the AUTO_COPY variable in property file to `false`."
    echo "For more information check the Yardstick framework site:"
    echo "https://github.com/gridgain/yardstick"
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

if ! [[ -d ${SCRIPT_DIR}/../output ]]
then
    echo "<"$(date +"%H:%M:%S")"><yardstick> Creating output directory"
    mkdir ${SCRIPT_DIR}/../output
fi

# Creating an array of IP addresses of the remote hosts from SERVER_HOSTS and DRIVER_HOSTS variables.
function define_ips()
{
    # Defining IP of the local machine.
    local local_ip_addresses=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'`
    local comma_separated_ips="${SERVER_HOSTS},${DRIVER_HOSTS}"
    local ips=${comma_separated_ips//,/ }
    local uniq_ips=`echo "${ips[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '`
    for local_ip in ${local_ip_addresses[@]}
    do
        uniq_ips=( "${uniq_ips[@]/$local_ip}" )
    done
    echo ${uniq_ips[@]}
}

# Deleting all the yardstick directories from the working directory on remote host
# $1 IP address of the remote host
function clear_remote_work_directory()
{
    ssh -o StrictHostKeyChecking=no $1 rm -rf $MAIN_DIR/bin
    ssh -o StrictHostKeyChecking=no $1 rm -rf $MAIN_DIR/config
    ssh -o StrictHostKeyChecking=no $1 rm -rf $MAIN_DIR/libs
    ssh -o StrictHostKeyChecking=no $1 rm -rf $MAIN_DIR/output
    ssh -o StrictHostKeyChecking=no $1 rm -rf $MAIN_DIR/work
}

# Copying working directory to remote hosts.
function copy_to_hosts()
{
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"><yardstick> Copying yardstick to the host ${ip}"
            ssh -o StrictHostKeyChecking=no $ip mkdir -p $MAIN_DIR
            clear_remote_work_directory $ip
            scp -o StrictHostKeyChecking=no -rq $MAIN_DIR/* $ip:$MAIN_DIR
        fi
    done
}

if [[ $AUTO_COPY != false ]]; then
    copy_to_hosts
fi

date_time=$(date +"%Y%m%d-%H%M%S")
result_dir_name=results-$date_time
log_dir_name=logs-$date_time

results_folder=${SCRIPT_DIR}/../output/$result_dir_name
LOGS_BASE=${SCRIPT_DIR}/../output/$log_dir_name

export LOGS_BASE

if [ -z "$RESTART_SERVERS" ]; then
    /bin/bash ${SCRIPT_DIR}/benchmark-servers-start.sh ${CONFIG_INCLUDE}

    sleep 3s
fi

IFS=',' read -ra configs0 <<< "${CONFIGS}"
for cfg in "${configs0[@]}";
do
    CONFIG=${cfg}

    if [[ ${CONFIG} != *'-of '* ]] && [[ ${CONFIG} != *'--outputFolder '* ]]; then
        OUTPUT_FOLDER="--outputFolder ${results_folder}"
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

# Collecting results and logs from the remote hosts
function collect_results()
{
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"><yardstick> Collecting results from the host ${ip}"
            # Checking if current IP belongs to the driver-host and therefore there should be the "results" directory
            if [[ ${DRIVER_HOSTS} == *"$ip"* ]]
            then
                scp -o StrictHostKeyChecking=no -rq $ip:$results_folder/../../output/$result_dir_name/* $MAIN_DIR/output/$result_dir_name
            fi
            scp -o StrictHostKeyChecking=no -rq $ip:$LOGS_BASE/../../output/$log_dir_name/* $MAIN_DIR/output/$log_dir_name
            clear_remote_work_directory $ip
        fi
    done
}

if [[ $AUTO_COPY != false ]]; then
    collect_results
fi

# Creating graphs.
function create_charts()
{
    if [ -d $results_folder ]
    then
        OUT_DIR=$(cd $results_folder/../; pwd)
        echo "<"$(date +"%H:%M:%S")"><yardstick> Creating charts"
        . ${SCRIPT_DIR}/jfreechart-graph-gen.sh -gm STANDARD -i $results_folder >> /dev/null
        . ${SCRIPT_DIR}/jfreechart-graph-gen.sh -i $results_folder >> /dev/null
        echo "Moving chart directory to the $MAIN_DIR/output/results-$date_time directory."
        mv $MAIN_DIR/output/results-compound* $MAIN_DIR/output/results-$date_time
    fi
}

if [[ $AUTO_COPY != false ]]; then
    create_charts
fi