@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

set JAVA=%JAVA_HOME%\bin\java
set cp=%JAVA_HOME%\lib\tools.jar
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i

set cp=
for %%i in (dist\*client*.jar) do call cp.bat %%i
for %%i in (dist\*test*.jar) do call cp.bat %%i
for %%i in (dist\openejb-*.jar) do call cp.bat %%i
set test_cp=%CLASSPATH%;%CP%

set SERVER=-Dopenejb.test.server=org.openejb.test.OpenEjbTestServer
set DATABASE=-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase
set OPTIONS=%SERVER% %DATABASE%

%JAVA% %OPTIONS% -classpath %test_cp% org.openejb.test.ClientTestRunner -s src\tests-ejb\OpenEjbServer_config.properties org.openejb.test.ClientTestSuite
