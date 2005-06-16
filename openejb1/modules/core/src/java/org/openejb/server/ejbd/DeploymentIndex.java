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

import java.rmi.RemoteException;
import java.util.HashMap;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.client.EJBRequest;
import org.openejb.util.Messages;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class DeploymentIndex {

    Messages messages = new Messages( "org.openejb.server.ejbd" );

    DeploymentInfo[] deployments    = null;
    
    HashMap index = null;

    public DeploymentIndex(){
        DeploymentInfo[] ds = OpenEJB.deployments();

        // This intentionally has the 0 index as null. The 0 index is the
        // default value of an unset deploymentCode.
        deployments = new DeploymentInfo[ ds.length +1 ];

        System.arraycopy( ds, 0, deployments, 1, ds.length);

        index = new HashMap( deployments.length );
        for (int i=1; i < deployments.length; i++){
            index.put( deployments[i].getDeploymentID(), new Integer(i));
        }
    }

    public DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {
        // This logic could probably be cleaned up quite a bit.

        DeploymentInfo info = null;

        if (req.getDeploymentCode() > 0 && req.getDeploymentCode() < deployments.length) {
            info = deployments[ req.getDeploymentCode() ];
            if ( info == null ) {
                throw new RemoteException("The deployement with this ID is null");
            }
            req.setDeploymentId((String) info.getDeploymentID() );
            return info;
        }

        if ( req.getDeploymentId() == null ) {
            throw new RemoteException("Invalid deployment id and code: id="+req.getDeploymentId()+": code="+req.getDeploymentCode());
        }

        int idCode = getDeploymentIndex( req.getDeploymentId() );

        if ( idCode == -1 ) {
            throw new RemoteException("No such deployment id and code: id="+req.getDeploymentId()+": code="+req.getDeploymentCode());
        }

        req.setDeploymentCode( idCode );

        if (req.getDeploymentCode() < 0 || req.getDeploymentCode() >= deployments.length){
            throw new RemoteException("Invalid deployment id and code: id="+req.getDeploymentId()+": code="+req.getDeploymentCode());
        }

        info = deployments[ req.getDeploymentCode() ];
        if ( info == null ) {
            throw new RemoteException("The deployement with this ID is null");
        }
        return info;
    }

    public int getDeploymentIndex(DeploymentInfo deployment){
        return getDeploymentIndex( (String)deployment.getDeploymentID() );
    }
    
    public int getDeploymentIndex(String deploymentID){
        Integer idCode = (Integer)index.get( deploymentID );
        
        return ( idCode == null )? -1: idCode.intValue();
    }
    
    public DeploymentInfo getDeployment(String deploymentID){
        return getDeployment(getDeploymentIndex(deploymentID));
    }
    
    public DeploymentInfo getDeployment(Integer index){
        return (index == null)? null: getDeployment(index.intValue());
    }
    
    public DeploymentInfo getDeployment(int index){
        return deployments[index];
    }
}


