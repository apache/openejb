@echo off
REM================================================
REM   Control script for OpenEJB
REM   --------------------------
REM    
REM   This script is the central entry point to 
REM   all of OpenEJB's functions.
REM  
REM   Tested on Windows 2000
REM
REM
REM   Created by David Blevins 
REM             <david.blevins@visi.com>
REM _______________________________________________
REM $Id$
REM================================================

SETLOCAL

set PATH=%PATH%;.\bin
if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

set P1=_%1
set P2=_%2

if /I %P1% EQU _LOCAL     goto TEST_INTRAVM
if /I %P1% EQU _REMOTE    goto TEST_SERVER
if /I %P1% EQU _CORBA     goto TEST_CORBA
if /I %P1% EQU _HELP      goto HELP_TEST
if /I %P1% EQU _-HELP     goto HELP_TEST
if /I %P1% EQU _--HEL     goto HELP_TEST
if /I %P1% EQU _          goto TEST_NOARGS

echo Unknown option: %1
goto HELP_TEST                                   

goto EOF
REM================================================
:TEST_INTRAVM
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"

   set SERVER="-Dopenejb.test.server=org.apache.openejb.test.IvmTestServer"
   set DATABASE="-Dopenejb.test.database=org.apache.openejb.test.AxionTestDatabase"
   
   java %SERVER% %DATABASE% %OPTIONS% -cp %OPENEJB_HOME%/lib/openejb-core-2.0-SNAPSHOT.jar org.openejb.util.Launcher org.apache.openejb.test.TestRunner org.apache.openejb.test.ClientTestSuite
         
goto EOF
REM================================================
:TEST_SERVER
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on Remote Server"
   echo "_________________________________________________"

   set SERVER="-Dopenejb.test.server=org.apache.openejb.test.RemoteTestServer"
   set DATABASE="-Dopenejb.test.database=org.apache.openejb.test.AxionTestDatabase"
   
   java %SERVER% %DATABASE% %OPTIONS% -cp %OPENEJB_HOME%\lib\openejb-core-2.0-SNAPSHOT.jar org.openejb.util.Launcher org.apache.openejb.test.TestRunner org.apache.openejb.test.ClientTestSuite

goto EOF
REM================================================
:TEST_CORBA
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on CORBA Server"
   echo "_________________________________________________"

   echo 1. OpenORB RMI/IIOP JNDI Naming Server...
   
   set NAMING_OPTIONS=-Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory
   set NAMING_OPTIONS=%NAMING_OPTIONS% -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB
   set NAMING_OPTIONS=%NAMING_OPTIONS% -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton
   set NAMING_OPTIONS=%NAMING_OPTIONS% -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl
   set NAMING_OPTIONS=%NAMING_OPTIONS% -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl
   set NAMING_OPTIONS=%NAMING_OPTIONS% -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl

   REM  >logs\corba.jndi.log 2>&1 doesn't work with 'start'
   start "OpenORB JNDI Server" java %OPTIONS% -cp %OPENEJB_HOME%/lib/openejb-core-2.0-SNAPSHOT.jar org.openejb.util.Launcher %NAMING_OPTIONS% org.openorb.util.MapNamingContext -ORBPort=2001 -default

   sleep 20
   echo 2. OpenEJB RMI/IIOP Server...

   set OPENORB_OPTIONS=-Djava.naming.provider.url=corbaloc::localhost:2001/NameService
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Djavax.rmi.CORBA.UtilClass=org.openejb.corba.core.UtilDelegateImpl
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Dorg.openejb.corba.core.UtilDelegateClass=org.openorb.rmi.system.UtilDelegateImpl
   set OPENORB_OPTIONS=%OPENORB_OPTIONS% -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl

   set SERVER_OPTIONS=-Dlog4j.configuration=file:conf/default.logging.conf
   set SERVER_OPTIONS=%SERVER_OPTIONS% -Dorg/openejb/core/ThreadContext/IMPL_CLASS=org.openejb.tyrex.TyrexThreadContext
   REM  > logs\corba.server.log 2>&1 doesn't work with 'start'
   start "OpenEJB RMI/IIOP Server" java %OPTIONS% -cp %OPENEJB_HOME%/lib/openejb-core-2.0-SNAPSHOT.jar org.openejb.util.Launcher %SERVER_OPTIONS% %OPENORB_OPTIONS% org.openejb.corba.Server -ORBProfile=ejb -domain conf\tyrex_resources.xml

   echo 3. Starting test client...

   set ORB=-DORBProfile=ejb
   set ORB=%ORB% -Djava.naming.provider.url=corbaloc::localhost:2001/NameService
   set ORB=%ORB% -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory
   set ORB=%ORB% -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB
   set ORB=%ORB% -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton
   set ORB=%ORB% -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl
   set ORB=%ORB% -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl
   set ORB=%ORB% -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl

   set PROPERTIES="-Dopenejb.testsuite.properties=src/tests-ejb/CorbaServer_config.properties"
   set SERVER="-Dopenejb.test.server=org.apache.openejb.test.CorbaTestServer"
   set DATABASE="-Dopenejb.test.database=org.apache.openejb.test.InstantDbTestDatabase"
   set SUITE="org.apache.openejb.test.ClientTestSuite"

   java %PROPERTIES% %SERVER% %DATABASE% %OPTIONS% %ORB% -jar dist/openejb_ejb_tests-1.0.jar %SUITE% 

goto EOF
REM================================================
:HELP_TEST
   more < .\bin\test.txt    

goto EOF

:EOF
ENDLOCAL
