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

if [ -z `uname -s` ]; then
    echo "Cannot determine your host operating system."
    exit 1
elif [ `uname -s` = "CYGWIN_NT-5.0" -o `uname -s` = "cygwin32" -o `uname -s` = "cygwin" ]; then
    PS=";"
fi

# Setup Classpath
CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CP=test/lib/junit_3.5.jar${PS}${CP}
CP=test/lib/idb_3.26.jar${PS}${CP}
CP=`echo lib/*.jar | tr ' ' ${PS}`${PS}${CP}
CP=`echo dist/*.jar | tr ' ' ${PS}`${PS}${CP}
CP=lib/xerces-J_1.3.1.jar${PS}${CP}
CLASSPATH=$CP


#######################################
# Launch the OpenEJB/CORBA adapter
OPENORB_OPTIONS=" -Djava.naming.provider.url=corbaloc::localhost:2001/NameService -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl -Djavax.rmi.CORBA.UtilClass=org.openejb.corba.core.UtilDelegateImpl -Dorg.openejb.corba.core.UtilDelegateClass=org.openorb.rmi.system.UtilDelegateImpl -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl"

#-----------------------------------------------------------
# Classic Assembler w/ new Configuration Factory 
#-----------------------------------------------------------
# The location of an OpenEJB .conf configuration file.
#
#OPENEJB_OPTION_1="-Dorg/openejb/configuration_source=conf/default.openejb.conf"
#OPENEJB_OPTION_X="-Dorg/openejb/configuration_factory=org.openejb.alt.config.ConfigurationFactory"

#-----------------------------------------------------------
# Classic Assembler w/ XML Configuration Factory 
#-----------------------------------------------------------
# The location of an OpenEJB xml configuration file.
#
#OPENEJB_OPTION_1="-Dorg/openejb/configuration_source=test/conf/openejb-server-config.xml"


#-----------------------------------------------------------
# Thread Context
#-----------------------------------------------------------
# By default the ThreadContext class uses its own class 
# definition for instances but this can overriden by binding
# this variable to fully qualified class name of a type that 
# subclasses ThreadContext. The binding should be added to 
# the System Properties.
#
# Using the thread class below will break transactions. DOnt' use it
#OPENEJB_OPTION_2="-Dorg/openejb/core/ThreadContext/IMPL_CLASS=org.openejb.sp.tyrex.TyrexThreadContext"


#-----------------------------------------------------------
# Logging Configuration
#-----------------------------------------------------------
# Specifies the file to use as Log4j's configuration.
#
OPENEJB_OPTION_3="-Dlog4j.configuration=file:conf/default.logging.conf"

#-----------------------------------------------------------
# Testing Server helper  -- For Testing Only
#-----------------------------------------------------------
# Used when running the test suite on this server.
# The test server class helps the server gather the needed
# information about how to connect with this server.
#
OPENEJB_OPTION_4="test.server.class=org.openejb.test.CorbaTestServer"

######################################
# startup options      
#        
OPTIONS="$OPENEJB_OPTION_X $OPENEJB_OPTION_1 $OPENEJB_OPTION_2 $OPENEJB_OPTION_3 $OPENORB_OPTIONS"

$JAVA $OPTIONS -classpath $CLASSPATH org.openejb.corba.Server -ORBProfile=ejb 
