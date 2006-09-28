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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * $Id: DDLExporterTask.java 449796 2006-09-25 20:03:25Z djencks $
 */
package org.apache.openejb.deployment.ant;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import javax.sql.DataSource;

import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.xmlbeans.XmlObject;
import org.apache.openejb.deployment.CmpSchemaBuilder;
import org.apache.openejb.deployment.TranqlCmpSchemaBuilder;
import org.apache.openejb.deployment.OpenEjbModuleBuilder;
import org.apache.openejb.entity.cmp.ModuleSchema;
import org.apache.openejb.entity.cmp.TranqlSchemaBuilder;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.ddl.DDLCommandBuilder;
import org.tranql.ddl.DDLGenerator;
import org.tranql.ejb.EJBProxyFactory;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.sql.SQLSchema;


/**
 * @version $Revision$ $Date$
 */
public class DDLExporterTask extends Task {
    private Path classpath;
    private EjbJarLocation ejbJarLocation;
    private OpenejbJarLocation openejbJarLocation;
    private File output;
    private String ddlCommandBuilder;
    private String type;

    public void setClasspath(Path classpath) {
        this.classpath = classpath;
    }

    public Path createClasspath() {
        if (null == classpath) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    public void setEjbJar(EjbJarLocation ejbJarLocation) {
        this.ejbJarLocation = ejbJarLocation;
    }

    public void setOpenejbJar(OpenejbJarLocation openejbJarLocation) {
        this.openejbJarLocation = openejbJarLocation;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public String getDdlCommandBuilder() {
        return ddlCommandBuilder;
    }

    public void setDdlCommandBuilder(String ddlCommandBuilder) {
        this.ddlCommandBuilder = ddlCommandBuilder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void execute() throws BuildException {
        if (null == ejbJarLocation) {
            throw new BuildException("ejbJar file is required.");
        } else if (null == openejbJarLocation) {
            throw new BuildException("openejbJar file is required.");
        } else if (null == ddlCommandBuilder) {
            throw new BuildException("ddlCommandBuilder is required.");
        } else if (null == type) {
            throw new BuildException("type is required.");
        }

        ClassLoader cl = new AntClassLoader(getClass().getClassLoader(), getProject(), classpath, true);
        SQLSchema sqlSchema;
        try {
            InputStream in = ejbJarLocation.getInputStream(project);
            XmlObject xmlObject;
            try {
                xmlObject = XmlBeansUtil.parse(in);
            } finally {
                in.close();
            }
            EjbJarType ejbJarType = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject).getEjbJar();

            in = openejbJarLocation.getInputStream(project);
            try {
                xmlObject = XmlBeansUtil.parse(in);
            } finally {
                in.close();
            }
            OpenejbOpenejbJarType openejbJarType = (OpenejbOpenejbJarType)
                    SchemaConversionUtils.getNestedObjectAsType(xmlObject,
                            OpenejbOpenejbJarDocument.type.getDocumentElementName(),
                            OpenejbOpenejbJarType.type);

            // fake j2ee context
            Naming naming = new Jsr77Naming();
            AbstractName earName = naming.createRootName(new Artifact("fakeGroup", "fackArtifact", "1", "fake"), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            AbstractName moduleName = naming.createChildName(earName, NameFactory.EJB_MODULE, "module");
            EJBModule ejbModule = new EJBModule(true, moduleName, new Environment(new Artifact("fakeGroup", "fackArtifact", "1", "fake")), null, null, ejbJarType, openejbJarType, null);

            // get the ModuleSchema
            CmpSchemaBuilder cmpSchemaBuilder = new TranqlCmpSchemaBuilder();
            ModuleSchema moduleSchema = cmpSchemaBuilder.buildModuleSchema(naming, ejbModule, cl);

            // build the tranql schema objects
            // use a subclass of the tranql builder that does not create pk generators or proxy factories
            TranqlSchemaBuilder tranqlSchemaBuilder = new TranqlSchemaBuilder(moduleSchema, null, null, null, cl) {
                protected PrimaryKeyGenerator configurePrimaryKeyGenerator(org.apache.openejb.entity.cmp.PrimaryKeyGenerator primaryKeyGenerator, Class pkClass) {
                    return null;
                }

                protected EJBProxyFactory buildEJBProxyFactory(String containerId, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) {
                    return null;
                }
            };
            tranqlSchemaBuilder.buildSchema();
            sqlSchema = tranqlSchemaBuilder.getSqlSchema();
        } catch (Exception e) {
            throw new BuildException("Cannot read DD", e);
        }

        DDLCommandBuilder builder;
        try {
            Class clazz = cl.loadClass(ddlCommandBuilder);
            Constructor ctr = clazz.getConstructor(new Class[]{DataSource.class});
            builder = (DDLCommandBuilder) ctr.newInstance(new Object[]{null});
        } catch (Exception e) {
            throw new BuildException("Cannot create ddlCommandBuilder", e);
        }

        DDLGenerator generator = new DDLGenerator(sqlSchema, builder);
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(output));
            DDLGenerator.ExecutionStrategy exec = new DDLGenerator.WriterExecutionStrategy(new PrintWriter(out));
            DDLGenerator.GenerationStrategy gen;
            if (type.equals("drop")) {
                gen = new DDLGenerator.DropStrategy(exec);
            } else if (type.equals("create") || type.equals("create-constraint")) {
                gen = new DDLGenerator.CreateStrategy(exec);
            } else if (type.equals("drop-create") || type.equals("drop-create-constraint")) {
                DDLGenerator.GenerationStrategy strategies[] = new DDLGenerator.GenerationStrategy[]{
                    new DDLGenerator.DropStrategy(exec),
                    new DDLGenerator.CreateStrategy(exec)
                };
                gen = new DDLGenerator.SequenceStrategy(strategies, exec);
            } else {
                throw new BuildException("type " + type + " is not supported.");
            }
            generator.generate(gen);
            if (type.equals("create-constraint") || type.equals("drop-create-constraint")) {
                gen = new DDLGenerator.CreateConstraintStrategy(exec);
                generator.generate(gen);
            }
            out.flush();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

}