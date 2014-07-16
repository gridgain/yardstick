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
:: Script that starts BenchmarkServer on remote machines.
:: This script expects the argument to be a path to run properties file which contains
:: the list of remote nodes to start server on and the list of configurations.
::

@echo off

:: Define script directory.
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

set CONFIG_INCLUDE=%1

setlocal

set or=false
if "%CONFIG_INCLUDE%"=="-h" set or=true
if "%CONFIG_INCLUDE%"=="--help" set or=true

if "%or%"=="true" (
    echo Usage: benchmark-servers-start.bat [PROPERTIES_FILE_PATH]
    echo Script that starts BenchmarkServer on remote machines.

    exit /b
)

endlocal

if not exist "%CONFIG_INCLUDE%" (
    echo ERROR: Properties file is not found.
    echo Type \"--help\" for usage.
    exit /b
)

shift

set CONFIG_TMP=tmp.%RANDOM%.bat

echo off > %CONFIG_TMP%

type %CONFIG_INCLUDE% >> %CONFIG_TMP%

call "%CONFIG_TMP%" > nul 2>&1

del %CONFIG_TMP%

:: Define user to establish remote ssh session.
if not defined REMOTE_USER (
    for /f %%i in ('whoami') do set REMOTE_USER=%%i
)

if not defined SERVER_HOSTS (
    echo ERROR: Benchmark hosts ^(SERVER_HOSTS^) is not defined in properties file.
    echo Type \"--help\" for usage.
    exit /b
)

if not defined REMOTE_USER (
    echo ERROR: Remote user ^(REMOTE_USER^) is not defined in properties file.
    echo Type \"--help\" for usage.
    exit /b
)

if not defined CONFIG (
    for /f "tokens=1 delims=," %%a in ("%CONFIGS%") do (
        set CONFIG=%%a
    )
)

if not defined CONFIG (
    echo ERROR: Configurations ^(CONFIGS^) are not defined in properties file.
    echo Type \"--help\" for usage.
    exit /b
)

:: Kill servers if they exist.
call %SCRIPT_DIR%\benchmark-servers-stop.bat %CONFIG_INCLUDE%

:: todo: call cleanup on ctrl+C

:: Define logs directory.
set LOGS_DIR=%SCRIPT_DIR%\..\%LOGS_BASE%\logs_servers

if not exist "%LOGS_DIR%" (
    mkdir %LOGS_DIR%
)

:: JVM options.
set JVM_OPTS=%JVM_OPTS% -Dyardstick.server
:: check custom jvm_opts

set CUR_DIR=%cd%

set cntr=0

setlocal enabledelayedexpansion

set srv_hosts=%SERVER_HOSTS%

:loop.hosts.next
for /f "tokens=1* delims=," %%a in ("%srv_hosts%") do (
    set host_name=%%a

    set srv_hosts=%%b

    echo ^<%time:~0,2%:%time:~3,2%:%time:~6,2%^>^<yardstick^> Starting server config '%CONFIG%' on !host_name!

    set file_log=%LOGS_DIR%\!cntr!_!host_name!.log

    start /min /low cmd /c ssh -o PasswordAuthentication=no %REMOTE_USER%@!host_name! ^
        "if not exist %LOGS_DIR% mkdir %LOGS_DIR% && set MAIN_CLASS=org.yardstickframework.BenchmarkServerStartUp && set JVM_OPTS=%JVM_OPTS% && set CP=%CP% && set CUR_DIR=%CUR_DIR% && %SCRIPT_DIR%\benchmark-bootstrap.bat %CONFIG% --config %CONFIG_INCLUDE% ^>^> !file_log! 2^>^&1"

    set /a cntr+=1
)

if defined srv_hosts (
    goto loop.hosts.next
)
