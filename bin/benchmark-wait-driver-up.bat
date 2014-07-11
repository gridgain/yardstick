::    Licensed under the Apache License, Version 2.0 (the "License");
::    you may not use this file except in compliance with the License.
::    You may obtain a copy of the License at
::
::        http://www.apache.org/licenses/LICENSE-2.0
::
::    Unless required by applicable law or agreed to in writing, software
::    distributed under the License is distributed on an "AS IS" BASIS,
::    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
::    See the License for the specific language governing permissions and
::    limitations under the License.

::
:: Script that waits for a driver to start.
::

@echo off

set max_count=10

set sleep_time=1

set /a timeout=max_count * sleep_time

setlocal enabledelayedexpansion

set /a cnt=0

:loop

echo before

for /f %%i in ('wmic process where (name^="java.exe" and commandline like "%%Dyardstick.driver%%"^) get ProcessId 2^>^&1 ^| findstr [0-9]') do (
    goto end
)

if !cnt! == %max_count% (
    echo ERROR: Driver process has not started on %HOST_NAME% during %timeout% seconds.
    echo Type \"--help\" for usage.

    exit 1
)

timeout /t %sleep_time% > nul

set /a cnt+=1

goto loop

:end