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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.xfire;

import java.lang.reflect.Method;
import java.util.*;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.codehaus.xfire.java.DefaultJavaService;
import org.codehaus.xfire.java.Parameter;
import org.codehaus.xfire.java.mapping.DefaultTypeMappingRegistry;
import org.codehaus.xfire.java.mapping.TypeMapping;
import org.codehaus.xfire.java.type.Type;

/**
 * TODO: I would be great to be able to configure XFire during deployment and write it to the configuration
 */
public class LightWeightServiceConfigurator extends org.apache.geronimo.webservices.WSDLVisitor {
    private DefaultJavaService service;
    private TypeMapping typeMappings;
    private Map parameterMap;
    private Map typeMap;

    public LightWeightServiceConfigurator(Definition definition, DefaultJavaService service) {
        super(definition);

        this.service = service;
        // Setup Type Mapping
        service.setAutoTyped(true);
        DefaultTypeMappingRegistry registry = new DefaultTypeMappingRegistry();
        typeMappings = registry.createDefaultMappings();
        registry.registerDefault(typeMappings);
        service.setTypeMappingRegistry(registry);
        service.initializeTypeMapping();

        parameterMap = new HashMap();
        typeMap = new HashMap();
    }

    public void configure() {
        this.walkTree();
    }

    protected void visit(Part part) {
        Type type = typeMappings.getType(part.getTypeName());
        Parameter parameter = new Parameter(new QName(part.getName()), type);
        parameterMap.put(part, parameter);
        typeMap.put(part, type.getTypeClass());
    }

    protected void visit(Operation wsdlOperation) {

        Method method = getMethod(wsdlOperation);

        org.codehaus.xfire.java.Operation xfireOperation = new org.codehaus.xfire.java.Operation(method);

        // setup input params
        Collection inParts = wsdlOperation.getInput().getMessage().getParts().values();
        for (Iterator iterator = inParts.iterator(); iterator.hasNext();) {
            Part part = (Part) iterator.next();
            Parameter inParameter = (Parameter) parameterMap.get(part);
            xfireOperation.addInParameter(inParameter);
        }

        // setup output param
        Iterator outParts = wsdlOperation.getOutput().getMessage().getParts().values().iterator();
        if (outParts.hasNext()) {
            Part part = (Part) outParts.next();
            Parameter outParameter = (Parameter) parameterMap.get(part);
            xfireOperation.addOutParameter(outParameter);
        }

        service.addOperation(xfireOperation);
    }

    private Method getMethod(Operation wsdlOperation) {
        Input input = wsdlOperation.getInput();
        List parts = input.getMessage().getOrderedParts(wsdlOperation.getParameterOrdering());
        Class[] types = new Class[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            types[i] = (Class) typeMap.get((Part) parts.get(i));
        }

        try {
            return service.getServiceClass().getMethod(wsdlOperation.getName(), types);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no method matching the operation named " + wsdlOperation.getName());
        }
    }
}
