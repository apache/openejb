#!/bin/sh

# $Id$

#   Contributions by:
#      Assaf Arkin
#      David Blevins
#      Gérald Quintana
#      Mesut Celik
#      Pizer Chen  <iceant@21cn.com>

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

if [ -z "$OSTYPE" ] ; then
  echo "OSTYPE environment variable is not set.  Cannot determine the host operating system!" 
  exit 1
fi

# PS stands for PATH_SEPARATOR 
PS=':'
if [ $OSTYPE = "cygwin32" ] || [ $OSTYPE = "cygwin" ] ; then
    PS=';'
fi

JAVA=$JAVA_HOME/bin/java

CP=
#==================================
# PUT *.jar file to $CP
for i in lib/*.jar ; do 
    if [ -f $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
#==================================
# put *.zip file to $CP
for i in lib/*.zip ; do 
    if [ -f $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i

CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CP=lib/xerces-J_1.3.1.jar${PS}${CP}
CP=lib/ejb-1.0.jar${PS}${CP}
CP=test/lib/junit_3.5.jar${PS}${CP}
CP=test/lib/idb_3.26.jar${PS}${CP}
CLASSPATH=$CP

$JAVA -classpath $CLASSPATH -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile src/build.xml

