#!/bin/sh

# $Id$

#   Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Mesut Celik <mcelik@bornova.ege.edu.tr>
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

if [ -z "$OPENEJB_HOME" ] ; then
  OPENEJB_HOME=$PWD
fi

JAVA=$JAVA_HOME/bin/java
TEST_HOME=test/conf

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
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}


for i in dist/*.jar ; do 
    if [ -e $i ] ; then
    	CP=$i${PS}$CP
    fi
done
unset i
CP=lib/xerces-J_1.3.1.jar${PS}${CP}

CLASSPATH=${CP}

# Setup options for testsuite execution
# LOGGING="-Dlog4j.configuration=file:conf/logging.conf"

# Setup options for testsuite execution
BASIC_OPTIONS="-Dopenejb.test.nowarn=true $LOGGING"


#--------------------------------------

test_architecture_01 () {
   echo ""
   echo ""
   echo "------------------------------------------"
   echo "||| Test Setup 1 |||||||||||||||||||||||||"
   echo "------------------------------------------"
   echo ""
   echo "  Server   = IntraVM Server";
   echo "  Database = InstantDB";
   echo "  Config   = conf/example1.openejb.conf";
   echo ""
   
   SERVER="-Dopenejb.test.server=org.openejb.test.IvmTestServer"
   DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
   CONFIG="-Dopenejb.configuration=conf/example1.openejb.conf"
   
   $JAVA $BASIC_OPTIONS $SERVER $DATABASE $CONFIG -classpath $CLASSPATH org.openejb.test.TestRunner org.openejb.test.ClientTestSuite
}


test_architecture_02 () {
   echo ""
   echo ""
   echo "------------------------------------------"
   echo "||| Test Setup 2 |||||||||||||||||||||||||"
   echo "------------------------------------------"
   echo ""
   echo "  Server   = IntraVM Server";
   echo "  Database = PostgreSQL";
   echo "  Config   = conf/example2.openejb.conf";
   echo ""
   
   SERVER="-Dopenejb.test.server=org.openejb.test.IvmTestServer"
   DATABASE="-Dopenejb.test.database=org.openejb.test.PostgreSqlTestDatabase"
   CONFIG="-Dopenejb.configuration=conf/example2.openejb.conf"
   
   $JAVA $BASIC_OPTIONS $SERVER $DATABASE $CONFIG -classpath $CLASSPATH org.openejb.test.TestRunner org.openejb.test.ClientTestSuite
}


#############################################################
#
#  Runs tests on all the servers and databases we support
#  Assumes the respective servers and databases are setup
#  and started.
#
echo "======================================================="
echo "Running OpenEJB EJB 1.1 Test Suite"
echo "======================================================="
echo ""
echo "Runs tests on all the servers and databases we support"
echo "Assumes the respective servers and databases are setup"
echo "and started."
echo ""

#  Start simple....

test_architecture_01
test_architecture_02


