@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\dist\openejb*.jar) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\lib\openejb*.jar) do call cp.bat %%i

set CP=%OPENEJB_HOME%\lib\castor-0.9.3.9.jar;%CP%
set CP=%OPENEJB_HOME%\lib\ejb-1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\idb_3.26.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jakarta-regexp-1.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jca_1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jta_1.0.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\junit_3.5.jar;%CP%
set CP=%OPENEJB_HOME%\lib\log4j-1.2.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xercesImpl-2.0.2.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xmlParserAPIs-2.0.2.jar;%CP%

set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CP% org.openejb.alt.config.Deploy  %1 %2 %3 %4 %5 %6 %7 %8 %9


