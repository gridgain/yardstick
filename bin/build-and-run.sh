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

cd ${SCRIPT_DIR}/../

mvn clean install -DskipTests -Dmaven.javadoc.skip=true

cp /home/oostanin/.m2/repository/org/yardstickframework/yardstick/0.8.7/yardstick-0.8.7.jar /home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/libs/yardstick-0.8.7.jar

#cp -r /home/oostanin/yardstick/bin/* /home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin/

#cp -r /home/oostanin/yardstick/config/docker/* /home/oostanin/gg/incubator-ignite/modules/yardstick/config/docker/
#cp -r /home/oostanin/yardstick/config/docker/* /home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/docker/

#cp /home/oostanin/gg/incubator-ignite/modules/yardstick/config/benchmark.properties /home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/benchmark.properties

cd /home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly

#/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin/run.sh config/docker/benchmark.properties
#
#/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin/run.sh
#
#/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin/run.sh /home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/docker/benchmark.properties
#
