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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.tyrex;

import java.util.Properties;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.assembler.classic.EntityBeanInfo;
import org.openejb.alt.assembler.classic.ResourceReferenceInfo;
import org.openejb.alt.assembler.classic.StatefulBeanInfo;
import org.openejb.alt.assembler.classic.StatelessBeanInfo;
import org.openejb.core.ivm.naming.ENCReference;
import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.core.ivm.naming.ObjectReference;
import org.openejb.core.ivm.naming.Reference;
import org.openejb.util.SafeToolkit;

import tyrex.resource.Resource;
import tyrex.resource.Resources;
import tyrex.tm.TransactionDomain;


public class TyrexClassicAssembler extends org.openejb.alt.assembler.classic.Assembler {
    protected static SafeToolkit toolkit = SafeToolkit.getToolkit("TyrexClassicAssembler");

    public void init(Properties props) throws OpenEJBException{

        // the EnvProps.THREAD_CONTEXT_IMPL is accessed from the System properties
        // rather then the props because the ThreadContext class doen't have access to the
        // the Assembler's props.

        /* sr: for some reason the transaction management fails totally if this is uncommented.

         if(!System.getProperties().contains(EnvProps.THREAD_CONTEXT_IMPL)){
             if(!props.contains(EnvProps.THREAD_CONTEXT_IMPL)){
                 System.setProperty(EnvProps.THREAD_CONTEXT_IMPL, "org.openejb.tyrex.TyrexThreadContext");
             }else{
                 System.setProperty(EnvProps.THREAD_CONTEXT_IMPL, props.getProperty(EnvProps.THREAD_CONTEXT_IMPL));
             }
        }
        */
        // the TyrexEnvProps.TX_DOMAIN property is accessed from the System properties from the
        // bindJndiResourceRefs() rather then the props because its a static method.

        // need to create the domain if it does not already exist.
        if(TransactionDomain.getDomain("default") ==null ){
            String domainPath = props.getProperty(TyrexEnvProps.TX_DOMAIN);
            if(domainPath==null){
                domainPath  = System.getProperty(TyrexEnvProps.TX_DOMAIN);
            }
            if(domainPath!=null){
                try{
                    TransactionDomain.createDomain(domainPath);
                }catch(tyrex.tm.DomainConfigurationException dce){
                    throw new OpenEJBException("Although the TyrexEnvProp.TX_DOMAIN property was set the domain could not be created", dce);
                }
            }
        }

        super.init(props);
    }

    protected org.openejb.core.DeploymentInfo createDeploymentInfoObject(javax.naming.Context root, Object did, Class homeClass, Class remoteClass, Class beanClass, Class pkClass, byte componentType) throws org.openejb.SystemException {
        return new TyrexDeploymentInfo(root, did, homeClass, remoteClass, beanClass, pkClass, componentType);
    }

    protected void bindJndiResourceRefs(EnterpriseBeanInfo bean,IvmContext root ) throws org.openejb.OpenEJBException {
        if(bean.jndiEnc == null || bean.jndiEnc.ejbReferences == null)
            return;

        TransactionDomain td = TransactionDomain.getDomain("default");
        if(td == null){
            //FIXME: log no domain path
            throw new RuntimeException("The Tyrex \"default\" was not set. The "+bean.ejbDeploymentId+" bean deployment utilizes resources and needs to access the domain.xml");
        }
        Resources resources = td.getResources();

        for (int i=0; i< bean.jndiEnc.resourceRefs.length; i++){
            ResourceReferenceInfo reference = bean.jndiEnc.resourceRefs[i];

            try{
                Resource resource = resources.getResource(reference.resourceID);
                if(resource==null) {
                    throw new org.openejb.OpenEJBException("The reference with resource id "+reference.resourceID+" defined in the OpenEJB deployment file is not present in the Tyrex configuration file.");
                }
                //sr: this looks suspicious: ref will be overwritten no matter what
                Reference ref = new ObjectReference( resource );
                if(EntityBeanInfo.class.isAssignableFrom(bean.getClass()))
                    ref = new org.openejb.core.entity.EncReference( ref );
                else if(StatefulBeanInfo.class.isAssignableFrom(bean.getClass()))
                    ref = new org.openejb.core.stateful.EncReference( ref );
                else if(StatelessBeanInfo.class.isAssignableFrom(bean.getClass()))
                    ref = new org.openejb.core.stateless.EncReference( ref );
                else
                    throw new org.openejb.SystemException("This can't happen");

                TyrexReference tyrexRef = new TyrexReference((ENCReference)ref);
                root.bind(prefixForBinding(reference.referenceName), tyrexRef);

            }catch(javax.naming.NamingException ne){
                // FIXME: Log this exception
                ne.printStackTrace();
                // this is not a critical error since it only impacts one deployment
                continue;

            }catch(tyrex.resource.ResourceException re){
                // FIXME: Log this exception
                re.printStackTrace();
                // this is not a critical error since it only impacts one deployment
                continue;
            }
        }
    }
}
