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
package org.openejb.alt.assembler.modern.xml;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility functions for reading and writing DOM trees.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public abstract class XMLUtils {
    public static String getChildText(Element parent, String name) {
        Element e = getChildByName(parent, name);
        if(e == null)
            return "";
        return getText(e);
    }

    public static String getText(Element e) {
        NodeList nl = e.getChildNodes();
        int max = nl.getLength();
        for(int i=0; i<max; i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.TEXT_NODE) {
                return n.getNodeValue();
            }
        }
        return "";
    }

    public static Element getChildByName(Element e, String name) {
        Element[] list = getChildrenByName(e, name);
        if(list.length == 0)
            return null;
        if(list.length > 1)
            throw new IllegalStateException("Too many ("+list.length+") '"+name+"' elements found!");
        return list[0];
    }

    public static Element[] getChildrenByName(Element e, String name) {
        NodeList nl = e.getChildNodes();
        int max = nl.getLength();
        LinkedList list = new LinkedList();
        for(int i=0; i<max; i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE &&
               n.getNodeName().equals(name)) {
                list.add(n);
            }
        }
        return (Element[])list.toArray(new Element[list.size()]);
    }

    public static String[] splitOnWhitespace(String source) {
        int pos = -1;
        LinkedList list = new LinkedList();
        int max = source.length();
        for(int i=0; i<max; i++) {
            char c = source.charAt(i);
            if(Character.isWhitespace(c)) {
                if(i-pos > 1)
                    list.add(source.substring(pos+1, i));
                pos = i;
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }

    public static Element createChild(Document doc, Element root, String name) {
        Element elem = doc.createElement(name);
        root.appendChild(elem);
        return elem;
    }

    public static void createChildText(Document doc, Element elem, String name, String value) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(value == null ? "" : value));
        elem.appendChild(child);
    }

    public static void createOptionalChildText(Document doc, Element elem, String name, String value) {
        if(value == null || value.length() == 0)
            return;
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(value));
        elem.appendChild(child);
    }

    public static String stripWhitespace(String source) {
        StringBuffer buf = new StringBuffer();
        int max = source.length();
        char c;
        for(int i=0; i<max; i++) {
            c = source.charAt(i);
            if(!Character.isWhitespace(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
