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
import org.openejb.alt.assembler.classic.MethodInfo;
import org.w3c.dom.Node;

/**
 * A subclass of MethodInfo filled with data from an XML file.
 * 
 * Populates the member variables of MethodInfo in this classes initializeFromDOM method.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.MethodInfo
 * @see #Method.initializeFromDOM
 */
public class Method extends MethodInfo implements DomObject{

    /**
     * Represents the <tt>description</tt> element in the XML config file.
     */
    public static final String DESCRIPTION = "description";

    /**
     * Represents the <tt>ejb-deployment-id</tt> element in the XML config file.
     */
    public static final String EJB_DEPLOYMENT_ID = "ejb-deployment-id";

    /**
     * Represents the <tt>method-intf</tt> element in the XML config file.
     */
    public static final String METHOD_INTF = "method-intf";

    /**
     * Represents the <tt>method-name</tt> element in the XML config file.
     */
    public static final String METHOD_NAME = "method-name";

    /**
     * Represents the <tt>method-params</tt> element in the XML config file.
     */
    public static final String METHOD_PARAMS = "method-params";

    /**
     * Represents the <tt>method-param</tt> element in the XML config file.
     */
    public static final String METHOD_PARAM = "method-param";

    /** 
     * Parses out the values needed by this DomObject from the DOM Node passed in.
     * @see org.w3c.dom.Node
     */
    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        ejbDeploymentId = DomTools.getChildElementPCData(node, EJB_DEPLOYMENT_ID);
        methodIntf = DomTools.getChildElementPCData(node, METHOD_INTF);
        methodName = DomTools.getChildElementPCData(node, METHOD_NAME);

        Node methodParamsElement = DomTools.getChildElement(node, METHOD_PARAMS);
        if (methodParamsElement == null) methodParams = null;
        else{
            methodParams = DomTools.getChildElementsPCData(methodParamsElement, METHOD_PARAM);
          //String[] methodParamNames = DomTools.getChildElementsPCData(methodParamsElement, METHOD_PARAM);
          //methodParams = new Class[methodParamNames.length];
          //try{
          //    for (int i=0; i< methodParamNames.length; i++)
          //        methodParams[i] = getClassForParam(methodParamNames[i]);
          //        
          //        
          //}catch (Exception e) {throw new RuntimeException(e.getMessage());}
        }


    }

    public void serializeToDOM(Node node) throws OpenEJBException{}


    /**
     * Return the correct Class object. Either use forName or
     * return a primitive TYPE Class. 
     */
    private java.lang.Class getClassForParam(java.lang.String className)
      throws Exception {
       
      // Test if the name is a primitive type name
      if(className.equals("int")) {
        return java.lang.Integer.TYPE; 
      }
      else if(className.equals("double")) {
        return java.lang.Double.TYPE; 
      }
      else if(className.equals("long")) {
        return java.lang.Long.TYPE; 
      } 
      else if(className.equals("boolean")) {
        return java.lang.Boolean.TYPE; 
      } 
      else if(className.equals("float")) {
        return java.lang.Float.TYPE; 
      } 
      else if(className.equals("char")) {
        return java.lang.Character.TYPE; 
      }
      else if(className.equals("short")) {
        return java.lang.Short.TYPE; 
      }
      else if(className.equals("byte")) {
        return java.lang.Byte.TYPE; 
      }     
      else return Class.forName(className); 
                
    } 
}
