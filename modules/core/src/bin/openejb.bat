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

SET COMMAND=%0
SET OPENEJB_HOME=%COMMAND:~0,-15%

echo OPENEJB_HOME=%OPENEJB_HOME%

java %JAVA_OPTS% -jar %OPENEJB_HOME%/lib/openejb-core-@REPLACED-BY-MAVEN-XML@.jar %*
