#!/bin/sh
#================================================
#   Control script for OpenEJB
#   --------------------------
#    
#   This script is the central entry point to 
#   all of OpenEJB's functions.
#  
#   Created by David Blevins 
#             <david.blevins@visi.com>
# _______________________________________________
# $Id$
#================================================

#================================================
function command_help () {
   case     $2 in
   "build"  ) cat ./bin/build.txt   | sed 's/openejb /openejb.sh /';;
   "test"   ) cat ./bin/test.txt    | sed 's/openejb /openejb.sh /';;
   "deploy" ) cat ./bin/deploy.txt  | sed 's/openejb /openejb.sh /';;
   "start"  ) cat ./bin/start.txt   | sed 's/openejb /openejb.sh /';;
   ""       ) cat ./bin/commands.txt| sed 's/openejb /openejb.sh /';;
   esac
}
#================================================
function command_build () {
    ./bin/build.sh $2 $3 $4 $5 $6 $7 $8
}
#================================================
function command_test () {
   case     $2 in
   "intravm"   ) test_intravm ;;
   "ejbserver" ) test_server  ;;
   "corba"     ) test_corba   ;;
   "help"      ) cat ./bin/test.txt    | sed 's/openejb /openejb.sh /';;
   ""          ) test_noargs  ;;
   esac
}
#================================================
function command_deploy  () {
   ./bin/deploy.sh $2 $3 $4 $5 $6 $7 $8 $9
}
#================================================
function command_start  () {
   case     $2 in
   "intra-vm"  ) start_intravm ;;
   "intravm"   ) start_intravm ;;
   "corba"     ) start_corba   ;;
   "ejbserver" ) start_server  ;;
   "server"    ) start_server  ;;
   ""          ) start_server  ;;
   esac
}
#================================================
function start_server () {
   echo "Starting OpenEJB Server..."
   ./bin/ejbserver.sh $2 $3 $4 $5 $6 $7
}
#================================================
function start_corba () {
   echo " 1. Starting OpenORB JNDI Server..."
   ./bin/launch_jndi.sh -print &> jndi.log &
   pid=$?
   trap ' kill $pid; exit 1' 1 2 15
   echo " 2. Starting OpenEJB CORBA Server with OpenORB"
   ./bin/launch_server.sh 
}
#================================================
function start_intravm () {
   cat ./bin/intravm.txt
}
#================================================
function test_noargs () {
   test_intravm
   test_server
   test_corba
}
#================================================
function test_intravm () {
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"
   ./bin/test.sh
}
#================================================
function test_server () {
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on EJB Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenEJB Server..."
   ./bin/ejbserver.sh &> ejb.server.log &
   sleep 4
   echo " 2. Starting test EJB client..."
   ./bin/ejbclient.sh
}
#================================================
function test_corba () {
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on CORBA Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenORB JNDI Server..."
   ./bin/launch_jndi.sh -default &> corba.jndi.log &
   sleep 2
   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
   ./bin/launch_server.sh &> corba.server.log &
   sleep 6
   echo " 3. Starting test client..."
   ./bin/launch_client.sh
}

case     $1 in
"build"  )  command_build  $@ ;;
"test"   )  command_test   $@ ;;
"deploy" )  command_deploy $@ ;;
"start"  )  command_start  $@ ;;
"help"   )  command_help   $@ ;;
"-help"  )  command_help   $@ ;;
"--help" )  command_help   $@ ;;
""       )  command_help   $@ ;;
esac



