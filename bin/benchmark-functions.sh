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
# Common functions.
#

start_node()
{
    if [[ ${host_name} = "127.0.0.1" || ${host_name} = "localhost" ]]; then
        mkdir -p ${LOGS_DIR}

        nohup ${SCRIPT_DIR}/benchmark-bootstrap.sh > ${file_log} 2>& 1 &

        if [[ $1 == "driver" ]]; then
            HOST_NAME=localhost ${SCRIPT_DIR}/benchmark-wait-driver-up.sh
        fi
    else
        ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} mkdir -p ${LOGS_DIR}

        scp -o StrictHostKeyChecking=no -o PasswordAuthentication=no  -q ${SCRIPT_DIR}/bootstrap.properties \
            ${REMOTE_USER}"@"${host_name}:${SCRIPT_DIR}/bootstrap.properties

        rm -f ${SCRIPT_DIR}/bootstrap.properties

        ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} \
            "nohup ${SCRIPT_DIR}/benchmark-bootstrap.sh > ${file_log} 2>& 1 &"

        if [[ $1 == "driver" ]]; then
            ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${host_name} "HOST_NAME='${host_name}'" \
            ${SCRIPT_DIR}/benchmark-wait-driver-up.sh
        fi
    fi
}

common_bootstrap_properties()
{
    type=$1

    rm -f ${SCRIPT_DIR}/bootstrap.properties

    touch ${SCRIPT_DIR}/bootstrap.properties

    if [[ ${JVM_OPTS} == *"PrintGC"* ]]; then
        echo "GC_JVM_OPTS=\"-Xloggc:${LOGS_DIR}/gc-${now}-${type}-id${id}-${host_name}-${DS}.log \"" >> ${SCRIPT_DIR}/bootstrap.properties
    fi

    if [[ ${JFR_TIME} != "" ]]; then
        if [[ ${JFR_DELAY} == "" ]]; then
            JFR_DELAY="60"
        fi
        echo "JFR_JVM_OPTS=\"-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=delay=${JFR_DELAY}s,duration=${JFR_TIME}s,filename=${LOGS_DIR}/rec-${now}-${type}-id${id}-${host_name}-${DS}.jfr \"" >> ${SCRIPT_DIR}/bootstrap.properties
    fi

    echo "DEFINED_JAVA_HOME=${DEFINED_JAVA_HOME}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "CP=${CP}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "CUR_DIR=${CUR_DIR}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "PROPS_ENV0=${PROPS_ENV}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "REMOTE_HOST_NAME=${host_name}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "CONFIG_INCLUDE=${CONFIG_INCLUDE}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "LOGS_DIR=${LOGS_DIR}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "REMOTE_USER=${REMOTE_USER}" >> ${SCRIPT_DIR}/bootstrap.properties
    echo "CONFIG_PRM=\"${CONFIG_PRM}\"" >> ${SCRIPT_DIR}/bootstrap.properties

}

function start_server()
{
    common_bootstrap_properties "server"

    echo  "MAIN_CLASS='org.yardstickframework.BenchmarkServerStartUp'" >> ${SCRIPT_DIR}/bootstrap.properties
    echo  "JVM_OPTS=\"${JVM_OPTS} ${SERVER_JVM_OPTS} -Dyardstick.server${id}\"" >> ${SCRIPT_DIR}/bootstrap.properties

    start_node
}

function start_driver()
{
    common_bootstrap_properties "driver"

    echo  "MAIN_CLASS='org.yardstickframework.BenchmarkDriverStartUp'" >> ${SCRIPT_DIR}/bootstrap.properties
    echo  "JVM_OPTS=\"${JVM_OPTS} ${DRIVER_JVM_OPTS} -Dyardstick.driver${id}\"" >> ${SCRIPT_DIR}/bootstrap.properties

    start_node "driver"
}