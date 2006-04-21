@echo off
REM================================================
REM   Control script for OpenEJB
REM   --------------------------
REM    
REM   This script is the central entry point to 
REM   all of OpenEJB's functions.
REM  
REM   Tested on Windows 2000, Windows XP
REM             Windows XP (Cygwin)
REM
REM
REM   Created by Jeremy Whitlock
REM             <jcscoobyrs@codehaus.org>
REM _______________________________________________
REM $Id$
REM================================================

SETLOCAL

SET CWD=%CD%
SET COMMAND=%0

SET EXT=%COMMAND:~-4%

IF EXIST %CWD%/%COMMAND% SET COMMAND=%CWD%/%COMMAND%

IF %EXT% == .bat (
  SET OPENEJB_HOME=%COMMAND:~0,-16%
) ELSE (
  SET OPENEJB_HOME=%COMMAND:~0,-12%
)

echo OPENEJB_HOME=%OPENEJB_HOME%

java %JAVA_OPTS% -jar %OPENEJB_HOME%/lib/openejb-core-@REPLACED-BY-MAVEN-XML@.jar %*
