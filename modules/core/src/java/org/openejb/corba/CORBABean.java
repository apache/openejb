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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.pool.ThreadPool;
import EDU.oswego.cs.dl.util.concurrent.Executor;


/**
 * @version $Revision$ $Date$
 */
public class CORBABean implements GBeanLifecycle {

    private final Log log = LogFactory.getLog(CORBABean.class);

    private final ClassLoader classLoader;
    private final Executor threadPool;
    private ORB orb;
    private POA rootPOA;
    private ArrayList args = new ArrayList();
    private Properties props = new Properties();

    public CORBABean() {
        this.classLoader = null;
        this.threadPool = null;
    }

    public CORBABean(ClassLoader classLoader, Executor threadPool) {
        this.classLoader = classLoader;
        this.threadPool = threadPool;
    }

    public ORB getORB() {
        return orb;
    }

    public POA getRootPOA() {
        return rootPOA;
    }

    public ArrayList getArgs() {
        return args;
    }

    public void setArgs(ArrayList args) {
        this.args = args;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public void doStart() throws Exception {

        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            orb = ORB.init((String[]) args.toArray(new String[args.size()]), props);

            org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");

            rootPOA = POAHelper.narrow(obj);

            threadPool.execute(new Runnable() {
                public void run() {
                    orb.run();
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.info("Started CORBABean");
    }

    public void doStop() throws Exception {
        orb.shutdown(true);
        log.info("Stopped CORBABean");
    }

    public void doFail() {
        log.info("Failed CORBABean");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(CORBABean.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addReference("ThreadPool", Executor.class);
        infoFactory.addAttribute("ORB", ORB.class, false);
        infoFactory.addAttribute("rootPOA", POA.class, false);
        infoFactory.addAttribute("args", ArrayList.class, true);
        infoFactory.addAttribute("props", Properties.class, true);

        infoFactory.setConstructor(new String[]{"classLoader", "ThreadPool"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
