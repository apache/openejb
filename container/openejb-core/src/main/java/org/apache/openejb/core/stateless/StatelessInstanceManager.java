/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.stateless;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.TimerServiceWrapper;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.openejb.util.Pool;
import org.apache.openejb.util.SafeToolkit;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBContext;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StatelessInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final Method removeSessionBeanMethod;

    static { // initialize it only once
        Method foundRemoveMethod;
        try {
            foundRemoveMethod = SessionBean.class.getDeclaredMethod("ejbRemove");
        } catch (NoSuchMethodException e) {
            foundRemoveMethod = null;
        }
        removeSessionBeanMethod = foundRemoveMethod;
    }

    protected Duration accessTimeout;
    protected Duration closeTimeout;
    protected int beanCount = 0;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    private SecurityService securityService;
    private final Pool.Builder poolBuilder;
    private final Executor executor;

    public StatelessInstanceManager(SecurityService securityService, Duration accessTimeout, Duration closeTimeout, Pool.Builder poolBuilder, int callbackThreads) {
        this.securityService = securityService;
        this.accessTimeout = accessTimeout;
        this.closeTimeout = closeTimeout;
        this.poolBuilder = poolBuilder;

        if (accessTimeout.getUnit() == null) accessTimeout.setUnit(TimeUnit.MILLISECONDS);

        executor = new ThreadPoolExecutor(callbackThreads, callbackThreads * 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(Runnable runable) {
                Thread t = new Thread(runable, "StatelessPool");
                t.setDaemon(true);
                return t;
            }
        });


    }

    private class StatelessSupplier implements Pool.Supplier<Instance> {
        private final BeanContext beanContext;

        private StatelessSupplier(BeanContext beanContext) {
            this.beanContext = beanContext;
        }

        public void discard(Instance instance, Pool.Event reason) {
            ThreadContext ctx = new ThreadContext(beanContext, null);
            ThreadContext oldCallContext = ThreadContext.enter(ctx);
            try {
                freeInstance(ctx, instance);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
        }

        public Instance create() {
            ThreadContext ctx = new ThreadContext(beanContext, null);
            ThreadContext oldCallContext = ThreadContext.enter(ctx);
            try {
                return ceateInstance(ctx, ctx.getBeanContext());
            } catch (OpenEJBException e) {
                logger.error("Unable to fill pool: for deployment '" + beanContext.getDeploymentID() + "'", e);
            } finally {
                ThreadContext.exit(oldCallContext);
            }

            return null;
        }
    }

    /**
     * Removes an instance from the pool and returns it for use
     * by the container in business methods.
     *
     * If the pool is at it's limit the StrictPooling flag will
     * cause this thread to wait.
     *
     * If StrictPooling is not enabled this method will create a
     * new stateless bean instance performing all required injection
     * and callbacks before returning it in a method ready state.
     *
     * @param callContext
     * @return
     * @throws OpenEJBException
     */
    public Object getInstance(ThreadContext callContext) throws OpenEJBException {
        BeanContext beanContext = callContext.getBeanContext();
        Data data = (Data) beanContext.getContainerData();

        Instance instance = null;
        try {
            final Pool<Instance>.Entry entry = data.poolPop();

            if (entry != null) {
                instance = entry.get();
                instance.setPoolEntry(entry);
            }
        } catch (TimeoutException e) {
            ConcurrentAccessTimeoutException timeoutException = new ConcurrentAccessTimeoutException("No instances available in Stateless Session Bean pool.  Waited " + data.accessTimeout.toString());
            timeoutException.fillInStackTrace();

            throw new ApplicationException(timeoutException);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new OpenEJBException("Unexpected Interruption of current thread: ", e);
        }

        if (instance != null) return instance;

        return ceateInstance(callContext, beanContext);
    }

    private Instance ceateInstance(ThreadContext callContext, BeanContext beanContext) throws org.apache.openejb.ApplicationException {

        try {

            final InstanceContext context = beanContext.newInstance();

            if (context.getBean() instanceof SessionBean) {

                final Operation originalOperation = callContext.getCurrentOperation();
                try {
                    callContext.setCurrentOperation(Operation.CREATE);
                    final Method create = beanContext.getCreateMethod();
                    final InterceptorStack ejbCreate = new InterceptorStack(context.getBean(), create, Operation.CREATE, new ArrayList<InterceptorData>(), new HashMap());
                    ejbCreate.invoke();
                } finally {
                    callContext.setCurrentOperation(originalOperation);
                }
            }

            return new Instance(context.getBean(), context.getInterceptors(), context.getCreationalContext());
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            String t = "The bean instance " + beanContext.getDeploymentID() + " threw a system exception:" + e;
            logger.error(t, e);
            throw new org.apache.openejb.ApplicationException(new RemoteException("Cannot obtain a free instance.", e));
        }
    }

    /**
     * All instances are removed from the pool in getInstance(...).  They are only
     * returned by the StatelessContainer via this method under two circumstances.
     *
     * 1.  The business method returns normally
     * 2.  The business method throws an application exception
     *
     * Instances are not returned to the pool if the business method threw a system
     * exception.
     *
     * @param callContext
     * @param bean
     * @throws OpenEJBException
     */
    public void poolInstance(ThreadContext callContext, Object bean) throws OpenEJBException {
        if (bean == null) throw new SystemException("Invalid arguments");
        Instance instance = Instance.class.cast(bean);

        BeanContext beanContext = callContext.getBeanContext();
        Data data = (Data) beanContext.getContainerData();

        Pool<Instance> pool = data.getPool();

        if (instance.getPoolEntry() != null) {
            pool.push(instance.getPoolEntry());
        } else {
            pool.push(instance);
        }
    }

    /**
     * This method is called to release the semaphore in case of the business method
     * throwing a system exception
     *
     * @param callContext
     * @param bean
     */
    public void discardInstance(final ThreadContext callContext, final Object bean) throws SystemException {
        if (bean == null) throw new SystemException("Invalid arguments");
        final Instance instance = Instance.class.cast(bean);

        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();

        if (null != data) {
            final Pool<Instance> pool = data.getPool();
            pool.discard(instance.getPoolEntry());
        }
    }

    private void freeInstance(ThreadContext callContext, Instance instance) {
        try {
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            BeanContext beanContext = callContext.getBeanContext();

            Method remove = instance.bean instanceof SessionBean ? removeSessionBeanMethod : null;

            List<InterceptorData> callbackInterceptors = beanContext.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();

            if (instance.creationalContext != null) {
                instance.creationalContext.release();
            }
        } catch (Throwable re) {
            logger.error("The bean instance " + instance + " threw a system exception:" + re, re);
        }

    }

    public void deploy(BeanContext beanContext) throws OpenEJBException {
        Options options = new Options(beanContext.getProperties());

        Duration accessTimeout = getDuration(options, "Timeout", this.accessTimeout, TimeUnit.MILLISECONDS);
        accessTimeout = getDuration(options, "AccessTimeout", accessTimeout, TimeUnit.MILLISECONDS);
        Duration closeTimeout = getDuration(options, "CloseTimeout", this.closeTimeout, TimeUnit.MINUTES);

        final ObjectRecipe recipe = PassthroughFactory.recipe(new Pool.Builder(poolBuilder));
        recipe.allow(Option.CASE_INSENSITIVE_FACTORY);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.setAllProperties(beanContext.getProperties());
        final Pool.Builder builder = (Pool.Builder) recipe.create();

        setDefault(builder.getMaxAge(), TimeUnit.HOURS);
        setDefault(builder.getIdleTimeout(), TimeUnit.MINUTES);
        setDefault(builder.getInterval(), TimeUnit.MINUTES);

        final StatelessSupplier supplier = new StatelessSupplier(beanContext);
        builder.setSupplier(supplier);
        builder.setExecutor(executor);


        Data data = new Data(builder.build(), accessTimeout, closeTimeout);
        beanContext.setContainerData(data);

        beanContext.set(EJBContext.class, data.sessionContext);

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", data.sessionContext);
            context.bind("comp/WebServiceContext", new EjbWsContext(data.sessionContext));
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (NamingException e) {
            throw new OpenEJBException("Failed to bind EJBContext/WebServiceContext/TimerService", e);
        }

        final int min = builder.getMin();
        long maxAge = builder.getMaxAge().getTime(TimeUnit.MILLISECONDS);
        double maxAgeOffset = builder.getMaxAgeOffset();

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", beanContext.getModuleID());
        jmxName.set("StatelessSessionBean", beanContext.getEjbName());
        jmxName.set("name", beanContext.getEjbName());

        final MBeanServer server = LocalMBeanServer.get();

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {

            StatsInterceptor stats = null;
            for (InterceptorInstance interceptor : beanContext.getUserAndSystemInterceptors()) {
                if (interceptor.getInterceptor() instanceof StatsInterceptor) {
                    stats = (StatsInterceptor) interceptor.getInterceptor();
                }
            }
            if (stats == null) { // normally useless
                stats = new StatsInterceptor(beanContext.getBeanClass());
                beanContext.addFirstSystemInterceptor(stats);
            }

            // register the invocation stats interceptor
            try {
                ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                data.add(objectName);
            } catch (Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // register the pool
        try {
            ObjectName objectName = jmxName.set("j2eeType", "Pool").build();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
            server.registerMBean(new ManagedMBean(data.pool), objectName);
            data.add(objectName);
        } catch (Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        // Finally, fill the pool and start it
        if (!options.get("BackgroundStartup", false) && min > 0) {
            ExecutorService es = Executors.newFixedThreadPool(min);
            for (int i = 0; i < min; i++) {
                es.submit(new InstanceCreatorRunnable(maxAge, i, min, maxAgeOffset, data, supplier));
            }
            es.shutdown();
            try {
                es.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                logger.error("can't fill the stateless pool", e);
            }
        }

        data.getPool().start();
    }

    private void setDefault(Duration duration, TimeUnit unit) {
        if (duration.getUnit() == null) duration.setUnit(unit);
    }

    private Duration getDuration(Options options, String property, Duration defaultValue, TimeUnit defaultUnit) {
        String s = options.get(property, defaultValue.toString());
        Duration duration = new Duration(s);
        if (duration.getUnit() == null) duration.setUnit(defaultUnit);
        return duration;
    }

    public void undeploy(BeanContext beanContext) {
        Data data = (Data) beanContext.getContainerData();
        if (data == null) return;

        MBeanServer server = LocalMBeanServer.get();
        for (ObjectName objectName : data.jmxNames) {
            try {
                server.unregisterMBean(objectName);
            } catch (Exception e) {
                logger.error("Unable to unregister MBean " + objectName);
            }
        }

        try {
            if (!data.closePool()) {
                logger.error("Timed-out waiting for stateless pool to close: for deployment '" + beanContext.getDeploymentID() + "'");
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        beanContext.setContainerData(null);
    }

    private final class Data {
        private final Pool<Instance> pool;
        private final Duration accessTimeout;
        private final Duration closeTimeout;
        private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
        private final SessionContext sessionContext;

        private Data(Pool<Instance> pool, Duration accessTimeout, Duration closeTimeout) {
            this.pool = pool;
            this.accessTimeout = accessTimeout;
            this.closeTimeout = closeTimeout;
            this.sessionContext = new StatelessContext(securityService, new Flushable() {
                public void flush() throws IOException {
                    getPool().flush();
                }
            });
        }

        public Duration getAccessTimeout() {
            return accessTimeout;
        }

        public Pool<Instance>.Entry poolPop() throws InterruptedException, TimeoutException {
            return pool.pop(accessTimeout.getTime(), accessTimeout.getUnit());
        }

        public Pool<Instance> getPool() {
            return pool;
        }

        public boolean closePool() throws InterruptedException {
            return pool.close(closeTimeout.getTime(), closeTimeout.getUnit());
        }

        public ObjectName add(ObjectName name) {
            jmxNames.add(name);
            return name;
        }
    }

    private class InstanceCreatorRunnable implements Runnable {
        private long maxAge;
        private long iteration;
        private double maxAgeOffset;
        private long min;
        private Data data;
        private StatelessSupplier supplier;

        private InstanceCreatorRunnable(long maxAge, long iteration, long min, double maxAgeOffset, Data data, StatelessSupplier supplier) {
            this.maxAge = maxAge;
            this.iteration = iteration;
            this.min = min;
            this.maxAgeOffset = maxAgeOffset;
            this.data = data;
            this.supplier = supplier;
        }

        @Override
        public void run() {
            final Instance obj = supplier.create();
            if (obj != null) {
                long offset = maxAge > 0 ? (long) (maxAge / maxAgeOffset * min * iteration) % maxAge : 0l;
                data.getPool().add(obj, offset);
            }
        }
    }
}
