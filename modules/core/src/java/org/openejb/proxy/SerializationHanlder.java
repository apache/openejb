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
package org.openejb.proxy;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;

import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;



/**
 *
 */
public class SerializationHanlder implements EJBInterceptor {
    
    private static InheritableThreadLocal serializationState = new InheritableThreadLocal();
    
    private final EJBInterceptor next;
    
    public SerializationHanlder(EJBInterceptor next){
        this.next = next;
    }
    
    
    public InvocationResult invoke(EJBInvocation ejbInvocation) throws Throwable{
        Object[] args = ejbInvocation.getArguments();
        if (args != null && args.length > 0) {
            try {
                setStrategy(ReplacementStrategy.COPY);
                args = copyArgs(args);
            } finally {
                setStrategy(null);
                ejbInvocation = new EJBInvocationImpl(ejbInvocation.getType(), ejbInvocation.getId(), ejbInvocation.getMethodIndex(), args);
            }
        }

        
        
        InvocationResult result = next.invoke(ejbInvocation);
        
        if (result.getResult() == null) {
            return result;
        }

        Object returnObj = result.getResult();
        
        try {
            setStrategy(ReplacementStrategy.COPY);
            returnObj = copyObj(returnObj);
        } finally {
            setStrategy(null);
            result = new SimpleInvocationResult(returnObj);
        }
        
        return result;
    }
    
    
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        if (args != null && args.length > 0) {
//            try {
//                setStrategy(ReplacementStrategy.COPY);
//                args = copyArgs(args);
//            } finally {
//                setStrategy(null);
//            }
//        }
//        
//        Object returnObj = next.invoke(proxy,method,args);
//        
//        try {
//            setStrategy(ReplacementStrategy.COPY);
//            returnObj = copyObj(returnObj);
//        } finally {
//            setStrategy(null);
//        }
//        
//        return returnObj;
//    }
    
    /**
     * This method is public so it can be called by other parts of the 
     * container during their serialization operations, namely session 
     * passivation
     * 
     * @param strategy
     */
    public static void setStrategy(ReplacementStrategy strategy){
        serializationState.set(strategy);
    }
    
    private static ReplacementStrategy getStrategy(){
        ReplacementStrategy strategy = (ReplacementStrategy)serializationState.get();
        if( strategy == null){
            return ReplacementStrategy.REPLACE;
        }
        return strategy;
    }
        
    private Object[] copyArgs(Object[] objects) throws IOException, ClassNotFoundException{
        for (int i=0; i < objects.length; i++){
            objects[i] = copyObj(objects[i]);
        }
        return objects;
    }

    private Object copyObj(Object object) throws IOException, ClassNotFoundException {
        MarshalledObject obj = new MarshalledObject(object);
        return obj.get();
    }
    
    public static Object writeReplace(Object object, ProxyInfo proxyInfo) throws ObjectStreamException {
        return getStrategy().writeReplace(object, proxyInfo);
    }
}

