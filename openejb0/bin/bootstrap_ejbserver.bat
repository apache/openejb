@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Start/Stop Script for the CATALINA Server
rem
rem Environment Variable Prequisites
rem
rem   OPENEJB_HOME    May point at your Catalina "dist" directory.
rem
rem   OPENEJB_BASE    (Optional) Base directory for resolving dynamic portions
rem                   of a OpenEJB installation.  If not present, resolves to
rem                   the same directory that OPENEJB_HOME points to.
rem
rem   OPENEJB_OPTS    (Optional) Java runtime options used when the "start",
rem                   "stop", or "run" command is executed.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the "start",
rem                   "stop", or "run" command is executed.
rem
rem $Id$
rem ---------------------------------------------------------------------------

rem Guess OPENEJB_HOME if not defined
if not "x%OPENEJB_HOME%" == "x" goto gotHome
set OPENEJB_HOME=.
if exist "%OPENEJB_HOME%\bin\bootstrap_ejbserver.bat" goto okHome
set OPENEJB_HOME=..
:gotHome
if exist "%OPENEJB_HOME%\bin\bootstrap_ejbserver.bat" goto okHome
echo The OPENEJB_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Get standard environment variables
if exist "%OPENEJB_HOME%\bin\setenv.bat" call "%OPENEJB_HOME%\bin\setenv.bat"

rem Get standard Java environment variables
if exist "%OPENEJB_HOME%\bin\setclasspath.bat" goto okSetclasspath
echo Cannot find %OPENEJB_HOME%\bin\setclasspath.bat
echo This file is needed to run this program
goto end
:okSetclasspath
set BASEDIR=%OPENEJB_HOME%
call "%OPENEJB_HOME%\bin\setclasspath.bat"

rem Add on extra jar files to CLASSPATH
set CLASSPATH=%CLASSPATH%;%OPENEJB_HOME%\bin\bootstrap.jar

if not "%OPENEJB_BASE%" == "" goto gotBase
set OPENEJB_BASE=%OPENEJB_HOME%
:gotBase

rem ----- Execute The Requested Command ---------------------------------------

echo Using OPENEJB_BASE:   %OPENEJB_BASE%
echo Using OPENEJB_HOME:   %OPENEJB_HOME%
echo Using JAVA_HOME:      %JAVA_HOME%
echo Using CLASSPATH:      %CLASSPATH%

set _EXECJAVA=%_RUNJAVA%
set MAINCLASS=org.openejb.startup.Bootstrap
set ACTION=start
set SECURITY_POLICY_FILE=

if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop

echo Usage:  OpenEJB ( commands ... )
echo commands:
echo   run               Start OpenEJB in the current window
echo   start             Start in a separate window
echo   stop              Stop Catalina
goto end

:doRun
goto execCmd

:doStart
shift
if not "%OS%" == "Windows_NT" goto noTitle
set _EXECJAVA=start "OpenEJB" %_RUNJAVA%
goto gotTitle
:noTitle
set _EXECJAVA=start %_RUNJAVA%
:gotTitle
goto execCmd

:doStop
shift
set ACTION=stop
goto execCmd

:execCmd
rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=-class org.openejb.server.startup.EjbDaemon
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Java with the applicable properties
%_EXECJAVA% %JAVA_OPTS% %OPENEJB_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Dopenejb.base="%OPENEJB_BASE%" -Dopenejb.home="%OPENEJB_HOME%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end

:end
