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

set PATH=%PATH%;.\bin

set P1=_%1
set P2=_%2

if /I %P1% EQU _TEST    goto TEST
if /I %P1% EQU _BUILD   goto BUILD
if /I %P1% EQU _DEPLOY  goto DEPLOY 
if /I %P1% EQU _START   goto START_SERVER
if /I %P1% EQU _STOP    goto STOP_SERVER
if /I %P1% EQU _CORBA   goto CORBA
if /I %P1% EQU _HELP    goto HELP
if /I %P1% EQU _-HELP   goto HELP
if /I %P1% EQU _--HELP  goto HELP

echo Unknown command: %1
more < .\bin\commands.txt

goto EOF
REM================================================
:HELP
   if /I %P2% EQU _BUILD    goto HELP_BUILD
   if /I %P2% EQU _TEST     goto HELP_TEST
   if /I %P2% EQU _VALIDATE goto HELP_VALIDATE
   if /I %P2% EQU _DEPLOY   goto HELP_DEPLOY
   if /I %P2% EQU _START    goto HELP_START
   if /I %P2% EQU _STOP     goto HELP_STOP
   
   more < .\bin\commands.txt

goto EOF
REM================================================
:BUILD
    .\bin\build.bat %2 %3 %4 %5 %6 %7 %8

goto EOF
REM================================================
:TEST
   if /I %P2% EQU _LOCAL     goto TEST_INTRAVM
   if /I %P2% EQU _REMOTE    goto TEST_SERVER
   if /I %P2% EQU _CORBA     goto TEST_CORBA
   if /I %P2% EQU _HELP      goto HELP_TEST
   if /I %P2% EQU _          goto TEST_NOARGS

   echo Unknown option: %2
   goto HELP_TEST                                   

goto EOF
REM================================================
:VALIDATE 
   .\bin\validate.bat %2 %3 %4 %5 %6 %7 %8 %9
   echo Unknown option: %2
   goto HELP_VALIDATE

goto EOF
REM================================================
:DEPLOY 
   .\bin\deploy.bat %2 %3 %4 %5 %6 %7 %8 %9
   echo Unknown option: %2
   goto HELP_DEPLOY

goto EOF
REM================================================
:START_SERVER
   .\bin\ejbserver.bat %2 %3 %4 %5 %6 %7 %8 %9

goto EOF
REM================================================
:STOP_SERVER
   .\bin\ejbserver-stop.bat %2 %3 %4 %5 %6 %7 %8 %9

goto EOF
REM================================================
:CORBA
   echo " 1. Starting OpenORB JNDI Server..."
   start "OpenORB JNDI Server" .\bin\launch_jndi.bat -print > jndi.log 2>&1
   sleep 2
   echo " 2. Starting OpenEJB CORBA Server with OpenORB"
   .\bin\launch_server.bat 

goto EOF
REM================================================
:TEST_NOARGS
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"
   call .\bin\test.bat src/tests-ejb/IvmServer_config.properties org.openejb.test.IvmTestServer
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on Remote Server"
   echo "_________________________________________________"
   call .\bin\test.bat src/tests-ejb/RemoteServer_config.properties org.openejb.test.RemoteTestServer
REM   echo "_________________________________________________"
REM   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
REM   echo " "
REM   echo "Running EJB compliance tests on CORBA Server"
REM   echo "_________________________________________________"
REM   echo " 1. Starting OpenORB JNDI Server..."
REM   start "OpenORB JNDI Server" .\bin\launch_jndi.bat -default > corba.jndi.log 2>&1
REM   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
REM   start "OpenEJB CORBA Server with OpenORB" .\bin\launch_server.bat > corba.server.log 2>&1
REM   echo " 3. Starting test client..."
REM   call .\bin\launch_client.bat

goto EOF
REM================================================
:TEST_INTRAVM
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"
   call .\bin\test.bat src/tests-ejb/IvmServer_config.properties org.openejb.test.IvmTestServer
         
goto EOF
REM================================================
:TEST_SERVER
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on Remote Server"
   echo "_________________________________________________"
   call .\bin\test.bat src/tests-ejb/RemoteServer_config.properties org.openejb.test.RemoteTestServer

goto EOF
REM================================================
:TEST_CORBA
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on CORBA Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenORB JNDI Server..."
   start "OpenORB JNDI Server" .\bin\launch_jndi.bat -default > corba.jndi.log 2>&1
   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
   start "OpenEJB CORBA Server with OpenORB" .\bin\launch_server.bat > corba.server.log 2>&1
   echo " 3. Starting test client..."
   .\bin\launch_client.bat

goto EOF
REM================================================
:HELP_BUILD
   more < .\bin\build.txt    
   
goto EOF
REM================================================
:HELP_TEST
   more < .\bin\test.txt    

goto EOF
REM================================================
:HELP_DEPLOY
   more < .\bin\deploy.txt

goto EOF
REM================================================
:HELP_VALIDATE
   more < .\bin\validate.txt

goto EOF
REM================================================
:HELP_START
   more < .\bin\start.txt    

goto EOF
REM================================================
:HELP_STOP
   more < .\bin\stop.txt    

goto EOF

:EOF