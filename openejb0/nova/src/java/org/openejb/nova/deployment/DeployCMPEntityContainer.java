/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.openejb.nova.deployment;

import java.lang.reflect.Constructor;

import javax.management.MBeanServer;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.classspace.ClassSpaceUtil;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.deployment.model.geronimo.ejb.Query;
import org.openejb.nova.entity.cmp.CMPQuery;
import org.openejb.nova.entity.cmp.CMPEntityContainer;
import org.openejb.nova.entity.cmp.SimpleCommandFactory;
import org.openejb.nova.entity.cmp.CMRelation;
import org.openejb.nova.entity.EntityContainerConfiguration;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.persistence.jdbc.Binding;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class DeployCMPEntityContainer extends DeployGeronimoMBean {

    private final DeploySchemaMBean schemaFactory;
    private final EntityContainerConfiguration config;
    private final Query[] queries;
    private final Query[] updates;
    private final String[] cmpFieldNames;


    public DeployCMPEntityContainer(MBeanServer server,
                                    MBeanMetadata metadata,
                                    DeploySchemaMBean schemaFactory,
                                    EntityContainerConfiguration config,
                                    Query[] queries,
                                    Query[] updates,
                                    String[] cmpFieldNames) {
        super(server, metadata);
        this.schemaFactory = schemaFactory;
        this.config = config;
        this.queries = queries;
        this.updates = updates;
        this.cmpFieldNames = cmpFieldNames;
    }

    public void perform() throws DeploymentException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassSpaceUtil.setContextClassLoader(server, JMXUtil.getObjectName("geronimo.system:role=ClassSpace,name=System"));
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            SimpleCommandFactory schema = schemaFactory.getSchema();

            //register the queries
            CMPQuery[] cmpQueries = new CMPQuery[queries.length];
            for (int i = 0; i < queries.length; i++) {
                Query query = queries[i];
                MethodSignature methodSignature = new MethodSignature(query.getQueryMethod().getMethodName(),
                        query.getQueryMethod().getMethodParam());

                Binding[] inputBindings = translateBindings(query.getInputBinding(), cl);

                Binding[] outputBindings = translateBindings(query.getOutputBinding(), cl);

                schema.defineQuery(methodSignature, query.getSql(), inputBindings, outputBindings);
                //if method name begins with "find", local is ignored.  Set to false.
                //otherwise,
                //if abstract schema name is specified, then you must specify local
                boolean local = "Local".equals(query.getResultTypeMapping());
                boolean multivalue = "Many".equals(query.getMultiplicity());
                cmpQueries[i] = new CMPQuery(query.getAbstractSchemaName(),
                        local,
                        methodSignature,
                        multivalue,
                        query.getEjbQl());
            }
            for (int i = 0; i < updates.length; i++) {
                Query query = updates[i];
                MethodSignature methodSignature = new MethodSignature(query.getQueryMethod().getMethodName(),
                        query.getQueryMethod().getMethodParam());

                Binding[] inputBindings = translateBindings(query.getInputBinding(), cl);
                //currently we are not using the output bindings.  Could be for "update returning"
                //Binding[] outputBindings = translateBindings(query.getOutputBinding(), cl);

                schema.defineUpdate(methodSignature, query.getSql(), inputBindings);
            }
            CMPEntityContainer container = new CMPEntityContainer(config,
                    schema,
                    cmpQueries,
                    cmpFieldNames,
                    new CMRelation[0]);
            GeronimoMBeanInfo mbeanInfo = metadata.getGeronimoMBeanInfo();
            mbeanInfo.setTarget(container);
            super.perform();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private Binding[] translateBindings(org.apache.geronimo.deployment.model.geronimo.ejb.Binding[] inBindings, ClassLoader cl) throws DeploymentException {
        Binding[] bindings = new Binding[inBindings.length];
        for (int j = 0; j < inBindings.length; j++) {
            String bindingClassName = inBindings[j].getType();
            int slot = inBindings[j].getParam();
            bindings[j] = getBinding(cl, bindingClassName, j, slot);
        }
        return bindings;
    }

    private Binding getBinding(ClassLoader cl, String bindingClassName, int j, int slot) throws DeploymentException {
        try {
            System.out.println("cl = " + cl);
            Class bindingClass = cl.loadClass(bindingClassName);
            Constructor constructor = bindingClass.getConstructor(new Class[]{Integer.TYPE, Integer.TYPE});

            // j+1 because ResultSet and PS use 1-based numbering
            return (Binding) constructor.newInstance(new Object[]{new Integer(j + 1), new Integer(slot)});
        } catch (Exception e) {
            throw new DeploymentException("Could not load binding class or create binding", e);
        }
    }
}
