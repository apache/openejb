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

package org.openejb.assembler;


/**
 * ContainerSystemInfo is part of the OpenEjbConfiguration object structure that provides
 * the information about the configuration of OpenEJB and the container system.
 * 
 * <p>
 * The OpenEjbConfiguration itself is created by a OpenEjbConfigurationFactory and
 * is used by the org.openejb.assembler.Assembler to build a running unstance of
 * OpenEJB.</p>
 * 
 * The default OpenEjbConfigurationFactory is DomOpenEjbConfigurationFactory, which
 * creates an OpenEjbConfiguration object based on XML config files located on the
 * local system.</p>
 * 
 * <p>
 * Other OpenEjbConfigurationFactory implementations can be created that might populate
 * this object using a different approach.  Other usefull implementations might be:<br>
 * <UL>
 * <LI>Populating the OpenEjbConfiguration from values in a RDBMS.
 * <LI>Populating the OpenEjbConfiguration from values in a Properties file.
 * <LI>Retrieving the OpenEjbConfiguration from a ODBMS.
 * <LI>Creating the OpenEjbConfiguration using a JavaBeans enabled editing tool or wizard.
 * </UL>
 * 
 * <p>
 * If you are interested in creating alternate an OpenEjbConfigurationFactory to do
 * any of the above techniques or a new approach, email the
 * <a href="mailto:openejb-dev@exolab.org">OpenEJB Developer list</a> with a description
 * of the new OpenEjbConfigurationFactory implementation.
 * </p>
 * 
 * @see org.openejb.spi.Assembler
 * @see org.openejb.assembler.Assembler
 * @see org.openejb.assembler.OpenEjbConfiguration
 * @see org.openejb.assembler.OpenEjbConfigurationFactory
 * @see org.openejb.xmlconf.DomOpenEjbConfigurationFactory
 */
public class ContainerSystemInfo extends InfoObject{

    
    public ContainerInfo[] containers;
    
    public EntityContainerInfo[] entityContainers;
    public StatelessSessionContainerInfo[] statelessContainers;
    public StatefulSessionContainerInfo[] statefulContainers;
    public SecurityRoleInfo[] securityRoles;
    public MethodPermissionInfo[] methodPermissions;
    public MethodTransactionInfo[] methodTransactions;
}
