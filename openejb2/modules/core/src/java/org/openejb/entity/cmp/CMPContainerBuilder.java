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
package org.openejb.entity.cmp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;

import org.openejb.AbstractContainerBuilder;
import org.openejb.EJBComponentType;
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.proxy.ProxyInfo;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.entity.BusinessMethod;
import org.openejb.entity.EntityInstanceFactory;
import org.openejb.entity.EntityInterceptorBuilder;
import org.openejb.entity.HomeMethod;
import org.tranql.cache.CacheLoadCommand;
import org.tranql.cache.CacheRowAccessor;
import org.tranql.cache.CacheTable;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.ModifiedSlotAccessor;
import org.tranql.cache.ModifiedSlotDetector;
import org.tranql.cache.QueryFaultHandler;
import org.tranql.ejb.CMPFieldAccessor;
import org.tranql.ejb.CMPFieldFaultTransform;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.ejb.EJB;
import org.tranql.ejb.LocalProxyTransform;
import org.tranql.ejb.ProxyQueryCommand;
import org.tranql.ejb.RemoteProxyTransform;
import org.tranql.ejb.SimplePKTransform;
import org.tranql.field.FieldAccessor;
import org.tranql.field.FieldTransform;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityTransform;
import org.tranql.identity.UserDefinedIdentity;
import org.tranql.ql.Query;
import org.tranql.ql.QueryBuilder;
import org.tranql.ql.QueryException;
import org.tranql.ql.QueryTransformer;
import org.tranql.query.QueryCommand;
import org.tranql.query.UpdateCommand;
import org.tranql.schema.Attribute;
import org.tranql.sql.DataSourceDelegate;
import org.tranql.sql.SQL92Generator;
import org.tranql.sql.SQLQuery;
import org.tranql.sql.SQLTransform;
import org.tranql.sql.jdbc.InputBinding;
import org.tranql.sql.jdbc.JDBCQueryCommand;
import org.tranql.sql.jdbc.JDBCUpdateCommand;
import org.tranql.sql.jdbc.ResultBinding;
import org.tranql.sql.jdbc.binding.BindingFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPContainerBuilder extends AbstractContainerBuilder {
    private EJB ejb;
    private String connectionFactoryName;
    private Map queries;

    // todo delete this
    private DataSource dataSource;

    protected int getEJBComponentType() {
        return EJBComponentType.CMP_ENTITY;
    }

    public EJB getEJB() {
        return ejb;
    }

    public void setEJB(EJB ejb) {
        this.ejb = ejb;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    // todo delete this
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map getQueries() {
        return queries;
    }

    public void setQueries(Map queries) {
        this.queries = queries;
    }

    protected Object buildIt(boolean buildContainer) throws Exception {
        DataSourceDelegate dataSourceDelegate = new DataSourceDelegate();

        // this should be more configurable
        QueryTransformer queryTransformer = new SQLTransform(SQL92Generator.class);

        // get the bean class
        Class beanClass = getClassLoader().loadClass(getBeanClassName());

        CacheTable cacheTable = createCacheTable(ejb, queryTransformer, dataSourceDelegate);

        // identity definer
        int pkSlot = -1;
        for (int index = 0; index < ejb.getAttributes().size(); index++) {
            Attribute attribute = (Attribute) ejb.getAttributes().get(index);
            if (attribute.isIdentity()) {
                if (pkSlot > 0) {
                    throw new DeploymentException("User defined pks are not currently supported");
                }
                pkSlot = index;
            }
        }
        IdentityDefiner identityDefiner = new UserDefinedIdentity(cacheTable, pkSlot);

        // the load all by primary key command
        QueryCommand loadCommand = createLoadAllCommand(ejb, queryTransformer, dataSourceDelegate, identityDefiner);

        // load all fault handler
        FaultHandler faultHandler = new QueryFaultHandler(loadCommand, identityDefiner);

        // build cmp field accessor map
        LinkedHashMap cmpFieldAccessors = createCMPFieldAccessors(ejb, faultHandler);

        // Identity Transforms
        TranqlEJBProxyFactory tranqlEJBProxyFactory = new TranqlEJBProxyFactory();
        IdentityTransform primaryKeyTransform = new SimplePKTransform(cacheTable);
        IdentityTransform localProxyTransform = new LocalProxyTransform(primaryKeyTransform, tranqlEJBProxyFactory);
        IdentityTransform remoteProxyTransform = new RemoteProxyTransform(primaryKeyTransform, tranqlEJBProxyFactory);

        // queries
        LinkedHashMap queryCommands = new LinkedHashMap();
        for (Iterator iterator = queries.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature signature = (MethodSignature)entry.getKey();
            String sql = (String)entry.getValue();

            QueryCommand query = buildQueryCommand(dataSourceDelegate, signature, sql);

            QueryCommand localProxyLoad = new ProxyQueryCommand(query, identityDefiner, localProxyTransform);
            QueryCommand remoteProxyLoad = new ProxyQueryCommand(query, identityDefiner, remoteProxyTransform);
            queryCommands.put(
                    new InterfaceMethodSignature(signature, true),
                    new QueryCommand[]{localProxyLoad, remoteProxyLoad});
        }

        QueryCommand localProxyLoad = new ProxyQueryCommand(loadCommand, identityDefiner, localProxyTransform);
        QueryCommand remoteProxyLoad = new ProxyQueryCommand(loadCommand, identityDefiner, remoteProxyTransform);
        queryCommands.put(
                new InterfaceMethodSignature("findByPrimaryKey", new String[]{getPrimaryKeyClassName()}, true),
                new QueryCommand[]{localProxyLoad, remoteProxyLoad});

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass, cacheTable, identityDefiner, primaryKeyTransform, localProxyTransform, remoteProxyTransform, queryCommands);
        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // create and intitalize the interceptor builder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new EntityInterceptorBuilder(), signatures, vtable);

        // build the instance factory
        Map instanceMap = buildInstanceMap(beanClass, cmpFieldAccessors);
        InstanceContextFactory contextFactory = new CMPInstanceContextFactory(getContainerId(), primaryKeyTransform, faultHandler, beanClass, instanceMap);
        EntityInstanceFactory instanceFactory = new EntityInstanceFactory(getComponentContext(), contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        CMPEngine cmpEngine = new CMPEngine(dataSourceDelegate, tranqlEJBProxyFactory);

        if (buildContainer) {

            return new CMPContainer(
                    getContainerId(),
                    getEJBName(),
                    createProxyInfo(),
                    signatures,
                    contextFactory,
                    interceptorBuilder,
                    pool,
                    getUserTransaction(),
                    getJndiNames(),
                    getLocalJndiNames(),
                    getTransactionManager(),
                    getTrackedConnectionAssociator(),
                    cmpEngine,
                    new DataSourceProxyFactory(dataSource));
        } else {

            GBeanMBean gbean = new GBeanMBean(CMPContainer.GBEAN_INFO);
            gbean.setAttribute("ContainerID", getContainerId());
            gbean.setAttribute("EJBName", getEJBName());
            gbean.setAttribute("ProxyInfo", createProxyInfo());
            gbean.setAttribute("Signatures", signatures);
            gbean.setAttribute("ContextFactory", contextFactory);
            gbean.setAttribute("InterceptorBuilder", interceptorBuilder);
            gbean.setAttribute("Pool", pool);
            gbean.setAttribute("UserTransaction", getUserTransaction());
            gbean.setAttribute("JndiNames", getJndiNames());
            gbean.setAttribute("LocalJndiNames", getLocalJndiNames());

            gbean.setAttribute("CMPEngine", cmpEngine);
            ObjectName connectionFactoryObjectName = ObjectName.getInstance(JMXReferenceFactory.BASE_MANAGED_CONNECTION_FACTORY_NAME + connectionFactoryName);
            gbean.setReferencePatterns("ConnectionProxyFactory", Collections.singleton(connectionFactoryObjectName));

            return gbean;
        }
    }

    private static LinkedHashMap createCMPFieldAccessors(EJB ejb, FaultHandler faultHandler) {
        List attributes = ejb.getAttributes();
        LinkedHashMap cmpFieldAccessors = new LinkedHashMap(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            String name = attribute.getName();
            CMPFieldTransform accessor = new CMPFieldAccessor(new CacheRowAccessor(i), name);
            accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});
            cmpFieldAccessors.put(name, accessor);
        }
        return cmpFieldAccessors;
    }

    private static QueryCommand createLoadAllCommand(EJB ejb, QueryTransformer queryTransformer, DataSourceDelegate dataSourceDelegate, IdentityDefiner idDefiner) throws QueryException {
        // READ
        Query loadQuery = QueryBuilder.buildSelectById(ejb, false).getQuery();
        SQLQuery loadSQLQuery = (SQLQuery) queryTransformer.transform(loadQuery);
        InputBinding[] loadBindings = BindingFactory.getInputBindings(loadSQLQuery.getParamTypes());

        // todo this should come from the query transform
        FieldTransform[] loadTransforms = new FieldTransform[loadBindings.length];
        for (int i = 0; i < loadTransforms.length; i++) {
            loadTransforms[i] = new FieldAccessor(i);
        }
        // todo there should be an easier way to create the results bindings
        ResultBinding[] resultBindings = createResultsBindings(ejb);
        QueryCommand loadCommand = new JDBCQueryCommand(dataSourceDelegate, loadSQLQuery.getSQLText(), loadBindings, loadTransforms, resultBindings);
        // todo this is lame... should be the default slot mapping
        int[] slotMap = new int[resultBindings.length];
        for (int i = 0; i < slotMap.length; i++) {
            slotMap[i] = i;
        }
        loadCommand = new CacheLoadCommand(loadCommand, idDefiner, slotMap);
        return loadCommand;
    }

    private QueryCommand buildQueryCommand(DataSourceDelegate dataSourceDelegate, MethodSignature signature, String sql) throws QueryException {
        InputBinding[] parameterBindings = BindingFactory.getInputBindings(signature.getParameterTypes());
        FieldTransform[] argTransforms = new FieldTransform[signature.getParameterTypes().length];
        for (int i = 0; i < argTransforms.length; i++) {
            argTransforms[i] = new FieldAccessor(i);
        }
        ResultBinding[] resultBindings = new ResultBinding[] {BindingFactory.getResultBinding(getPrimaryKeyClassName(), 1)};
        QueryCommand query = new JDBCQueryCommand(dataSourceDelegate, sql, parameterBindings, argTransforms, resultBindings);
        return query;
    }

    private static CacheTable createCacheTable(EJB ejb, QueryTransformer queryTransformer, DataSourceDelegate dataSourceDelegate) throws QueryException {
        CacheTable cacheTable;
        // CREATE
        Query createQuery = QueryBuilder.buildInsert(ejb).getQuery();
        SQLQuery createSQLQuery = (SQLQuery) queryTransformer.transform(createQuery);
        // todo shouldn't UpdateCommand take a query directly?
        InputBinding[] createBindings = BindingFactory.getInputBindings(createSQLQuery.getParamTypes());

        // todo this should be obtained from the query
        FieldTransform[] createTransforms = new FieldTransform[createBindings.length];
        for (int i = 0; i < createTransforms.length; i++) {
            createTransforms[i] = new FieldAccessor(i);
        }
        UpdateCommand createCommand = new JDBCUpdateCommand(dataSourceDelegate, createSQLQuery.getSQLText(), createBindings, createTransforms);

        // UPDATE
        Query updateQuery = QueryBuilder.buildUpdate(ejb).getQuery();
        SQLQuery updateSQLQuery = (SQLQuery) queryTransformer.transform(updateQuery);
        InputBinding[] updateBindings = BindingFactory.getInputBindings(updateSQLQuery.getParamTypes());

        // todo this should be obtained from the query
        List attributes = ejb.getAttributes();
        List updateParamsList = new ArrayList(attributes.size() * 2);
        List pkParamsList = new ArrayList(1);
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            if (attribute.isIdentity()) {
                pkParamsList.add(new FieldAccessor(i));
            } else {
                updateParamsList.add(new ModifiedSlotDetector(i));
                updateParamsList.add(new ModifiedSlotAccessor(i));
            }
        }
        updateParamsList.addAll(pkParamsList);
        FieldTransform[] updateTransforms = (FieldTransform[]) updateParamsList.toArray(new FieldTransform[updateParamsList.size()]);
        UpdateCommand updateCommand = new JDBCUpdateCommand(dataSourceDelegate, updateSQLQuery.getSQLText(), updateBindings, updateTransforms);

        // DELETE
        Query removeQuery = QueryBuilder.buildDelete(ejb).getQuery();
        SQLQuery removeSQLQuery = (SQLQuery) queryTransformer.transform(removeQuery);
        InputBinding[] removeBindings = BindingFactory.getInputBindings(removeSQLQuery.getParamTypes());

        // todo shouldn't query builder do this transform?
        List removeParamsList = new ArrayList(1);
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            if (attribute.isIdentity()) {
                removeParamsList.add(new FieldAccessor(i));
            }
        }
        FieldTransform[] removeTransforms = (FieldTransform[]) removeParamsList.toArray(new FieldTransform[removeParamsList.size()]);
        UpdateCommand removeCommand = new JDBCUpdateCommand(dataSourceDelegate, removeSQLQuery.getSQLText(), removeBindings, removeTransforms);

        // defaults
        Object[] defaults = createDefaults(ejb);

        // cahce table
        cacheTable = new CacheTable(defaults, createCommand, updateCommand, removeCommand);
        return cacheTable;
    }

    private static ResultBinding[] createResultsBindings(EJB ejb) throws QueryException {
        List attributes = ejb.getAttributes();
        ResultBinding[] resultBindings = new ResultBinding[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            resultBindings[i] = BindingFactory.getResultBinding(attribute.getType(), i + 1);
        }
        return resultBindings;
    }

    private static Object[] createDefaults(EJB ejb) {
        List attributes = ejb.getAttributes();
        Object[] defaults = new Object[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            Class type = attribute.getType();

            if (type == Boolean.TYPE) {
                defaults[i] = Boolean.FALSE;

            } else if (type == Byte.TYPE) {
                defaults[i] = new Byte((byte) 0);

            } else if (type == Character.TYPE) {
                defaults[i] = new Character((char) 0);

            } else if (type == Short.TYPE) {
                defaults[i] = new Short((short) 0);

            } else if (type == Integer.TYPE) {
                defaults[i] = new Integer(0);

            } else if (type == Long.TYPE) {
                defaults[i] = new Long(0);

            } else if (type == Float.TYPE) {
                defaults[i] = new Float(0);

            } else if (type == Double.TYPE) {
                defaults[i] = new Double(0);

            } else {
                defaults[i] = null;
            }
        }
        return defaults;
    }

    private Map buildInstanceMap(Class beanClass, LinkedHashMap cmpFields) {
        Map instanceMap;
        instanceMap = new HashMap();

        // add the cmp-field getters and setters to the instance table
        addFieldOperations(instanceMap, beanClass, cmpFields);

        // todo add the cmr getters and setters to the instance table.


        // todo add the select methods

        return instanceMap;
    }

    private void addFieldOperations(Map instanceMap, Class beanClass, LinkedHashMap fields) {
        for (Iterator iterator = fields.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String fieldName = (String) entry.getKey();
            CMPFieldTransform fieldTransform = (CMPFieldTransform) entry.getValue();

            try {
                String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = beanClass.getMethod("get" + baseName, null);
                Method setter = beanClass.getMethod("set" + baseName, new Class[]{getter.getReturnType()});

                instanceMap.put(new MethodSignature(getter), new CMPGetter(fieldName, fieldTransform));
                instanceMap.put(new MethodSignature(setter), new CMPSetter(fieldName, fieldTransform));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing accessor for field " + fieldName);
            }
        }
    }

    protected LinkedHashMap buildVopMap(
            Class beanClass,
            CacheTable cacheTable,
            IdentityDefiner identityDefiner,
            IdentityTransform primaryKeyTransform,
            IdentityTransform localProxyTransform,
            IdentityTransform remoteProxyTransform,
            LinkedHashMap queries) throws Exception {

        ProxyInfo proxyInfo = createProxyInfo();

        LinkedHashMap vopMap = new LinkedHashMap();

        // get the context set unset method objects
        Method setEntityContext;
        Method unsetEntityContext;
        try {
            Class entityContextClass = getClassLoader().loadClass("javax.ejb.EntityContext");
            setEntityContext = beanClass.getMethod("setEntityContext", new Class[]{entityContextClass});
            unsetEntityContext = beanClass.getMethod("unsetEntityContext", null);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement javax.ejb.EntityBean");
        }

        // Build the VirtualOperations for business methods defined by the EJB implementation
        Method[] beanMethods = beanClass.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];

            // skip the rejects
            if (Object.class == beanMethod.getDeclaringClass()) {
                continue;
            }
            if (setEntityContext.equals(beanMethod)) {
                continue;
            }
            if (unsetEntityContext.equals(beanMethod)) {
                continue;
            }

            // create a VirtualOperation for the method (if the method is understood)
            String name = beanMethod.getName();
            MethodSignature signature = new MethodSignature(beanMethod);

            if (name.startsWith("ejbCreate")) {
                // ejbCreate vop needs a reference to the ejbPostCreate method
                MethodSignature postCreateSignature = new MethodSignature(
                        "ejbPostCreate" + name.substring(9),
                        beanMethod.getParameterTypes());
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new CMPCreateMethod(
                                beanClass,
                                signature,
                                postCreateSignature,
                                cacheTable,
                                identityDefiner,
                                localProxyTransform,
                                remoteProxyTransform));
            } else if (name.startsWith("ejbHome")) {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new HomeMethod(beanClass, signature));
            } else if (name.equals("ejbRemove")) {
                // there are three valid ways to invoke remove on an entity bean

                // ejbObject.remove()
                vopMap.put(
                        new InterfaceMethodSignature("remove", false),
                        new CMPRemoveMethod(beanClass, signature));

                // ejbHome.remove(primaryKey)
                vopMap.put(
                        new InterfaceMethodSignature("ejbRemove", new Class[]{Object.class}, true),
                        new CMPRemoveMethod(beanClass, signature));

                // ejbHome.remove(handle)
                Class handleClass = getClassLoader().loadClass("javax.ejb.Handle");
                vopMap.put(
                        new InterfaceMethodSignature("ejbRemove", new Class[]{handleClass}, true),
                        new CMPRemoveMethod(beanClass, signature));
            } else if (name.startsWith("ejb")) {
                continue;
            } else {
                vopMap.put(
                        MethodHelper.translateToInterface(signature),
                        new BusinessMethod(beanClass, signature));
            }
        }

        Class homeInterface = proxyInfo.getHomeInterface();
        Class localHomeInterface = proxyInfo.getLocalHomeInterface();
        for (Iterator iterator = queries.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature signature = (InterfaceMethodSignature) entry.getKey();
            QueryCommand[] queryCommands = (QueryCommand[]) entry.getValue();

            Method method = signature.getMethod(homeInterface);
            if (method == null) {
                method = signature.getMethod(localHomeInterface);
            }

            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                vopMap.put(signature, new CMPFinder(queryCommands[0], queryCommands[1], new CollectionResults.Factory()));
            } else if(returnType.equals("java.util.Set")) {
                vopMap.put(signature, new CMPFinder(queryCommands[0], queryCommands[1], new SetResults.Factory()));
            } else {
                vopMap.put(signature, new CMPFinder(queryCommands[0], queryCommands[1], new SingleValuedQueryResultsFactory()));
            }
        }
        return vopMap;
    }

    private static class DataSourceProxyFactory implements ConnectionProxyFactory {
        private final DataSource dataSource;

        public DataSourceProxyFactory(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public Object getProxy() {
            return dataSource;
        }
    }
}
