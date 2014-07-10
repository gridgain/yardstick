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
:: Script that starts BenchmarkDriver or BenchmarkDriver.
:: This script expects the argument to be a path to run properties file which contains
:: the list of remote nodes to start server on and the list of configurations.
::

@echo off

set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

if defined CUR_DIR cd %CUR_DIR%

set CONFIG_INCLUDE=%1

if "%CONFIG_INCLUDE%"=="%CONFIG_INCLUDE:-h=%" || "%CONFIG_INCLUDE%"=="%CONFIG_INCLUDE:--help=%" (
    echo Usage: benchmark-servers-start.sh [PROPERTIES_FILE_PATH]
    echo
    echo Script that starts BenchmarkServer on remote machines.
    exit 1
)

if not defined CONFIG_INCLUDE (
    set CONFIG_INCLUDE=%SCRIPT_DIR%\..\config\benchmark.properties.win
    echo ^<%TIME%^>^<yardstick^> Using default properties file: config/benchmark.properties.win
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

if not defined DRIVER_HOSTS (
    echo ERROR: Benchmark hosts ^(DRIVER_HOSTS^) is not defined in properties file.
    echo Type \"--help\" for usage.
    exit 1
)

if not defined REMOTE_USER (
    echo ERROR: Remote user ^(REMOTE_USER^) is not defined in properties file.
    echo Type \"--help\" for usage.
    exit 1
)

if not defined CONFIG (
    for /f "tokens=1 delims=," %%a in ("%CONFIGS%") do (
        set CONFIG=%%a
    )
) else (
    set CONFIG="%CONFIG% %*"
)

if not defined CONFIG (
    echo ERROR: Configurations ^(CONFIGS^) are not defined in properties file.
    echo Type \"--help\" for usage.
    exit 1
)

:: todo: cleanup

:: Define logs directory.
set LOGS_DIR=%SCRIPT_DIR%\..\logs_servers

if not exist "%LOGS_DIR%" (
    mkdir %LOGS_DIR%
)

if not defined OUTPUT_FOLDER && "%CONFIG%"!="%CONFIG:-of =%" | "%CONFIG%"!="%CONFIG:--outputFolder =%" (
    set folder=%TIME%

    set OUTPUT_FOLDER=--outputFolder %folder%}
)

:: JVM options.
set JVM_OPTS=%JVM_OPTS% -Dyardstick.server
:: check custom jvm_opts

set CUR_DIR=%cd%

set cntr=0

setlocal enabledelayedexpansion

set drv_hosts=%DRIVER_HOSTS%

:loop.hosts.next
for /f "tokens=1* delims=," %%a in ("%drv_hosts%") do (
    set host_name=%%a

    set drv_hosts=%%b

    if defined %%b || !cntr! ghr 0 (
        set outFol=%OUTPUT_FOLDER%/!cntr!-!host_name!

        if "%CONFIG%"!="%CONFIG:-hn =%" && "%CONFIG%"!="%CONFIG:--hostName =%" (
            set host_name0=--hostName !host_name!
        )
    ) else
        set outFol=%OUTPUT_FOLDER%
    )

    set cfg=!outFol! !host_name0! %CONFIG%

    echo ^<%TIME%^>^<yardstick^> Starting driver config '%CONFIG%' on !host_name!

    set file_log=%LOGS_DIR%\!cntr!_!host_name!.log

    start /min cmd /c ssh -o PasswordAuthentication=no %REMOTE_USER%@%host_name% ^
        "set MAIN_CLASS=org.yardstickframework.BenchmarkDriverStartUp && set JVM_OPTS=%JVM_OPTS% && set CP=%CP% && set CUR_DIR=%CUR_DIR% && %SCRIPT_DIR%\benchmark-bootstrap.bat %CONFIG% --config %CONFIG_INCLUDE% ^> !file_log! 2^>^&1"

    start /min cmd /c ssh -o PasswordAuthentication=no %REMOTE_USER%@%host_name% ^
        "set HOST_NAME=!host_name! && %SCRIPT_DIR%\%benchmark-wait-driver-up.bat"

    echo ^<%TIME%^>^<yardstick^> Driver is started on !host_name!

    set /a cntr+=1
)

if defined drv_hosts (
    goto loop.hosts.next
)

set drv_hosts=%DRIVER_HOSTS%

:loop.hosts2.next
for /f "tokens=1* delims=," %%a in ("%drv_hosts%") do (
    set host_name=%%a

    set drv_hosts=%%b

    start /min cmd /c ssh -o PasswordAuthentication=no %REMOTE_USER%@%host_name% ^
        "%SCRIPT_DIR%\%benchmark-wait-driver-finish.bat"

    echo ^<%TIME%^>^<yardstick^> Driver is stopped on !host_name!

    set /a cntr+=1
)

if defined drv_hosts (
    goto loop.hosts2.next
)