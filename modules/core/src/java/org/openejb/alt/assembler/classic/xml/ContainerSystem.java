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

package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ContainerInfo;
import org.openejb.alt.assembler.classic.ContainerSystemInfo;
import org.w3c.dom.Node;

/**
 * A subclass of ContainerSystemInfo filled with data from an XML file.
 * 
 * Populates the member variables of ContainerSystemInfo in this classes initializeFromDOM method.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.ContainerSystemInfo
 */
public class ContainerSystem extends ContainerSystemInfo implements DomObject{


    /**
     * Represents the <tt>containers</tt> element in the XML config file.
     */
    public static final String CONTAINERS = "containers";

    /**
     * Represents the <tt>entity-container</tt> element in the XML config file.
     */
    public static final String ENTITY_CONTAINER = "entity-container";

    /**
     * Represents the <tt>cmp-entity-container</tt> element in the XML config file.
     */
    public static final String CMP_ENTITY_CONTAINER = "cmp-entity-container";

    /**
     * Represents the <tt>stateful-session-container</tt> element in the XML config file.
     */
    public static final String STATEFUL_SESSION_CONTAINER = "stateful-session-container";
    
    /**
     * Represents the <tt>stateless-session-container</tt> element in the XML config file.
     */
    public static final String STATELESS_SESSION_CONTAINER = "stateless-session-container";


    /**
     * Represents the <tt>security-role</tt> element in the XML config file.
     */
    public static final String SECURITY_ROLE = "security-role";

    /**
     * Represents the <tt>method-permission</tt> element in the XML config file.
     */
    public static final String METHOD_PERMISSION = "method-permission";

    /**
     * Represents the <tt>method-transaction</tt> element in the XML config file.
     */
    public static final String METHOD_TRANSACTION = "method-transaction";

    /** 
     * Parses out the values needed by this InfoObject from the DOM Node passed in.
     */
    public void initializeFromDOM(Node node) throws OpenEJBException{
        
        Node containersElement = DomTools.getChildElement(node, CONTAINERS);

        /* EntityContainer */
        DomObject[] dos = DomTools.collectChildElementsByType(containersElement, EntityContainer.class, ENTITY_CONTAINER);
        entityContainers = new EntityContainer[dos.length];
        for (int i=0; i < dos.length; i++) entityContainers[i] = (EntityContainer)dos[i];

        /* StatelessSessionContainer */
        dos = DomTools.collectChildElementsByType(containersElement, StatelessSessionContainer.class, STATELESS_SESSION_CONTAINER);
        statelessContainers = new StatelessSessionContainer[dos.length];
        for (int i=0; i < dos.length; i++) statelessContainers[i] = (StatelessSessionContainer)dos[i];

        /* StatefulSessionContainer */
        dos = DomTools.collectChildElementsByType(containersElement, StatefulSessionContainer.class, STATEFUL_SESSION_CONTAINER);
        statefulContainers = new StatefulSessionContainer[dos.length];
        for (int i=0; i < dos.length; i++) statefulContainers[i] = (StatefulSessionContainer)dos[i];

        int x=0;
        containers = new ContainerInfo[entityContainers.length +
                                       statelessContainers.length +
                                       statefulContainers.length];

        System.arraycopy(entityContainers      , 0, containers, x                            ,entityContainers.length);
        
        System.arraycopy(statelessContainers, 0, containers, x += entityContainers.length    ,statelessContainers.length);
        System.arraycopy(statefulContainers , 0, containers, x += statelessContainers.length ,statefulContainers.length);
        
        
        dos = DomTools.collectChildElementsByType(node, SecurityRole.class, SECURITY_ROLE);
        securityRoles = new SecurityRole[dos.length];
        for (int i=0; i < dos.length; i++) securityRoles[i] = (SecurityRole)dos[i];

        dos = DomTools.collectChildElementsByType(node, MethodPermission.class, METHOD_PERMISSION);
        methodPermissions = new MethodPermission[dos.length];
        for (int i=0; i < dos.length; i++) methodPermissions[i] = (MethodPermission)dos[i];
 
        dos = DomTools.collectChildElementsByType(node, MethodTransaction.class, METHOD_TRANSACTION);
        methodTransactions = new MethodTransaction[dos.length];
        for (int i=0; i < dos.length; i++) methodTransactions[i] = (MethodTransaction)dos[i];
    }


    public void serializeToDOM(Node node) throws OpenEJBException{}
}

