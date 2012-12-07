/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.core.WebContext;
import org.apache.webbeans.el.WebBeansELResolver;
import org.apache.webbeans.el.WrappedExpressionFactory;
import org.apache.webbeans.spi.adaptor.ELAdaptor;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;

/**
 * @version $Rev$ $Date$
 */
public class CustomELAdapter implements ELAdaptor {

    private final AppContext appContext;
    private final WebContext webContext;

    public CustomELAdapter(AppContext appContext) {
        this(appContext, null);
    }

    public CustomELAdapter(AppContext appContext, WebContext webContext) {
        this.appContext = appContext;
        this.webContext = webContext;
    }

    @Override
    public ELResolver getOwbELResolver() {
        return new WebBeansELResolver();
    }

    @Override
    public ExpressionFactory getOwbWrappedExpressionFactory(ExpressionFactory expressionFactory) {
        if (!appContext.isCdiEnabled()) return expressionFactory;
        return new WrappedExpressionFactory(expressionFactory);
    }
}
