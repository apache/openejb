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
  OPENEJB_HOME=$PWD/../..
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

JAVA=$JAVA_HOME/bin/java

CP=
#==================================
# PUT *.jar file to $CP
for i in ${OPENEJB_HOME}/lib/*.jar ; do 
    if [ -e $i ]; then
    	CP=$i${PS}$CP
    fi
done
unset i

CP=${OPENEJB_HOME}\src\examples\conf${PS}${CP}
CP=${JAVA_HOME}/lib/tools.jar${PS}${CP}

# $JAVA -classpath $CP -Dopenejb.home=${OPENEJB_HOME} com.titan.clients.Client_1
# $JAVA -classpath $CP -Dopenejb.home=${OPENEJB_HOME} com.titan.clients.Client_2
# $JAVA -classpath $CP -Dopenejb.home=${OPENEJB_HOME} com.titan.clients.Client_3
# $JAVA -classpath $CP -Dopenejb.home=${OPENEJB_HOME} com.titan.clients.Client_4
# $JAVA -classpath $CP -Dopenejb.home=${OPENEJB_HOME} com.titan.clients.Client_cleanup
$JAVA -classpath $CP -Dopenejb.home=${OPENEJB_HOME} org.acme.clients.HelloWorld

