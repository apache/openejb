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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.deployment.mdb;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.openejb.deployment.DeploymentHelper;
import org.openejb.deployment.MdbBuilder;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
public class BasicMdbContainerTest extends DeploymentHelper {
    protected void setUp() throws Exception {
        super.setUp();

        MdbBuilder builder = new MdbBuilder();
        builder.setContainerId(CONTAINER_NAME.toURI().toString());
        builder.setEjbName("MockEJB");

        builder.setEndpointInterfaceName("javax.jms.MessageListener");
        builder.setBeanClassName(MockEJB.class.getName());

        builder.setActivationSpecName(activationSpecName);

        builder.setEjbContainerName(mdbEjbContainerName);

        GBeanData deployment = builder.createConfiguration();

        //start the ejb container
        ConfigurationData configurationData = new ConfigurationData(TEST_CONFIGURATION_ID, kernel.getNaming());
        configurationData.getEnvironment().addDependency(new Dependency(BOOTSTRAP_ID, ImportType.ALL));
        configurationData.addGBean(deployment);
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(TEST_CONFIGURATION_ID);
    }

    public void testMessage() throws Exception {
        // @todo put a wait limit in here... otherwise this can lock a build
        // Wait for 3 messages to arrive..
        System.out.println("Waiting for message 1");
        assertTrue(MockEJB.messageCounter.tryAcquire(10000, TimeUnit.MILLISECONDS));
        System.out.println("Waiting for message 2");
        assertTrue(MockEJB.messageCounter.tryAcquire(10000, TimeUnit.MILLISECONDS));
        System.out.println("Waiting for message 3");
        assertTrue(MockEJB.messageCounter.tryAcquire(10000, TimeUnit.MILLISECONDS));

        System.out.println("Done.");
        assertTrue("Timer should have fired once by now...", MockEJB.timerFired);
    }
}
