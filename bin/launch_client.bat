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

set ORB=-DORBProfile=ejb -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl
set SERVER=-Dopenejb.test.server=org.openejb.test.CorbaTestServer
set DATABASE=-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase
set OPTIONS=%SERVER% %DATABASE% %ORB%

%JAVA% %OPTIONS% -classpath %test_cp% org.openejb.test.ClientTestRunner -s src\tests-ejb\CorbaServer_config.properties org.openejb.test.ClientTestSuite
