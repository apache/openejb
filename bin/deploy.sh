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
for i in $OPENEJB_HOME/lib/*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
#==================================
# put *.zip file to $CP
for i in $OPENEJB_HOME/lib/*.zip ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
#==================================
# put dist/*.jar file to $CP
for i in $OPENEJB_HOME/dist/*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i

CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CP=lib/xerces-J_1.3.1.jar${PS}${CP}
CLASSPATH=$CP

$JAVA -cp $CLASSPATH -Dopenejb.home=$OPENEJB_HOME org.openejb.alt.config.Deploy $@

