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
package org.openejb.corba.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.interop.generator.GenOptions;
import org.openejb.corba.compiler.PortableStubCompiler;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.main.ToolsJarHack;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;


/**
 * @version $Revision$ $Date$
 */
public class PortableStubGenerator implements StubGenerator, GBeanLifecycle {
    private static Log log = LogFactory.getLog(PortableStubGenerator.class);

    private boolean verbose;
    private Properties props = new Properties();
    private Compiler compiler;
    private boolean saveStubCode;

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

    public boolean isSaveStubCode() {
        return saveStubCode;
    }

    public void setSaveStubCode(boolean saveStubCode) {
        this.saveStubCode = saveStubCode;
    }

    public void generateStubs(Set interfaces, File destination, ClassLoader classLoader) throws CompilerException {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        File TEMPDIR = null;
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            if (log.isDebugEnabled()) log.debug("ClassLoader: " + classLoader);

            TEMPDIR = FileUtils.createTempDirectory();
            File SRCDIR = new File(TEMPDIR, "stubs");
            File CLASSESDIR = new File(TEMPDIR, "stubs");
            SRCDIR.mkdirs();
            CLASSESDIR.mkdirs();

            GenOptions genOptions = new GenOptions();
            genOptions.setClasspath(SRCDIR.getAbsolutePath());
            genOptions.setCompile(false);
            genOptions.setGenSrcDir(SRCDIR.getAbsolutePath());
            genOptions.setGenerate(true);
            genOptions.setInterfaces(new ArrayList(interfaces));
            genOptions.setLoadclass(true);
            genOptions.setOverwrite(true);
            genOptions.setSimpleIdl(false);
            genOptions.setVerbose(verbose);

            PortableStubCompiler stubCompiler = new PortableStubCompiler(genOptions, classLoader);

            stubCompiler.generate();

            Set classpath = new HashSet();
            for (Iterator iterator = interfaces.iterator(); iterator.hasNext();) {
                Class iface = classLoader.loadClass((String) iterator.next());
                collectClasspaths(classpath, iface);
            }

            compiler.compileDirectory(SRCDIR, CLASSESDIR, classpath);

            // delete this file since someone may be holding on to it.
            if (!saveStubCode) {
                destination.delete();
            }

            Project project = new Project();
            Jar jar = new Jar();
            jar.setProject(project);
            jar.setDestFile(destination);
            jar.setBasedir(CLASSESDIR);
            jar.setUpdate(true);
            jar.execute();
        } catch (Exception e) {
            logAndThrow(e);
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
            FileUtils.recursiveDelete(TEMPDIR);
        }
    }

    protected void collectClasspaths(Set set, Class iface) throws CompilerException {
        set.add(iface.getProtectionDomain().getCodeSource().getLocation());

        Class[] classes = iface.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++) {
            collectClasspaths(set, classes[i]);
        }
    }

    private static void logAndThrow(Exception e) throws CompilerException {
        boolean shouldLog = true;
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            if (stackTraceElement.getClassName().equals("org.omg.CosNaming.NamingContextExtPOA")
                && stackTraceElement.getMethodName().equals("_invoke")) {
                shouldLog = false;
                break;
            }
        }
        if (shouldLog) {
            log.error("Unable to generate stub", e);
        }

        /**
         * Convert the msg to string so that we don't try to serialize
         * anything that is unserializable in a cause exception
         */
        throw new CompilerException("Unable to generate stub: " + e.toString());
    }

    public void doStart() throws Exception {
        if (compiler == null) {
            compiler = new AntCompiler();
        }
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        JarUtils.setHandlerSystemProperty();
        // Install the lame tools jar hack
        ToolsJarHack.install();

        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(PortableStubGenerator.class, NameFactory.CORBA_SERVICE);
        infoFactory.addInterface(StubGenerator.class);
        infoFactory.addAttribute("verbose", Boolean.TYPE, true);
        infoFactory.addAttribute("props", Properties.class, true);
        infoFactory.addAttribute("saveStubCode", Boolean.TYPE, true);
        infoFactory.addReference("Compiler", Compiler.class, NameFactory.CORBA_SERVICE);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
