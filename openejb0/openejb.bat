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

set P1=_%1
set P2=_%2

if /I %P1% EQU _TEST    goto TEST
if /I %P1% EQU _BUILD   goto BUILD
if /I %P1% EQU _DEPLOY  goto DEPLOY 
if /I %P1% EQU _START   goto START
if /I %P1% EQU _HELP    goto HELP
if /I %P1% EQU _-HELP   goto HELP
if /I %P1% EQU _--HELP  goto HELP

echo Unknown command: %1
more .\bin\commands.txt

goto EOF
REM================================================
:HELP
   if /I %P2% EQU _BUILD    goto HELP_BUILD
   if /I %P2% EQU _TEST     goto HELP_TEST
   if /I %P2% EQU _DEPLOY   goto HELP_DEPLOY
   if /I %P2% EQU _START    goto HELP_START
   
   more .\bin\commands.txt

goto EOF
REM================================================
:BUILD
    .\bin\build.bat %2 %3 %4 %5 %6 %7 %8

goto EOF
REM================================================
:TEST
   if /I %P2% EQU _INTRA-VM  goto TEST_INTRAVM
   if /I %P2% EQU _EJBSERVER goto TEST_SERVER
   if /I %P2% EQU _CORBA     goto TEST_CORBA
   if /I %P2% EQU _HELP      goto HELP_TEST
   if /I %P2% EQU _          goto TEST_NOARGS

   echo Unknown option: %2
   goto HELP_TEST                                   

goto EOF
REM================================================
:DEPLOY 
   .\bin\deploy.bat
   echo Unknown option: %2
   goto HELP_DEPLOY

goto EOF
REM================================================
:START 
   if /I %P2% EQU _INTRA-VM  goto START_INTRAVM
   if /I %P2% EQU _INTRAVM   goto START_INTRAVM
   if /I %P2% EQU _CORBA     goto START_CORBA
   if /I %P2% EQU _EJBSERVER goto START_SERVER
   if /I %P2% EQU _SERVER    goto START_SERVER
   if /I %P2% EQU _          goto START_SERVER
   
   goto START_SERVER

goto EOF
REM================================================
:START_SERVER
   echo "Starting OpenEJB Server..."
   .\bin\ejbserver.bat %2 %3 %4 %5 %6 %7  

goto EOF
REM================================================
:START_CORBA
   echo " 1. Starting OpenORB JNDI Server..."
   start "OpenORB JNDI Server" .\bin\launch_jndi.bat -print > jndi.log 2>&1
   sleep 2
   echo " 2. Starting OpenEJB CORBA Server with OpenORB"
   .\bin\launch_server.bat 

goto EOF
REM================================================
:START_INTRAVM
   more .\bin\intravm.txt

goto EOF
REM================================================
:TEST_NOARGS
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"
   call .\bin\test.bat
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on EJB Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenEJB Server..."
   start "OpenEJB Server" .\bin\ejbserver.bat > ejb.server.log 2>&1
   sleep 4
   echo " 2. Starting test EJB client..."
   call .\bin\ejbclient.bat
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on CORBA Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenORB JNDI Server..."
   start "OpenORB JNDI Server" .\bin\launch_jndi.bat -default > corba.jndi.log 2>&1
   sleep 2
   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
   start "OpenEJB CORBA Server with OpenORB" .\bin\launch_server.bat > corba.server.log 2>&1
   sleep 15
   echo " 3. Starting test client..."
   call .\bin\launch_client.bat

goto EOF
REM================================================
:TEST_INTRAVM
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"
   call .\bin\test.bat
         
goto EOF
REM================================================
:TEST_SERVER
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on EJB Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenEJB Server..."
   start "OpenEJB Server" .\bin\ejbserver.bat > ejb.server.log 2>&1
   sleep 4
   echo " 2. Starting test EJB client..."
   call .\bin\ejbclient.bat

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
   sleep 2
   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
   start "OpenEJB CORBA Server with OpenORB" .\bin\launch_server.bat > corba.server.log 2>&1
   sleep 15
   echo " 3. Starting test client..."
   .\bin\launch_client.bat

goto EOF
REM================================================
:HELP_BUILD
   more .\bin\build.txt    
   
goto EOF
REM================================================
:HELP_TEST
   more .\bin\test.txt    

goto EOF
REM================================================
:HELP_DEPLOY
   more .\bin\deploy.txt

goto EOF
REM================================================
:HELP_START
   more .\bin\start.txt    

goto EOF


      
:EOF