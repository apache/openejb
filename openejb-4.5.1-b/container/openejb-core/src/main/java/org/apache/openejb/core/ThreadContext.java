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
package org.apache.openejb.core;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class ThreadContext {
    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final ThreadLocal<ThreadContext> threadStorage = new ThreadLocal<ThreadContext>();
    private static final List<ThreadContextListener> listeners = new CopyOnWriteArrayList<ThreadContextListener>();
    private static final ThreadLocal<AtomicBoolean> asynchronousCancelled = new ThreadLocal<AtomicBoolean>();

    public static ThreadContext getThreadContext() {
        ThreadContext threadContext = threadStorage.get();
        return threadContext;
    }

    public static ThreadContext enter(ThreadContext newContext) {
        if (newContext == null) {
            throw new NullPointerException("newContext is null");
        }

        // set the thread context class loader
        newContext.oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(newContext.beanContext.getClassLoader());

        // update thread local
        ThreadContext oldContext = threadStorage.get();
        threadStorage.set(newContext);

        // notify listeners
        for (ThreadContextListener listener : listeners) {
            try {
                listener.contextEntered(oldContext, newContext);
            } catch (Throwable e) {
                log.warning("ThreadContextListener threw an exception", e);
            }
        }

        // return old context so it can be used for exit call below
        return oldContext;
    }

    public static void exit(ThreadContext oldContext) {
        ThreadContext exitingContext = threadStorage.get();
        if (exitingContext == null) {
            throw new IllegalStateException("No existing context");
        }

        // set the thread context class loader back
        Thread.currentThread().setContextClassLoader(exitingContext.oldClassLoader);
        exitingContext.oldClassLoader = null;

        // update thread local
        threadStorage.set(oldContext);

        // notify listeners
        for (ThreadContextListener listener : listeners) {
            try {
                listener.contextExited(exitingContext, oldContext);
            } catch (Throwable e) {
                log.debug("ThreadContextListener threw an exception", e);
            }
        }
    }

    public static void initAsynchronousCancelled(AtomicBoolean initializeValue) {
        asynchronousCancelled.set(initializeValue);
    }

    public static boolean isAsynchronousCancelled() {
        return asynchronousCancelled.get().get();
    }

    public static void removeAsynchronousCancelled() {
        asynchronousCancelled.remove();
    }

    public static void addThreadContextListener(ThreadContextListener listener) {
        listeners.add(listener);
    }

    public static void removeThreadContextListener(ThreadContextListener listener) {
        listeners.remove(listener);
    }

    private final BeanContext beanContext;
    private final Object primaryKey;
    private final HashMap<Class, Object> data = new HashMap<Class, Object>();
    private ClassLoader oldClassLoader;
    private Operation currentOperation;
    private Class invokedInterface;
    private TransactionPolicy transactionPolicy;

    /**
     * A boolean which keeps track of whether to discard the bean instance after the method invocation.
     * The boolean would be set to true in case of exceptions which mandate bean discard.
     */
    private boolean discardInstance;

    public ThreadContext(BeanContext beanContext, Object primaryKey) {
        this(beanContext, primaryKey, null);
    }

    public ThreadContext(BeanContext beanContext, Object primaryKey, Operation operation) {
        if (beanContext == null) {
            throw new NullPointerException("deploymentInfo is null");
        }
        this.beanContext = beanContext;
        this.primaryKey = primaryKey;
        this.currentOperation = operation;
    }

    public ThreadContext(ThreadContext that) {
        this.beanContext = that.beanContext;
        this.primaryKey = that.primaryKey;
        this.data.putAll(that.data);
        this.oldClassLoader = that.oldClassLoader;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(Operation operation) {
        currentOperation = operation;
    }

    public Class getInvokedInterface() {
        return invokedInterface;
    }

    public void setInvokedInterface(Class invokedInterface) {
        this.invokedInterface = invokedInterface;
    }

    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public void setTransactionPolicy(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }

    public BaseContext.State[] getCurrentAllowedStates() {
        return null;
    }

    public BaseContext.State[] setCurrentAllowedStates(BaseContext.State[] newAllowedStates) {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T get(Class<T> type) {
        return (T)data.get(type);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T set(Class<T> type, T value) {
        return (T) data.put(type, value);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T remove(Class<T> type) {
        return (T) data.remove(type);
    }

    public boolean isDiscardInstance() {
        return discardInstance;
    }

    public void setDiscardInstance(boolean discardInstance) {
        this.discardInstance = discardInstance;
    }

    @Override
    public String toString() {
        return "ThreadContext{" +
                "beanContext=" + beanContext.getId() +
                ", primaryKey=" + primaryKey +
                ", data=" + data.size() +
                ", oldClassLoader=" + oldClassLoader +
                ", currentOperation=" + currentOperation +
                ", invokedInterface=" + invokedInterface +
                ", transactionPolicy=" + transactionPolicy +
                ", discardInstance=" + discardInstance +
                '}';
    }
}
