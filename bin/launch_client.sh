#!/bin/sh

# $Id$

#   Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Pizer Chen  <iceant@21cn.com>

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVA=$JAVA_HOME/bin/java

if [ -z "$OPENEJB_HOME" ] ; then
  OPENEJB_HOME=$PWD
fi

# PS stands for PATH SEPERATOR
PS=":"

case "`uname`" in
  CYGWIN*) PS=";";;
esac

# Setup Classpath
CP=
#==================================
# PUT *.jar file to $CP
for i in lib/*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
#==================================
# put dist/*.jar file to $CP
for i in dist/*ejb_tests*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
for i in dist/*corba*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
for i in dist/openejb-*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i

CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CLASSPATH=lib/xerces-J_1.3.1.jar${PS}${CP}

# Setup options for testsuite execution
ORB=" -DORBProfile=ejb -Djava.naming.provider.url=corbaloc::localhost:2001/NameService -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"
#  Test suite properties
SERVER="-Dopenejb.test.server=org.openejb.test.CorbaTestServer"
DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
PROPERTIES="-Dopenejb.testsuite.properties=src/tests-ejb/CorbaServer_config.properties"
OPTIONS="$SERVER $DATABASE $ORB $PROPERTIES"
$JAVA $OPTIONS -classpath $CLASSPATH org.openejb.test.TestRunner org.openejb.test.ClientTestSuite

