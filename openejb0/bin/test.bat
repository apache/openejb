@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\dist\*.jar) do call cp.bat %%i

set CP=%OPENEJB_HOME%\lib\junit_3.8.1.jar;%CP%
set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set PROPERTIES=-Dopenejb.testsuite.properties=%1
set SERVER=-Dopenejb.test.server=%2
set DATABASE=-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase
set OPTIONS=%DATABASE% %SERVER% %PROPERTIES% -Dopenejb.home=%OPENEJB_HOME%

echo --------------SUPPORT INFO-------------
echo %OS%
echo Using JAVA_HOME:     %JAVA_HOME%
echo Using OPENEJB_HOME:  %OPENEJB_HOME%
echo OPTIONS:             %OPTIONS%
echo --------------SUPPORT INFO-------------

%JAVA% %OPTIONS% -classpath %CP% org.openejb.test.TestRunner org.openejb.test.ClientTestSuite