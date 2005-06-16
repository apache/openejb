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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.client.EJBMetaDataImpl;
import org.openejb.client.JNDIRequest;
import org.openejb.client.JNDIResponse;
import org.openejb.client.RequestMethods;
import org.openejb.client.ResponseCodes;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
class JndiRequestHandler  implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;

    javax.naming.Context clientJndi;

    JndiRequestHandler(EjbDaemon daemon) throws Exception{
        clientJndi = (javax.naming.Context)OpenEJB.getJNDIContext().lookup("openejb/ejb");
        this.daemon = daemon;
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        JNDIRequest  req = new JNDIRequest();
        JNDIResponse res = new JNDIResponse();
        req.readExternal( in );

        // We are assuming that the request method is JNDI_LOOKUP
        // TODO: Implement the JNDI_LIST and JNDI_LIST_BINDINGS methods

        String name = req.getRequestString();
        if ( name.startsWith("/") ) name = name.substring(1);

        DeploymentInfo deployment = daemon.deploymentIndex.getDeployment(name);

        if (deployment == null) {
            try {
                Object obj = clientJndi.lookup(name);

                if ( obj instanceof Context ) {
                    res.setResponseCode( JNDI_CONTEXT );
                } else res.setResponseCode( JNDI_NOT_FOUND );

            } catch (NameNotFoundException e) {
                res.setResponseCode(JNDI_NOT_FOUND);
            } catch (NamingException e) {
                res.setResponseCode(JNDI_NAMING_EXCEPTION);
                res.setResult( e );
            }
        } else {
            res.setResponseCode( JNDI_EJBHOME );
            EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                           deployment.getRemoteInterface(),
                                                           deployment.getPrimaryKeyClass(),
                                                           deployment.getComponentType(),
                                                           deployment.getDeploymentID().toString(),
                                                           this.daemon.deploymentIndex.getDeploymentIndex(name));
            res.setResult( metaData );
        }

        res.writeExternal( out );
    }
}