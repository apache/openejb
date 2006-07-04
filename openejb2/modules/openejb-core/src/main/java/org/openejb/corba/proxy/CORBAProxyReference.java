/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.corba.proxy;

import java.net.URI;
import javax.naming.NameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.reference.ConfigurationAwareReference;


/**
 * @version $Revision$ $Date$
 */
public final class CORBAProxyReference extends ConfigurationAwareReference {

    private final static Log log = LogFactory.getLog(CORBAProxyReference.class);

    private final URI nsCorbaloc;
    private final String objectName;
    private final String home;

    public CORBAProxyReference(Artifact configId, AbstractNameQuery abstractNameQuery, URI nsCorbaloc, String objectName, String home) {
        super(configId, abstractNameQuery);
        this.nsCorbaloc = nsCorbaloc;
        this.objectName = objectName;
        this.home = home;
        if (log.isDebugEnabled()) {
            log.debug("<init> " + nsCorbaloc.toString() + ", " + objectName + ", " + abstractNameQuery + ", " + home);
        }
    }

    public String getClassName() {
        return home;
    }

    public Object getContent() throws NameNotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Obtaining home from " + nsCorbaloc.toString() + ", " + objectName + ", " + abstractNameQueries + ", " + home);
        }
        AbstractName containerName;
        try {
            containerName = resolveTargetName();
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException) new NameNotFoundException("Could not resolve gbean from name query: " + abstractNameQueries).initCause(e);
        }
        Kernel kernel = getKernel();
        Object proxy;
        try {
            //TODO configid objectname might well be wrong kind of thing.
            proxy = kernel.invoke(containerName, "getHome", new Object[]{nsCorbaloc, objectName}, new String[]{URI.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.error("Could not get proxy from " + containerName, e);
            throw (IllegalStateException) new IllegalStateException("Could not get proxy").initCause(e);
        }
        if (proxy == null) {
            log.error("Proxy not returned from " + containerName);
            throw new IllegalStateException("Proxy not returned. Target " + containerName + " not started");
        }
        if (!org.omg.CORBA.Object.class.isAssignableFrom(proxy.getClass())) {
            log.error("Proxy not an instance of expected class org.omg.CORBA.Object from " + containerName);
            throw new ClassCastException("Proxy not an instance of expected class org.omg.CORBA.Object");
        }
        return proxy;
    }
}
