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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
package org.openejb.alt.config;

import org.openejb.alt.config.sys.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.OpenEJBException;
import org.openejb.util.Messages;
import org.openejb.util.FileUtils;
import org.openejb.util.SafeToolkit;
import java.util.Enumeration;
import java.util.Vector;
import java.io.PrintStream;
import java.io.DataInputStream;
import java.io.File;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbValidator {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    private DataInputStream in;
    private PrintStream out;
    private Openejb config;
    private String configFile;
    private boolean configChanged;
    private boolean autoAssign;
    private Container[] containers;
    private Connector[] resources;

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public EjbValidator() throws OpenEJBException {
    }

    public void validateJar(String jarLocation) throws OpenEJBException{
        try {
            // TODO:1: Finish this constructor
            EjbJar ejbJar = null;
            validateJar( ejbJar, jarLocation);
        } catch ( Exception e ) {
            // TODO: Better exception handling.
            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }
    }

    public void validateJar(EjbJar ejbJar, String jarLocation) throws OpenEJBException{
        try {
            
            Bean[] beans = getBeans(ejbJar);

            //Validate all classes are present
            

            //Validate classes are the correct type
            //Validate ejb references
            //Validate resource references
            //Validate security references
            
        } catch ( Exception e ) {
            // TODO: Better exception handling.
            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }

    }

    
    /*------------------------------------------------------*/
    /*    Static methods                                    */
    /*------------------------------------------------------*/

    public static void main(String args[]) {
    }


    /*------------------------------------------------------*/
    /*    Methods for collecting beans                      */
    /*------------------------------------------------------*/
    private Bean[] getBeans(EjbJar jar) {
        Enumeration beanItemList = jar.getEnterpriseBeans().enumerateEnterpriseBeansItem();
        Vector beanList = new Vector();
        while ( beanItemList.hasMoreElements() ) {
            EnterpriseBeansItem item = (EnterpriseBeansItem)beanItemList.nextElement();
            if ( item.getEntity() == null ) {
                beanList.add(new SessionBean(item.getSession()));
            } else {
                beanList.add(new EntityBean(item.getEntity()));
            }
        }
        Bean[] beans = new Bean[beanList.size()];
        beanList.copyInto(beans);
        return beans;
    }




    /*------------------------------------------------------*/
    /*    Inner Classes for easy bean collections           */
    /*------------------------------------------------------*/
    
    interface Bean {

        public final String BMP_ENTITY = "BMP_ENTITY";
        public final String CMP_ENTITY = "CMP_ENTITY";
        public final String STATEFUL   = "STATEFUL";
        public final String STATELESS  = "STATELESS";


        public String getType();
        
        public Object getBean();
        
        public String getEjbName();
        public String getEjbClass();
        public String getHome();
        public String getRemote();
        
        public EjbRef[] getEjbRef();
        public EnvEntry[] getEnvEntry();
        public ResourceRef[] getResourceRef();
        public SecurityRoleRef[] getSecurityRoleRef();

    }
    

    class EntityBean implements Bean {
        
        Entity bean;
        String type;
        
        EntityBean(Entity bean) {
            this.bean = bean;
            if ( bean.getPersistenceType().equals("Container") ) {
                type = CMP_ENTITY;
            } else {
                type = BMP_ENTITY;
            }
        }

        public String getType() {
            return type;
        }
        
        public Object getBean() {
            return bean;
        }
    
        public String getEjbName(){
            return bean.getEjbName();
        }

        public String getEjbClass(){
            return bean.getEjbClass();
        }

        public String getHome(){
            return bean.getHome();
        }

        public String getRemote(){
            return bean.getRemote();
        }


        public EjbRef[] getEjbRef(){
            return bean.getEjbRef();
        }

        public EnvEntry[] getEnvEntry(){
            return bean.getEnvEntry();
        }

        public ResourceRef[] getResourceRef(){
            return bean.getResourceRef();
        }

        public SecurityRoleRef[] getSecurityRoleRef(){
            return bean.getSecurityRoleRef();
        }

    }

    class SessionBean implements Bean {

        Session bean;
        String type;

        SessionBean(Session bean) {
            this.bean = bean;
            if ( bean.getSessionType().equals("Stateful") ) {
                type = STATEFUL;
            } else {
                type = STATELESS;
            }
        }

        public String getType() {
            return type;
        }
        
        public Object getBean() {
            return bean;
        }
    
        public String getEjbName(){
            return bean.getEjbName();
        }

        public String getEjbClass(){
            return bean.getEjbClass();
        }

        public String getHome(){
            return bean.getHome();
        }

        public String getRemote(){
            return bean.getRemote();
        }

        public EjbRef[] getEjbRef(){
            return bean.getEjbRef();
        }

        public EnvEntry[] getEnvEntry(){
            return bean.getEnvEntry();
        }

        public ResourceRef[] getResourceRef(){
            return bean.getResourceRef();
        }

        public SecurityRoleRef[] getSecurityRoleRef(){
            return bean.getSecurityRoleRef();
        }
    }

    interface ValidationRule {

        public void validate(Bean[] beans, EjbJar jar, String jarLocation) throws ValidationException;
    }

    class ValidationException extends Exception{
        public ValidationException( String message, Object[] args){
            super(_messages.format(message, args));
        }
    }
    
    class HasEjbClasses implements ValidationRule{
        public void validate(Bean[] beans, EjbJar jar, String jarLocation) throws ValidationException {

            for (int i=0; i < beans.length; i++){
                Bean b = beans[i];
                checkForClass(b, b.getEjbClass(), "<ejb-class>", jarLocation);
                checkForClass(b, b.getHome(), "<home>", jarLocation);
                checkForClass(b, b.getRemote(), "<remote>", jarLocation);
            }

        }

        private void checkForClass(Bean b, String clazz, String type, String jarLocation) throws ValidationException {
            try{
                //Bean class
                if (b.getClass() == null) {
                    //throw new ValidationException("ejb.validate.1.010", 
                }
                SafeToolkit.loadClass(b.getEjbClass(), jarLocation);

            } catch (OpenEJBException e){
                //throw new ValidationException("ejb.validate.1.020", 
            }
        }

    }
    
    class EjbClassesAreRightType implements ValidationRule{
        public void validate(Bean[] beans, EjbJar jar, String jarLocation) throws ValidationException {
        }
    }
}
