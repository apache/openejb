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

JAVA=$JAVA_HOME/bin/java

if [ -z "$OPENEJB_HOME" ] ; then
  OPENEJB_HOME=$PWD
fi

# PS stands for PATH_SEPARATOR 
PS=':'
 if [ $OSTYPE = "cygwin32" ] || [ $OSTYPE = "cygwin" ] ; then
    PS=';'
 fi

# Setup Classpath

CP=
for i in dist/*.jar ; do 
    if [ -e $i ] ; then
    	CP=$i${PS}$CP
    fi
done
unset i
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}

# Setup options for testsuite execution
SERVER="-Dopenejb.test.server=org.openejb.test.OpenEjbTestServer"
DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
OPTIONS=" $SERVER $DATABASE -Dopenejb.home=$OPENEJB_HOME"

CLASSPATH=${CP}
$JAVA $OPTIONS -classpath $CLASSPATH org.openejb.test.Main -s src/tests-ejb/OpenEjbServer_config.properties org.openejb.test.ClientTestSuite

