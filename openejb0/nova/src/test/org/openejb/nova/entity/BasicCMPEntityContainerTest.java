/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.entity;

import java.net.URI;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import junit.framework.TestCase;

import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.entity.cmp.CMPEntityContainer;
import org.openejb.nova.util.ServerUtil;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BasicCMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private MBeanServer mbServer;
    private EntityContainerConfiguration config;
    private CMPEntityContainer container;

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        assertEquals(2, home.intMethod(1));

//        MockLocal local = home.findByPrimaryKey(new Integer(1));
//        assertEquals(3, local.intMethod(1));
//        assertEquals(1, local.getIntField());
    }

    protected void setUp() throws Exception {
        super.setUp();
        mbServer = ServerUtil.newRemoteServer();

        config = new EntityContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockCMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.txnManager = new MockTransactionManager();
        config.pkClassName = Integer.class.getName();

        container = new CMPEntityContainer(config);
        mbServer.registerMBean(container, CONTAINER_NAME);
        container.start();
    }

    protected void tearDown() throws Exception {
        container.stop();
        ServerUtil.stopRemoteServer(mbServer);
        super.tearDown();
    }
}
