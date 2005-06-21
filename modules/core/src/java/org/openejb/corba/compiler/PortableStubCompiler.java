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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import org.apache.geronimo.interop.generator.GenException;
import org.apache.geronimo.interop.generator.GenOptions;
import org.apache.geronimo.interop.generator.JCatchStatement;
import org.apache.geronimo.interop.generator.JClass;
import org.apache.geronimo.interop.generator.JCodeStatement;
import org.apache.geronimo.interop.generator.JExpression;
import org.apache.geronimo.interop.generator.JField;
import org.apache.geronimo.interop.generator.JIfStatement;
import org.apache.geronimo.interop.generator.JLocalVariable;
import org.apache.geronimo.interop.generator.JMethod;
import org.apache.geronimo.interop.generator.JPackage;
import org.apache.geronimo.interop.generator.JParameter;
import org.apache.geronimo.interop.generator.JReturnType;
import org.apache.geronimo.interop.generator.JTryCatchFinallyStatement;
import org.apache.geronimo.interop.generator.JTryStatement;
import org.apache.geronimo.interop.generator.JType;
import org.apache.geronimo.interop.generator.JVariable;
import org.apache.geronimo.interop.generator.JavaGenerator;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.portable.RemarshalException;

public class PortableStubCompiler {
    private HashMap packages = new HashMap();
    private final GenOptions genOptions;
    private final ClassLoader classLoader;

    public PortableStubCompiler(GenOptions genOptions, ClassLoader classLoader) {
        this.genOptions = genOptions;
        this.classLoader = classLoader;
    }

    public void generate() throws GenException {
        JavaGenerator javaGenerator = new JavaGenerator(genOptions);

        List interfaces = genOptions.getInterfaces();
        for (Iterator iterator = interfaces.iterator(); iterator.hasNext();) {
            String interfaceName = (String) iterator.next();

            // load the interface class
            Class interfaceClass = null;
            try {
                interfaceClass = classLoader.loadClass(interfaceName);
            } catch (Exception ex) {
                throw new GenException("Generate Stubs Failed:", ex);
            }

            // get the package object
            String packageName = getPackageName(interfaceName);
            if (packageName.length() > 0) {
                packageName = "org.omg.stub." + packageName;
            } else {
                packageName = "org.omg.stub";
            }
            JPackage jpackage = (JPackage) packages.get(packageName);
            if (jpackage == null) {
                jpackage = new JPackage(packageName);
                packages.put(packageName, jpackage);
            }

            // build the basic class object
            String className = "_" + getClassName(interfaceClass) + "_Stub";
            JClass jclass = jpackage.newClass(className);
            jclass.addImport("javax.rmi.CORBA", "Stub");
            jclass.setExtends("Stub");
            jclass.addImplements(interfaceClass.getName());
            jclass.addImplements("org.openejb.corba.ClientContextHolder");

            addClientContextMethods(jclass);
            addIdsMethod(jclass, interfaceClass);

            IiopOperation[] iiopOperations = createIiopOperations(interfaceClass);
            for (int i = 0; iiopOperations != null && i < iiopOperations.length; i++) {
                addMethod(iiopOperations[i], jclass);
            }
        }

        for (Iterator iterator = packages.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String packageName = (String) entry.getKey();
            JPackage jpackage = (JPackage) entry.getValue();

            System.out.println("Generating Package: " + packageName);
            javaGenerator.generate(jpackage);
        }
    }

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

        // index the methods by name... used to determine which methods are overloaded
        HashMap overloadedMethods = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            List methodList = (List) overloadedMethods.get(methodName);
            if (methodList == null) {
                methodList = new LinkedList();
                overloadedMethods.put(methodName, methodList);
            }
            methodList.add(methods[i]);
        }

        // index the methods by lower case name... used to determine which methods differ only by case
        HashMap caseCollisionMethods = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            String lowerCaseMethodName = methods[i].getName().toLowerCase();
            Set methodSet = (Set) caseCollisionMethods.get(lowerCaseMethodName);
            if (methodSet == null) {
                methodSet = new HashSet();
                caseCollisionMethods.put(lowerCaseMethodName, methodSet);
            }
            methodSet.add(methods[i].getName());
        }

        String className = getClassName(intfClass);
        List overloadList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            String iiopName = method.getName();

            if (((Set) caseCollisionMethods.get(method.getName().toLowerCase())).size() > 1) {
                iiopName += upperCaseIndexString(iiopName);
            }

            // if we have a leading underscore prepend with J
            if (iiopName.charAt(0) == '_') {
                iiopName = "J" + iiopName;
            }

            // if this is an overloaded method append the parameter string
            if (((List) overloadedMethods.get(method.getName())).size() > 1) {
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

    private void addMethod(JClass jclass, String iiopMethodName, JReturnType jreturnType, String name, JParameter[] jparameters, Class[] exceptions) {
        //
        // Method Template:
        //
        // if (!Util.isLocal(this)) {
        //     org.openejb.corba.ClientContext saved = org.openejb.corba.ClientContextManager.getClientContext();
        //     try {
        //         org.omg.CORBA_2_3.portable.InputStream in = null;
        //         try {
        //             org.omg.CORBA_2_3.portable.OutputStream out =
        //                 (org.omg.CORBA_2_3.portable.OutputStream)
        //                 _request("passAndReturnCheese__org_apache_geronimo_interop_rmi_iiop_compiler_Cheese", true);
        //             out.write_value((Serializable)arg0,Cheese.class);
        //             in = (org.omg.CORBA_2_3.portable.InputStream)_invoke(out);
        //
        //             Object result = javax.rmi.PortableRemoteObject.narrow( _input.read_Object(), Cheese.class );
        //             if (result instanceof org.openejb.corba.ClientContextHolder) {
        //                 ((org.openejb.corba.ClientContextHolder)result).setClientContext(_context);
        //             }
        //             return (Cheese) result;
        //         } catch (ApplicationException ex) {
        //             in = (org.omg.CORBA_2_3.portable.InputStream) ex.getInputStream();
        //             String id = in.read_string();
        //             throw new UnexpectedException(id);
        //         } catch (RemarshalException ex) {
        //             return passAndReturnCheese(arg0);
        //         } finally {
        //             _releaseReply(in);
        //         }
        //     } catch (SystemException ex) {
        //         throw Util.mapSystemException(ex);
        //     }
        // } else {
        //     ServantObject so = _servant_preinvoke("passAndReturnCheese__org_apache_geronimo_interop_rmi_iiop_compiler_Cheese",Foo.class);
        //     if (so == null) {
        //         return passAndReturnCheese(arg0);
        //     }
        //     try {
        //         Cheese arg0Copy = (Cheese) Util.copyObject(arg0,_orb());
        //         Cheese result = ((Foo)so.servant).passAndReturnCheese(arg0Copy);
        //         return (Cheese)Util.copyObject(result,_orb());
        //     } catch (Throwable ex) {
        //         Throwable exCopy = (Throwable)Util.copyObject(ex,_orb());
        //         throw Util.wrapException(exCopy);
        //     } finally {
        //         _servant_postinvoke(so);
        //     }
        // }

        JMethod jmethod = jclass.newMethod(jreturnType, name, jparameters, exceptions);

        JVariable saved = jmethod.newLocalVariable(org.openejb.corba.ClientContext.class, "saved", new JExpression(new JCodeStatement(
                "org.openejb.corba.ClientContextManager.getClientContext()")));


        JTryCatchFinallyStatement outerTryCatchFinally = new JTryCatchFinallyStatement();
        jmethod.addStatement(outerTryCatchFinally);
        JTryStatement outerTry = outerTryCatchFinally.getTryStatement();

        JLocalVariable inVar = outerTry.newLocalVariable(org.omg.CORBA_2_3.portable.InputStream.class,
                "in",
                new JExpression(new JCodeStatement("null")));

        outerTry.addStatement(new JCodeStatement("org.openejb.corba.ClientContextManager.setClientContext(_context);"));

        JTryCatchFinallyStatement innterTryCatchFinally = new JTryCatchFinallyStatement();
        outerTry.addStatement(innterTryCatchFinally);
        JTryStatement innerTry = innterTryCatchFinally.getTryStatement();

        JLocalVariable outVar = innerTry.newLocalVariable(org.omg.CORBA_2_3.portable.OutputStream.class,
                "out",
                new JExpression(new JCodeStatement("(" + org.omg.CORBA_2_3.portable.OutputStream.class.getName() + ") _request(\"" + iiopMethodName + "\", true)")));

        // Write the variables
        for (int i = 0; i < jparameters.length; i++) {
            JParameter jparameter = jparameters[i];

            String writeMethod = getWriteMethod(jparameter);
            String writeCall;
            if (writeMethod != null) {
                writeCall = writeMethod + "( " + jparameter.getName() + " )";
            } else {
                String cast = "";
                if (!Serializable.class.isAssignableFrom(jparameter.getType())) {
                    cast = "(java.io.Serializable)";
                }
                writeCall = "write_value(" + cast + jparameter.getName() + ", " + jparameter.getTypeDecl() + ".class)";
            }

            innerTry.addStatement(new JCodeStatement(outVar.getName() + "." + writeCall + ";"));
        }

        // invoke the method
        String invoke = "_invoke(" + outVar.getName() + ");";
        if (jreturnType.getType() != Void.TYPE) {
            invoke = inVar.getName() + " = (" + inVar.getTypeDecl() + ")" + invoke;
        }
        innerTry.addStatement(new JCodeStatement(invoke));

        // read the return value
        if (jreturnType.getType() != Void.TYPE) {
            String readMethod = getReadMethod(jreturnType);
            if (readMethod != null) {
                String readCall = inVar.getName() + "." + readMethod + "()";
                innerTry.addStatement(new JCodeStatement("return " + readCall + ";"));
            } else {
                // Object result = javax.rmi.PortableRemoteObject.narrow( _input.read_Object(), Cheese.class );
                // if (result instanceof org.openejb.corba.ClientContextHolder) {
                //     ((org.openejb.corba.ClientContextHolder)result).setClientContext(_context);
                // }
                // return (Cheese) result;
//                String readCall = "(" + jreturnType.getTypeDecl() + ")" + inVar.getName() + ".read_value( " + jreturnType.getTypeDecl() + ".class)";

                // can not use a local variable here since the java generator will reorder the fields
                String resultName = "result";
                innerTry.addStatement(new JCodeStatement("Object " + resultName + " = " +
                        "javax.rmi.PortableRemoteObject.narrow(" + inVar.getName() + ".read_Object(), " + jreturnType.getTypeDecl() + ".class);"
                ));

                JIfStatement jif = new JIfStatement(new JExpression(new JCodeStatement(resultName + " instanceof org.openejb.corba.ClientContextHolder")));
                jif.addStatement(new JCodeStatement("((org.openejb.corba.ClientContextHolder)" + resultName + ").setClientContext(_context);"));
                innerTry.addStatement(jif);

                innerTry.addStatement(new JCodeStatement("return (" + jreturnType.getTypeDecl() + ") " + resultName + ";"));
            }
        }

        JVariable exVar = new JVariable(ApplicationException.class, "ex");
        JCatchStatement jcatchStatement = innterTryCatchFinally.newCatch(exVar);

        jcatchStatement.addStatement(new JCodeStatement(inVar.getName() + " = (" + inVar.getTypeDecl() + ") " + exVar.getName() + ".getInputStream();"));
        JLocalVariable idVar = jcatchStatement.newLocalVariable(String.class,
                "id",
                new JExpression(new JCodeStatement(inVar.getName() + ".read_string()")));
//        if (id.equals("IDL:org/apache/geronimo/interop/rmi/iiop/compiler/other/BlahEx:1.0")) {
//            throw (BlahException) in.read_value(BlahException.class);
//        }
//        if (id.equals("IDL:org/apache/geronimo/interop/rmi/iiop/compiler/BooEx:1.0")) {
//            throw (BooException) in.read_value(BooException.class);
//        }
        for (int i = 0; i < exceptions.length; i++) {
            Class exception = exceptions[i];
            if (RemoteException.class.isAssignableFrom(exception) ||
                    RuntimeException.class.isAssignableFrom(exception) ) {
                continue;
            }
            String exceptionName = exception.getName().replace('.', '/');
            if (exceptionName.endsWith("Exception")) {
                exceptionName = exceptionName.substring(0, exceptionName.length() - "Exception".length());
            }
            exceptionName += "Ex";
            JIfStatement jif = new JIfStatement(new JExpression(new JCodeStatement(idVar.getName() + ".equals(\"IDL:" + exceptionName + ":1.0\")")));
            jif.addStatement(new JCodeStatement("throw (" + exception.getName() + ") in.read_value(" + exception.getName() + ".class);"));
            jcatchStatement.addStatement(jif);

        }
        jcatchStatement.addStatement(new JCodeStatement("throw new java.rmi.UnexpectedException(" + idVar.getName() + ");"));

        //         } catch (RemarshalException ex) {
        //             return passAndReturnCheese(arg0);
        exVar = new JVariable(RemarshalException.class, "ex");
        jcatchStatement = innterTryCatchFinally.newCatch(exVar);

        String remarshal = name + "(";
        for (int i = 0; i < jparameters.length; i++) {
            JParameter jparameter = jparameters[i];
            if (i > 0) {
                remarshal += ", ";
            }
            remarshal += jparameter.getName();
        }
        remarshal += ");";
        if (jreturnType.getType() != Void.TYPE) {
            remarshal = "return " + remarshal;
        }
        jcatchStatement.addStatement(new JCodeStatement(remarshal));

        //         } finally {
        //             _releaseReply(in);
        innterTryCatchFinally.addFinallyStatement(new JCodeStatement("org.openejb.corba.ClientContextManager.setClientContext(" + saved.getName() + ");"));
        innterTryCatchFinally.addFinallyStatement(new JCodeStatement("_releaseReply(" + inVar.getName() + ");"));

        //     } catch (SystemException ex) {
        //         throw Util.mapSystemException(ex);
        exVar = new JVariable(SystemException.class, "ex");
        jcatchStatement = outerTryCatchFinally.newCatch(exVar);
        jcatchStatement.addStatement(new JCodeStatement("throw javax.rmi.CORBA.Util.mapSystemException(" + exVar.getName() + ");"));
    }

    private void addClientContextMethods(JClass jclass) {
        //
        // Method Template:
        //
        // private org.openejb.corba.ClientContext _context;
        //
        // public org.openejb.corba.ClientContext getClientContext() {
        //     return _context;
        // }
        //
        // public void setClientContext(org.openejb.corba.ClientContext context) {
        //     _context = context;
        // }

        JField typesField = jclass.newField(org.openejb.corba.ClientContext.class, "_context");
        typesField.setModifiers(Modifier.PRIVATE);

        JMethod getterMethod = jclass.newMethod(new JReturnType(org.openejb.corba.ClientContext.class), "getClientContext", null, null);
        getterMethod.addStatement(new JCodeStatement("return " + typesField.getName() + ";"));

        JParameter jparameter = new JParameter(org.openejb.corba.ClientContext.class, typesField.getName());
        JMethod setterMethod = jclass.newMethod(new JReturnType(Void.TYPE), "setClientContext", new JParameter[] {jparameter}, null);
        setterMethod.addStatement(new JCodeStatement("this." + typesField.getName() + " = " + jparameter.getName() +  ";"));
    }

    private void addIdsMethod(JClass jclass, Class iface) {
        //
        // Method Template:
        //
        // private static final String[] _type_ids = {
        //     "RMI:org.openejb.corba.compiler.Foo:0000000000000000"
        //     "RMI:org.openejb.corba.compiler.All:0000000000000000"
        // };
        //
        // public String[] getIds()
        // {
        //     return _type_ids;
        // }
        //

        String ids = "";
        for (Iterator iterator = getAllInterfaces(iface).iterator(); iterator.hasNext();) {
            Class superInterface = (Class) iterator.next();
            if (Remote.class.isAssignableFrom(superInterface) && superInterface != Remote.class) {
                if (ids.length() > 0) {
                    ids += ", ";
                }
                ids += "\"RMI:" + superInterface.getName() + ":0000000000000000\"";
            }
        }
        ids = "{ " + ids + " }";

        JField typesField = jclass.newField(String[].class, "_type_ids", new JExpression(new JCodeStatement(ids)));
        typesField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);

        JMethod jmethod = jclass.newMethod(new JReturnType(String[].class), "_ids", null, null);
        jmethod.addStatement(new JCodeStatement("return _type_ids;"));
    }


    private void addMethod(IiopOperation iiopOperation, JClass jclass) {
        Method method = iiopOperation.getMethod();

        JReturnType jreturnType = new JReturnType(method.getReturnType());

        Class[] parameterTypes = method.getParameterTypes();
        JParameter[] jparameters = new JParameter[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            jparameters[i] = new JParameter(parameterTypes[i], "arg" + i);
        }

        addMethod(jclass,
                iiopOperation.getName(),
                jreturnType,
                method.getName(),
                jparameters,
                method.getExceptionTypes());
    }

    private static final Map readMethods;
    private static final Map writeMethods;
    private static final Map specialTypeNames;
    private static final Map specialTypePackages;
    private static final Set keywords;

    static {
        readMethods = new HashMap();
        readMethods.put("boolean", "read_boolean");
        readMethods.put("char", "read_wchar");
        readMethods.put("byte", "read_octet");
        readMethods.put("short", "read_short");
        readMethods.put("int", "read_long");
        readMethods.put("long", "read_longlong");
        readMethods.put("float", "read_float");
        readMethods.put("double", "read_double");
        readMethods.put("org.omg.CORBA.Object", "read_Object");

        writeMethods = new HashMap();
        writeMethods.put("boolean", "write_boolean");
        writeMethods.put("char", "write_wchar");
        writeMethods.put("byte", "write_octet");
        writeMethods.put("short", "write_short");
        writeMethods.put("int", "write_long");
        writeMethods.put("long", "write_longlong");
        writeMethods.put("float", "write_float");
        writeMethods.put("double", "write_double");
        writeMethods.put("org.omg.CORBA.Object", "write_Object");

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

    protected String getWriteMethod(JVariable jvariable) {
        if (jvariable != null) {
            return (String) writeMethods.get(jvariable.getTypeDecl());
        }
        return null;
    }

    protected String getReadMethod(JType jtype) {
        if (jtype != null) {
            return (String) readMethods.get(jtype.getTypeDecl());
        }
        return null;
    }

}
