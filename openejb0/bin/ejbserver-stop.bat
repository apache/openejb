@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (%OPENEJB_HOME%\lib\*.jar) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\dist\*.jar) do call cp.bat %%i
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CLASSPATH% org.openejb.server.Stop  %1 %2 %3 %4 %5 %6


