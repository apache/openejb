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
package org.openejb.alt.assembler.modern.jar.ejb11;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Metadata for an EJB JAR method permission.  This includes a list of roles
 * allowed to access the methods, and a description of the methods in
 * question.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class MethodPermissionMetaData {
    private String description;
    private ArrayList roles;
    private ArrayList methods;

    public MethodPermissionMetaData() {
        roles = new ArrayList();
        methods = new ArrayList();
    }
    public MethodPermissionMetaData(MethodPermissionMetaData source) {
        roles = new ArrayList(source.roles);
        methods = new ArrayList(source.methods);
        description = source.description;
    }

    public void setDescription(String desc) {description = desc;}
    public String getDescription() {return description;}

    public void addRole(String role) {roles.add(role);}
    public void removeRole(String role) {roles.remove(role);}
    public void setRoles(String[] roleList) {
        roles.clear();
        roles.addAll(Arrays.asList(roleList));
    }
    public String[] getRoles() {
        return (String[])roles.toArray(new String[roles.size()]);
    }

    public void addMethod(MethodMetaData method) {methods.add(method);}
    public void removeMethod(MethodMetaData method) {methods.remove(method);}
    public void setMethods(MethodMetaData[] methodList) {
        methods.clear();
        methods.addAll(Arrays.asList(methodList));
    }
    public MethodMetaData[] getMethods() {
        return (MethodMetaData[])methods.toArray(new MethodMetaData[methods.size()]);
    }
}
