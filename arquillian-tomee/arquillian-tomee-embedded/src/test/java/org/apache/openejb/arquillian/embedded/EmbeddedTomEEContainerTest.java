package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.arquillian.embedded.AnEJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;

import static junit.framework.Assert.assertNotNull;

/**
 * @author rmannibucau
 */
@RunWith(Arquillian.class)
@RunAsClient
public class EmbeddedTomEEContainerTest {
    @Deployment public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                    .addClass(AnEJB.class)
                    .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                                                   .version("3.0").exportAsString()));
    }

    @EJB private AnEJB ejb;

    @Test public void testEjbIsNotNull() throws Exception {
    	assertNotNull(ejb);
    }
}
