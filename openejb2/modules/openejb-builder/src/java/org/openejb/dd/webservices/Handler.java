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
 *    please contact openejb@openejb.org.
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
package org.openejb.dd.webservices;

import java.util.ArrayList;

public class Handler {
    private String handlerName;
    private String handlerClass;
    private ArrayList soapHeaderList = new ArrayList();
    private ArrayList soapRoleList = new ArrayList();

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }


    public void addSoapHeader(String soapHeader) throws IndexOutOfBoundsException {
        soapHeaderList.add(soapHeader);
    }

    public void addSoapHeader(int index, String soapHeader) throws IndexOutOfBoundsException {
        soapHeaderList.add(index, soapHeader);
    }

    public boolean removeSoapHeader(String soapHeader) {
        return soapHeaderList.remove(soapHeader);
    }

    public String getSoapHeader(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapHeaderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (String) soapHeaderList.get(index);
    }

    public String[] getSoapHeader() {
        int size = soapHeaderList.size();
        String[] mArray = new String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) soapHeaderList.get(index);
        }
        return mArray;
    }

    public void setSoapHeader(int index, String soapHeader) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapHeaderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        soapHeaderList.set(index, soapHeader);
    }

    public void setSoapHeader(String[] soapHeaderArray) {
        soapHeaderList.clear();
        for (int i = 0; i < soapHeaderArray.length; i++) {
            String soapHeader = soapHeaderArray[i];
            soapHeaderList.add(soapHeader);
        }
    }

    public void clearSoapHeader() {
        soapHeaderList.clear();
    }


    public void addSoapRole(String soapRole) throws IndexOutOfBoundsException {
        soapRoleList.add(soapRole);
    }

    public void addSoapRole(int index, String soapRole) throws IndexOutOfBoundsException {
        soapRoleList.add(index, soapRole);
    }

    public boolean removeSoapRole(String soapRole) {
        return soapRoleList.remove(soapRole);
    }

    public String getSoapRole(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (String) soapRoleList.get(index);
    }

    public String[] getSoapRole() {
        int size = soapRoleList.size();
        String[] mArray = new String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) soapRoleList.get(index);
        }
        return mArray;
    }

    public void setSoapRole(int index, String soapRole) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        soapRoleList.set(index, soapRole);
    }

    public void setSoapRole(String[] soapRoleArray) {
        soapRoleList.clear();
        for (int i = 0; i < soapRoleArray.length; i++) {
            String soapRole = soapRoleArray[i];
            soapRoleList.add(soapRole);
        }
    }

    public void clearSoapRole() {
        soapRoleList.clear();
    }


}
