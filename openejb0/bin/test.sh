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

# PS stands for PATH SEPERATOR
PS=":"

if [ -z `uname -s` ]; then
    echo "Cannot determine your host operating system."
    exit 1
elif [ `uname -s` = "CYGWIN_NT-5.0" -o `uname -s` = "cygwin32" -o `uname -s` = "cygwin" ]; then
    PS=";"
fi

# Setup Classpath
CP=
#==================================
# PUT *.jar file to $CP
for i in ./dist/*.jar
do 
    CP=$i${PS}$CP
done

CP=./lib/junit_3.8.1.jar${PS}${CP}
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}

# Setup options for testsuite execution
#  Test suite properties
PROPERTIES="-Dopenejb.testsuite.properties=$1"
SERVER="-Dopenejb.test.server=$2"
DATABASE="-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase"
OPTIONS="$SERVER $DATABASE $PROPERTIES -Dopenejb.home=$OPENEJB_HOME"

echo "--------------SUPPORT INFO-------------"
echo "`uname -srv`"
echo "Using JAVA_HOME:     $JAVA_HOME"
echo "Using OPENEJB_HOME:  $OPENEJB_HOME"

$JAVA $OPTIONS -classpath $CP org.openejb.test.TestRunner org.openejb.test.ClientTestSuite
