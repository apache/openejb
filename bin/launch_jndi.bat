@echo off
REM $Id$

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
set CP=%OPENEJB_HOME%\lib\jdbc2_0-stdext.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jta_1.0.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\junit_3.5.jar;%CP%
set CP=%OPENEJB_HOME%\lib\log4j-1.2.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xercesImpl-2.0.2.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xmlParserAPIs-2.0.2.jar;%CP%

set CP=%JAVA_HOME%\lib\tools.jar;%CP%

REM   Launch the naming server
set JAVA=%JAVA_HOME%\bin\java
set NAMING_OPTIONS=-Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl

%JAVA% %NAMING_OPTIONS% -classpath %CP% org.openorb.util.MapNamingContext -ORBPort=2001
