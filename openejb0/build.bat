@echo off
REM $Id$

REM   Contributions by:
REM      Assaf Arkin
REM      David Blevins
REM      Gérald Quintana

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (lib\*.jar) do call bin\cp.bat %%i
for %%i in (lib\*.zip) do call bin\cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%
set CP=lib\xerces-J_1.3.1.jar;%CP%

%JAVA% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 -buildfile src/build.xml

