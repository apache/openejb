/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.FieldValidator;
import org.exolab.castor.xml.NodeType;
import org.exolab.castor.xml.XMLFieldHandler;
import org.exolab.castor.xml.util.XMLFieldDescriptorImpl;
import org.exolab.castor.xml.validators.StringValidator;

/**
 * 
 * @version $Revision$ $Date$
**/
public class OpenejbDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


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

    public OpenejbDescriptor() {
        super();
        nsURI = "http://www.openejb.org/System/Configuration";
        xmlName = "openejb";
        XMLFieldDescriptorImpl  desc           = null;
        XMLFieldHandler         handler        = null;
        FieldValidator          fieldValidator = null;
        
        //-- set grouping compositor
        setCompositorAsSequence();
        //-- _content
        desc = new XMLFieldDescriptorImpl(java.lang.String.class, "_content", "PCDATA", NodeType.Text);
        desc.setImmutable(true);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getContent();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.setContent( (java.lang.String) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return null;
            }
        } );
        desc.setHandler(handler);
        addFieldDescriptor(desc);
        
        //-- validation code for: _content
        fieldValidator = new FieldValidator();
        { //-- local scope
            StringValidator sv = new StringValidator();
            sv.setWhiteSpace("preserve");
            fieldValidator.setValidator(sv);
        }
        desc.setValidator(fieldValidator);
        
        //-- initialize attribute descriptors
        
        //-- initialize element descriptors
        
        //-- _containerList
        desc = new XMLFieldDescriptorImpl(Container.class, "_containerList", "Container", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getContainer();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.addContainer( (Container) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new Container();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setRequired(true);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _containerList
        fieldValidator = new FieldValidator();
        fieldValidator.setMinOccurs(1);
        desc.setValidator(fieldValidator);
        
        //-- _jndiProviderList
        desc = new XMLFieldDescriptorImpl(JndiProvider.class, "_jndiProviderList", "JndiProvider", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getJndiProvider();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.addJndiProvider( (JndiProvider) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new JndiProvider();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _jndiProviderList
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _securityService
        desc = new XMLFieldDescriptorImpl(SecurityService.class, "_securityService", "SecurityService", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getSecurityService();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.setSecurityService( (SecurityService) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new SecurityService();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _securityService
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _transactionService
        desc = new XMLFieldDescriptorImpl(TransactionService.class, "_transactionService", "TransactionService", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getTransactionService();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.setTransactionService( (TransactionService) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new TransactionService();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _transactionService
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _connectionManager
        desc = new XMLFieldDescriptorImpl(ConnectionManager.class, "_connectionManager", "ConnectionManager", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getConnectionManager();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.setConnectionManager( (ConnectionManager) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new ConnectionManager();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _connectionManager
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);

        //-- _proxyFactory
        desc = new XMLFieldDescriptorImpl(ProxyFactory.class, "_proxyFactory", "ProxyFactory", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getProxyFactory();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.setProxyFactory( (ProxyFactory) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new ProxyFactory();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _proxyFactory
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _connectorList
        desc = new XMLFieldDescriptorImpl(Connector.class, "_connectorList", "Connector", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getConnector();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.addConnector( (Connector) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new Connector();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _connectorList
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _resourceList
        desc = new XMLFieldDescriptorImpl(Resource.class, "_resourceList", "Resource", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getResource();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.addResource( (Resource) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new Resource();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _resourceList
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- _deploymentsList
        desc = new XMLFieldDescriptorImpl(Deployments.class, "_deploymentsList", "Deployments", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Openejb target = (Openejb) object;
                return target.getDeployments();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Openejb target = (Openejb) object;
                    target.addDeployments( (Deployments) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new Deployments();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/System/Configuration");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _deploymentsList
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
    } //-- org.openejb.alt.config.sys.OpenejbDescriptor()


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
        return org.openejb.alt.config.sys.Openejb.class;
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
