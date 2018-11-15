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


docker system prune -f

docker rmi $(docker images | grep 'none\|yardstick' | awk '{print $3}')

cp ${SCRIPT_DIR}/../config/Dockerfile ${SCRIPT_DIR}/../../Dockerfile

cd ${SCRIPT_DIR}/../../

docker build -t yardstick:1.1 --no-cache --build-arg COMMON_PATH=${SCRIPT_DIR}/../../ .