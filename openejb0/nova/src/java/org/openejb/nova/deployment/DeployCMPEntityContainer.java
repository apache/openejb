/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
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
import org.openejb.nova.entity.cmp.CMPConfiguration;
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
    private final Query[] calls;
    private final String[] cmpFieldNames;
    private final CMRelation[] cmRelations;


    public DeployCMPEntityContainer(MBeanServer server,
                                    MBeanMetadata metadata,
                                    DeploySchemaMBean schemaFactory,
                                    EntityContainerConfiguration config,
                                    Query[] queries,
                                    Query[] updates,
                                    Query[] calls,
                                    String[] cmpFieldNames,
                                    CMRelation[] cmRelations) {
        super(server, metadata);
        this.schemaFactory = schemaFactory;
        this.config = config;
        this.queries = queries;
        this.updates = updates;
        this.calls = calls;
        this.cmpFieldNames = cmpFieldNames;
        this.cmRelations = cmRelations;
    }

    public void perform() throws DeploymentException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = ClassSpaceUtil.setContextClassLoader(server, JMXUtil.getObjectName("geronimo.system:role=ClassSpace,name=System"));
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
            for (int i = 0; i < calls.length; i++) {
                Query query = calls[i];
                MethodSignature methodSignature = new MethodSignature(query.getQueryMethod().getMethodName(),
                        query.getQueryMethod().getMethodParam());

                Binding[] inputBindings = translateBindings(query.getInputBinding(), cl);

                Binding[] outputBindings = translateBindings(query.getOutputBinding(), cl);

                schema.defineCall(methodSignature, query.getSql(), inputBindings, outputBindings);
            }
            CMPConfiguration cmpConfig = new CMPConfiguration();
            cmpConfig.persistenceFactory = schema;
            cmpConfig.queries = cmpQueries;
            cmpConfig.cmpFieldNames = cmpFieldNames;
            cmpConfig.relations = cmRelations;
            CMPEntityContainer container = new CMPEntityContainer(config, cmpConfig);
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
