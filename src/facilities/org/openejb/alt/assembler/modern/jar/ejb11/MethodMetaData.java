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

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Metadata for a describing a method or set of methods.  They must all be
 * from one bean, but may be from the home or remote interface.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class MethodMetaData {
    private String ejbName;
    private String description;
    private Boolean remoteInterface;
    private String name;
    private LinkedList args = null;

    public MethodMetaData() {
    }

    public void setEJBName(String name) {ejbName = name;}
    public String getEJBName() {return ejbName;}
    public void setDescription(String desc) {description = desc;}
    public String getDescription() {return description;}
    public void setInterfaceRemote(Boolean isRemote) {remoteInterface = isRemote;}
    public Boolean isRemoteInterface() {return remoteInterface;}
    public void setMethodName(String name) {this.name = name;}
    public String getMethodName() {return name;}
    public void addArgument(String arg) {
        if(args == null)
            args = new LinkedList();
        args.add(arg);
    }
    public void removeArgument(String arg) {
        if(args != null)
            args.remove(arg);
    }
    public void setArguments(String[] argList) {
        if(args == null)
            args = new LinkedList();
        else
            args.clear();
        args.addAll(Arrays.asList(argList));
    }
    public String[] getArguments() {
        if(args == null)
            return null;
        return (String[])args.toArray(new String[args.size()]);
    }
    public void setArguments(Class[] argClasses) {
        if(args == null)
            args = new LinkedList();
        else
            args.clear();
        for(int i=0; i<argClasses.length; i++)
            args.add(argClasses[i].getName());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(ejbName);
        if(remoteInterface != null && !remoteInterface.booleanValue())
            buf.append("Home");
        buf.append('.').append(name).append('(');
        if(args == null) {
            buf.append('*');
        } else {
            for(int i=0; i<args.size(); i++) {
                if(i > 0)
                    buf.append(", ");
                buf.append((String)args.get(i));
            }
        }
        buf.append(')');
        return buf.toString();
    }
}
