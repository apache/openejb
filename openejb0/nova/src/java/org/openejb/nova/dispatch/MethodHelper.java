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
package org.openejb.nova.dispatch;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import org.openejb.nova.dispatch.MethodSignature;

/**
 * Helper methods to deal with the whack handeling of indexes in cglib MethodProxy objects.
 *
 * @version $Revision$ $Date$
 */
public final class MethodHelper {
    private MethodHelper() {
    }

    /**
     * Gets the super index of the specified method on the proxyImpl.  This is the number returned by
     * MethodProxy.getSuperIndex() when the specifiec method is intercepted.
     * @param proxyImpl the enhanced proxy class; this is the generated sub class of your interface
     * @param method the method that will be intercepted
     * @return the number that MethodProxy.getSuperIndex() will return when the specifiec method is
     * intercepted or -1 if the method can not be intercepted (i.e., it was not enhanced)
     */
    public static int getSuperIndex(FastClass proxyImpl, Method method) {
        return getSuperIndex(proxyImpl, method.getName(), method.getParameterTypes());
    }

    /**
     * Gets the super index of the specified method on the proxyImpl.  This is the number returned by
     * MethodProxy.getSuperIndex() when the specifiec method is intercepted.
     * @param proxyImpl the enhanced proxy class; this is the generated sub class of your interface
     * @param signature the signature of method that will be intercepted
     * @return the number that MethodProxy.getSuperIndex() will return when the specifiec method is
     * intercepted or -1 if the method can not be intercepted (i.e., it was not enhanced)
     */
    public static int getSuperIndex(FastClass proxyImpl, MethodSignature signature) throws ClassNotFoundException {
        String[] parameterTypes = signature.getParameterTypes();
        ClassLoader cl = proxyImpl.getJavaClass().getClassLoader();
        Class[] params = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            params[i] = cl.loadClass(parameterTypes[i]);
        }
        return getSuperIndex(proxyImpl, signature.getMethodName(), params);
    }

    /**
     * Gets the super index of the specified method on the proxyImpl.  This is the number returned by
     * MethodProxy.getSuperIndex() when the specifiec method is intercepted.
     * @param proxyImpl the enhanced proxy class; this is the generated sub class of your interface
     * @param methodName the name of the method that will be intercepted
     * @param methodParameters the parameter types of the method that will be intercepted
     * @return the number that MethodProxy.getSuperIndex() will return when the specifiec method is
     * intercepted or -1 if the method can not be intercepted (i.e., it was not enhanced)
     */
    public static int getSuperIndex(FastClass proxyImpl, String methodName, Class[] methodParameters) {
        String prefix = ACCESS_PREFIX + methodName + "_";
        int lastUnderscore = prefix.length() - 1;

        Method[] methods = proxyImpl.getJavaClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.startsWith(prefix) &&
                    name.lastIndexOf('_') == lastUnderscore &&
                    Arrays.equals(methods[i].getParameterTypes(), methodParameters)) {
                return proxyImpl.getIndex(name, methodParameters);
            }
        }
        return -1;
    }

    /**
     * Returns a lookup table from the number that MethodProxy.getSuperIndex() returns to
     * the index if it's EJB[Local]Home method signature.  The method signatures are translated
     * from ejb bean implementation names to EJB[Local]ome interface names (i.e., ejbCreate is
     * translated to create) before the lookup table is build.  With this map you can determine
     * which bean implementation the proxy is targeted.
     *
     *  MethodSignature[] signatures = ...
     *  int[] homeMap = getHomeMap(signatures, proxyImpl);
     *
     * Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
     *     int superIndex = proxy.getSuperIndex();
     *     MethodSignature signature = signatures[superIndex];
     *     ....
     * }
     *
     * @param signatures the signature of the ejb bean implementation method
     * @param proxyImpl the implementation of the proxy class
     * @return a map from the superIndex to the index of the corresponding method signature
     */
    public static int[] getHomeMap(MethodSignature[] signatures, FastClass proxyImpl) {
        return getMap(translateHome(signatures), proxyImpl);
    }

    /**
     * Returns a lookup table from the number that MethodProxy.getSuperIndex() returns to
     * the index if it's EJB[Local]Object method signature.  The method signatures are translated
     * from ejb bean implementation names to EJB[Local]Object interface names (i.e., ejbRemove is
     * translated to remove) before the lookup table is build.  With this map you can determine
     * which bean implementation the proxy is targeted.
     *
     *  MethodSignature[] signatures = ...
     *  int[] homeMap = getHomeMap(signatures, proxyImpl);
     *
     * Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
     *     int superIndex = proxy.getSuperIndex();
     *     MethodSignature signature = signatures[superIndex];
     *     ....
     * }
     *
     * @param signatures the signature of the ejb bean implementation method
     * @param proxyImpl the implementation of the proxy class
     * @return a map from the superIndex to the index of the corresponding method signature
     */
    public static int[] getObjectMap(MethodSignature[] signatures, FastClass proxyImpl) {
        return getMap(translateObject(signatures), proxyImpl);
    }

    private static int[] getMap(MethodSignature[] translated, FastClass proxyImpl) {
        // get the map from method keys to the intercepted shadow index
        Map proxyToShadowIndex = buildProxyToShadowIndex(proxyImpl);

        // create the method lookup table and fill it with -1
        int[] shadowIndexToProxy = new int[proxyImpl.getMaxIndex() + 1];
        Arrays.fill(shadowIndexToProxy, -1);

        // for each translated method (the method signature on the proxy),
        // fill in it's id into the shadowIndex table
        for (int i = 0; i < translated.length; i++) {
            if(translated[i] != null) {
                Integer shadowIndex = (Integer)proxyToShadowIndex.get(translated[i]);
                if(shadowIndex != null) {
                    shadowIndexToProxy[shadowIndex.intValue()] = i;
                }
            }
        }
        return shadowIndexToProxy;
    }

    public static Map getHomeMethodMap(MethodSignature[] signatures, Class homeClass) {
        return getMethodMap(homeClass, translateHome(signatures));
    }

    public static Map getObjectMethodMap(MethodSignature[] signatures, Class homeClass) {
        return getMethodMap(homeClass, translateObject(signatures));
    }

    private static Map getMethodMap(Class homeClass, MethodSignature[] signatures) {
        Method[] methods = homeClass.getMethods();
        Map methodMap = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Integer index = findMethodIndex(signatures, method);
            if (index == null) {
                methodMap.put(method, index);
            }
        }
        return methodMap;
    }

    private static Integer findMethodIndex(MethodSignature[] signatures, Method method) {
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            if(signature != null && signature.match(method)) {
                return new Integer(i);
            }
        }
        return null;
    }

    private static MethodSignature[] translateHome(MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            String name = signature.getMethodName();
            if (name.startsWith("ejbCreate")) {
                translated[i] = new MethodSignature("c" + name.substring(4), signature.getParameterTypes());
            } else if (name.startsWith("ejbFind")) {
                translated[i] = new MethodSignature("f" + name.substring(4), signature.getParameterTypes());
            } else if (name.startsWith("ejbHome")) {
                String translatedName = Character.toLowerCase(name.charAt(7)) + name.substring(8);
                translated[i] = new MethodSignature(translatedName, signature.getParameterTypes());
            } else if (name.startsWith("ejbRemove")) {
                translated[i] = new MethodSignature("remove", signature.getParameterTypes());
            }
        }
        return translated;
    }

    private static MethodSignature[] translateObject(MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            String name = signature.getMethodName();
            if (name.startsWith("ejbRemove")) {
                translated[i] = new MethodSignature("remove", signature.getParameterTypes());
            } else {
                translated[i] = new MethodSignature(signature.getMethodName(), signature.getParameterTypes());
            }
        }
        return translated;
    }

    private static final String ACCESS_PREFIX = "CGLIB$$ACCESS_";
    /**
     * Builds a map from the MethodKeys for the real method to the index of
     * the shadow method, which is the same number returned from MethodProxy.getSuperIndex().
     * The map contains only the MethodKeys of methods that have shadow methods (i.e., only
     * the enhanced methods).
     * @param enhancedClass the enhanced class
     * @return a map from MethodKeys to the Integer for the shadow method
     */
    private static Map buildProxyToShadowIndex(FastClass enhancedClass) {
        Map shadowMap = new HashMap(enhancedClass.getMaxIndex() + 1);
        Method[] methods = enhancedClass.getJavaClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String shadowName = methods[i].getName();
            int lastUnderscore = shadowName.lastIndexOf('_');

            // is this a potential enhanced method?
            if (lastUnderscore > ACCESS_PREFIX.length() && shadowName.startsWith(ACCESS_PREFIX)) {
                // this is a shadow op
                String realName = shadowName.substring(ACCESS_PREFIX.length(), lastUnderscore);
                Class[] parameterTypes = methods[i].getParameterTypes();
                try {
                    int shadowIndex = enhancedClass.getIndex(shadowName, parameterTypes);
                    MethodSignature realSignature = new MethodSignature(realName, parameterTypes);
                    shadowMap.put(realSignature, new Integer(shadowIndex));
                } catch (Exception e) {
                    // ok not a shadow method
                }
            }
        }
        return shadowMap;
    }
}
