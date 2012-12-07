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
package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
/*
  This class is used as a replacement when a IvmContext referenced by a stateful bean 
  is being serialized for passivation along with the bean.  It ensures that the entire
  JNDI ENC graph is not serialized with the bean and returns a reference to the correct
  IvmContext when its deserialized.

  Stateful beans are activated by a thread with the relavent BeanContext object in the 
  ThreadContext which makes it possible to lookup the correct IvmContext and swap in place of 
  this object.
*/

public class JndiEncArtifact implements java.io.Serializable {
    String path = new String();

    public JndiEncArtifact(IvmContext context) {
        NameNode node = context.mynode;
        do {
            path = node.getAtomicName() + "/" + path;
            node = node.getParent();
        } while (node != null);
    }

    public Object readResolve() throws java.io.ObjectStreamException {
        ThreadContext thrdCntx = ThreadContext.getThreadContext();
        BeanContext deployment = thrdCntx.getBeanContext();
        javax.naming.Context cntx = deployment.getJndiEnc();
        try {
            Object obj = cntx.lookup(path);
            if (obj == null)
                throw new java.io.InvalidObjectException("JNDI ENC context reference could not be properly resolved when bean instance was activated");
            return obj;
        } catch (javax.naming.NamingException e) {
            throw (java.io.InvalidObjectException)new java.io.InvalidObjectException("JNDI ENC context reference could not be properly resolved due to a JNDI exception, when bean instance was activated").initCause(e);
        }
    }

}