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
:: Script that starts BenchmarkDriver on a local machine.
:: This script expects the argument to be a path to run properties file which contains
:: list of configurations.
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
    echo Usage: benchmark-manual-drivers-start.bat [PROPERTIES_FILE_PATH]
    echo Script that starts BenchmarkDriver on a local machine.

    exit /b
)

endlocal

set now0=%time: =0%

if not defined CONFIG_INCLUDE (
    set CONFIG_INCLUDE=%SCRIPT_DIR%\..\config\benchmark-win.properties
    echo ^<%now0:~0,2%:%now0:~3,2%:%now0:~6,2%^>^<yardstick^> Using default properties file: config\benchmark-win.properties
)

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

:: Define logs directory.
set now=%time: =0%
set LOGS_BASE=logs-%date:~10,4%%date:~4,2%%date:~7,2%-%now:~0,2%%now:~3,2%%now:~6,2%

set LOGS_DIR=%SCRIPT_DIR%\..\%LOGS_BASE%\logs_drivers

if not exist "%LOGS_DIR%" (
    mkdir %LOGS_DIR%
)

if not defined OUTPUT_FOLDER (
    if "x%CONFIG%"=="x%CONFIG:-of =%" (
        if "x%CONFIG%"=="x%CONFIG:--outputFolder =%" (
            set OUTPUT_FOLDER=--outputFolder results-%date:~10,4%%date:~4,2%%date:~7,2%-%now:~0,2%%now:~3,2%%now:~6,2%
        )
    )
)

:: JVM options.
set JVM_OPTS=%JVM_OPTS% -Dyardstick.driver

set CUR_DIR=%cd%

setlocal enabledelayedexpansion

set cntr=0

set cfgs=%CONFIGS%

:loop.configs.next
for /f "tokens=1* delims=," %%a in ("%cfgs%") do (
    set cfgs=%%b

    set cfg=%OUTPUT_FOLDER% %%a

    set file_log=%LOGS_DIR%\!cntr!_driver.log

    set now=%time: =0%
    echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> Starting driver config '!cfg!'
    echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> Log file: !file_log!

    start /min cmd /c ^
       "set MAIN_CLASS=org.yardstickframework.BenchmarkDriverStartUp && set JVM_OPTS=%JVM_OPTS% && set CP=%CP% && set CUR_DIR=%CUR_DIR% && %SCRIPT_DIR%\benchmark-bootstrap.bat !cfg! --config %CONFIG_INCLUDE% ^>^> !file_log! 2^>^&1"

    set HOST_NAME=localhost

    call %SCRIPT_DIR%\%benchmark-wait-driver-up.bat"

    set now=%time: =0%
    echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> Driver is started

    call "%SCRIPT_DIR%\%benchmark-wait-driver-finish.bat"

    set now=%time: =0%
    echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> Driver is stopped

    set /a cntr+=1
)

if defined cfgs (
    goto loop.configs.next
)

set now=%time: =0%
echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> All drivers are stopped
