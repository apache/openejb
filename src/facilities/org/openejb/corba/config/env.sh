# ===============================================================================
# = Set the properties to specify classpath
# =

# = --------------------------- OPENORB -----------------------------
# = The OpenORB lib directory

export OPENORB_LIB=xxxxxx

# = The OpenORB directory where the OpenORB Jar files are available

export OPENORB_DIST=xxxxx

# = ------------------------ RMI over IIOP --------------------------
# = The RMI over IIOP lib directory

export RMI_LIB=xxxx

# = The RMI over IIOP directory where the RMI over IIOP Jar files are available

export RMI_DIST=xxxx

# = ---------------------------- TYREX ------------------------------
# = The Tyrex lib directory

export TYREX_LIB=xxxx

# = The Tyrex directory where the Tyrex Jar files are available

export TYREX_DIST=xxxx

# = --------------------------- OPENEJB -----------------------------
# = The OpenEJB lib directory

export OPENEJB_LIB=xxxx

# = The OpenEJB test lib directory

export OPENEJB_TEST_LIB=xxxx

# = The OpenEJB directory where the OpenEJB Jar files are available

export OPENEJB_DIST=xxxxx

# ===============================================================================


export OPENORBCP=$OPENORB_LIB/xerces-J_1.3.1.jar:$OPENORB_DIST/openorb-1.0.2.jar:$OPENORB_DIST/openorb_tools-1.0.2.jar

export RMICP=$RMI_LIB/jndi_1.2.1.jar:$RMI_DIST/openorb_rmi-1.1.0.jar

export TYREXCP=$TYREX_LIB/castor-0.9.1.jar:$TYREX_LIB/jdbc-se2.0.jar:$TYREX_LIB/jaas_1.0.jar:$TYREX_LIB/jca_1.0.jar:$TYREX_LIB/jta_1.0.1.jar:$TYREX_LIB/ldapjdk.jar:$TYREX_LIB/log4J_1.0.4.jar:$TYREX_LIB/ots-jts_1.0.jar:$TYREX_LIB/xslp_1.1.jar:$TYREX_DIST/tyrex-0.9.8.jar:$TYREX_DIST/tyrex-0.9.8-iiop.jar

export OPENEJBCP=$OPENEJB_LIB/ejb-1.0.jar:$OPENEJB_LIB/ejb-2.0.jar:$OPENEJB_LIB/jdbc2_0-stdext.jar:$OPENEJB_LIB/jdk12-proxies.jar:$OPENEJB_LIB/jms_1.0.2a.jar:$OPENEJB_LIB/minerva.jar:$OPENEJB_DIST/openejb-0.7.jar:$OPENEJB_DIST/openejb-corba-0.7.jar:$OPENEJB_DIST/openejb-tests-0.7.jar:$OPENEJB_DIST/openejb-ri-0.7.jar:OPENEJB_TEST_LIB/idb_3.26.jar

export CLASSPATH=$OPENORBCP:$RMICP:$TYREXCP:$OPENEJBCP:$OPENORB_LIB/junit_3.5.jar


