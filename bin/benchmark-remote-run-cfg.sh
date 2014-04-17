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
# Script allows to define the set of benchmark configurations that are run on remote machines.
#

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

# Comma-separated List of the hosts to run BenchmarkServers on.
export BHOSTS=localhost

# Name of the BenchmarkServer that is to be run on remote machines.
export BSERVER=EchoServer

# Name of the BenchmarkDriver that is to be run on remote machines.
export BSERVER=EchoServerBenchmark

# Comma-separated list of configs.
cfgs="\
-c ${SCRIPT_DIR}../benchmark.properties --host localhost -d 10,\
-c ${SCRIPT_DIR}../benchmark.properties --host localhost -d 20 -t 64
"

IFS=',' read -ra configs0 <<< "${cfgs}"
for cfg in "${configs0[@]}";
do
    export BCONFIG=cfg

    /bin/bash ${SCRIPT_DIR}/benchmark-remote-run.sh
done