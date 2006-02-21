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
package org.openejb.server;

import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceException;
import org.activeio.xnet.ServiceLogger;
import org.activeio.xnet.ServicePool;
import org.activeio.xnet.SocketService;
import org.activeio.xnet.hba.IPAddressPermission;
import org.activeio.xnet.hba.ServiceAccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.ClassLoading;
import org.openejb.DeploymentIndex;
import org.openejb.OpenEJB;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;

/**
 * @version $Revision$ $Date$
 */
public class SimpleSocketService implements SocketService, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(SimpleSocketService.class);
    private final ServerService server;

    public SimpleSocketService(String serviceClassName, IPAddressPermission[] onlyFrom, DeploymentIndex deploymentIndex, ClassLoader cl) throws Exception {
        ServerService service;

        Class serviceClass = ClassLoading.loadClass(serviceClassName, cl);
        if (!serviceClass.isAssignableFrom(serviceClass)) {
            throw new ServiceException("Server service class does not implement " + ServerService.class.getName() + ": " + serviceClassName);
        }
        try {
            Constructor constructor = serviceClass.getConstructor(new Class[]{DeploymentIndex.class});
            service = (ServerService) constructor.newInstance(new Object[]{deploymentIndex});
        } catch (Exception e) {
            throw new ServiceException("Error constructing server service class", e);
        }

        String name = "ejb";
        int threads = 20;
        int priority = Thread.NORM_PRIORITY;
        String[] logOnSuccess = new String[]{"HOST", "NAME", "THREADID", "USERID"};
        String[] logOnFailure = new String[]{"HOST", "NAME"};

        service = new ServicePool(service, name, threads, priority);
        service = new ServiceAccessController(name, service, onlyFrom);
        service = new ServiceLogger(name, service, logOnSuccess, logOnFailure);
        server = service;

        // TODO Horrid hack, the concept needs to survive somewhere
        if (OpenEJB.getApplicationServer() == null) {
            OpenEJB.setApplicationServer(new ServerFederation());
        }
    }

    public synchronized void doStart() throws ServiceException {
        server.start();
    }

    public synchronized void doStop() throws ServiceException {
        server.stop();
    }

    public void doFail() {
        try {
            server.stop();
        } catch (ServiceException e) {
            log.error("Could not clean up simple socket service");
        }
    }

    public void service(Socket socket) throws ServiceException, IOException {
        server.service(socket);
    }

    public String getName() {
        return server.getName();
    }

}
