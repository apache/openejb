@echo off
REM================================================
REM   Control script for OpenEJB
REM   --------------------------
REM    
REM   This script is the central entry point to 
REM   all of OpenEJB's functions.
REM  
REM   Tested on Windows 2000
REM
REM
REM   Created by David Blevins 
REM             <david.blevins@visi.com>
REM _______________________________________________
REM $Id$
REM================================================

SETLOCAL

rem find OPENEJB_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%OPENEJB_HOME%\lib\openejb-core-*.jar" goto openejbHomeSet

:noOpenEJBHome
echo OPENEJB_HOME is set incorrectly or OpenEJB could not be located. Please set OPENEJB_HOME.
goto EOF

:openejbHomeSet

java -jar %OPENEJB_HOME%/lib/openejb-core-@REPLACED-BY-MAVEN-XML@.jar %*