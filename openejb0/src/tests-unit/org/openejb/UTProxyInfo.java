package org.openejb;

import org.openejb.test.NamedTestCase;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class UTProxyInfo extends NamedTestCase{
    
    public UTProxyInfo(){
        super("org.openejb.ProxyInfo.");
    }

    public void test01_constructor(){
        ProxyInfo proxyInfo = new ProxyInfo();
    }
}


