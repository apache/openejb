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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
package org.openejb.corba.compiler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.rmi.RemoteException;

import org.omg.CORBA.portable.IDLEntity;

public class PortableStubCompiler {
    private static String getClassName(Class type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("type is an array: " + type);
        }

        // get the classname
        String typeName = type.getName();
        int endIndex = typeName.lastIndexOf('.');
        if (endIndex < 0) {
            return typeName;
        }
        return typeName.substring(endIndex + 1);
    }

    private static String getPackageName(String interfaceName) {
        int endIndex = interfaceName.lastIndexOf('.');
        if (endIndex < 0) {
            return "";
        }
        return interfaceName.substring(0, endIndex);
    }

    public static Method[] getAllMethods(Class intfClass) {
        LinkedList methods = new LinkedList();
        for (Iterator iterator = getAllInterfaces(intfClass).iterator(); iterator.hasNext();) {
            Class intf = (Class) iterator.next();
            methods.addAll(Arrays.asList(intf.getDeclaredMethods()));
        }

        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    public static Set getAllInterfaces(Class intfClass) {
        Set allInterfaces = new LinkedHashSet();

        LinkedList stack = new LinkedList();
        stack.addFirst(intfClass);

        while (!stack.isEmpty()) {
            Class intf = (Class) stack.removeFirst();
            allInterfaces.add(intf);
            stack.addAll(0, Arrays.asList(intf.getInterfaces()));
        }

        return allInterfaces;
    }

    public static IiopOperation[] createIiopOperations(Class intfClass) {
        Method[] methods = getAllMethods(intfClass);

        // find every valid getter
        HashMap getterByMethod = new HashMap(methods.length);
        HashMap getterByName = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();

            // no arguments allowed
            if (method.getParameterTypes().length != 0) {
                continue;
            }

            // must start with get or is
            String verb;
            if (methodName.startsWith("get") && methodName.length() > 3 && method.getReturnType() != void.class) {
                verb = "get";
            } else if (methodName.startsWith("is") && methodName.length() > 2 && method.getReturnType() == boolean.class) {
                verb = "is";
            } else {
                continue;
            }

            // must only throw Remote or Runtime Exceptions
            boolean exceptionsValid = true;
            Class[] exceptionTypes = method.getExceptionTypes();
            for (int j = 0; j < exceptionTypes.length; j++) {
                Class exceptionType = exceptionTypes[j];
                if (!RemoteException.class.isAssignableFrom(exceptionType) &&
                        !RuntimeException.class.isAssignableFrom(exceptionType) &&
                        !Error.class.isAssignableFrom(exceptionType)) {
                    exceptionsValid = false;
                    break;
                }
            }
            if (!exceptionsValid) {
                continue;
            }

            String propertyName;
            if (methodName.length() > verb.length() + 1 && Character.isUpperCase(methodName.charAt(verb.length() + 1))) {
                propertyName = methodName.substring(verb.length());
            } else {
                propertyName = Character.toLowerCase(methodName.charAt(verb.length())) + methodName.substring(verb.length() + 1);
            }
            getterByMethod.put(method, propertyName);
            getterByName.put(propertyName, method);
        }

        HashMap setterByMethod = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();

            // must have exactally one arg
            if (method.getParameterTypes().length != 1) {
                continue;
            }

            // must return non void
            if (method.getReturnType() != void.class) {
                continue;
            }

            // must start with set
            if (!methodName.startsWith("set") || methodName.length() <= 3) {
                continue;
            }

            // must only throw Remote or Runtime Exceptions
            boolean exceptionsValid = true;
            Class[] exceptionTypes = method.getExceptionTypes();
            for (int j = 0; j < exceptionTypes.length; j++) {
                Class exceptionType = exceptionTypes[j];
                if (!RemoteException.class.isAssignableFrom(exceptionType) &&
                        !RuntimeException.class.isAssignableFrom(exceptionType) &&
                        !Error.class.isAssignableFrom(exceptionType)) {
                    exceptionsValid = false;
                    break;
                }
            }
            if (!exceptionsValid) {
                continue;
            }

            String propertyName;
            if (methodName.length() > 4 && Character.isUpperCase(methodName.charAt(4))) {
                propertyName = methodName.substring(3);
            } else {
                propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }

            // must have a matching getter
            Method getter = (Method) getterByName.get(propertyName);
            if (getter == null) {
                continue;
            }

            // setter property must match gettter return value
            if (!method.getParameterTypes()[0].equals(getter.getReturnType())) {
                continue;
            }
            setterByMethod.put(method, propertyName);
        }

        // index the methods by name... used to determine which methods are overloaded
        HashMap overloadedMethods = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (getterByMethod.containsKey(method) || setterByMethod.containsKey(method)) {
                continue;
            }
            String methodName = method.getName();
            List methodList = (List) overloadedMethods.get(methodName);
            if (methodList == null) {
                methodList = new LinkedList();
                overloadedMethods.put(methodName, methodList);
            }
            methodList.add(method);
        }

        // index the methods by lower case name... used to determine which methods differ only by case
        HashMap caseCollisionMethods = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (getterByMethod.containsKey(method) || setterByMethod.containsKey(method)) {
                continue;
            }
            String lowerCaseMethodName = method.getName().toLowerCase();
            Set methodSet = (Set) caseCollisionMethods.get(lowerCaseMethodName);
            if (methodSet == null) {
                methodSet = new HashSet();
                caseCollisionMethods.put(lowerCaseMethodName, methodSet);
            }
            methodSet.add(method.getName());
        }

        String className = getClassName(intfClass);
        List overloadList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            String iiopName = (String) getterByMethod.get(method);
            if (iiopName != null) {
                // if we have a leading underscore prepend with J
                if (iiopName.charAt(0) == '_') {
                    iiopName = "J_get_" + iiopName.substring(1);
                } else {
                    iiopName = "_get_" + iiopName;
                }
            } else {
                iiopName = (String) setterByMethod.get(method);
                if (iiopName != null) {
                    // if we have a leading underscore prepend with J
                    if (iiopName.charAt(0) == '_') {
                        iiopName = "J_set_" + iiopName.substring(1);
                    } else {
                        iiopName = "_set_" + iiopName;
                    }
                } else {
                    iiopName = method.getName();

                    // if we have a leading underscore prepend with J
                    if (iiopName.charAt(0) == '_') {
                        iiopName = "J" + iiopName;
                    }
                }
            }

            // if this name only differs by case add the case index to the end
            Set caseCollisions = (Set) caseCollisionMethods.get(method.getName().toLowerCase());
            if (caseCollisions != null && caseCollisions.size() > 1) {
                iiopName += upperCaseIndexString(iiopName);
            }

            // if this is an overloaded method append the parameter string
            List overloads = (List) overloadedMethods.get(method.getName());
            if (overloads != null && overloads.size() > 1) {
                iiopName += buildOverloadParameterString(method.getParameterTypes());
            }

            // if we have a leading underscore prepend with J
            iiopName = replace(iiopName, '$', "U0024");

            // if we have matched a keyword prepend with an underscore
            if (keywords.contains(iiopName.toLowerCase())) {
                iiopName = "_" + iiopName;
            }

            // if the name is the same as the class name, append an underscore
            if (iiopName.equalsIgnoreCase(className)) {
                iiopName += "_";
            }

            overloadList.add(new IiopOperation(iiopName, method));
        }

        return (IiopOperation[]) overloadList.toArray(new IiopOperation[overloadList.size()]);
    }

    private static String upperCaseIndexString(String iiopName) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < iiopName.length(); i++) {
            char c = iiopName.charAt(i);
            if (Character.isUpperCase(c)) {
                stringBuffer.append('_').append(i);
            }
        }
        return stringBuffer.toString();
    }

    public static String replace(String source, char oldChar, String newString) {
        StringBuffer stringBuffer = new StringBuffer(source.length());
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == oldChar) {
                stringBuffer.append(newString);
            } else {
                stringBuffer.append(c);
            }
        }
        return stringBuffer.toString();
    }

    public static String buildOverloadParameterString(Class[] parameterTypes) {
        String name = "";
        if (parameterTypes.length ==0) {
            name += "__";
        } else {
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                name += buildOverloadParameterString(parameterType);
            }
        }
        return name.replace('.', '_');
    }

    public static String buildOverloadParameterString(Class parameterType) {
        String name = "_";

        int arrayDimensions = 0;
        while (parameterType.isArray()) {
            arrayDimensions++;
            parameterType = parameterType.getComponentType();
        }

        // arrays start with org_omg_boxedRMI_
        if (arrayDimensions > 0) {
            name += "_org_omg_boxedRMI";
        }

        // IDLEntity types must be prefixed with org_omg_boxedIDL_
        if (IDLEntity.class.isAssignableFrom(parameterType)) {
            name += "_org_omg_boxedIDL";
        }

        // add package... some types have special mappings in corba
        String packageName = (String) specialTypePackages.get(parameterType.getName());
        if (packageName == null) {
            packageName = getPackageName(parameterType.getName());
        }
        if (packageName.length() > 0) {
            name += "_" + packageName;
        }

        // arrays now contain a dimension indicator
        if (arrayDimensions > 0) {
            name += "_" + "seq" + arrayDimensions;
        }

        // add the class name
        String className = (String) specialTypeNames.get(parameterType.getName());
        if (className == null) {
            className = buildClassName(parameterType);
        }
        name += "_" + className;

        return name;
    }

    private static String buildClassName(Class type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("type is an array: " + type);
        }

        // get the classname
        String typeName = type.getName();
        int endIndex = typeName.lastIndexOf('.');
        if (endIndex < 0) {
            return typeName;
        }
        StringBuffer className = new StringBuffer(typeName.substring(endIndex + 1));

        // for innerclasses replace the $ separator with two underscores
        // we can't just blindly replace all $ characters since class names can contain the $ character
        if (type.getDeclaringClass() != null) {
            String declaringClassName = getClassName(type.getDeclaringClass());
            assert className.toString().startsWith(declaringClassName + "$");
            className.replace(declaringClassName.length(), declaringClassName.length() + 1, "__");
        }

        // if we have a leading underscore prepend with J
        if (className.charAt(0) == '_') {
            className.insert(0, "J");
        }
        return className.toString();
    }


    private static final Map specialTypeNames;
    private static final Map specialTypePackages;
    private static final Set keywords;

    static {
        specialTypeNames = new HashMap();
        specialTypeNames.put("boolean", "boolean");
        specialTypeNames.put("char", "wchar");
        specialTypeNames.put("byte", "octet");
        specialTypeNames.put("short", "short");
        specialTypeNames.put("int", "long");
        specialTypeNames.put("long", "long_long");
        specialTypeNames.put("float", "float");
        specialTypeNames.put("double", "double");
        specialTypeNames.put("java.lang.Class", "ClassDesc");
        specialTypeNames.put("java.lang.String", "WStringValue");
        specialTypeNames.put("org.omg.CORBA.Object", "Object");

        specialTypePackages = new HashMap();
        specialTypePackages.put("boolean", "");
        specialTypePackages.put("char", "");
        specialTypePackages.put("byte", "");
        specialTypePackages.put("short", "");
        specialTypePackages.put("int", "");
        specialTypePackages.put("long", "");
        specialTypePackages.put("float", "");
        specialTypePackages.put("double", "");
        specialTypePackages.put("java.lang.Class", "javax.rmi.CORBA");
        specialTypePackages.put("java.lang.String", "CORBA");
        specialTypePackages.put("org.omg.CORBA.Object", "");

        keywords = new HashSet();
        keywords.add("abstract");
        keywords.add("any");
        keywords.add("attribute");
        keywords.add("boolean");
        keywords.add("case");
        keywords.add("char");
        keywords.add("const");
        keywords.add("context");
        keywords.add("custom");
        keywords.add("default");
        keywords.add("double");
        keywords.add("enum");
        keywords.add("exception");
        keywords.add("factory");
        keywords.add("false");
        keywords.add("fixed");
        keywords.add("float");
        keywords.add("in");
        keywords.add("inout");
        keywords.add("interface");
        keywords.add("long");
        keywords.add("module");
        keywords.add("native");
        keywords.add("object");
        keywords.add("octet");
        keywords.add("oneway");
        keywords.add("out");
        keywords.add("private");
        keywords.add("public");
        keywords.add("raises");
        keywords.add("readonly");
        keywords.add("sequence");
        keywords.add("short");
        keywords.add("string");
        keywords.add("struct");
        keywords.add("supports");
        keywords.add("switch");
        keywords.add("true");
        keywords.add("truncatable");
        keywords.add("typedef");
        keywords.add("union");
        keywords.add("unsigned");
        keywords.add("valuebase");
        keywords.add("valuetype");
        keywords.add("void");
        keywords.add("wchar");
        keywords.add("wstring");
    }
}
