/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core;


/**
 * Class to encapsulate the configuration options available in the core implementation
 * of the container system
 *
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:dan@danmassey.com">Dan Massey</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EnvProps {
    
    /**
     * Property name that specifies the class name of the ConnectionBuilder a JmsContainer should use.
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>org/openejb/core/jms/JmsConnectionBuilder</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>org.example.asf.ReplacementConnectionBuilder</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.jms.JmsContainer}
     * </UL>
     * </p>
     */
    public final static String CB_CLASS_NAME = "org.openejb.core.jms.JmsConnectionBuilder";
    
    /**
     * Property name that specifies the class name of the InstanceManager a Container should use.
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>org/openejb/core/InstanceManager/CLASS_NAME</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>org.openejb.core.entity.EntityInstanceManager</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.entity.EntityContainer}
     * <LI>{@link org.openejb.core.stateful.StatefulContainer}
     * <LI>{@link org.openejb.core.stateless.StatelessContainer}
     * </UL>
     * </p>
     */
    public final static String IM_CLASS_NAME = "InstanceManager";

    /**
     * Property name that specifies the time to wait between invocations.
     * 
     * <p>
     * For the StatefulInstanceManager, this value is measured in minutes.  
     * A value of 5 would result in a time-out of 5 minutes between invocations.</p>
     * 
     * <p>
     * For the StatelessInstanceManager, this value is measured in milliseconds.  
     * A value of 5 would result in a time-out of 5 milliseconds between invocations.</p>
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>TimeOut</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>5</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.stateful.StatefulInstanceManager}
     * <LI>{@link org.openejb.core.stateless.StatelessInstanceManager}
     * </UL>
     * </p>
     */
    public final static String IM_TIME_OUT = "TimeOut";

    /**
     * Property name that specifies the path prefix for directories created by bean passivation
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>org/openejb/core/InstanceManager/PASSIVATOR_PATH_PREFIX</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>/tmp/openejb</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.stateful.StatefulInstanceManager}
     * </UL>
     * </p>
     */
    public final static String IM_PASSIVATOR_PATH_PREFIX = "org/openejb/core/InstanceManager/PASSIVATOR_PATH_PREFIX";

    /**
     * Property name that specifies the size of the bean pools
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>PoolSize</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>100</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.entity.EntityInstanceManager}
     * <LI>{@link org.openejb.core.stateful.StatefulInstanceManager}
     * <LI>{@link org.openejb.core.stateless.StatelessInstanceManager}
     * <LI>{@link org.openejb.core.jms.JmsContainer}
     * </UL>
     * </p>
     */
    public final static String IM_POOL_SIZE = "PoolSize";

    /**
     * Property name that specifies the number of instances to passivate at one time when doing bulk passivation.
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>BulkPassivate</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>25</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.stateful.StatefulContainer}
     * </UL>
     * </p>
     */
    public final static String IM_PASSIVATE_SIZE = "BulkPassivate";

    /**
     * Property name that specifies the class name of the PassivationStrategy an InstanceManager
     * should use to passivate bean instances.
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>org/openejb/core/InstanceManager/PASSIVATOR</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>org.openejb.core.stateful.RAFPassivater</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.stateful.StatefulContainer}
     * </UL>
     * </p>
     * 
     * @see org.openejb.core.stateful.PassivationStrategy
     * @see org.openejb.core.stateful.RAFPassivater
     * @see org.openejb.core.stateful.SimplePassivater
     */
    public final static String IM_PASSIVATOR = "Passivator";


    /**
     * Not yet used
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>org/openejb/core/InstanceManager/CONCURRENT_ATTEMPTS</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code></code>
     * </p>
     * 
     * <p>
     * USED BY:<br>
     * Not yet used
     * </p>
     */
    public final static String IM_CONCURRENT_ATTEMPTS = "org/openejb/core/InstanceManager/CONCURRENT_ATTEMPTS";

    /**
     * Property name that specifies the whether or not to use a strict pooling algorithm.
     * 
     * <p>
     * NAME:<br>&nbsp;&nbsp;
     * <code>StrictPooling</code>
     * </p>
     * 
     * <p>
     * EXAMPLE VALUE:<br>&nbsp;&nbsp;
     * <code>true</code>
     * </p>
     * 
     * <p>
     * USED BY:
     * <UL>
     * <LI>{@link org.openejb.core.stateless.StatelessContainer}
     * </UL>
     * </p>
     */
    public final static String IM_STRICT_POOLING = "StrictPooling";

    /**
    * By default the ThreadContext class uses its own class definition for instances but this can 
    * overriden by binding this variable to fully qualified class name of a type that subclasses ThreadContext.
    * The binding should be added to the System Properties.
    */
    public final static String THREAD_CONTEXT_IMPL = "org/openejb/core/ThreadContext/IMPL_CLASS";

    /*
    * The EJB 1.1 specification requires that arguments and return values between beans adhere to the
    * Java RMI copy semantics which requires that the all arguments be passed by value (copied) and 
    * never passed as references.  However, it is possible for the system administrator to turn off the
    * copy operation so that arguments and return values are passed by reference as a performance optimization.
    * Simply setting the org.openejb.core.EnvProps.INTRA_VM_COPY property to FALSE will cause  
    * IntraVM to bypass the copy operations; arguments and return values will be passed by reference not value. 
    * This property is, by default, alwasy TRUE but it can be changed to FALSE by setting it as a System property
    * or a property of the Property argument when invoking OpenEJB.init(props).
    */
    public final static String INTRA_VM_COPY = "org/openejb/core/ivm/BaseEjbProxyHandler/INTRA_VM_COPY";

	/** The JdbcDriver string for a connector */
	public static final String JDBC_DRIVER = "JdbcDriver";

	/** The JdbcUrl string for a connector */
	public static final String JDBC_URL = "JdbcUrl";

	/** The UserName string for a connector */
	public static final String USER_NAME = "UserName";

	/** The Password string for a connector */
	public static final String PASSWORD = "Password";

	/** The Global_TX_Database for CMP beans */
	public static final String GLOBAL_TX_DATABASE = "Global_TX_Database";

	/** The Local_TX_Database for CMP beans */
	public static final String LOCAL_TX_DATABASE = "Local_TX_Database";
}