@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i
for %%i in (dist\*.jar) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;lib\xerces-J_1.3.1.jar;%CP%


%JAVA% %OPTIONS% -classpath %CP% org.openejb.alt.config.Deploy  %1 %2


