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


/**
 *
 *
 * @version $Revision$ $Date$
 */
public class OpenORBSkeletonGenerator extends SkeletonGenerator implements GBeanLifecycle, CompilerHost {
    private static Log log = LogFactory.getLog(OpenORBSkeletonGenerator.class);

    private boolean verbose;
    private Compiler compiler;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public void generateSkeleton(Class iface, File destination) throws CompilerException {
        File TEMPDIR = null;
        try {
            TEMPDIR = DeploymentUtil.createTempDir();
            File IDLDIR = new File(TEMPDIR, "IDL");
            File CLASSESDIR = new File(TEMPDIR, "classes");
            IDLDIR.mkdirs();
            CLASSESDIR.mkdirs();

            RmiCompilerProperties rcp = new RmiCompilerProperties();
            rcp.setClassloader(getClass().getClassLoader());
            rcp.setM_verbose(verbose);
            rcp.setM_destdir(IDLDIR);
            Configurator configurator = new Configurator(new String[0], getProps());
            JavaParser parser = new JavaParser(rcp, this, null, null, null);

            parser.load_standard_idl(configurator, rcp.getM_includeList());
            parser.add_idl_files(rcp.getIncludedFiles(), rcp.getM_includeList());
            int start = parser.getCompilationTree().size();

            parser.parse_class(iface);

            IdlObject compilationGraph = parser.getIdlTreeRoot();
            Javatoidl toIDL = new Javatoidl(rcp, this);

            // -------------
            // map tie class
            // -------------
            int end = parser.getCompilationTree().size();
            for (int i = start; i < end; i++) {
                toIDL.translateRMITie((IdlRoot) parser.getCompilationTree().get(i));
            }
            toIDL.translateRMITie(compilationGraph);

            compiler.compileDirectory(IDLDIR, CLASSESDIR);

            Project project = new Project();
            Jar jar = new Jar();
            jar.setProject(project);
            jar.setDestFile(destination);
            jar.setBasedir(CLASSESDIR);
            jar.execute();
        } catch (Exception e) {
            throw new CompilerException(e);
        } finally {
            DeploymentUtil.recursiveDelete(TEMPDIR);
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(OpenORBSkeletonGenerator.class, SkeletonGenerator.GBEAN_INFO);
        infoFactory.addAttribute("verbose", Boolean.TYPE, true);
        infoFactory.addReference("Compiler", Compiler.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public void display(String s) {
        log.trace(s);
    }
}
