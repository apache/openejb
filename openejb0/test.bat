@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i
set OPENEJB_CP=%JAVA_HOME%\lib\tools.jar;lib\xerces-J_1.3.1.jar;%CP%

set cp=
for %%i in (dist\*.jar) do call cp.bat %%i
set ri_cp=%OPENEJB_CP%;%CP%

set cp=
for %%i in (dist\*client*.jar) do call cp.bat %%i
for %%i in (dist\*testsuite*.jar) do call cp.bat %%i
set test_cp=%OPENEJB_CP%;%CP%

set SERVER=-Dopenejb.test.server=org.openejb.test.IvmTestServer
set DATABASE=-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase
set OPTIONS=%SERVER% %DATABASE%

%JAVA% %OPTIONS% -classpath %ri_cp% org.openejb.test.ClientTestRunner -s src\tests-ejb\IvmServer_config.properties org.openejb.test.ClientTestSuite

