/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/


/**
 * Class AssemblyDescriptorDescriptor.
 *
 * @version $Revision$ $Date$
 */
public class AssemblyDescriptorDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field nsPrefix
     */
    private java.lang.String nsPrefix;

    /**
     * Field nsURI
     */
    private java.lang.String nsURI;

    /**
     * Field xmlName
     */
    private java.lang.String xmlName;

    /**
     * Field identity
     */
    private org.exolab.castor.xml.XMLFieldDescriptor identity;


    //----------------/
    //- Constructors -/
    //----------------/

    public AssemblyDescriptorDescriptor() {
        super();
        nsURI = "http://www.openejb.org/ejb-jar/1.1";
        xmlName = "assembly-descriptor";
        
        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;
        //-- initialize attribute descriptors
        
        //-- _id
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_id", "id", org.exolab.castor.xml.NodeType.Attribute);
        this.identity = desc;
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getId();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.setId((java.lang.String) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new java.lang.String();
            }
        });
        desc.setHandler(handler);
        addFieldDescriptor(desc);
        
        //-- validation code for: _id
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- initialize element descriptors
        
        //-- _securityRoleList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.apache.openejb.config.ejb11.SecurityRole.class, "_securityRoleList", "security-role", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getSecurityRole();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.addSecurityRole((org.apache.openejb.config.ejb11.SecurityRole) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.apache.openejb.config.ejb11.SecurityRole();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _securityRoleList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _methodPermissionList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.apache.openejb.config.ejb11.MethodPermission.class, "_methodPermissionList", "method-permission", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getMethodPermission();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.addMethodPermission((org.apache.openejb.config.ejb11.MethodPermission) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.apache.openejb.config.ejb11.MethodPermission();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _methodPermissionList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _containerTransactionList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.apache.openejb.config.ejb11.ContainerTransaction.class, "_containerTransactionList", "container-transaction", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getContainerTransaction();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.addContainerTransaction((org.apache.openejb.config.ejb11.ContainerTransaction) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.apache.openejb.config.ejb11.ContainerTransaction();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _containerTransactionList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
    } //-- org.apache.openejb.config.ejb11.AssemblyDescriptorDescriptor()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method getAccessMode
     */
    public org.exolab.castor.mapping.AccessMode getAccessMode() {
        return null;
    } //-- org.exolab.castor.mapping.AccessMode getAccessMode() 

    /**
     * Method getExtends
     */
    public org.exolab.castor.mapping.ClassDescriptor getExtends() {
        return null;
    } //-- org.exolab.castor.mapping.ClassDescriptor getExtends() 

    /**
     * Method getIdentity
     */
    public org.exolab.castor.mapping.FieldDescriptor getIdentity() {
        return identity;
    } //-- org.exolab.castor.mapping.FieldDescriptor getIdentity() 

    /**
     * Method getJavaClass
     */
    public java.lang.Class getJavaClass() {
        return org.apache.openejb.config.ejb11.AssemblyDescriptor.class;
    } //-- java.lang.Class getJavaClass() 

    /**
     * Method getNameSpacePrefix
     */
    public java.lang.String getNameSpacePrefix() {
        return nsPrefix;
    } //-- java.lang.String getNameSpacePrefix() 

    /**
     * Method getNameSpaceURI
     */
    public java.lang.String getNameSpaceURI() {
        return nsURI;
    } //-- java.lang.String getNameSpaceURI() 

    /**
     * Method getValidator
     */
    public org.exolab.castor.xml.TypeValidator getValidator() {
        return this;
    } //-- org.exolab.castor.xml.TypeValidator getValidator() 

    /**
     * Method getXMLName
     */
    public java.lang.String getXMLName() {
        return xmlName;
    } //-- java.lang.String getXMLName() 

}
