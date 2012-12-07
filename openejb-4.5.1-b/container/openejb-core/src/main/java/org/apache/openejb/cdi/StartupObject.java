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


package org.apache.openejb.cdi;

import java.util.List;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.core.WebContext;
import org.apache.webbeans.config.WebBeansContext;

/**
 * @version $Rev:$ $Date:$
 */
public class StartupObject {
    private final AppInfo appInfo;
    private final AppContext appContext;
    private final List<BeanContext> beanContexts;
    private final WebContext webContext;

    public StartupObject(AppContext appContext, AppInfo appInfo, List<BeanContext> beanContexts) {
        this(appContext, appInfo, beanContexts, null);
    }

    public StartupObject(AppContext appContext, AppInfo appInfo, List<BeanContext> beanContexts, WebContext webContext) {
        assert appContext != null;
        assert appInfo != null;
        assert beanContexts != null;

        this.appContext = appContext;
        this.appInfo = appInfo;
        this.beanContexts = beanContexts;
        this.webContext = webContext;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public List<BeanContext> getBeanContexts() {
        return beanContexts;
    }

    public WebContext getWebContext() {
        return webContext;
    }

    public WebBeansContext getWebBeansContext() {
        if (isFromWebApp()) {
            return webContext.getWebbeansContext();
        }
        return appContext.getWebBeansContext();
    }

    public ClassLoader getClassLoader() {
        if (isFromWebApp()) {
            return webContext.getClassLoader();
        }
        return appContext.getClassLoader();
    }

    public boolean isFromWebApp() {
        return webContext != null;
    }
}
