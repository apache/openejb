@echo off
REM $Id$

REM   Contributions by:
REM      Assaf Arkin
REM      David Blevins
REM      Gérald Quintana

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%\..\..

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\lib\*.jar) do call %OPENEJB_HOME%\bin\cp.bat %%i 

set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main install -buildfile ./build.xml

