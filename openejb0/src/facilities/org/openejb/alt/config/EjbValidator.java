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
            super(org.openejb.util.Messages.format(message, args));
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
