#!/bin/sh
#============================================================
#   Control script for OpenEJB
#   --------------------------
#    
#   This script is the central entry point to 
#   all of OpenEJB's functions.
#  
#   Contributed by:
#
#    - David Blevins <david.blevins@visi.com>
#    - Daniel S. Haischt <daniel.haischt@daniel-s-haischt.biz>
#             
#               
# ___________________________________________________________
# $Id$
#============================================================

if [ -z "$OPENEJB_HOME" ]; then
  OPENEJB_HOME=$PWD
fi

OPTIONS="-Dopenejb.home=$OPENEJB_HOME"

#============================================================
_command_help()
{
    case $2 in
        "build")
            ant -f src/build.xml -projecthelp
        ;;
        "test")
            cat ./bin/test.txt | sed 's/openejb /openejb.sh /'
        ;;
        "validate")
            cat ./bin/validate.txt | sed 's/openejb /openejb.sh /'
        ;;
        "deploy")
            cat ./bin/deploy.txt | sed 's/openejb /openejb.sh /'
        ;;
        "start")
            cat ./bin/start.txt | sed 's/openejb /openejb.sh /'
        ;;
        "stop")
            cat ./bin/stop.txt | sed 's/openejb /openejb.sh /'
        ;;
        *)
            cat ./bin/commands.txt | sed 's/openejb /openejb.sh /'
        ;;
    esac
}
#============================================================
_command_build()
{
    ant -f src/build.xml $2 $3 $4 $5 $6 $7 $8                                     
}
#============================================================
_command_test()
{
    case $2 in
        "local")
            _test_intravm
        ;;
        "remote")
            _test_server
        ;;
        "corba")
            _test_corba
        ;;
        "help")
            cat ./bin/test.txt | sed 's/openejb /openejb.sh /'
        ;;
        *)
            _test_noargs
        ;;
    esac
}
#============================================================
_command_deploy()
{
   ./bin/deploy.sh $2 $3 $4 $5 $6 $7 $8 $9
}
#============================================================
_command_validate()
{
   shift
   java -jar dist/openejb_validator-1.0.jar $@
}
#============================================================
_command_start()
{
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher org.openejb.server.Main $@
}
#============================================================
_command_stop()
{
   ./bin/ejbserver-stop.sh $@
}
#============================================================
_start_corba()
{
   echo " 1. Starting OpenORB JNDI Server..."
   sh ./bin/launch_jndi.sh -print &> jndi.log
   pid=$?
   trap ' kill $pid; exit 1' 1 2 15
   sleep 20
   echo " 2. Starting OpenEJB CORBA Server with OpenORB"
   ./bin/launch_server.sh 
}
#============================================================
_test_noargs()
{
   _test_intravm
   _test_server
   ##_test_corba
}
#============================================================
_test_intravm()
{
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on IntraVM Server"
   echo "_________________________________________________"
   ./bin/test.sh src/tests-ejb/IvmServer_config.properties org.openejb.test.IvmTestServer
}
#================================================
_test_server()
{
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on Remote Server"
   echo "_________________________________________________"
   ./bin/test.sh src/tests-ejb/RemoteServer_config.properties org.openejb.test.RemoteTestServer
}
#============================================================
_test_corba()
{
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on CORBA Server"
   echo "_________________________________________________"
   echo " 1. Starting OpenORB JNDI Server..."
   trap ' kill $pids; exit 1' 1 2 15
   sh ./bin/launch_jndi.sh -default > corba.jndi.log &
   pids="$?"
   sleep 10
   echo " 2. Starting OpenEJB CORBA Server with OpenORB..."
   sh ./bin/launch_server.sh > corba.server.log &
   pids="$pids $?"
   sleep 20
   echo " 3. Starting test client..."
   ./bin/launch_client.sh
   kill $pids
}
#============================================================

case $1 in
    "build")
        _command_build $@
    ;;
    "test")
        _command_test $@
    ;;
    "validate")
        _command_validate $@
    ;;
    "deploy")
        _command_deploy $@
    ;;
    "start")
        _command_start $@
    ;;
    "stop")
        _command_stop $@
    ;;
    "corba")
        _start_corba $@
    ;;
    "help")
        _command_help $@
    ;;
    "-help")
        _command_help $@
    ;;
    "--help")
        _command_help $@
    ;;
    *)  _command_help $@
    ;;
esac



