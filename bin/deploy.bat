@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (%OPENEJB_HOME%\lib\*.jar) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\lib\*.zip) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\dist\*.jar) do call cp.bat %%i
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%OPENEJB_HOME%\lib\xerces-J_1.3.1.jar;%OPENEJB_HOME%\test\lib\junit_3.5.jar;%OPENEJB_HOME%\test\lib\idb_3.26.jar%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CLASSPATH% org.openejb.alt.config.Deploy  %1 %2 %3 %4 %5 %6 %7 %8 %9


