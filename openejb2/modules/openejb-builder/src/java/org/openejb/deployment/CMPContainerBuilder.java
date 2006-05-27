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
import javax.transaction.TransactionManager;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
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
import org.openejb.entity.cmp.CMPEJBContainer;
import org.openejb.entity.cmp.CMPEntityInterceptorBuilder;
import org.openejb.entity.cmp.CMPGetter;
import org.openejb.entity.cmp.CMPInstanceContextFactory;
import org.openejb.entity.cmp.CMPRemoveMethod;
import org.openejb.entity.cmp.CMPSetter;
import org.openejb.entity.cmp.CollectionValuedFinder;
import org.openejb.entity.cmp.CollectionValuedSelect;
import org.openejb.entity.cmp.EnumerationValuedFinder;
import org.openejb.entity.cmp.SetValuedFinder;
import org.openejb.entity.cmp.SetValuedSelect;
import org.openejb.entity.cmp.SingleValuedFinder;
import org.openejb.entity.cmp.SingleValuedSelect;
import org.openejb.entity.dispatch.EJBActivateOperation;
import org.openejb.entity.dispatch.EJBLoadOperation;
import org.openejb.entity.dispatch.EJBPassivateOperation;
import org.openejb.entity.dispatch.EJBStoreOperation;
import org.openejb.entity.dispatch.SetEntityContextOperation;
import org.openejb.entity.dispatch.UnsetEntityContextOperation;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.builder.SQLQueryBuilder;
import org.tranql.cache.CacheRowAccessor;
import org.tranql.cache.CacheTable;
import org.tranql.cache.EmptySlotLoader;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.QueryFaultHandler;
import org.tranql.ejb.CMPFieldAccessor;
import org.tranql.ejb.CMPFieldFaultTransform;
import org.tranql.ejb.CMPFieldIdentityExtractorAccessor;
import org.tranql.ejb.CMPFieldNestedRowAccessor;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.ejb.CMPMappedToCMRAccessor;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.CMRMappedToInversePKCMP;
import org.tranql.ejb.CMRMappedToOwningPKCMP;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.FinderEJBQLQuery;
import org.tranql.ejb.LocalProxyTransform;
import org.tranql.ejb.ManyToManyCMR;
import org.tranql.ejb.ManyToOneCMR;
import org.tranql.ejb.MultiValuedCMRAccessor;
import org.tranql.ejb.MultiValuedCMRFaultHandler;
import org.tranql.ejb.OneToManyCMR;
import org.tranql.ejb.OneToOneCMR;
import org.tranql.ejb.ReadOnlyCMPFieldAccessor;
import org.tranql.ejb.RemoteProxyTransform;
import org.tranql.ejb.SelectEJBQLQuery;
import org.tranql.ejb.SingleValuedCMRAccessor;
import org.tranql.ejb.SingleValuedCMRFaultHandler;
import org.tranql.field.FieldAccessor;
import org.tranql.field.FieldTransform;
import org.tranql.field.ReferenceAccessor;
import org.tranql.identity.DerivedIdentity;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityTransform;
import org.tranql.identity.UserDefinedIdentity;
import org.tranql.intertxcache.CacheFactory;
import org.tranql.intertxcache.CacheFaultHandler;
import org.tranql.intertxcache.CacheFieldFaultTransform;
import org.tranql.intertxcache.FindByPKCacheQueryCommand;
import org.tranql.intertxcache.FrontEndCacheDelegate;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.schema.Association;
import org.tranql.schema.AssociationEnd;
import org.tranql.schema.Attribute;
import org.tranql.schema.Entity;
import org.tranql.schema.FKAttribute;
import org.tranql.schema.Schema;
import org.tranql.sql.SQLSchema;

/**
 * @version $Revision$ $Date$
 */
public class CMPContainerBuilder extends AbstractContainerBuilder {
    private boolean cmp2 = true;
    private EJBSchema ejbSchema;
    private SQLSchema sqlSchema;
    private GlobalSchema globalSchema;
    private EJB ejb;
    private TransactionManager tm;
    private FrontEndCacheDelegate cache;
    private CacheFactory factory;
    private boolean reentrant;
    
    public boolean isCMP2() {
        return cmp2;
    }

    public void setCMP2(boolean cmp2) {
        this.cmp2 = cmp2;
    }

    protected int getEJBComponentType() {
        return EJBComponentType.CMP_ENTITY;
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

    public void setSQLSchema(SQLSchema sqlSchema) {
        this.sqlSchema = sqlSchema;
    }

    public GlobalSchema getGlobalSchema() {
        return globalSchema;
    }

    public void setGlobalSchema(GlobalSchema globalSchema) {
        this.globalSchema = globalSchema;
    }

    public TransactionManager getTransactionManager() {
        return tm;
    }

    public void setTransactionManager(TransactionManager tm) {
        this.tm = tm;
    }

    public FrontEndCacheDelegate getFrontEndCacheDelegate() {
        return cache;
    }

    public void setFrontEndCacheDelegate(FrontEndCacheDelegate cache) {
        this.cache = cache;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    protected InterceptorBuilder initializeInterceptorBuilder(CMPEntityInterceptorBuilder interceptorBuilder, InterfaceMethodSignature[] signatures, VirtualOperation[] vtable) {
        super.initializeInterceptorBuilder(interceptorBuilder, signatures, vtable);
        interceptorBuilder.setCacheFlushStrategyFactory(globalSchema.getCacheFlushStrategyFactorr());
        interceptorBuilder.setFrontEndCache(cache);
        interceptorBuilder.setReentrant(reentrant);
        return interceptorBuilder;
    }

    protected void initialize() throws Exception {
        String name = getEJBName();
        ejb = ejbSchema.getEJB(name);
        if (ejb == null) {
            throw new DeploymentException("Schema does not contain EJB: " + name);
        }
        CacheTable cacheTable = globalSchema.getCacheTable(name);
        factory = cacheTable.getCacheFactory();
    }
    
    protected Object buildIt(GBeanData gbeanData) throws Exception {
        initialize();

        // get the bean classes
        ClassLoader classLoader = getClassLoader();
        Class beanClass = classLoader.loadClass(getBeanClassName());

        EJBProxyFactory proxyFactory = (EJBProxyFactory) ejb.getProxyFactory();

        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(ejbSchema, globalSchema);
        SQLQueryBuilder queryBuilder = new SQLQueryBuilder(ejbSchema, sqlSchema, globalSchema);
        CacheTable cacheTable = (CacheTable) globalSchema.getEntity(getEJBName());

        // Identity Transforms
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);
        IdentityTransform primaryKeyTransform = identityDefinerBuilder.getPrimaryKeyTransform(ejb);
        IdentityTransform localProxyTransform = new LocalProxyTransform(primaryKeyTransform, proxyFactory);
        IdentityTransform remoteProxyTransform = new RemoteProxyTransform(primaryKeyTransform, proxyFactory);

        List attributes = ejb.getAttributes();
        List pkAttributes = ejb.getPrimaryKeyFields();
        EmptySlotLoader[] slotLoaders = new EmptySlotLoader[pkAttributes.size()];
        String[] attributeNames = new String[pkAttributes.size()];
        int[] indexes = new int[pkAttributes.size()];
        for (int i = 0; i < pkAttributes.size(); i++) {
            Attribute attr = (Attribute) pkAttributes.get(i);
            attributeNames[i] = attr.getName();
            indexes[i] = attributes.indexOf(attr);
            slotLoaders[i] = new EmptySlotLoader(attributes.indexOf(attr), new FieldAccessor(i, attr.getType()));
        }
        QueryCommand loadCommand = queryBuilder.buildLoadEntity(getEJBName(), attributeNames);
        FaultHandler faultHandler = new QueryFaultHandler(loadCommand, identityDefiner, slotLoaders);
        faultHandler = new CacheFaultHandler(cache, faultHandler, indexes);

        // EJB QL queries
        Map finders = queryBuilder.buildFinders(ejb.getName());
        
        // findByPrimaryKey
        QueryCommand localProxyLoad = queryBuilder.buildFindByPrimaryKey(getEJBName(), true);
        localProxyLoad = new FindByPKCacheQueryCommand(cache, primaryKeyTransform, localProxyLoad);
        QueryCommand remoteProxyLoad = queryBuilder.buildFindByPrimaryKey(getEJBName(), false);
        remoteProxyLoad = new FindByPKCacheQueryCommand(cache, primaryKeyTransform, remoteProxyLoad);
        
        Class pkClass = ejb.isUnknownPK() ? Object.class :  ejb.getPrimaryKeyClass();
        FinderEJBQLQuery pkFinder = new FinderEJBQLQuery("findByPrimaryKey", new Class[] {pkClass}, "UNDEFINED");
        QueryCommand[] commands = new QueryCommand[]{localProxyLoad, remoteProxyLoad};
        boolean found = false;
        for (Iterator iter = finders.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            FinderEJBQLQuery query = (FinderEJBQLQuery) entry.getKey();
            if (query.equals(pkFinder)) {
                entry.setValue(commands);
                found = true;
                break;
            }
        }
        if (!found) {
            finders.put(pkFinder, commands);
        }

        // build the instance factory
        LinkedHashMap cmrFieldAccessors[] = createCMRFieldAccessors(queryBuilder, true);
        LinkedHashMap cmrNoPrefetchFieldAccessors[] = createCMRFieldAccessors(queryBuilder, false);
        LinkedHashMap cmpFieldAccessors = createCMPFieldAccessors(queryBuilder, cmrFieldAccessors[0]);
        Map selects = queryBuilder.buildSelects(ejb.getName());
        Map instanceMap = null;
        CMP1Bridge cmp1Bridge = null;
        if (cmp2) {
            // filter out the accessors associated to virtual CMR fields.
            LinkedHashMap existingCMRFieldAccessors = new LinkedHashMap(cmrFieldAccessors[1]);
            for (Iterator iter = existingCMRFieldAccessors.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                if ( ejb.getAssociationEnd(name).isVirtual() ) {
                    iter.remove();
                }
            }
            instanceMap = buildInstanceMap(beanClass, cmpFieldAccessors, existingCMRFieldAccessors, selects, identityDefinerBuilder);
        } else {
            cmp1Bridge = new CMP1Bridge(beanClass, cmpFieldAccessors);
        }

        // build the vop table
        LinkedHashMap vopMap = buildVopMap(beanClass, cacheTable, cmrNoPrefetchFieldAccessors[1], cmp1Bridge, identityDefinerBuilder, ejb.getPrimaryKeyGenerator(), primaryKeyTransform, localProxyTransform, remoteProxyTransform, finders);

        InterfaceMethodSignature[] signatures = (InterfaceMethodSignature[]) vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopMap.values().toArray(new VirtualOperation[vopMap.size()]);

        // create and intitalize the interceptor moduleBuilder
        InterceptorBuilder interceptorBuilder = initializeInterceptorBuilder(new CMPEntityInterceptorBuilder(), signatures, vtable);

        InstanceContextFactory contextFactory = new CMPInstanceContextFactory(getContainerId(), cmp1Bridge, primaryKeyTransform, faultHandler, beanClass, instanceMap, getUnshareableResources(), getApplicationManagedSecurityResources());
        EntityInstanceFactory instanceFactory = new EntityInstanceFactory(contextFactory);

        // build the pool
        InstancePool pool = createInstancePool(instanceFactory);
        AbstractNameQuery timerName = getTimerName(beanClass);

        if (gbeanData == null) {
            return createContainer(signatures, contextFactory, interceptorBuilder, pool);
        }
        return createConfiguration(gbeanData, classLoader, signatures, contextFactory, interceptorBuilder, pool, timerName);
    }

    private LinkedHashMap createCMPFieldAccessors(SQLQueryBuilder queryBuilder, LinkedHashMap cmrFieldAccessor) throws QueryException {
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(ejbSchema, globalSchema);
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
            if (null != ejb.getAssociationEndDefiningFKAttribute(name)) {
                AssociationEnd end = ejb.getAssociationEndDefiningFKAttribute(name);
                CMPFieldTransform cmrAccessor = (CMPFieldTransform) cmrFieldAccessor.get(end.getName());

                Entity relatedEntity = end.getEntity();
                if (1 < relatedEntity.getPrimaryKeyFields().size()) {
                    IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(end.getEntity());
                    accessor = new CMPFieldIdentityExtractorAccessor(cmrAccessor, identityDefiner);

                    int index = 0;
                    LinkedHashMap pkToFK = end.getAssociation().getJoinDefinition().getPKToFKMapping();
                    for (Iterator iter = pkToFK.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        FKAttribute fkAttribute = (FKAttribute) entry.getValue();
                        if (fkAttribute.getName().equals(name)) {
                            accessor = new CMPFieldNestedRowAccessor(accessor, index);
                            break;
                        }
                        index++;
                    }
                    
                    accessor = new ReadOnlyCMPFieldAccessor(accessor, attribute.getName());
                } else {
                    IdentityTransform transform = identityDefinerBuilder.getPrimaryKeyTransform(relatedEntity);
                    accessor = new CMPMappedToCMRAccessor(cmrAccessor, accessor, transform);
                }
            }  else {
                IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);
                QueryCommand command = queryBuilder.buildLoadAttribute(ejb.getName(), name, true);
                FieldTransform attAccessor = command.getQuery().getResultAccessors()[0];
                EmptySlotLoader[] loaders = new EmptySlotLoader[] {new EmptySlotLoader(i, attAccessor)};
                FaultHandler faultHandler = new QueryFaultHandler(command, identityDefiner, loaders);
                accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});
                accessor = new CacheFieldFaultTransform(cache, accessor, i);
            }
            // TODO: this breaks the CMP1 bridge.
//            if (attribute.isIdentity()) {
//                accessor = new PKFieldAccessCheck(accessor);
//            }
            
            cmpFieldAccessors.put(name, accessor);
        }
        return cmpFieldAccessors;
    }

    private LinkedHashMap[] createCMRFieldAccessors(SQLQueryBuilder queryBuilder, boolean prefetch) throws QueryException {
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(ejbSchema, globalSchema);
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);

        List associationEnds = ejb.getAssociationEnds();
        LinkedHashMap cmrFaultAccessors = new LinkedHashMap(associationEnds.size());
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

            FaultHandler faultHandler = buildFaultHandler(queryBuilder, ejb, field, i, prefetch);
            accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});

            accessor = new CacheFieldFaultTransform(cache, accessor, i);

            int relatedIndex = relatedEJB.getAttributes().size() + relatedEJB.getAssociationEnds().indexOf(relatedField);
            FaultHandler relatedFaultHandler = buildFaultHandler(queryBuilder, relatedEJB, relatedField, relatedIndex, prefetch);
            CMPFieldTransform relatedAccessor = new CMPFieldAccessor(new CacheRowAccessor(relatedIndex, null), name);
            relatedAccessor = new CMPFieldFaultTransform(relatedAccessor, relatedFaultHandler, new int[]{relatedIndex});
            if ( field.isOneToOne() ) {
                accessor = new OneToOneCMR(accessor, identityDefiner, relatedAccessor, relatedIdentityDefiner);
                accessor = buildCMRMappedToPKCMP(relatedEJB, relatedField, accessor, false, relatedIndex);
                accessor = buildCMRMappedToPKCMP(ejb, field, accessor, true, i);
            } else if ( field.isOneToMany() ) {
                relatedAccessor = buildCMRMappedToPKCMP(relatedEJB, relatedField, relatedAccessor, true, relatedIndex);
                accessor = new ManyToOneCMR(accessor, identityDefiner, relatedAccessor, relatedIdentityDefiner);
            } else if ( field.isManyToOne() ) {
                accessor = new OneToManyCMR(accessor, relatedAccessor, relatedIdentityDefiner);
                accessor = buildCMRMappedToPKCMP(ejb, field, accessor, true, i);
            } else {
                CacheTable mtm = (CacheTable) getGlobalSchema().getEntity(association.getManyToManyEntity().getName());
                boolean isRight = association.getRightJoinDefinition().getPKEntity() == ejb;
                accessor = new ManyToManyCMR(accessor, relatedAccessor, relatedIdentityDefiner, mtm, isRight);
            }
            
            cmrFaultAccessors.put(name, accessor);

            IdentityTransform relatedIdentityTransform = identityDefinerBuilder.getPrimaryKeyTransform(relatedEJB);
            if ( field.isOneToOne() || field.isManyToOne() ) {
                accessor = new SingleValuedCMRAccessor(accessor,
                        new LocalProxyTransform(relatedIdentityTransform, relatedEJB.getProxyFactory()));
            } else {
                accessor = new MultiValuedCMRAccessor(accessor, tm,
                        new LocalProxyTransform(relatedIdentityTransform, relatedEJB.getProxyFactory()),
                        relatedEJB.getProxyFactory().getLocalInterfaceClass());
            }

            cmrFieldAccessors.put(name, accessor);
        }

        return new LinkedHashMap[] {cmrFaultAccessors, cmrFieldAccessors};
    }

    private CMPFieldTransform buildCMRMappedToPKCMP(Entity entity, AssociationEnd end, CMPFieldTransform accessor, boolean owning, int cmrSlot) {
        List pkFields = entity.getPrimaryKeyFields();
        for (Iterator iter = pkFields.iterator(); iter.hasNext();) {
            Attribute pkField = (Attribute) iter.next();
            if (end.hasFKAttribute(pkField.getName())) {
                if (owning) {
                    return new CMRMappedToOwningPKCMP(accessor, cmrSlot);
                }
                return new CMRMappedToInversePKCMP(accessor, cmrSlot);
            }
        }
        return accessor;
    }
    
    private FaultHandler buildFaultHandler(SQLQueryBuilder queryBuilder, EJB definingEJB, CMRField field, int slot, boolean prefetch) throws QueryException {
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(ejbSchema, globalSchema);
        Association association = field.getAssociation();
        CMRField relatedField = (CMRField) association.getOtherEnd(field);
        EJB relatedEJB = (EJB) field.getEntity();
        CacheTable relatedCacheTbl = (CacheTable) globalSchema.getEntity(relatedEJB.getName());
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(relatedField.getEntity());

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

        QueryCommand faultCommand = queryBuilder.buildLoadAssociationEnd(definingEJB.getName(), field.getName(), prefetch);
        if ( field.isOneToOne() || field.isManyToOne() ) {
            return new SingleValuedCMRFaultHandler(faultCommand,
                    identityDefiner,
                    new EmptySlotLoader[]{new EmptySlotLoader(slot, new ReferenceAccessor(relatedIdentityDefiner))});
        }
        return new MultiValuedCMRFaultHandler(faultCommand,
                slot,
                identityDefiner,
                new ReferenceAccessor(relatedIdentityDefiner));
    }

    private Map buildInstanceMap(Class beanClass, LinkedHashMap cmpFields, LinkedHashMap cmrFields, Map selects, IdentityDefinerBuilder identityDefinerBuilder) {
        Map instanceMap;
        instanceMap = new HashMap();

        // add the cmp-field getters and setters to the instance table
        addFieldOperations(instanceMap, beanClass, cmpFields);

        // add the cmr getters and setters to the instance table.
        addFieldOperations(instanceMap, beanClass, cmrFields);

        // add the select methods
        addSelects(instanceMap, beanClass, selects, identityDefinerBuilder);

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

    private void addSelects(Map instanceMap, Class beanClass, Map selects, IdentityDefinerBuilder idDefinerBuilder) throws IllegalArgumentException {
        for (Iterator iterator = selects.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SelectEJBQLQuery query = (SelectEJBQLQuery) entry.getKey();
            
            InterfaceMethodSignature signature = new InterfaceMethodSignature(query.getMethodName(), query.getParameterTypes(), true);
            QueryCommand command = (QueryCommand) entry.getValue();

            Method method = signature.getMethod(beanClass);
            if (method == null) {
                throw new IllegalArgumentException("Could not find select for signature: " + signature);
            }
            MethodSignature methodSignature = new MethodSignature(method);
            
            IdentityDefiner idDefiner = null;
            IdentityDefiner idInjector = null;
            if (null != query.getSelectedEJB()) {
                idDefiner = idDefinerBuilder.getIdentityDefiner(query.getSelectedEJB(), 0);
                idInjector = idDefinerBuilder.getIdentityDefiner(query.getSelectedEJB());
            }
            
            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                instanceMap.put(methodSignature, new CollectionValuedSelect(command, query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            } else if (returnType.equals("java.util.Set")) {
                instanceMap.put(methodSignature, new SetValuedSelect(command, query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            } else {
                instanceMap.put(methodSignature, new SingleValuedSelect(command, query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            }
        }
    }

    protected LinkedHashMap buildVopMap(Class beanClass,
            CacheTable cacheTable,
            Map cmrFieldAccessors,
            CMP1Bridge cmp1Bridge,
            IdentityDefinerBuilder identityDefinerBuilder,
            PrimaryKeyGenerator keyGenerator,
            IdentityTransform primaryKeyTransform,
            IdentityTransform localProxyTransform,
            IdentityTransform remoteProxyTransform,
            Map finders) throws Exception {

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

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            MethodSignature signature = new MethodSignature("ejbTimeout", new Class[]{Timer.class});
            vopMap.put(MethodHelper.translateToInterface(signature)
                    , EJBTimeoutOperation.INSTANCE);
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

            MethodSignature signature = new MethodSignature(beanMethod);

            if (name.startsWith("ejbCreate")) {
                // ejbCreate vop needs a reference to the ejbPostCreate method
                MethodSignature postCreateSignature = new MethodSignature("ejbPostCreate" + name.substring(9),
                        beanMethod.getParameterTypes());
                IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb); 
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
                vopMap.put(new InterfaceMethodSignature("remove", new Class[]{Object.class}, true),
                        new CMPRemoveMethod(beanClass, signature, cacheTable, cmrFieldAccessors));

                // ejbHome.remove(handle)
                Class handleClass = getClassLoader().loadClass("javax.ejb.Handle");
                vopMap.put(new InterfaceMethodSignature("remove", new Class[]{handleClass}, true),
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
            } else if (name.startsWith("ejbSelect")) {
                ;
            } else if (name.startsWith("ejb")) {
                //TODO this shouldn't happen?
                continue;
            } else {
                vopMap.put(MethodHelper.translateToInterface(signature),
                        new BusinessMethod(beanClass, signature));
            }
        }

        IdentityDefiner idDefiner = identityDefinerBuilder.getIdentityDefiner(ejb, 0);
        IdentityDefiner idInjector = identityDefinerBuilder.getIdentityDefiner(ejb);
        Class homeInterface = proxyInfo.getHomeInterface();
        Class localHomeInterface = proxyInfo.getLocalHomeInterface();
        for (Iterator iterator = finders.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            FinderEJBQLQuery query = (FinderEJBQLQuery) entry.getKey();
            
            InterfaceMethodSignature signature = new InterfaceMethodSignature(query.getMethodName(), query.getParameterTypes(), true);
            QueryCommand[] commands = (QueryCommand[]) entry.getValue();

            Method method = signature.getMethod(homeInterface);
            if (method == null) {
                method = signature.getMethod(localHomeInterface);
            }
            if (method == null) {
                throw new DeploymentException("Could not find method for signature: " + signature);
            }

            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                vopMap.put(signature, new CollectionValuedFinder(commands[0], commands[1], query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            } else if (returnType.equals("java.util.Set")) {
                vopMap.put(signature, new SetValuedFinder(commands[0], commands[1], query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            } else if (returnType.equals("java.util.Enumeration")) {
                vopMap.put(signature, new EnumerationValuedFinder(commands[0], commands[1], query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            } else {
                vopMap.put(signature, new SingleValuedFinder(commands[0], commands[1], query.isFlushCacheBeforeQuery(), idDefiner, idInjector));
            }
        }
        
        return vopMap;
    }
    
//    protected GBeanData buildGBeanData() {
//        return new GBeanData(CMPEJBContainer.GBEAN_INFO);
//    }
    
    protected EJBContainer createContainer(InterfaceMethodSignature[] signatures,
            InstanceContextFactory contextFactory,
            InterceptorBuilder interceptorBuilder,
            InstancePool pool) throws Exception {

        return new CMPEJBContainer(
                getContainerId(),
                getEJBName(),
                createProxyInfo(),
                signatures,
                contextFactory,
                interceptorBuilder,
                pool,
                getComponentContext(),
                getUserTransaction(),
                getJndiNames(),
                getLocalJndiNames(),
                getTransactionContextManager(),
                getTrackedConnectionAssociator(),
                null, // timer
                null, // objectname
                null, // kernel
                getDefaultPrincipal(),
                runAs,
                null,
                getHomeTxPolicyConfig(),
                getRemoteTxPolicyConfig(),
                Thread.currentThread().getContextClassLoader(),
                cache,
                factory);
    }
    
    protected GBeanData createConfiguration(GBeanData gbean, ClassLoader cl, InterfaceMethodSignature[] signatures,
            InstanceContextFactory contextFactory,
            InterceptorBuilder interceptorBuilder,
            InstancePool pool,
            AbstractNameQuery timerName) throws Exception {
        super.createConfiguration(gbean, cl, signatures, contextFactory, interceptorBuilder, pool, timerName);
        
        gbean.setAttribute("frontEndCacheDelegate", cache);
        gbean.setAttribute("cacheFactory", factory);
        
        return gbean;
    }
}
