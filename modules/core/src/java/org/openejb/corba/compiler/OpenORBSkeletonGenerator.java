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
 * Copyright 2004 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.compiler;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.ejb.EJBHome;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.openorb.compiler.CompilerHost;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlRoot;
import org.openorb.compiler.orb.Configurator;
import org.openorb.compiler.rmi.RmiCompilerProperties;
import org.openorb.compiler.rmi.generator.Javatoidl;
import org.openorb.compiler.rmi.parser.JavaParser;

import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.system.main.ToolsJarHack;

import org.openejb.util.JarUtils;


/**
 * @version $Revision$ $Date$
 */
public class OpenORBSkeletonGenerator implements SkeletonGenerator, GBeanLifecycle, CompilerHost {

    private static Log log = LogFactory.getLog(OpenORBSkeletonGenerator.class);

    private final ClassLoader classLoader;
    private boolean verbose;
    private Properties props = new Properties();
    private Compiler compiler;

    public OpenORBSkeletonGenerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public void generateSkeletons(Set interfaces, File destination, ClassLoader cl) throws CompilerException {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        File TEMPDIR = null;
        try {
            Thread.currentThread().setContextClassLoader(classLoader);


            TEMPDIR = DeploymentUtil.createTempDir();
            File SRCDIR = new File(TEMPDIR, "JAVA");
            File CLASSESDIR = new File(TEMPDIR, "classes");
            SRCDIR.mkdirs();
            CLASSESDIR.mkdirs();

            RmiCompilerProperties rcp = new RmiCompilerProperties();
            rcp.setClassloader(cl);
            rcp.setM_verbose(verbose);
            rcp.setM_destdir(SRCDIR);
            rcp.getM_includeList().add(new URL("resource:/org/openorb/idl/"));
            Configurator configurator = new Configurator(new String[0], getProps());
            JavaParser parser = new JavaParser(rcp, this, null, null, null);

            parser.load_standard_idl(configurator, rcp.getM_includeList());
            parser.add_idl_files(rcp.getIncludedFiles(), rcp.getM_includeList());
            int start = parser.getCompilationTree().size();

            Set set = new HashSet();
            for (Iterator iter = interfaces.iterator(); iter.hasNext();) {
                Class iface = cl.loadClass((String) iter.next());

                parser.parse_class(iface);

                IdlObject compilationGraph = parser.getIdlTreeRoot();
                Javatoidl toIDL = new Javatoidl(rcp, this);
                int end = parser.getCompilationTree().size();
                for (int i = start; i < end; i++) {
                    toIDL.translateRMITie((IdlRoot) parser.getCompilationTree().get(i));
                }
                toIDL.translateRMITie(compilationGraph);

                start = end;

                collectClasspaths(set, iface);
            }
            collectClasspaths(set, EJBHome.class);

            compiler.compileDirectory(SRCDIR, CLASSESDIR, set);

            // delete this file since someone may be holding on to it.
            destination.delete();

            Project project = new Project();
            Jar jar = new Jar();
            jar.setProject(project);
            jar.setDestFile(destination);
            jar.setBasedir(CLASSESDIR);
            jar.setUpdate(true);
            jar.execute();
        } catch (Exception e) {
            /**
             * Convert the msg to string so that we don't try to serialize
             * anything that is unserializable in a cause exception
             */
            throw new CompilerException(e.toString());
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
            DeploymentUtil.recursiveDelete(TEMPDIR);
        }
    }

    protected void collectClasspaths(Set set, Class iface) throws CompilerException {
        set.add(iface.getProtectionDomain().getCodeSource().getLocation());

        Class[] classes = iface.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++) {
            collectClasspaths(set, classes[i]);
        }
    }

    public void doStart() throws WaitingException, Exception {
        if (compiler == null) {
            compiler = new AntCompiler();
        }
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        JarUtils.setHandlerSystemProperty();
        // Install the lame tools jar hack
        ToolsJarHack.install();

        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(OpenORBSkeletonGenerator.class);
        infoFactory.addInterface(SkeletonGenerator.class);
        infoFactory.addAttribute("verbose", Boolean.TYPE, true);
        infoFactory.addAttribute("props", Properties.class, true);
        infoFactory.addReference("Compiler", Compiler.class);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.setConstructor(new String[]{"classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public void display(String s) {
        log.trace(s);
    }
}
