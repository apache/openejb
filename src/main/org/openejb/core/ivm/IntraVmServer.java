package org.openejb.core.ivm;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.openejb.ProxyInfo;
import org.openejb.core.entity.EntityEjbHomeHandler;
import org.openejb.core.stateful.StatefulEjbHomeHandler;
import org.openejb.core.stateless.StatelessEjbHomeHandler;
import org.openejb.util.proxy.ProxyManager;

/**
 * <h2><b>REMOTE to LOCAL SERIALIZATION</b></h2> <p>
 * 
 * <i>Definition:</i><p>
 *     This is a serialization that initiates in a remote client vm 
 *     and finishes in same vm as OpenEJB and the Application Server.
 * <p>
 * <i>Circumstances:</i><p>
 *     When a remote ApplicationServer-specific implementation of a 
 *     javax.ejb.* interface is serialized outside the scope of the 
 *     remote client vm and deserializes in the Application Server 
 *     and OpenEJB IntraVM Server's vm.
 * <p>
 *     These serializations happen, for example, when javax.ejb.* 
 *     objects are used as parameters to an EJBObject method either 
 *     directly or nested in another object.
 * <p>
 * <i>Action:</i><p>
 *     No special action is required by the OpenEJB specification.
 * <p>
 * <i>Notes on Optimization:</i><p>    
 *     For optimization purposes, the ApplicationServer can replace 
 *     its javax.ejb.* interface implementations with those created 
 *     by the IntraVM Server via the IntraVM's own implementation 
 *     of the org.openejb.spi.ApplicationServer interface.
 * <p>
 *     The Application Server may wish to this by implementing the 
 *     serialization readResolve method in it's javax.ejb.* 
 *     implementations.  Then, when these implementations are 
 *     deserialized, certain checks can be made to determine if they 
 *     in the same VM as the Application Server and OpenEJB IntraVM 
 *     Server. If they are, it can replace itself with the 
 *     equivalent IntraVM Server implementation.  An easy way for an 
 *     implementation to determine which VM it is in is to simply 
 *     set a system variable that is only present in the Application 
 *     Server's VM.
 * <p>    
 *     This is identical to how the IntraVM Server replaces its 
 *     javax.ejb.* interface implementations with those created by 
 *     the ApplicationServer when objects are leaving the local VM.  
 * <p>
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class IntraVmServer implements org.openejb.spi.ApplicationServer{
    
    
    public EJBMetaData getEJBMetaData(ProxyInfo pi) {
        org.openejb.DeploymentInfo di = pi.getDeploymentInfo();        
        IntraVmMetaData metaData = new IntraVmMetaData(di.getHomeInterface(), di.getRemoteInterface(),di.getComponentType());
        
        metaData.setEJBHome( getEJBHome(pi) );
        return metaData;
    }
    
    
    public Handle getHandle(ProxyInfo pi) {
        return new IntraVmHandle(getEJBObject(pi));
    }
    
    public HomeHandle getHomeHandle(ProxyInfo pi) {
        return new IntraVmHandle(getEJBHome(pi));
    }
    
    public EJBObject getEJBObject(ProxyInfo pi) {
        EjbHomeProxyHandler handler = null;
        return (EJBObject)getEjbHomeHandler(pi).createProxy(pi);
    }
    
    
    public EJBHome getEJBHome(ProxyInfo pi) {
        
        if (pi.getDeploymentInfo() instanceof org.openejb.core.DeploymentInfo) {
            org.openejb.core.DeploymentInfo coreDeployment = (org.openejb.core.DeploymentInfo) pi.getDeploymentInfo();
            return coreDeployment.getEJBHome();
        
        } else {
            try{
                Class[] interfaces = new Class[]{ pi.getDeploymentInfo().getHomeInterface(), org.openejb.core.ivm.IntraVmProxy.class };
                return (javax.ejb.EJBHome)ProxyManager.newProxyInstance( interfaces , getEjbHomeHandler(pi) );
            }catch(Exception e){
                throw new RuntimeException("Can't create EJBHome stub" + e.getMessage());
            }
        }
    }

    private EjbHomeProxyHandler getEjbHomeHandler(ProxyInfo pi){
        
        switch (pi.getDeploymentInfo().getComponentType()) {
            
            case org.openejb.DeploymentInfo.BMP_ENTITY:
            case org.openejb.DeploymentInfo.CMP_ENTITY:
                return new EntityEjbHomeHandler(pi.getBeanContainer(),pi.getPrimaryKey(),pi.getDeploymentInfo().getDeploymentID());
            
            case org.openejb.DeploymentInfo.STATEFUL:
                return new StatefulEjbHomeHandler(pi.getBeanContainer(),pi.getPrimaryKey(),pi.getDeploymentInfo().getDeploymentID());
            
            case org.openejb.DeploymentInfo.STATELESS:
                return new StatelessEjbHomeHandler(pi.getBeanContainer(),pi.getPrimaryKey(),pi.getDeploymentInfo().getDeploymentID());
            default:
                throw new RuntimeException("Unknown EJB type: "+pi.getDeploymentInfo());
        }
    }
}
