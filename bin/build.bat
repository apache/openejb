@echo off
REM $Id$

REM   Contributions by:
REM      Assaf Arkin
REM      David Blevins
REM      Gérald Quintana

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (%OPENEJB_HOME%\lib\*.jar) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\lib\*.zip) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xerces-J_1.3.1.jar;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 -buildfile src/build.xml

