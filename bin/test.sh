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

if [ -z "$OSTYPE" ] ; then
  echo "OSTYPE environment variable is not set.  Cannot determine the host operating system!" 
  exit 1
fi

# PS stands for PATH_SEPARATOR 
PS=':'
 if [ $OSTYPE = "cygwin32" ] || [ $OSTYPE = "cygwin" ] ; then
    PS=';'
 fi

# Setup Classpath
CP=
#==================================
# PUT *.jar file to $CP
for i in $OPENEJB_HOME/dist/*.jar ; do 
    if [ -e $i ] ; then
    	CP=$i${PS}$CP
    fi
done
unset i
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}

# Setup options for testsuite execution
#  Test suite properties
SERVER="-Dopenejb.test.server=org.openejb.test.IvmTestServer"
DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
OPTIONS="$SERVER $DATABASE -Dopenejb.home=$OPENEJB_HOME"

CLASSPATH=${CP}
#$JAVA $OPTIONS -classpath $CLASSPATH org.openejb.test.ClientTestRunner -s test/conf/IvmServer_config.properties org.openejb.test.ClientTestSuite
$JAVA $OPTIONS -classpath $CLASSPATH org.openejb.test.ClientTestRunner -s src/tests-ejb/IvmServer_config.properties org.openejb.test.ClientTestSuite

