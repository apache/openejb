/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.FieldValidator;
import org.exolab.castor.xml.NodeType;
import org.exolab.castor.xml.XMLFieldHandler;
import org.exolab.castor.xml.util.XMLFieldDescriptorImpl;

/**
 * 
 * @version $Revision$ $Date$
**/
public class EnterpriseBeansItemDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;


      //----------------/
     //- Constructors -/
    //----------------/

    public EnterpriseBeansItemDescriptor() {
        super();
        nsURI = "http://www.openejb.org/ejb-jar/1.1";
        xmlName = "enterprise-beans";
        XMLFieldDescriptorImpl  desc           = null;
        XMLFieldHandler         handler        = null;
        FieldValidator          fieldValidator = null;
        //-- initialize attribute descriptors
        
        //-- _id
        desc = new XMLFieldDescriptorImpl(java.lang.String.class, "_id", "id", NodeType.Attribute);
        this.identity = desc;
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                return target.getId();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                    target.setId( (java.lang.String) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new java.lang.String();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        addFieldDescriptor(desc);
        
        //-- validation code for: _id
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- initialize element descriptors
        
        //-- _session
        desc = new XMLFieldDescriptorImpl(Session.class, "_session", "session", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                return target.getSession();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                    target.setSession( (Session) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new Session();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _session
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _entity
        desc = new XMLFieldDescriptorImpl(Entity.class, "_entity", "entity", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                return target.getEntity();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                    target.setEntity( (Entity) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new Entity();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _entity
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeansItemDescriptor()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public org.exolab.castor.mapping.AccessMode getAccessMode()
    {
        return null;
    } //-- org.exolab.castor.mapping.AccessMode getAccessMode() 

    /**
    **/
    public org.exolab.castor.mapping.ClassDescriptor getExtends()
    {
        return null;
    } //-- org.exolab.castor.mapping.ClassDescriptor getExtends() 

    /**
    **/
    public org.exolab.castor.mapping.FieldDescriptor getIdentity()
    {
        return identity;
    } //-- org.exolab.castor.mapping.FieldDescriptor getIdentity() 

    /**
    **/
    public java.lang.Class getJavaClass()
    {
        return org.openejb.alt.config.ejb11.EnterpriseBeansItem.class;
    } //-- java.lang.Class getJavaClass() 

    /**
    **/
    public java.lang.String getNameSpacePrefix()
    {
        return nsPrefix;
    } //-- java.lang.String getNameSpacePrefix() 

    /**
    **/
    public java.lang.String getNameSpaceURI()
    {
        return nsURI;
    } //-- java.lang.String getNameSpaceURI() 

    /**
    **/
    public org.exolab.castor.xml.TypeValidator getValidator()
    {
        return this;
    } //-- org.exolab.castor.xml.TypeValidator getValidator() 

    /**
    **/
    public java.lang.String getXMLName()
    {
        return xmlName;
    } //-- java.lang.String getXMLName() 

}
