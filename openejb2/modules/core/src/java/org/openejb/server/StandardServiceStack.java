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
package org.openejb.server;

import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;

import org.apache.geronimo.gbean.GBeanLifecycle;

import EDU.oswego.cs.dl.util.concurrent.Executor;

public class StandardServiceStack implements GBeanLifecycle {

    private String name;

    private ServiceDaemon daemon;
    private ServiceLogger logger;
    private ServiceAccessController hba;
    private ServicePool pool;
    private ServerService server;

    public StandardServiceStack(String name, int port, InetAddress address, InetAddress[] allowHosts, String[] logOnSuccess, String[] logOnFailure, Executor executor, ServerService server) {
        this.server = server;
        this.name = name;
        this.pool = new ServicePool(server, executor);
        this.hba = new ServiceAccessController(name, pool, allowHosts);
        this.logger = new ServiceLogger(name, hba, logOnSuccess, logOnFailure);
        this.daemon = new ServiceDaemon(name, logger, address, port);

    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return daemon.getAddress();
    }

    public int getPort() {
        return daemon.getPort();
    }

    public int getSoTimeout() throws IOException {
        return daemon.getSoTimeout();
    }

    public void setSoTimeout(int timeout) throws SocketException {
        daemon.setSoTimeout(timeout);
    }

    public String[] getLogOnSuccess() {
        return logger.getLogOnSuccess();
    }

    public String[] getLogOnFailure() {
        return logger.getLogOnFailure();
    }

    public InetAddress[] getAllowHosts() {
        return hba.getAllowHosts();
    }

    public void setAllowHosts(InetAddress[] allowHosts) {
        hba.setAllowHosts(allowHosts);
    }

    public void doStart() throws Exception {
        daemon.doStart();
    }

    public void doStop() throws Exception {
        daemon.doStop();
    }

    public void doFail() {
        daemon.doFail();
    }
}
