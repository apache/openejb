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

import java.util.Properties;
import java.util.Vector;

import org.openejb.OpenEJBException;
import org.openejb.util.SafeToolkit;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class DomTools{

    public static SafeToolkit toolkit = SafeToolkit.getToolkit("XML configuration loader");
    
    /**
     * Represents the <tt>properties</tt> element in the XML config file.
     */
    public static final String PROPERTIES = "properties";

    /**
     * Represents the <tt>property</tt> element in the XML config file.
     */
    public static final String PROPERTY = "property";

    /**
     * Represents the <tt>property-name</tt> element in the XML config file.
     */
    public static final String PROPERTY_NAME = "property-name";

    /**
     * Represents the <tt>property-value</tt> element in the XML config file.
     */
    public static final String PROPERTY_VALUE = "property-value";
    
    
    
    public static Properties readProperties(Node node)throws org.openejb.OpenEJBException{
        Node propertiesElement = getChildElement(node, PROPERTIES);
        
        if(propertiesElement == null) return new Properties();
        
        Node[] property = getChildElements(propertiesElement,PROPERTY);
        Properties properties = new Properties();
        String name = null, value = null;

        for (int i=0; i< property.length; i++){
            name = getChildElementPCData(property[i], PROPERTY_NAME);
            value = getChildElementPCData(property[i], PROPERTY_VALUE);
            if (name == null || value == null) continue;
            properties.setProperty( name, value );
        }
        return properties;
    }
    
    /**
     * If true debug data will be printed to the System.out containing the data in the 
     * XML config file being parsed.
     */
    public static final boolean debug = false;

    public static int debugRecursionDepth = 0;


    /**
     * Convenience method for obtaining all the child elements of the node passed in.
     * When a child element with a name matching the <tt>elementType</tt> is found in the <tt>node</tt>
     * a new instance of <tt>classType</tt> is created, cast to <tt>DomObject</tt>, then <tt>initializeFromDOM</tt> is called 
     * on the new instance and the child element is passed in as the parameter.
     * 
     * @param node  the node in the DOM containing the child elements needed.
     * @param classType  the subclass of <tt>DomObject</tt> that will parse the data in the child elements.
     * @param elementType   the name of the child element as it appears in the DTD.
     * @returns an array of the <tt>DomObject</tt> subclasses initialized with the child elements.
     * @see #initializeFromDOM
     * @see org.w3c.dom.Node
     */
    protected static DomObject[] collectChildElementsByType(Node node, Class classType, String elementType) throws OpenEJBException{

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.println(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        if (node == null) return null;

        NodeList list = node.getChildNodes();
        Vector tmp = new Vector();
        Node child = null;

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    DomObject info = (DomObject)toolkit.newInstance(classType);
                    tmp.addElement(info);
                    info.initializeFromDOM(element);
                }
            }
        }

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        DomObject[] domObjects = new DomObject[tmp.size()];
        tmp.copyInto(domObjects);
        return domObjects;
    }

    /**
     * Convenience method for obtaining a single child element from the node passed in.
     * When a child element with a name matching the <tt>elementType</tt> is found in the <tt>node</tt>
     * a new instance of <tt>classType</tt> is created, cast to <tt>DomObject</tt>, then <tt>initializeFromDOM</tt> is called 
     * on the new instance and the child element is passed in as the parameter.
     * 
     * @param node  the node in the DOM containing the child elements needed.
     * @param classType  the subclass of <tt>DomObject</tt> that will parse the data in the child elements.
     * @param elementType   the name of the child element as it appears in the DTD.
     * @returns an <tt>DomObject</tt> subclass of type <tt>classType</tt> initialized with the child element.
     * @see #initializeFromDOM
     * @see org.w3c.dom.Node
     */
    protected static DomObject collectChildElementByType(Node node, Class classType, String elementType) throws OpenEJBException{
        try{
            
        
        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.println(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        NodeList list = node.getChildNodes();
        Node child = null;
        DomObject domObject = null;
        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    domObject = (DomObject)toolkit.newInstance(classType);
                    domObject.initializeFromDOM(element);
                    break;
                }
            }
        }

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        return domObject;
        } catch(Exception e){
            e.printStackTrace();
            throw new OpenEJBException(e);
        }

    }

    /**
     * Returns the PCDATA of all child elements to the <tt>node</tt> passed in.
     * A child elements PCDATA will be collected if its name matches the <tt>elementType</tt> specified.
     * 
     * @param node  the node in the DOM containing the child element.
     * @param elementType   the name of the child element as it appears in the DTD.
     * @returns an array of <tt>String</tt> containing the PCDATA of the child elements.
     */
    protected static String[] getChildElementsPCData(Node node, String elementType) throws OpenEJBException{

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.print(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        NodeList list = node.getChildNodes();

        Node child = null;
        Vector tmp = new Vector();

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    tmp.addElement(getElementPCData(element));
                }
            }
        }
        String[] pcdata = new String[tmp.size()];
        tmp.copyInto(pcdata);

        if (debug){/*--------------------------------- * Debug Block * ------*/

            String tabs = "";
            for(int i=0;i<debugRecursionDepth;i++){tabs+="\t";}
            System.out.println(".length = "+pcdata.length);
            for(int i=0;i<pcdata.length;i++) System.out.println(tabs + node.getNodeName()+"."+ elementType + "["+ i +"] = " +pcdata[i]);
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        return pcdata;
    }

    /**
     * Returns the PCDATA of a child element in the <tt>node</tt> passed in.
     * A child elements PCDATA will be returned if its name matches the <tt>elementType</tt> specified.
     * 
     * @param node  the node in the DOM containing the child element.
     * @param elementType   the name of the child element as it appears in the DTD.
     * @returns the PCDATA of the child elements.
     */
    protected static String getChildElementPCData(Node node, String elementType) throws OpenEJBException{

        if (debug){/*--------------------------------- * Debug Block * ------*/
            debugRecursionDepth++;
            for(int i=0;i<debugRecursionDepth;i++)System.out.print("\t");
            System.out.print(node.getNodeName()+"."+ elementType);
        }/*------------------------------------------- * Debug Block * ------*/

        NodeList list = node.getChildNodes();
        Node child = null;
        String pcdata = null;
        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(elementType)){
                    pcdata = getElementPCData(element);
                    break;
                }
            }
        }

        if (debug){/*--------------------------------- * Debug Block * ------*/
            System.out.println(" = "+pcdata);
            debugRecursionDepth--;
        }/*------------------------------------------- * Debug Block * ------*/

        return pcdata;
    }

    /**
     * Returns the PCDATA of the <tt>node</tt> passed in.
     * 
     * @param node  the node in the DOM containing the PCDATA.
     * @returns the PCDATA of the node.
     */
    protected static String getElementPCData(Node node) throws OpenEJBException{
        Node child = node.getFirstChild();
        if (child == null || child.getNodeType() != Node.TEXT_NODE) return null;

        try{
            Text text = (Text)child;
            String pcdata = text.getData();
            return (pcdata!=null)?pcdata.trim():null;            
        } catch (DOMException e) {
            throw e;
        }
    }
    
    /**
     * Returns the named attributes of the <tt>node</tt> passed in.
     * 
     * @param node  the node in the DOM containing the attributes.
     * @returns a Properties object containing the attributes of the node.
     */
    protected static Properties getElementAttributes(Node node){
        NamedNodeMap nodeMap = node.getAttributes();
        int size = nodeMap.getLength();
        Properties attributes = new Properties();
        for(int i = 0; i < size; i++){
            node = nodeMap.item(i);
            attributes.setProperty(node.getNodeName(), node.getNodeValue());
        }
        return attributes;
    }

    /**
     * Returns the child element of the <tt>node</tt> passed in that matches the element name passed in.
     * 
     * @param node  the node in the DOM containing the PCDATA.
     * @param childName  the element name of the desired child element as defined in the DTD.
     * @returns the desired child element. OR null if the child element is not present
     */
    protected static Node getChildElement(Node node, String childName) throws OpenEJBException{

        NodeList list = node.getChildNodes();
        Node child = null;

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(childName))
                    return child;
            }
        }
        return null;
    }

    /**
     * Returns the child elements of the <tt>node</tt> passed in that match the element name passed in.
     * 
     * @param node  the node in the DOM containing the PCDATA.
     * @param childName  the element name of the desired child element as defined in the DTD.
     * @returns an array of <tt>Node</tt> containing all the desired child elements.
     */
    protected static Node[] getChildElements(Node node, String childName) throws OpenEJBException{

        NodeList list = node.getChildNodes();
        Node child = null;
        Vector tmp = new Vector();

        for (int i=0; i< list.getLength(); i++){
            child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)child;
                if (element.getTagName().equals(childName)){
                    tmp.addElement(element);
                }
            }
        }

        Node[] children = new Node[tmp.size()];
        tmp.copyInto(children);
        return children;
    }

}