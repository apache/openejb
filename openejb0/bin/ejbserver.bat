@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i
for %%i in (beans\*.jar) do call cp.bat %%i
for %%i in (dist\*.jar) do call cp.bat %%i
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CP%


%JAVA% %OPTIONS% -classpath %CLASSPATH% org.openejb.server.EjbDaemon  %1 %2 %3 %4


