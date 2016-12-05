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
:: Script that starts BenchmarkServers on a local machine.
:: This script expects first argument to be a path to run properties file.
:: Second argument is optional and defines number of starting servers.
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
    echo Usage: benchmark-manual-servers-start.bat [PROPERTIES_FILE_PATH]
    echo Script that starts BenchmarkServers on a local machine.

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

if "%1"=="" (
    if not defined SERVER_HOSTS (
        set SERVER_NODES=1
    ) else (
        setlocal enabledelayedexpansion

        set srv_num=0

        set hosts=%SERVER_HOSTS%

        :loop.configs.next
        for /f "tokens=1* delims=," %%a in ("%hosts%") do (
            set hosts=%%b

            if "%%a"=="localhost" (
                set /a srv_num+=1
            ) else if "%%a"=="127.0.0.1" (
                set /a srv_num+=1
            ) else (
                for /f "skip=1 delims={}, " %%A in ('wmic nicconfig get ipaddress') do for /f "tokens=1" %%B in ("%%~A") do set "ip=%%B"

                if "%%a"=="!ip!" (
                    set /a srv_num+=1
                ) else (
                    for /f %%i in ('hostname') do set host_name=%%i

                    if "%%a"=="!host_name!" (
                        set /a srv_num+=1
                    )
                )
            )
        )

        if defined hosts (
            goto loop.configs.next
        )

        endlocal & set srv_num=%srv_num%
    )
) else (
    set SERVER_NODES=%1
)

if defined srv_num (
    set SERVER_NODES=%srv_num%
)

if %SERVER_NODES% LSS 1 (
    echo ERROR: Servers number is should be greater than 0: %SERVER_NODES%
    echo Type \"--help\" for usage.
    exit /b
)

:: Define logs directory.
set now=%time: =0%
set LOGS_BASE=logs-%date:~10,4%%date:~4,2%%date:~7,2%-%now:~0,2%%now:~3,2%%now:~6,2%

set LOGS_DIR=%SCRIPT_DIR%\..\%LOGS_BASE%\logs_servers

if not exist "%LOGS_DIR%" (
    mkdir %LOGS_DIR%
)

:: JVM options.
set JVM_OPTS=%JVM_OPTS% -Dyardstick.server

set CUR_DIR=%cd%

setlocal enabledelayedexpansion

for /L %%i IN (1,1,%SERVER_NODES%) DO (
    set file_log=%LOGS_DIR%\%%i_server.log

    set now=%time: =0%
    echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> Starting server config '%CONFIG%'
    echo ^<%now:~0,2%:%now:~3,2%:%now:~6,2%^>^<yardstick^> Log file: !file_log!

    start /min /low cmd /c ^
        "set MAIN_CLASS=org.yardstickframework.BenchmarkServerStartUp && set JVM_OPTS=%JVM_OPTS% && set CP=%CP% && set CUR_DIR=%CUR_DIR% && %SCRIPT_DIR%\benchmark-bootstrap.bat %CONFIG% --config %CONFIG_INCLUDE% ^>^> !file_log! 2^>^&1"
)