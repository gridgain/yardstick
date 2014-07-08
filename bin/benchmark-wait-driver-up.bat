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

for /l %%i in (1,1,%max_count%) do (
    set cnt=%%i

    "%JAVA_HOME%\bin\jps.exe" -lv | findstr "Dyardstick.driver" > nul

    if !ERRORLEVEL! equ 0 (
        goto end
    ) else (
        timeout /t %sleep_time% > nul
    )
)

:end

if %cnt% == %max_count% (
    echo ERROR: Driver process has not started on %HOST_NAME% during %timeout% seconds.
    echo Type \"--help\" for usage.

    exit 1
)