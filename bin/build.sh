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

if [ -z "$OPENEJB_HOME" ] ; then
  OPENEJB_HOME=$PWD
fi

# PS stands for PATH_SEPARATOR 
PS=":"

if [ -z `uname -s` ]; then
    echo "Cannot determine your host operating system."
    exit 1
elif [ `uname -s` = "CYGWIN_NT-5.0" -o `uname -s` = "cygwin32" -o `uname -s` = "cygwin" ]; then
    PS=";"
fi

JAVA=$JAVA_HOME/bin/java

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
# put *.zip file to $CP
for i in lib/*.zip
do 
    CP=$i${PS}$CP
done

#CP=
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CP=lib/ant_1_4_1.jar${PS}${CP}
CP=lib/ant_optional_1_4_1.jar${PS}${CP}
CP=lib/xerces-J_1.4.0.jar${PS}${CP}
#CP=lib/ejb-1.0.jar${PS}${CP}
#CP=test/lib/junit_3.5.jar${PS}${CP}
#CP=test/lib/idb_3.26.jar${PS}${CP}
CLASSPATH=$CP

$JAVA -classpath $CLASSPATH -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile src/build.xml
