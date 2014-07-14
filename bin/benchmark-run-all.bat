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
:: Script that starts BenchmarkServers on remote machines, runs BenchmarkDriver and stops the servers on remote machines.
:: This procedure is performed for all configurations defined in run properties file.
:: This script expects the argument to be a path to run properties file which contains
:: the list of remote nodes to start server on and the list of configurations.
::

@echo off

set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

set CONFIG_INCLUDE=%1

setlocal

set or=false
if "%CONFIG_INCLUDE%"=="-h" set or=true
if "%CONFIG_INCLUDE%"=="--help" set or=true

if "%or%"=="true" (
    echo Usage: benchmark-run-all.bat [PROPERTIES_FILE_PATH]
    echo Script that executes BenchmarkDriver locally and BenchmarkServers on remote machines.

    exit /b
)

endlocal

if not defined CONFIG_INCLUDE (
    set CONFIG_INCLUDE=%SCRIPT_DIR%\..\config\benchmark.properties.win
    echo ^<%time:~0,2%:%time:~3,2%:%time:~6,2%^>^<yardstick^> Using default properties file: config\benchmark.properties.win
)

if not exist "%CONFIG_INCLUDE%" (
    echo ERROR: Properties file is not found.
    echo Type \"--help\" for usage.
    exit /b
)

set CONFIG_TMP=tmp.%RANDOM%.bat

echo off > %CONFIG_TMP%

type %CONFIG_INCLUDE% >> %CONFIG_TMP%

call "%CONFIG_TMP%" > nul 2>&1

del %CONFIG_TMP%

if not defined CONFIGS (
    echo ERROR: Configurations ^(CONFIGS^) are not defined in properties file.
    echo Type \"--help\" for usage.
    exit /b
)

set now=%time:~0,2%%time:~3,2%%time:~6,2%
set folder=results-%now%

set cfgs=%CONFIGS%

:loop.configs.next
for /f "tokens=1* delims=," %%a in ("%cfgs%") do (
    set CONFIG=%%a

    set cfgs=%%b

    if "x%CONFIG%"=="x%CONFIG:-of =%" (
        if "x%CONFIG%"=="x%CONFIG:--outputFolder =%" (
            set OUTPUT_FOLDER=--outputFolder %folder%
        )
    )

    set LOGS_BASE=logs-%now%

    call %SCRIPT_DIR%\benchmark-servers-start.bat %CONFIG_INCLUDE%

    :: Sleep.
    ping 192.0.2.2 -n 3 -w 1000 > nul

    call %SCRIPT_DIR%\benchmark-drivers-start.bat %CONFIG_INCLUDE%

    call %SCRIPT_DIR%\benchmark-servers-stop.bat %CONFIG_INCLUDE%

    :: Sleep.
    ping 192.0.2.2 -n 1 -w 1000 > nul
)

if defined cfgs (
    goto loop.configs.next
)