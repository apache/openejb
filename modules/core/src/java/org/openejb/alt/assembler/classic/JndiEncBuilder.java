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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.alt.assembler.classic;

import org.openejb.OpenEJBException;
import org.openejb.core.CoreUserTransaction;
import org.openejb.core.ivm.naming.IntraVmJndiReference;
import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.core.ivm.naming.JndiReference;
import org.openejb.core.ivm.naming.NameNode;
import org.openejb.core.ivm.naming.ParsedName;
import org.openejb.core.ivm.naming.Reference;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class JndiEncBuilder {

    private final ReferenceWrapper referenceWrapper;
    private final boolean beanManagedTransactions;
    private final EjbReferenceInfo[] ejbReferences;
    private final EjbLocalReferenceInfo[] ejbLocalReferences;
    private final EnvEntryInfo[] envEntries;
    private final ResourceReferenceInfo[] resourceRefs;

    /**
     * Constructs the builder and normalizes the input data.  Does not build till build() is called
     *
     * @param jndiEnc
     * @param transactionType
     * @throws OpenEJBException
     */
    public JndiEncBuilder(JndiEncInfo jndiEnc, String transactionType, EjbType ejbType) throws OpenEJBException {
        if (ejbType.isEntity()) {
            referenceWrapper = new EntityRefereceWrapper();
        } else if (ejbType == EjbType.STATEFUL) {
            referenceWrapper = new StatefulRefereceWrapper();
        } else if (ejbType == EjbType.STATELESS) {
            referenceWrapper = new StatelessRefereceWrapper();
        } else {
            throw new org.openejb.OpenEJBException("Unknown component type");
        }

        beanManagedTransactions = transactionType != null && transactionType.equalsIgnoreCase("Bean");

        ejbReferences = (jndiEnc != null && jndiEnc.ejbReferences != null) ? jndiEnc.ejbReferences : new EjbReferenceInfo[]{};
        ejbLocalReferences = (jndiEnc != null && jndiEnc.ejbLocalReferences != null) ? jndiEnc.ejbLocalReferences : new EjbLocalReferenceInfo[]{};
        envEntries = (jndiEnc != null && jndiEnc.envEntries != null) ? jndiEnc.envEntries : new EnvEntryInfo[]{};
        resourceRefs = (jndiEnc != null && jndiEnc.resourceRefs != null) ? jndiEnc.resourceRefs : new ResourceReferenceInfo[]{};
    }

    public Context build() throws OpenEJBException {
        HashMap bindings = new HashMap();

        if (beanManagedTransactions) {
            Object userTransaction = referenceWrapper.wrap(new CoreUserTransaction());
            bindings.put("java:comp/UserTransaction", userTransaction);
        }

        for (int i = 0; i < ejbReferences.length; i++) {
            EjbReferenceInfo referenceInfo = ejbReferences[i];
            EjbReferenceLocationInfo location = referenceInfo.location;

            Reference reference = null;

            if (!location.remote) {
                String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId;
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String openEjbSubContextName = "java:openejb/remote_jndi_contexts/" + location.jndiContextId;
                reference = new JndiReference(openEjbSubContextName, location.remoteRefName);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        for (int i = 0; i < ejbLocalReferences.length; i++) {
            EjbLocalReferenceInfo referenceInfo = ejbLocalReferences[i];

            EjbReferenceLocationInfo location = referenceInfo.location;
            if (location != null && !location.remote) {
                String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId + "Local";
                Reference reference = new IntraVmJndiReference(jndiName);
                bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
            }
        }

        for (int i = 0; i < envEntries.length; i++) {
            EnvEntryInfo entry = envEntries[i];
            try {
                Class type = Class.forName(entry.type.trim());
                Object obj = null;
                if (type == String.class)
                    obj = new String(entry.value);
                else if (type == Double.class) {
                    obj = new Double(entry.value);
                } else if (type == Integer.class) {
                    obj = new Integer(entry.value);
                } else if (type == Long.class) {
                    obj = new Long(entry.value);
                } else if (type == Float.class) {
                    obj = new Float(entry.value);
                } else if (type == Short.class) {
                    obj = new Short(entry.value);
                } else if (type == Boolean.class) {
                    obj = new Boolean(entry.value);
                } else if (type == Byte.class) {
                    obj = new Byte(entry.value);
                } else {
                    throw new IllegalArgumentException("Invalid env-ref-type " + type);
                }

                bindings.put(normalize(entry.name), obj);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid environment entry type: " + entry.type.trim() + " for entry: " + entry.name);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The env-entry-value for entry " + entry.name + " was not recognizable as type " + entry.type + ". Received Message: " + e.getLocalizedMessage());
            }
        }

        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceReferenceInfo referenceInfo = resourceRefs[i];
            Reference reference = null;

            if (referenceInfo.resourceID != null) {
                String jndiName = "java:openejb/connector/" + referenceInfo.resourceID;
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String openEjbSubContextName1 = "java:openejb/remote_jndi_contexts/" + referenceInfo.location.jndiContextId;
                String jndiName2 = referenceInfo.location.remoteRefName;
                reference = new JndiReference(openEjbSubContextName1, jndiName2);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        IvmContext enc = new IvmContext(new NameNode(null, new ParsedName("comp"), null));

        for (Iterator iterator = bindings.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            try {
                enc.bind(name, value);
            } catch (NamingException e) {
                throw new org.openejb.SystemException("Unable to bind '" + name + "' into bean's enc.", e);
            }
        }
        return enc;
    }

    private String normalize(String name) {
        if (name.charAt(0) == '/')
            name = name.substring(1);
        if (!(name.startsWith("java:comp/env") || name.startsWith("comp/env"))) {
            if (name.startsWith("env/"))
                name = "comp/" + name;
            else
                name = "comp/env/" + name;
        }
        return name;
    }

    private Object wrapReference(Reference reference) {
        return referenceWrapper.wrap(reference);
    }

    static abstract class ReferenceWrapper {
        abstract Object wrap(Reference reference);

        abstract Object wrap(UserTransaction reference);
    }

    static class EntityRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.openejb.core.entity.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            throw new IllegalStateException("Entity beans cannot have references to UserTransaction instance");
        }
    }

    static class StatelessRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.openejb.core.stateless.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.openejb.core.stateless.EncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }

    static class StatefulRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.openejb.core.stateful.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.openejb.core.stateful.EncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }
}
