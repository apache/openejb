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
for i in lib/*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
#==================================
# put *.zip file to $CP
for i in lib/*.zip ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i
#==================================
# put dist/*.jar file to $CP
for i in dist/*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i

CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CP=lib/xerces-J_1.3.1.jar${PS}${CP}
CLASSPATH=$CP

$JAVA -cp $CLASSPATH org.openejb.alt.config.Deploy $@

