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

if [ -z "$OPENEJB_HOME" ] ; then
  OPENEJB_HOME=$PWD
fi

# PS stands for PATH SEPERATOR
PS=":"

case "`uname`" in
  CYGWIN*) PS=";";;
esac

#
# setup CLASSPATH
#
for i in ./dist/*.jar
do
    CP=${CP}${PS}${i}
done

echo "--------------SUPPORT INFO-------------"
echo "`uname -srv`"
echo "Using JAVA_HOME:     $JAVA_HOME"
echo "Using OPENEJB_HOME:  $OPENEJB_HOME"
echo "."

${JAVA} ${OPTIONS} -classpath ${CP} org.openejb.server.Stop $@

