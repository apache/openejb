@echo off
REM $Id$

set JAVA=%JAVA_HOME%\bin\java
set cp=
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%
set CP=lib\xerces-J_1.3.1.jar;%CP%

REM   Launch the naming server
set JAVA=%JAVA_HOME%\bin\java
set NAMING_OPTIONS=-Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl

%JAVA% %NAMING_OPTIONS% -classpath %CP% org.openorb.util.MapNamingContext -ORBPort=2001
