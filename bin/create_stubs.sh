#!/bin/sh

#   Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Pizer Chen  <iceant@21cn.com>

if [ -z "$1" ] ; then

    echo ""
    echo "Usage: create_stubs.sh [OPTIONS] [CLASSES] ..."
    echo "Create the CORBA stubs and ties for a bean's remote and "
    echo "home interface."
    echo ""
    echo "Options:"
    echo ""
    echo "  -d DIRECTORY       output the generated class into"
    echo "                     a specific directory"
    echo ""
    echo "Classes:"
    echo "  the full class name of the remote or home interface"
    echo ""
    echo "Run the compiler two times :"
    echo "  - one for the remote interface"
    echo "  - another one for the home interface"
    echo ""
    echo "Example:"
    echo "  create_stubs.sh org.openejb.test.beans.DatabaseHome"
    echo "  create_stubs.sh -d test/src org.openejb.test.beans.DatabaseHome" 
    echo ""
    echo "The class specified must be in the classpath before"
    echo "running create_stub.sh"
    echo ""
    exit
fi

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

case "`uname`" in
  CYGWIN*) PS=";";;
esac

# Setup Classpath
CP=$CLASSPATH
CP=`echo lib/*.jar | tr ' ' ${PS}`
CP=`echo dist/*.jar | tr ' ' ${PS}`${PS}${CP}
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CLASSPATH=lib/xerces-J_1.3.1.jar${PS}${CP}

$JAVA $OPTIONS -classpath $CLASSPATH org.openorb.rmi.compiler.JavaToIdl -tie -stub -noidl -local $@
#java org.openorb.rmi.compiler.JavaToIdl -tie -stub -noidl  
