#!/bin/bash

# $Id$

# Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Pizer Chen  <iceant@21cn.com>

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`type -p java`
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

# ============ BEGIN OS TYPE TESTS ============

if [ -n "$OS" ]; then
    if [ "$OS" = "Windows_NT" ]; then
        OSTYPE="Windows_NT"
    fi
fi

if [ -z "$OSTYPE" ] ; then
  echo "OSTYPE environment variable is not set.  Cannot determine the host operating system!" 
  exit 1
fi

# PS stands for PATH_SEPARATOR 
PS=":"

if [ "$OSTYPE" = "cygwin32" ]; then
    PS=";"
elif [ "$OSTYPE" = "Windows_NT" ]; then
    PS=";"
elif [ "$OSTYPE" = "cygwin" ]; then
    PS=";"
fi

# ============= END OS TYPE TESTS =============


# Setup Classpath

CP=
#==================================
# put dist/*.jar file to $CP
for i in $OPENEJB_HOME/dist/*.jar;
do 
    CP=$i${PS}$CP
done


CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CLASSPATH=$CP

$JAVA -cp $CLASSPATH -Dopenejb.home=$OPENEJB_HOME org.openejb.alt.config.Deploy $@

