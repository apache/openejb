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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;

import org.openejb.AbstractContainerBuilder;
import org.openejb.EJBComponentType;
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.entity.BusinessMethod;
import org.openejb.entity.EntityInstanceFactory;
import org.openejb.entity.EntityInterceptorBuilder;
import org.openejb.entity.HomeMethod;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;

import org.tranql.cache.CacheRowAccessor;
import org.tranql.cache.CacheSlot;
import org.tranql.cache.CacheTable;
import org.tranql.cache.EmptySlotLoader;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.QueryFaultHandler;
import org.tranql.ejb.CMPFieldAccessor;
import org.tranql.ejb.CMPFieldFaultTransform;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBQueryBuilder;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.LocalProxyTransform;
import org.tranql.ejb.RemoteProxyTransform;
import org.tranql.ejb.SimplePKTransform;
import org.tranql.ejb.IdAsEJBLocalObjectTransform;
import org.tranql.ejb.IdAsEJBObjectTransform;
import org.tranql.field.FieldAccessor;
import org.tranql.field.FieldTransform;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityTransform;
import org.tranql.identity.UserDefinedIdentity;
import org.tranql.ql.QueryException;
import org.tranql.query.CommandTransform;
import org.tranql.query.QueryCommand;
import org.tranql.query.SchemaMapper;
import org.tranql.query.UpdateCommand;
import org.tranql.schema.Attribute;
import org.tranql.schema.Schema;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPContainerBuilder extends AbstractContainerBuilder {
    private EJBSchema ejbSchema;
    private Schema sqlSchema;
    private EJB ejb;
    private String connectionFactoryName;
    private Map queries;

    protected int getEJBComponentType() {
        return EJBComponentType.CMP_ENTITY;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public Map getQueries() {
        return queries;
    }

    public void setQueries(Map queries) {
        this.queries = queries;
    }

    public EJBSchema getEJBSchema() {
        return ejbSchema;
    }

    public void setEJBSchema(EJBSchema ejbSchema) {
        this.ejbSchema = ejbSchema;
    }

    public Schema getSQLSchema() {
        return sqlSchema;
    }

    public void setSQLSchema(Schema sqlSchema) {
        this.sqlSchema = sqlSchema;
    }

    protected Object buildIt(boolean buildContainer) throws Exception {
        ejb = ejbSchema.getEJB(getEJBName());
        if (ejb == null) {
            throw new DeploymentException("Schema does not contain EJB: " + getEJBName());
        }

        // get the bean classes
        Class beanClass = getClassLoader().loadClass(getBeanClassName());

        EJBProxyFactory proxyFactory = (EJBProxyFactory) ejb.getProxyFactory();

        EJBQueryBuilder queryBuilder = new EJBQueryBuilder(ejbSchema);
        CommandTransform mapper = new SchemaMapper(sqlSchema);
        CacheTable cacheTable = createCacheTable(queryBuilder, mapper);

        // Identity Transforms
        IdentityTransform primaryKeyTransform = new SimplePKTransform(cacheTable);
        IdentityTransform localProxyTransform = new LocalProxyTransform(primaryKeyTransform, proxyFactory);
        IdentityTransform remoteProxyTransform = new RemoteProxyTransform(primaryKeyTransform, proxyFactory);
        IdentityDefiner identityDefiner = getIdentityDefiner(cacheTable);

        List attributes = ejb.getAttributes();
        EmptySlotLoader[] slotLoaders = new EmptySlotLoader[attributes.size()];
        String[] attributeNames = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = (Attribute) attributes.get(i);
            attributeNames[i] = attr.getPhysicalName();
            slotLoaders[i] = new EmptySlotLoader(i, new FieldAccessor(i, attr.getType()));
        }
        QueryCommand loadCommand = queryBuilder.buildLoad(getEJBName(), attributeNames);
        loadCommand = mapper.transform(loadCommand);
        FaultHandler faultHandler = new QueryFaultHandler(loadCommand, identityDefiner, slotLoaders);

        // queries
        LinkedHashMap queryCommands = new LinkedHashMap();
        for (Iterator iterator = queries.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature signature = (MethodSignature) entry.getKey();

            // The SQL
            String sql = (String) entry.getValue();

            // Parameters
            String[] parameterTypes = signature.getParameterTypes();
            FieldTransform[] parameterTransforms = new FieldTransform[parameterTypes.length];
            for (int i = 0; i < parameterTransforms.length; i++) {
                parameterTransforms[i] = new FieldAccessor(i, ClassLoading.loadClass(parameterTypes[i], getClassLoader()));
            }

            // Local Proxy Results
            FieldTransform localResultsTransform;
            localResultsTransform = new FieldAccessor(0, proxyFactory.getLocalInterfaceClass());
            localResultsTransform = new IdAsEJBLocalObjectTransform(localResultsTransform, proxyFactory, ejb.getPrimaryKeyClass());

            QueryCommand localProxyLoad = sqlSchema.getCommandFactory().createQuery(
                    sql,
                    parameterTransforms,
                    new FieldTransform[] {localResultsTransform});

            // Remote Proxy Results
            FieldTransform remoteResultsTransform;
            remoteResultsTransform = new FieldAccessor(0, proxyFactory.getRemoteInterfaceClass());
            remoteResultsTransform = new IdAsEJBObjectTransform(remoteResultsTransform, proxyFactory, ejb.getPrimaryKeyClass());

            QueryCommand remoteProxyLoad = sqlSchema.getCommandFactory().createQuery(
                    sql,
                    parameterTransforms,
                    new FieldTransform[] {remoteResultsTransform});

            queryCommands.put(
                    new InterfaceMethodSignature(signature, true),
                    new QueryCommand[]{localProxyLoad, remoteProxyLoad});
        }

        // findByPrimaryKey
        QueryCommand localProxyLoad = mapper.transform(queryBuilder.buildFindByPrimaryKey(getEJBName(), true));
        QueryCommand remoteProxyLoad = mapper.transform(queryBuilder.buildFindByPrimaryKey(getEJBName(), false));
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
        LinkedHashMap cmpFieldAccessors = createCMPFieldAccessors(faultHandler);
        Map instanceMap = buildInstanceMap(beanClass, cmpFieldAccessors);

        InstanceContextFactory contextFactory = new CMPInstanceContextFactory(getContainerId(), primaryKeyTransform, faultHandler, beanClass, instanceMap, getUnshareableResources(), getApplicationManagedSecurityResources());
        EntityInstanceFactory instanceFactory = new EntityInstanceFactory(getComponentContext(), contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);

        if (buildContainer) {
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        } else {
            return createConfiguration(signatures, contextFactory, interceptorBuilder, pool);
        }
    }

    private IdentityDefiner getIdentityDefiner(CacheTable table) throws DeploymentException {
        // @todo support more than single field primary keys
        int pkSlot = -1;
        List attributes = ejb.getAttributes();
        for (int index = 0; index < attributes.size(); index++) {
            Attribute attribute = (Attribute) attributes.get(index);
            if (attribute.isIdentity()) {
                if (pkSlot != -1) {
                    throw new DeploymentException("User defined pks are not currently supported");
                }
                pkSlot = index;
            }
        }
        return new UserDefinedIdentity(table, pkSlot);
    }

    private LinkedHashMap createCMPFieldAccessors(FaultHandler faultHandler) {
        List attributes = ejb.getAttributes();
        LinkedHashMap cmpFieldAccessors = new LinkedHashMap(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            String name = attribute.getName();
            CMPFieldTransform accessor = new CMPFieldAccessor(new CacheRowAccessor(i, attribute.getType()), name);
            accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});
            cmpFieldAccessors.put(name, accessor);
        }
        return cmpFieldAccessors;
    }

    private CacheTable createCacheTable(EJBQueryBuilder builder, CommandTransform mapper) throws QueryException {
        String name = getEJBName();
        UpdateCommand createCommand = mapper.transform(builder.buildCreate(name));
        UpdateCommand storeCommand = mapper.transform(builder.buildStore(name));
        UpdateCommand removeCommand = mapper.transform(builder.buildRemove(name));

        List attributes = ejb.getAttributes();
        CacheSlot[] slots = new CacheSlot[attributes.size()];
        for (int i = 0; i < slots.length; i++) {
            Attribute attr = (Attribute) attributes.get(i);
            slots[i] = new CacheSlot(attr.getName(), attr.getType(), getDefault(attr.getType()));
        }
        return new CacheTable(name, slots, createCommand, storeCommand, removeCommand);
    }

    private static final Map DEFAULTS = new HashMap();

    static {
        DEFAULTS.put(Boolean.TYPE, Boolean.FALSE);
        DEFAULTS.put(Byte.TYPE, new Byte((byte) 0));
        DEFAULTS.put(Short.TYPE, new Short((short) 0));
        DEFAULTS.put(Integer.TYPE, new Integer(0));
        DEFAULTS.put(Long.TYPE, new Long(0L));
        DEFAULTS.put(Float.TYPE, new Float(0.0f));
        DEFAULTS.put(Double.TYPE, new Double(0.0d));
        DEFAULTS.put(Character.TYPE, new Character(Character.MIN_VALUE));
    }

    private Object getDefault(Class type) {
        // assumes get returns null and that is valid ...
        return DEFAULTS.get(type);
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
            if (method == null) {
                throw new DeploymentException("Could not find method for signature: " + signature);
            }

            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                vopMap.put(signature, new CollectionValuedFinder(queryCommands[0], queryCommands[1]));
            } else if (returnType.equals("java.util.Enumeration")) {
                vopMap.put(signature, new EnumerationValuedFinder(queryCommands[0], queryCommands[1]));
            } else {
                vopMap.put(signature, new SingleValuedFinder(queryCommands[0], queryCommands[1]));
            }
        }
        return vopMap;
    }
}
