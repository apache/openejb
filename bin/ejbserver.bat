@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\dist\openejb*.jar) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CP% org.openejb.server.Main  %1 %2 %3 %4 %5 %6 %7 %8 %9


exit