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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.ui.jedi.openejb11;

import org.openejb.ui.jedi.openejb11.ejb.OpenEjbPlugin;
import org.openejb.ui.jedi.openejb11.jca.JcaPlugin;
import org.openejb.ui.jedi.openejb11.server.ServerPlugin;
import org.opentools.deployer.plugins.J2EEPlugin;
import org.opentools.deployer.plugins.Plugin;
import org.opentools.deployer.plugins.PluginManager;
import org.opentools.deployer.plugins.ServerInterface;
import org.opentools.deployer.plugins.j2ee12.J2ee12Plugin;

/**
 * Top-level plugin for JEDI.  This defines the name that shows up on the
 * plugins menu, and the sub-plugins used to handle server XMLs, JARs, RARs,
 * etc.
 * <p>This plugin will be loaded if this class is instantiated.  When packaged,
 * you will see a "META-INF/plugin.properties" file that references this
 * class name so it can be instantiated and loaded.  You can select the
 * default plugins that should be selected on the plugins menu on the
 * command line by settings the system property "deployer.plugins" or by
 * creating a file on the classpath called "deployer.properties" and setting
 * the property "deployer.plugins" in there.  The format of the property is
 * a comma-separated list of plugins, such as "OpenEJB 1.1,J2EE 1.2".</p>
 *
 * @see org.openejb.ui.jedi.server.ServerPlugin
 * @see org.openejb.ui.jedi.ejb.OpenEjbPlugin
 * @see org.openejb.ui.jedi.jca.JcaPlugin
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class OpenEjbJ2EEPlugin implements J2EEPlugin {
    static {
        // Add this plugin to the list of available plugins
        PluginManager.addPlugin(new OpenEjbJ2EEPlugin());
    }

    public OpenEjbJ2EEPlugin() {
    }

    /**
     * The name of the plugin.
     */
    public String getName() {
        return "OpenEJB 1.1";
    }

    /**
     * Other plugins this depends on (namely, J2EE).
     */
    public J2EEPlugin[] getDependencies() {
        return new J2EEPlugin[]{new J2ee12Plugin()};
    }

    /**
     * The plugin for configuring the server itself.
     */
    public Plugin getServerConfigurator() {
        return new ServerPlugin();
    }

    /**
     * The plugin for configuring an EJB JAR.
     */
    public Plugin getEJBPlugin() {
        return new OpenEjbPlugin();
    }

    /**
     * This plugin does not handle WARs.
     */
    public Plugin getWARPlugin() {
        return null;
    }

    /**
     * The plugin for configuring a J2EE Connector RAR.
     */
    public Plugin getRARPlugin() {
        return new JcaPlugin();
    }

    /**
     * An interface to the server for getting information and actually
     * deploying and stuff.  Not yet implemented.
     */
    public ServerInterface getServerInterface() {
        return null;
    }
}
