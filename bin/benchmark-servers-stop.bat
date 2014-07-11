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
:: Script that stops BenchmarkServer on remote machines.
:: This script expects the argument to be a path to run properties file which contains
:: the list of remote nodes to start server on and the list of configurations.
::

@echo off

:: Define script directory.
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

set CONFIG_INCLUDE=%1

if "%CONFIG_INCLUDE%"=="%CONFIG_INCLUDE:-h=%" || "%CONFIG_INCLUDE%"=="%CONFIG_INCLUDE:--help=%" (
    echo Usage: benchmark-servers-start.sh [PROPERTIES_FILE_PATH]
    echo
    echo Script that starts BenchmarkServer on remote machines.
    exit 1
)

if not defined CONFIG_INCLUDE (
    set CONFIG_INCLUDE=%SCRIPT_DIR%\..\config\benchmark.properties.win
    echo ^<%TIME%^>^<yardstick^> Using default properties file: config\benchmark.properties.win
)

if not exist "%CONFIG_INCLUDE%" (
    echo ERROR: Properties file is not found.
    echo Type \"--help\" for usage.
    exit 1
)

shift

set CONFIG_TMP=tmp.%RANDOM%.bat

echo off > %CONFIG_TMP%

type %CONFIG_INCLUDE% >> %CONFIG_TMP%

call "%CONFIG_TMP%"

del %CONFIG_TMP%

:: Define user to establish remote ssh session.
if not defined REMOTE_USER (
    for /f %%i in ('whoami') do set REMOTE_USER=%%i
)

if not defined SERVER_HOSTS (
    echo ERROR: Benchmark hosts ^(SERVER_HOSTS^) is not defined in properties file.
    echo Type \"--help\" for usage.
    exit 1
)

if not defined REMOTE_USER (
    echo ERROR: Remote user ^(REMOTE_USER^) is not defined in properties file.
    echo Type \"--help\" for usage.
    exit 1
)

for /f %%i in ('wmic process where (name^="java.exe" and commandline like "%%Dyardstick.server%%"^) get ProcessId 2^>^&1 ^| findstr [0-9]') do (
    taskkill /F /PID %%i > nul
)

:: todo: kill remote nodes
