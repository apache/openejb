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
package org.openejb.deployment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.openejb.EJBComponentType;
import org.openejb.InstanceContextFactory;
import org.openejb.InterceptorBuilder;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.EJBTimeoutOperation;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodHelper;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.entity.BusinessMethod;
import org.openejb.entity.EntityInstanceFactory;
import org.openejb.entity.HomeMethod;
import org.openejb.entity.cmp.CMP1Bridge;
import org.openejb.entity.cmp.CMPCreateMethod;
import org.openejb.entity.cmp.CMPEntityInterceptorBuilder;
import org.openejb.entity.cmp.CMPGetter;
import org.openejb.entity.cmp.CMPInstanceContextFactory;
import org.openejb.entity.cmp.CMPRemoveMethod;
import org.openejb.entity.cmp.CMPSetter;
import org.openejb.entity.cmp.CollectionValuedFinder;
import org.openejb.entity.cmp.EnumerationValuedFinder;
import org.openejb.entity.cmp.SingleValuedFinder;
import org.openejb.entity.dispatch.EJBActivateOperation;
import org.openejb.entity.dispatch.EJBLoadOperation;
import org.openejb.entity.dispatch.EJBPassivateOperation;
import org.openejb.entity.dispatch.EJBStoreOperation;
import org.openejb.entity.dispatch.SetEntityContextOperation;
import org.openejb.entity.dispatch.UnsetEntityContextOperation;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.tranql.cache.CacheRowAccessor;
import org.tranql.cache.CacheTable;
import org.tranql.cache.EmptySlotLoader;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.QueryFaultHandler;
import org.tranql.ejb.CMPFieldAccessor;
import org.tranql.ejb.CMPFieldFaultTransform;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBQueryBuilder;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.IdAsEJBLocalObjectTransform;
import org.tranql.ejb.IdAsEJBObjectTransform;
import org.tranql.ejb.LocalProxyTransform;
import org.tranql.ejb.ManyToManyCMR;
import org.tranql.ejb.ManyToOneCMR;
import org.tranql.ejb.MultiValuedCMRAccessor;
import org.tranql.ejb.MultiValuedCMRFaultHandler;
import org.tranql.ejb.OneToManyCMR;
import org.tranql.ejb.OneToOneCMR;
import org.tranql.ejb.RemoteProxyTransform;
import org.tranql.ejb.SingleValuedCMRAccessor;
import org.tranql.ejb.SingleValuedCMRFaultHandler;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.field.FieldAccessor;
import org.tranql.field.FieldTransform;
import org.tranql.field.ReferenceAccessor;
import org.tranql.identity.DerivedIdentity;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityDefinerBuilder;
import org.tranql.identity.IdentityTransform;
import org.tranql.identity.UndefinedIdentityException;
import org.tranql.identity.UserDefinedIdentity;
import org.tranql.pkgenerator.PrimaryKeyGeneratorDelegate;
import org.tranql.ql.QueryException;
import org.tranql.query.AssociationEndFaultHandlerBuilder;
import org.tranql.query.CommandTransform;
import org.tranql.query.QueryCommand;
import org.tranql.query.QueryCommandView;
import org.tranql.query.SchemaMapper;
import org.tranql.schema.Association;
import org.tranql.schema.Attribute;
import org.tranql.schema.Schema;

/**
 * @version $Revision$ $Date$
 */
public class CMPContainerBuilder extends AbstractContainerBuilder {
    private boolean cmp2 = true;
    private EJBSchema ejbSchema;
    private Schema sqlSchema;
    private GlobalSchema globalSchema;
    private EJB ejb;
    private Map queries;
    private TransactionManagerDelegate tm;

    public boolean isCMP2() {
        return cmp2;
    }

    public void setCMP2(boolean cmp2) {
        this.cmp2 = cmp2;
    }

    protected int getEJBComponentType() {
        return EJBComponentType.CMP_ENTITY;
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

    public GlobalSchema getGlobalSchema() {
        return globalSchema;
    }

    public void setGlobalSchema(GlobalSchema globalSchema) {
        this.globalSchema = globalSchema;
    }

    public TransactionManagerDelegate getTransactionManagerDelegate() {
        return tm;
    }

    public void setTransactionManagerDelegate(TransactionManagerDelegate tm) {
        this.tm = tm;
    }
    
    protected InterceptorBuilder initializeInterceptorBuilder(CMPEntityInterceptorBuilder interceptorBuilder, InterfaceMethodSignature[] signatures, VirtualOperation[] vtable) {
        super.initializeInterceptorBuilder(interceptorBuilder, signatures, vtable);
        interceptorBuilder.setCacheFlushStrategyFactory(globalSchema.getCacheFlushStrategyFactorr());
        return interceptorBuilder;
    }
    
    protected Object buildIt(boolean buildContainer) throws Exception {
        ejb = ejbSchema.getEJB(getEJBName());
        if (ejb == null) {
            throw new DeploymentException("Schema does not contain EJB: " + getEJBName());
        }

        // get the bean classes
        ClassLoader classLoader = getClassLoader();
        Class beanClass = classLoader.loadClass(getBeanClassName());

        EJBProxyFactory proxyFactory = (EJBProxyFactory) ejb.getProxyFactory();

        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(globalSchema);
        EJBQueryBuilder queryBuilder = new EJBQueryBuilder(ejbSchema, identityDefinerBuilder);
        CommandTransform mapper = new SchemaMapper(sqlSchema);
        CacheTable cacheTable = (CacheTable) globalSchema.getEntity(getEJBName());

        // Identity Transforms
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);
        IdentityTransform primaryKeyTransform = identityDefinerBuilder.getPrimaryKeyTransform(ejb);
        IdentityTransform localProxyTransform = new LocalProxyTransform(primaryKeyTransform, proxyFactory);
        IdentityTransform remoteProxyTransform = new RemoteProxyTransform(primaryKeyTransform, proxyFactory);

        List attributes = ejb.getAttributes();
        EmptySlotLoader[] slotLoaders = new EmptySlotLoader[attributes.size()];
        String[] attributeNames = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = (Attribute) attributes.get(i);
            attributeNames[i] = attr.getName();
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
                parameterTransforms[i] = new FieldAccessor(i, ClassLoading.loadClass(parameterTypes[i], classLoader));
            }

            // Local Proxy Results
            FieldTransform localResultsTransform;
            localResultsTransform = new FieldAccessor(0, proxyFactory.getLocalInterfaceClass());
            localResultsTransform = new IdAsEJBLocalObjectTransform(localResultsTransform, proxyFactory, ejb.getPrimaryKeyClass());

            QueryCommand localProxyLoad = sqlSchema.getCommandFactory().createQuery(sql,
                    parameterTransforms,
                    new FieldTransform[]{localResultsTransform});
            QueryCommandView localProxyLoadView = new QueryCommandView(localProxyLoad, new FieldTransform[] {new FieldAccessor(0, null)});

            // Remote Proxy Results
            FieldTransform remoteResultsTransform;
            remoteResultsTransform = new FieldAccessor(0, proxyFactory.getRemoteInterfaceClass());
            remoteResultsTransform = new IdAsEJBObjectTransform(remoteResultsTransform, proxyFactory, ejb.getPrimaryKeyClass());

            QueryCommand remoteProxyLoad = sqlSchema.getCommandFactory().createQuery(sql,
                    parameterTransforms,
                    new FieldTransform[]{remoteResultsTransform});
            QueryCommandView remoteProxyLoadView = new QueryCommandView(remoteProxyLoad, new FieldTransform[] {new FieldAccessor(0, null)});
            
            queryCommands.put(new InterfaceMethodSignature(signature, true),
                    new QueryCommandView[]{localProxyLoadView, remoteProxyLoadView});
        }

        // findByPrimaryKey
        QueryCommandView localProxyLoadView = queryBuilder.buildFindByPrimaryKey(getEJBName(), true);
        QueryCommand localProxyLoad = mapper.transform(localProxyLoadView.getQueryCommand());
        localProxyLoadView = new QueryCommandView(localProxyLoad, localProxyLoadView.getView());

        QueryCommandView remoteProxyLoadView = queryBuilder.buildFindByPrimaryKey(getEJBName(), false);
        QueryCommand remoteProxyLoad = mapper.transform(remoteProxyLoadView.getQueryCommand());
        remoteProxyLoadView = new QueryCommandView(remoteProxyLoad, remoteProxyLoadView.getView());

        queryCommands.put(new InterfaceMethodSignature("findByPrimaryKey",
                new String[]{getPrimaryKeyClassName()}, true),
                new QueryCommandView[]{localProxyLoadView, remoteProxyLoadView});

        // build the instance factory
        LinkedHashMap cmpFieldAccessors = createCMPFieldAccessors(faultHandler);
        LinkedHashMap cmrFieldAccessors = createCMRFieldAccessors();
        Map instanceMap = null;
        CMP1Bridge cmp1Bridge = null;
        if (cmp2) {
            // filter out the accessors associated to virtual CMR fields.
            LinkedHashMap existingCMRFieldAccessores = new LinkedHashMap(cmrFieldAccessors);
            for (Iterator iter = existingCMRFieldAccessores.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                if ( ejb.getAssociationEnd(name).isVirtual() ) {
                    iter.remove();
                }
            }
            instanceMap = buildInstanceMap(beanClass, cmpFieldAccessors, existingCMRFieldAccessores);
        } else {
            cmp1Bridge = new CMP1Bridge(beanClass, cmpFieldAccessors);
        }

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass, cacheTable, cmrFieldAccessors, cmp1Bridge, identityDefiner, ejb.getPrimaryKeyGeneratorDelegate(), primaryKeyTransform, localProxyTransform, remoteProxyTransform, queryCommands);

        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // create and intitalize the interceptor moduleBuilder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new CMPEntityInterceptorBuilder(), signatures, vtable);

        InstanceContextFactory contextFactory = new CMPInstanceContextFactory(getContainerId(), cmp1Bridge, primaryKeyTransform, faultHandler, beanClass, instanceMap, getUnshareableResources(), getApplicationManagedSecurityResources());
        EntityInstanceFactory instanceFactory = new EntityInstanceFactory(contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);
        ObjectName timerName = getTimerName(beanClass);

        if (buildContainer) {
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        } else {
            return createConfiguration(classLoader, signatures, contextFactory, interceptorBuilder, pool, timerName);
        }
    }

    private LinkedHashMap createCMPFieldAccessors(FaultHandler faultHandler) {
        List attributes = ejb.getAttributes();
        List virtualAttributes = ejb.getVirtualCMPFields();
        LinkedHashMap cmpFieldAccessors = new LinkedHashMap(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            if ( virtualAttributes.contains(attribute) ) {
                continue;
            }
            String name = attribute.getName();
            CMPFieldTransform accessor = new CMPFieldAccessor(new CacheRowAccessor(i, attribute.getType()), name);
            accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});
            cmpFieldAccessors.put(name, accessor);
        }
        return cmpFieldAccessors;
    }

    private LinkedHashMap createCMRFieldAccessors() throws Exception {
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(globalSchema);
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);

        SchemaMapper mapper = new SchemaMapper(sqlSchema);
        AssociationEndFaultHandlerBuilder handlerBuilder = new AssociationEndFaultHandlerBuilder(mapper);

        List associationEnds = ejb.getAssociationEnds();
        LinkedHashMap cmrFieldAccessors = new LinkedHashMap(associationEnds.size());
        int offset = ejb.getAttributes().size();
        for (int i = offset; i < offset + associationEnds.size(); i++) {
            CMRField field = (CMRField) associationEnds.get(i - offset);

            String name = field.getName();
            Association association = field.getAssociation(); 
            CMRField relatedField = (CMRField) association.getOtherEnd(field);
            EJB relatedEJB = (EJB) field.getEntity();
            IdentityDefiner relatedIdentityDefiner = identityDefinerBuilder.getIdentityDefiner(relatedEJB);

            CMPFieldTransform accessor = new CMPFieldAccessor(new CacheRowAccessor(i, null), name);

            FaultHandler faultHandler = buildFaultHandler(handlerBuilder, field, i);
            accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});

            int relatedIndex = relatedEJB.getAttributes().size() + relatedEJB.getAssociationEnds().indexOf(relatedField);
            FaultHandler relatedFaultHandler = buildFaultHandler(handlerBuilder, relatedField, relatedIndex);
            CMPFieldTransform relatedAccessor = new CMPFieldAccessor(new CacheRowAccessor(relatedIndex, null), name);
            relatedAccessor = new CMPFieldFaultTransform(relatedAccessor, relatedFaultHandler, new int[]{relatedIndex});
            if ( association.isOneToOne() ) {
                accessor = new OneToOneCMR(accessor, identityDefiner, relatedAccessor, relatedIdentityDefiner);
            } else if ( association.isOneToMany(field) ) {
                accessor = new ManyToOneCMR(accessor, identityDefiner, relatedAccessor, relatedIdentityDefiner);
            } else if ( association.isManyToOne(field) ) {
                accessor = new OneToManyCMR(accessor, relatedAccessor, relatedIdentityDefiner);
            } else {
                CacheTable mtm = (CacheTable) getGlobalSchema().getEntity(association.getManyToManyEntity().getName());
                boolean isRight = association.getRightJoinDefinition().getPKEntity() == ejb;
                accessor = new ManyToManyCMR(accessor, relatedAccessor, relatedIdentityDefiner, mtm, isRight);
            }

            IdentityTransform relatedIdentityTransform = identityDefinerBuilder.getPrimaryKeyTransform(relatedEJB);
            if ( association.isOneToOne() || association.isManyToOne(field) ) {
                accessor = new SingleValuedCMRAccessor(accessor,
                        new LocalProxyTransform(relatedIdentityTransform, relatedEJB.getProxyFactory()));
            } else {
                accessor = new MultiValuedCMRAccessor(accessor, tm,
                        new LocalProxyTransform(relatedIdentityTransform, relatedEJB.getProxyFactory()),
                        relatedEJB.getProxyFactory().getLocalInterfaceClass());
            }

            cmrFieldAccessors.put(name, accessor);
        }
        return cmrFieldAccessors;
    }

    private FaultHandler buildFaultHandler(AssociationEndFaultHandlerBuilder handlerBuilder, CMRField field, int slot) throws UndefinedIdentityException, QueryException {
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(globalSchema);
        Association association = field.getAssociation();
        CMRField relatedField = (CMRField) association.getOtherEnd(field);
        EJB relatedEJB = (EJB) field.getEntity();
        CacheTable relatedCacheTbl = (CacheTable) globalSchema.getEntity(relatedEJB.getName());
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner((EJB) relatedField.getEntity());

        List pkFields = relatedEJB.getPrimaryKeyFields();
        IdentityDefiner relatedIdentityDefiner;
        if ( 1 == pkFields.size() ) {
            relatedIdentityDefiner = new UserDefinedIdentity(relatedCacheTbl, 0);
        } else {
            int slots[] = new int[pkFields.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = i;
            }
            relatedIdentityDefiner = new DerivedIdentity(relatedCacheTbl, slots);
        }

        QueryCommand faultCommand = handlerBuilder.buildCMRFaultHandler(field);
        if ( association.isOneToOne() || association.isManyToOne(field) ) {
            return new SingleValuedCMRFaultHandler(faultCommand,
                    identityDefiner,
                    new EmptySlotLoader[]{new EmptySlotLoader(slot, new ReferenceAccessor(relatedIdentityDefiner))});
        }
        return new MultiValuedCMRFaultHandler(faultCommand,
                slot,
                identityDefiner,
                new ReferenceAccessor(relatedIdentityDefiner));
    }

    private Map buildInstanceMap(Class beanClass, LinkedHashMap cmpFields, LinkedHashMap cmrFields) {
        Map instanceMap;
        instanceMap = new HashMap();

        // add the cmp-field getters and setters to the instance table
        addFieldOperations(instanceMap, beanClass, cmpFields);

        // add the cmr getters and setters to the instance table.
        addFieldOperations(instanceMap, beanClass, cmrFields);

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

    protected LinkedHashMap buildVopMap(Class beanClass,
            CacheTable cacheTable,
            Map cmrFieldAccessors,
            CMP1Bridge cmp1Bridge,
            IdentityDefiner identityDefiner,
            PrimaryKeyGeneratorDelegate keyGenerator,
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

            // create a VirtualOperation for the method (if the method is understood)
            String name = beanMethod.getName();

            if (TimedObject.class.isAssignableFrom(beanClass)) {
                MethodSignature signature = new MethodSignature("ejbTimeout", new Class[]{Timer.class});
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , EJBTimeoutOperation.INSTANCE);
            }

            MethodSignature signature = new MethodSignature(beanMethod);

            if (name.startsWith("ejbCreate")) {
                // ejbCreate vop needs a reference to the ejbPostCreate method
                MethodSignature postCreateSignature = new MethodSignature("ejbPostCreate" + name.substring(9),
                        beanMethod.getParameterTypes());
                vopMap.put(MethodHelper.translateToInterface(signature),
                        new CMPCreateMethod(beanClass,
                                cmp1Bridge,
                                signature,
                                postCreateSignature,
                                cacheTable,
                                identityDefiner,
                                keyGenerator,
                                primaryKeyTransform,
                                localProxyTransform,
                                remoteProxyTransform));
            } else if (name.startsWith("ejbHome")) {
                vopMap.put(MethodHelper.translateToInterface(signature),
                        new HomeMethod(beanClass, signature));
            } else if (name.equals("ejbRemove")) {
                // there are three valid ways to invoke remove on an entity bean

                // ejbObject.remove()
                vopMap.put(new InterfaceMethodSignature("remove", false),
                        new CMPRemoveMethod(beanClass, signature, cacheTable, cmrFieldAccessors));

                // ejbHome.remove(primaryKey)
                vopMap.put(new InterfaceMethodSignature("ejbRemove", new Class[]{Object.class}, true),
                        new CMPRemoveMethod(beanClass, signature, cacheTable, cmrFieldAccessors));

                // ejbHome.remove(handle)
                Class handleClass = getClassLoader().loadClass("javax.ejb.Handle");
                vopMap.put(new InterfaceMethodSignature("ejbRemove", new Class[]{handleClass}, true),
                        new CMPRemoveMethod(beanClass, signature, cacheTable, cmrFieldAccessors));
            } else if (name.equals("ejbActivate")) {
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , EJBActivateOperation.INSTANCE);
            } else if (name.equals("ejbLoad")) {
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , EJBLoadOperation.INSTANCE);
            } else if (name.equals("ejbPassivate")) {
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , EJBPassivateOperation.INSTANCE);
            } else if (name.equals("ejbStore")) {
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , EJBStoreOperation.INSTANCE);
            } else if (setEntityContext.equals(beanMethod)) {
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , SetEntityContextOperation.INSTANCE);
            } else if (unsetEntityContext.equals(beanMethod)) {
                vopMap.put(MethodHelper.translateToInterface(signature)
                        , UnsetEntityContextOperation.INSTANCE);
            } else if (name.startsWith("ejb")) {
                //TODO this shouldn't happen?
                continue;
            } else {
                vopMap.put(MethodHelper.translateToInterface(signature),
                        new BusinessMethod(beanClass, signature));
            }
        }

        Class homeInterface = proxyInfo.getHomeInterface();
        Class localHomeInterface = proxyInfo.getLocalHomeInterface();
        for (Iterator iterator = queries.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature signature = (InterfaceMethodSignature) entry.getKey();
            QueryCommandView[] queryCommandViews = (QueryCommandView[]) entry.getValue();

            Method method = signature.getMethod(homeInterface);
            if (method == null) {
                method = signature.getMethod(localHomeInterface);
            }
            if (method == null) {
                throw new DeploymentException("Could not find method for signature: " + signature);
            }

            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                vopMap.put(signature, new CollectionValuedFinder(cacheTable, identityDefiner,
                        localProxyTransform, remoteProxyTransform, queryCommandViews[0], queryCommandViews[1]));
            } else if (returnType.equals("java.util.Enumeration")) {
                vopMap.put(signature, new EnumerationValuedFinder(cacheTable, identityDefiner,
                        localProxyTransform, remoteProxyTransform, queryCommandViews[0], queryCommandViews[1]));
            } else {
                vopMap.put(signature, new SingleValuedFinder(cacheTable, identityDefiner,
                        localProxyTransform, remoteProxyTransform, queryCommandViews[0], queryCommandViews[1]));
            }
        }
        return vopMap;
    }
}
