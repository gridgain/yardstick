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
# Script that starts BenchmarkServer or BenchmarkDriver.
#

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

DOCKER_FILE_PATH=$1

IMAGE_NAME=$2

IMAGE_VER=$3

CURRENT_USER_NAME=$4

# Define user.
if [ "${CURRENT_USER_NAME}" == "" ]; then
    CURRENT_USER_NAME=$(whoami)
fi

docker system prune -f

#docker rmi $(docker images | grep 'none\|yardstickserver\|yardstickdriver' | awk '{print $3}')

#echo "PATH"

#echo $DOCKER_FILE_PATH

if [ -f ${SCRIPT_DIR}/../../Dockerfile ]; then
    rm ${SCRIPT_DIR}/../../Dockerfile
fi

#echo "User = ${CURRENT_USER_NAME}"

cp ${DOCKER_FILE_PATH} ${SCRIPT_DIR}/../../Dockerfile

cd ${SCRIPT_DIR}/../../

#echo "IMAGE NAME"

#echo $IMAGE_NAME


#echo "IMAGE VER"

#echo $IMAGE_VER

docker build -t ${IMAGE_NAME}:${IMAGE_VER} --no-cache --build-arg USER_NAME=${CURRENT_USER_NAME} --build-arg COMMON_PATH=${SCRIPT_DIR}/../../ .