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
 * $Id$
 */
package org.openejb.entity.cmp;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.common.DeploymentException;
import org.openejb.proxy.ProxyInfo;
import org.tranql.cache.CacheTable;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.cache.Cache;
import org.tranql.cache.cache.FrontEndCache;
import org.tranql.cache.cache.FrontEndCacheDelegate;
import org.tranql.cache.cache.FrontEndToCacheAdaptor;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.identity.IdentityDefinerBuilder;
import org.tranql.sql.SQLQueryBuilder;
import org.tranql.sql.SQLSchema;

/**
 * @version $Revision$ $Date$
 */
public class TranqlModuleCmpEngine implements ModuleCmpEngine {
    private EJBSchema ejbSchema;
    private GlobalSchema globalSchema;
    private TransactionManager transactionManager;
    private final TranqlCommandBuilder tranqlCommandBuilder;
    private final FrontEndCacheDelegate cacheDelegate = new FrontEndCacheDelegate();

    public TranqlModuleCmpEngine(ModuleSchema moduleSchema, 
            TransactionManager transactionManager,
            ConnectionProxyFactory connectionFactory,
            ClassLoader classLoader,
            Kernel kernel) throws DeploymentException {
        if (moduleSchema == null) {
            throw new NullPointerException("moduleSchema is null");
        }
        if (connectionFactory == null) {
            throw new NullPointerException("connectionFactory is null");
        }
        if (classLoader == null) {
            throw new NullPointerException("classLoader is null");
        }
        if (kernel == null) {
            throw new NullPointerException("kernel is null");
        }

        this.transactionManager = transactionManager;

        DataSource dataSource = (DataSource) connectionFactory.$getResource();
        if (dataSource == null) {
            throw new IllegalStateException("Connection factory returned a null data source");
        }

        TranqlSchemaBuilder tranqlSchemaBuilder = new TranqlSchemaBuilder(moduleSchema,
                dataSource,
                kernel,
                transactionManager,
                classLoader);
        tranqlSchemaBuilder.buildSchema();
        this.globalSchema = tranqlSchemaBuilder.getGlobalSchema();
        this.ejbSchema = tranqlSchemaBuilder.getEjbSchema();
        SQLSchema sqlSchema = tranqlSchemaBuilder.getSqlSchema();

        SQLQueryBuilder queryBuilder = new SQLQueryBuilder(ejbSchema, sqlSchema, globalSchema);
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(ejbSchema, globalSchema);
        tranqlCommandBuilder = new TranqlCommandBuilder(globalSchema, transactionManager, identityDefinerBuilder, queryBuilder);
    }

    public EjbCmpEngine getEjbCmpEngine(String ejbName, Class beanClass, ProxyInfo proxyInfo) throws Exception {
        EJB ejb = ejbSchema.getEJB(ejbName);
        if (ejb == null) {
            throw new IllegalArgumentException("Schema does not contain EJB: " + ejbName);
        }

        CacheTable cacheTable = globalSchema.getCacheTable(ejbName);
        Cache cache = cacheTable.getCacheFactory().factory();
        FrontEndCache frontEndCache = new FrontEndToCacheAdaptor(transactionManager, cache);
        cacheDelegate.addFrontEndCache(ejbName, frontEndCache);

        return new TranqlEjbCmpEngine(ejb, beanClass, proxyInfo, frontEndCache, cacheTable, tranqlCommandBuilder);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(TranqlModuleCmpEngine.class, "ModuleCmpEngine");

        infoFactory.addAttribute("moduleSchema", ModuleSchema.class, true);
        infoFactory.addReference("transactionManager", TransactionManager.class, NameFactory.TRANSACTION_MANAGER);
        infoFactory.addReference("connectionFactory", ConnectionProxyFactory.class, NameFactory.JCA_CONNECTION_FACTORY);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{
            "moduleSchema",
            "transactionManager",
            "connectionFactory",
            "classLoader",
            "kernel",
        });

        infoFactory.addInterface(ModuleCmpEngine.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
