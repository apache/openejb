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
        "corba")
            cat ./bin/corba.txt | sed 's/openejb /openejb.sh /'
        ;;
        "create_stubs")
            cat ./bin/create_stubs.txt | sed 's/openejb /openejb.sh /'
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
   shift
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher org.openejb.alt.config.Deploy $@
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
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher org.openejb.server.Stop $@
}
#============================================================
_start_corba()
{
   echo " 1. OpenORB RMI/IIOP JNDI Naming Server..."
   
   NAMING_OPTIONS="-Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory \
          -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB \
          -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton \
          -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl \
          -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl \
          -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher $NAMING_OPTIONS \
        org.openorb.util.MapNamingContext -ORBPort=2001 -print > logs/jndi.log 2>&1 &
   
   pid=$?
   trap ' kill $pid; exit 1' 1 2 15
   sleep 20
   echo " 2. OpenEJB RMI/IIOP Server..."

   OPENORB_OPTIONS="-Djava.naming.provider.url=corbaloc::localhost:2001/NameService \
           -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory \
           -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB \
           -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton \
           -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl \
           -Djavax.rmi.CORBA.UtilClass=org.openejb.corba.core.UtilDelegateImpl \
           -Dorg.openejb.corba.core.UtilDelegateClass=org.openorb.rmi.system.UtilDelegateImpl \
           -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"
   SERVER_OPTIONS="-Dlog4j.configuration=file:conf/default.logging.conf \
           -Dorg/openejb/core/ThreadContext/IMPL_CLASS=org.openejb.tyrex.TyrexThreadContext"
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher $SERVER_OPTIONS $OPENORB_OPTIONS \
        org.openejb.corba.Server -ORBProfile=ejb -domain conf/tyrex_resources.xml
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
   
   PROPERTIES="-Dopenejb.testsuite.properties=src/tests-ejb/IvmServer_config.properties"
   SERVER="-Dopenejb.test.server=org.openejb.test.IvmTestServer"
   DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
   SUITE="org.openejb.test.ClientTestSuite"
   
   java $PROPERTIES $SERVER $DATABASE $OPTIONS -jar dist/openejb_ejb_tests-1.0.jar $SUITE
}
#================================================
_test_server()
{
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on Remote Server"
   echo "_________________________________________________"

   PROPERTIES="-Dopenejb.testsuite.properties=src/tests-ejb/RemoteServer_config.properties"
   SERVER="-Dopenejb.test.server=org.openejb.test.RemoteTestServer"
   DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
   SUITE="org.openejb.test.ClientTestSuite"

   java $PROPERTIES $SERVER $DATABASE $OPTIONS -jar dist/openejb_ejb_tests-1.0.jar $SUITE 
}
#============================================================
_test_corba()
{
   echo "_________________________________________________"
   echo "|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|"
   echo " "
   echo "Running EJB compliance tests on CORBA Server"
   echo "_________________________________________________"
   
      echo " 1. OpenORB RMI/IIOP JNDI Naming Server..."
   
   NAMING_OPTIONS="-Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory \
          -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB \
          -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton \
          -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl \
          -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl \
          -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher $NAMING_OPTIONS \
        org.openorb.util.MapNamingContext -ORBPort=2001 -default > logs/corba.jndi.log 2>&1 &
   
   pid=$?
   trap ' kill $pid; exit 1' 1 2 15
   sleep 20
   echo " 2. OpenEJB RMI/IIOP Server..."

   OPENORB_OPTIONS="-Djava.naming.provider.url=corbaloc::localhost:2001/NameService \
           -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory \
           -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB \
           -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton \
           -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl \
           -Djavax.rmi.CORBA.UtilClass=org.openejb.corba.core.UtilDelegateImpl \
           -Dorg.openejb.corba.core.UtilDelegateClass=org.openorb.rmi.system.UtilDelegateImpl \
           -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"
   SERVER_OPTIONS="-Dlog4j.configuration=file:conf/default.logging.conf \
           -Dorg/openejb/core/ThreadContext/IMPL_CLASS=org.openejb.tyrex.TyrexThreadContext"
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher $SERVER_OPTIONS $OPENORB_OPTIONS \
        org.openejb.corba.Server -ORBProfile=ejb -domain conf/tyrex_resources.xml > logs/corba.server.log 2>&1 &

   pid="$pid $?"
   trap ' kill $pid; exit 1' 1 2 15
   sleep 20

   echo " 3. Starting test client..."

   ORB="-DORBProfile=ejb \
        -Djava.naming.provider.url=corbaloc::localhost:2001/NameService \
        -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory \
        -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB \
        -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton \
        -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl \
        -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl \
        -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"

   PROPERTIES="-Dopenejb.testsuite.properties=src/tests-ejb/CorbaServer_config.properties"
   SERVER="-Dopenejb.test.server=org.openejb.test.CorbaTestServer"
   DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
   SUITE="org.openejb.test.ClientTestSuite"

   java $PROPERTIES $SERVER $DATABASE $OPTIONS $ORB -jar dist/openejb_ejb_tests-1.0.jar $SUITE 

   kill $pids
}
#============================================================
_create_stubs()
{
   shift
   if [ ! -z "$1" ]; then
      JAVATOIDL_OPTS="-tie -stub -noidl -local"
   fi
   java -cp dist/openejb-1.0.jar org.openejb.util.Launcher $OPTIONS org.openorb.rmi.compiler.JavaToIdl $JAVATOIDL_OPTS $@
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
    "create_stubs")
        _create_stubs $@
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



