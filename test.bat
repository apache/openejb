@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;lib\xerces-J_1.3.1.jar;test\lib\junit_3.5.jar;test\lib\idb_3.26.jar%CP%

set cp=
for %%i in (dist\*.jar) do call cp.bat %%i
set ri_cp=%CLASSPATH%;%CP%

set cp=
for %%i in (dist\*client*.jar) do call cp.bat %%i
for %%i in (dist\*testsuite*.jar) do call cp.bat %%i
set test_cp=%CLASSPATH%;%CP%

set OPTIONS=

REM %JAVA% -classpath %test_cp% -Dserver.classpath=%ri_cp% org.openejb.test.ClientTestRunner -s test/conf/basic_RiServer_config.properties org.openejb.test.stateless.StatelessTestSuite
%JAVA% %OPTIONS% -classpath %ri_cp% org.openejb.test.ClientTestRunner -s test\conf\IvmServer_config.properties org.openejb.OpenEJBTestSuite

