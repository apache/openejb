#!/bin/sh

#   Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Pizer Chen  <iceant@21cn.com>

#######################################
# Set the environment

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

# PS stands for PATH_SEPARATOR 
PS=':'
 if [ $OSTYPE = "cygwin32" ] || [ $OSTYPE = "cygwin" ] ; then
    PS=';'
 fi


# Setup Classpath
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CP=`echo lib/*.jar | tr ' ' ${PS}`${PS}${CP}
CP=`echo dist/*.jar | tr ' ' ${PS}`${PS}${CP}
CLASSPATH=$CP


######################################
# startup options      
#        
OPTIONS=""

$JAVA $OPTIONS -classpath $CLASSPATH org.openejb.server.EjbDaemon $1 $2 $3 $4

