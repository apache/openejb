@echo off
REM $Id$

REM   Contributions by:
REM      Assaf Arkin
REM      David Blevins
REM      Gérald Quintana

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
set CP=%OPENEJB_HOME%\lib\ant-1.5.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\ant-optional-1.5.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\castor-0.9.3.9.jar;%CP%
set CP=%OPENEJB_HOME%\lib\ejb-1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\idb_3.26.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jakarta-regexp-1.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jca_1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jdbc2_0-stdext.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jta_1.0.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\junit_3.5.jar;%CP%
set CP=%OPENEJB_HOME%\lib\log4j-1.2.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xalan-2.4.D1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xercesImpl-2.0.2.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xmlParserAPIs-2.0.2.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xml-apis-2.4.D1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\ots-jts_1.0.jar;%CP%

set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

%JAVA% %OPTIONS% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 -buildfile src/build.xml

