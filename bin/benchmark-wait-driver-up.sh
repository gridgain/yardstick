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
# Script that waits for the drivers to finish.
#

i=1

max_time=10

for (( ; i < ${max_time}; i++ )) do
    if pgrep -f "Dyardstick.driver" > /dev/null 2>&1; then
        break
    else
        sleep 1s
    fi
done

if ((${i} == ${max_time})); then
    echo "ERROR: Driver process has not started on "${HOST_NAME}" during "${max_time}" seconds."
    echo "Type \"--help\" for usage."
    exit 1
fi