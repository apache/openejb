/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.tomcat.catalina.owb;

import org.apache.AnnotationProcessor;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.naming.ContextAccessController;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.LinkedList;

/**
 * Observing events for OpenWebBeans related configurations.
 *
 * @version $Rev$ $Date$
 */
public class OWBLifecycleListener implements LifecycleListener, ContainerListener {

    public static final String IGNORE_OWB_LIBRARY = OWBLifecycleListener.class.getName() + ".IGNORE_OWB_LIBRARY";

    /**
     * Creates a new instance.
     */
    public OWBLifecycleListener() {

    }

    /**
     * {@inheritDoc}
     */
    public void lifecycleEvent(LifecycleEvent event) {
        try {
            //Look if starting is Context
            if (event.getSource() instanceof StandardContext) {
                StandardContext context = (StandardContext) event.getSource();

                if (event.getType().equals(Lifecycle.START_EVENT)) {
                    //Look for beans.xml file
                    ServletContext scontext = context.getServletContext();
                    URL url = scontext.getResource("/WEB-INF/beans.xml");
                    if (url != null) {
                        context.getServletContext().setAttribute(IGNORE_OWB_LIBRARY, "");
                        String[] oldListeners = context.findApplicationListeners();
                        LinkedList<String> listeners = new LinkedList<String>();

                        //Add first listener
                        listeners.addFirst("org.apache.webbeans.servlet.WebBeansConfigurationListener");
                        for (String listener : oldListeners) {
                            listeners.add(listener);
                            context.removeApplicationListener(listener);
                        }

                        for (String listener : listeners) {
                            context.addApplicationListener(listener);
                        }

                        context.addApplicationListener(TomcatSecurityListener.class.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void containerEvent(ContainerEvent event) {
        StandardContext context = null;
        try {
            if (event.getSource() instanceof StandardContext) {
                context = (StandardContext) event.getSource();

                if (event.getType().equals("beforeContextInitialized")) {
                    ClassLoader loader = context.getLoader().getClassLoader();
                    Object listener = event.getData();

                    if (listener.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                        ContextAccessController.setWritable(context.getNamingContextListener().getName(), context);
                        return;
                    } else {
                        URL url = context.getServletContext().getResource("/WEB-INF/beans.xml");
                        if (url != null) {
                            TomcatUtil.inject(listener, loader);
                        }
                    }

                } else if (event.getType().equals("afterContextInitialized")) {
                    ClassLoader loader = context.getLoader().getClassLoader();
                    Object listener = event.getData();

                    if (listener.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                        AnnotationProcessor processor = context.getAnnotationProcessor();
                        AnnotationProcessor custom = new TomcatAnnotProcessor(context.getLoader().getClassLoader(), processor);
                        context.setAnnotationProcessor(custom);

                        context.getServletContext().setAttribute(AnnotationProcessor.class.getName(), custom);

                        ContextAccessController.setReadOnly(context.getNamingContextListener().getName());

                        URL url = context.getServletContext().getResource("/WEB-INF/beans.xml");
                        if (url != null) {
                            Object[] listeners = context.getApplicationEventListeners();
                            for (Object instance : listeners) {
                                if (!instance.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                                    TomcatUtil.inject(instance, loader);
                                }
                            }
                        }
                    }
                } else if (event.getType().equals("beforeContextDestroyed")) {
                    Object listener = event.getData();
                    if (listener.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                        ContextAccessController.setWritable(context.getNamingContextListener().getName(), context);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
