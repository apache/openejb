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
   if /I %P2% EQU _INTRA-VM goto TEST_INTRA-VM
   if /I %P2% EQU _CORBA    goto TEST_CORBA
   if /I %P2% EQU _HELP     goto HELP_TEST
   if /I %P2% EQU _         goto TEST_NOARGS

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
   if /I %P2% EQU _INTRA-VM goto START_INTRA-VM
   if /I %P2% EQU _CORBA    goto START_CORBA
   if /I %P2% EQU _         goto START_CORBA
   
   echo Unknown option: %2
   goto HELP_START                                   

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
:START_INTRA-VM
   more .\bin\intravm.txt

goto EOF
REM================================================
:TEST_NOARGS
   call :TEST_INTRAVM
   call :TEST_CORBA
   
goto EOF
REM================================================
:TEST_INTRAVM
   echo "Running EJB compliance tests on IntraVM Server"
   .\bin\test.bat
         
goto EOF
REM================================================
:TEST_CORBA
   echo "Running EJB compliance tests on CORBA Server"
   echo " 1. Starting OpenORB JNDI Server..."
   start "OpenORB JNDI Server" .\bin\launch_jndi.bat -default > jndi.log 2>&1
   sleep 2
   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
   start "OpenEJB CORBA Server with OpenORB" .\bin\launch_server.bat > server.log 2>&1
   sleep 6
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