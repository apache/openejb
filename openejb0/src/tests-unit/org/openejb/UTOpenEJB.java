package org.openejb;

import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.openejb.spi.ApplicationServer;
import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTOpenEJB extends NamedTestCase{
    
    ApplicationServer server;
    
    public UTOpenEJB(){
        super("org.openejb.OpenEJB.");
    }
    
    public void setUp() throws Exception{
        server = new ApplicationServer(){
                public EJBObject getEJBObject(ProxyInfo proxyInfo) {
                    return null;
                }
                public EJBHome getEJBHome(ProxyInfo proxyInfo) {
                    return null;
                }
                public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo) {
                    return null;
                }
                public Handle getHandle(ProxyInfo proxyInfo) {
                    return null;
                }
            };
    }
    
    public void test01_createInstance(){
        OpenEJB openEJB = new OpenEJB();
    }
        
    public void test02_deployments(){
        DeploymentInfo[] dis = OpenEJB.deployments();
        assertNotNull("The deployment array is null.",  dis );
        assert("The deployment array is empty.", (dis.length > 0));
    }
    
    public void test03_getDeploymentInfo(){
        DeploymentInfo[] dis = OpenEJB.deployments();
        DeploymentInfo di = OpenEJB.getDeploymentInfo(dis[0].getDeploymentID());
        assertNotNull("The DeploymentInfo object is null.",  di );
        assertEquals("The DeploymentInfo objects are not equal.", di, dis[0]);
    }
    
    public void test04_containers(){
        Container[] cs = OpenEJB.containers();
        assertNotNull("The container array is null.",  cs );
        assert("The container array is empty.", (cs.length > 0));
    }
    
    public void test05_getContainer(){
        Container[] cs = OpenEJB.containers();
        Container c = OpenEJB.getContainer(cs[0].getContainerID());
        assertNotNull("The Container object is null.",  c );
        assertEquals("The Container objects are not equal.", c, cs[0]);
    }
    
    
    public void test06_init(){
        try{
            OpenEJB.init(new Properties(), server);
            
        } catch (OpenEJBException e){
            if ( !e.getMessage().equals("OpenEJB already initiated") ){
                server = null;
                assert("Received Exception "+e.getClass()+ " : "+e.getMessage(), false);
            }
        }
    }
    
    public void test07_init(){
        Object expected = server;
        Object actual = OpenEJB.getApplicationServer();
        assertEquals(expected, actual);
    }
}


