#!/bin/sh

#   Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Pizer Chen  <iceant@21cn.com>
#     Daniel S. Haischt <daniel.haischt@daniel-s-haischt.biz>

#######################################
# Set the environment

if [ -z "$JAVA_HOME" ]; then
  JAVA=`which java`
  if [ -z "$JAVA" ]; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVA=$JAVA_HOME/bin/java

if [ -z "$OPENEJB_HOME" ]; then
  OPENEJB_HOME=$PWD
fi

# PS stands for PATH SEPERATOR
PS=":"

case "`uname`" in
  CYGWIN*) PS=";";;
esac

#
# Setup Classpath
#
CP=$JAVA_HOME/lib/tools.jar
#CP=`echo $OPENEJB_HOME/lib/*.jar | tr ' ' ${PS}`${PS}${CP}
#CP=`echo $OPENEJB_HOME/beans/*.jar | tr ' ' ${PS}`${PS}${CP}
#CP=`echo $OPENEJB_HOME/dist/*.jar | tr ' ' ${PS}`

for i in ./dist/*.jar
do
    CP=${CP}${PS}${i}
done

if [ -z "$CLASSPATH" ]; then
    CP=${CP}${PS}${CLASSPATH}
fi

#
# startup options      
#        
OPTIONS="-Dopenejb.home=$OPENEJB_HOME"

echo "--------------SUPPORT INFO-------------"
echo "`uname -srv`"
echo "Using JAVA_HOME:     $JAVA_HOME"
echo "Using OPENEJB_HOME:  $OPENEJB_HOME"
echo "."
#echo "Using OPTIONS:       $OPTIONS"
#echo "Using CLASSPATH:     $CP"
                                      
$JAVA ${OPTIONS} -classpath ${CP} org.openejb.server.Main $@

