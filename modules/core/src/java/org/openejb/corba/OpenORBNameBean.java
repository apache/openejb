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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openorb.ins.Server;
import org.openorb.ins.Service;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import EDU.oswego.cs.dl.util.concurrent.Executor;


/**
 * @version $Revision$ $Date$
 */
public class OpenORBNameBean implements GBeanLifecycle {

    private final Log log = LogFactory.getLog(OpenORBNameBean.class);

    private final ClassLoader classLoader;
    private final Server server;
    private final Executor threadPool;
    private ArrayList args = new ArrayList();
    private Properties props = new Properties();

    public OpenORBNameBean() {
        this.classLoader = null;
        this.server = null;
        this.threadPool = null;
        this.args = null;
        this.props = null;
    }

    public OpenORBNameBean(ClassLoader classLoader, Executor threadPool, ArrayList args, Properties props) {
        this.server = new Server();
        this.classLoader = classLoader;
        this.threadPool = threadPool;
        this.args = args;
        this.props = props;

        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            Options options = new Options();
            // persistenceType
            options.addOption(OptionBuilder.withArgName(Service.OPT_PERSISTENCE_ARG_NAME)
                              .hasArg()
                              .withDescription(Service.OPT_PERSISTENCE_DESCRIP)
                              .withLongOpt(Service.OPT_PERSISTENCE_LONG)
                              .create(Service.OPT_PERSISTENCE));
            // shutdown
            options.addOption(new Option(Service.OPT_SHUTDOWN_ROOT,
                                         Service.OPT_SHUTDOWN_ROOT_LONG, false,
                                         Service.OPT_SHUTDOWN_ROOT_DESCRIP));

            // make sure to import the pss config
            props.put("ImportModule.pss", "${openorb.home}config/pss.xml#pss");
            server.init((String[]) args.toArray(new String[args.size()]), options, props);
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }
    }

    public ArrayList getArgs() {
        return args;
    }

    public Properties getProps() {
        return props;
    }

    public void doStart() throws Exception {
        threadPool.execute(new Runnable() {
            public void run() {
                Thread.currentThread().setContextClassLoader(classLoader);
                server.run();
            }
        });

        log.info("Started OpenORBNameBean");
    }

    public void doStop() throws Exception {
        server.getORB().shutdown(true);

        log.info("Stopped OpenORBNameBean");
    }

    public void doFail() {
        log.info("Failed OpenORBNameBean");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(OpenORBNameBean.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addReference("ThreadPool", Executor.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addAttribute("args", ArrayList.class, true);
        infoFactory.addAttribute("props", Properties.class, true);

        infoFactory.setConstructor(new String[]{"classLoader", "ThreadPool", "args", "props"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
