@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

rem JDK 25 is required for 26.x and can compile every older version too.
if exist "D:\dev-tools\jdk-25.0.5+8\bin\java.exe" set "JAVA_HOME=D:\dev-tools\jdk-25.0.5+8"
if not defined JAVA_HOME (
  echo Set JAVA_HOME to JDK 25, or install it at D:\dev-tools\jdk-25.0.5+8
  pause
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem A double-click has no arguments. Build every version and leave the window open.
if "%~1"=="" (
  set "PAUSE_AT_END=1"
  goto all
)
if /I "%~1"=="all" goto all
if /I "%~1"=="help" goto usage
if /I "%~1"=="/?" goto usage

set "ARG=%~1"
if not "%ARG:-=%"=="%ARG%" (
  call :splitproject "%ARG%"
  if errorlevel 1 goto usage
  call :one "!MC!" "!LOADER!"
  set "RC=!ERRORLEVEL!"
  goto done
)

if not "%~2"=="" (
  call :one "%~1" "%~2"
  set "RC=!ERRORLEVEL!"
  goto done
)

call :version "%~1"
set "RC=!ERRORLEVEL!"
goto done

:all
set "FAILS="
for %%V in (
  1.18.2 1.19.2 1.19.4 1.20 1.20.1 1.20.2 1.20.4 1.20.6
  1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.7
  1.21.8 1.21.9 1.21.10 1.21.11
  26.1 26.1.1 26.1.2 26.2 26.3
) do (
  call :version %%V
  if errorlevel 1 set "FAILS=!FAILS! %%V"
)
set "RC=0"
if defined FAILS (
  echo.
  echo Failed versions:!FAILS!
  set "RC=1"
)
goto done

:version
set "FAILED_LOADERS="
call :one %~1 fabric
if errorlevel 1 set "FAILED_LOADERS=1"
if /I not "%~1"=="1.21.2" (
  call :one %~1 forge
  if errorlevel 1 set "FAILED_LOADERS=1"
)
if /I not "%~1"=="1.18.2" if /I not "%~1"=="1.19.2" if /I not "%~1"=="1.19.4" if /I not "%~1"=="1.20" if /I not "%~1"=="1.20.1" (
  call :one %~1 neoforge
  if errorlevel 1 set "FAILED_LOADERS=1"
)
if defined FAILED_LOADERS exit /b 1
exit /b 0

:one
echo.
echo === %~1-%~2 ===
call "%~dp0gradlew.bat" --configure-on-demand ":%~1-%~2:build" --console=plain
if errorlevel 1 (
  echo FAILED %~1-%~2
  exit /b 1
)
call :javafor %~1
echo.
echo Jars in versions\!JAVA_FOLDER!\%~1\%~2\build\libs\
dir /b "versions\!JAVA_FOLDER!\%~1\%~2\build\libs\*.jar"
exit /b 0

:javafor
set "JAVA_FOLDER=21"
set "VER=%~1"
if "!VER:~0,3!"=="26." set "JAVA_FOLDER=25"
for %%V in (1.18.2 1.19.2 1.19.4 1.20 1.20.1 1.20.2 1.20.4) do if /I "%~1"=="%%V" set "JAVA_FOLDER=17"
exit /b 0

:splitproject
set "PROJECT=%~1"
for %%P in ("%PROJECT:-=\%") do set "LOADER=%%~nxP"
call set "MC=%%PROJECT:-%LOADER%=%%"
if "%MC%"=="" exit /b 1
if "%LOADER%"=="" exit /b 1
exit /b 0

:done
if not defined RC set "RC=0"
if "%RC%"=="0" (
  echo.
  echo Done.
) else (
  echo.
  echo Build failed.
)
if defined PAUSE_AT_END (
  echo.
  pause
)
exit /b %RC%

:usage
echo Builds jars into versions\^<java^>\^<minecraft^>\^<loader^>\build\libs\
echo.
echo   build.bat                   every supported version and loader
echo   build.bat 26.3              fabric, forge, and neoforge for that version
echo   build.bat 26.3 fabric       one loader
echo   build.bat 26.3-fabric       same, using the project folder name
echo   build.bat all               every supported version and loader
echo.
echo Forge has no 1.21.2 build. NeoForge starts at 1.20.2.
if defined PAUSE_AT_END pause
exit /b 1
