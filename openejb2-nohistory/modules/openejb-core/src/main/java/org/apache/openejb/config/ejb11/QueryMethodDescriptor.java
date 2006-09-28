/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: QueryMethodDescriptor.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.validators.StringValidator;

/**
 * Class QueryMethodDescriptor.
 *
 * @version $Revision$ $Date$
 */
public class QueryMethodDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


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

    public QueryMethodDescriptor() {
        super();
        nsURI = "http://www.openejb.org/openejb-jar/1.1";
        xmlName = "query-method";
        
        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;
        //-- initialize attribute descriptors
        
        //-- initialize element descriptors
        
        //-- _methodName
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_methodName", "method-name", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                QueryMethod target = (QueryMethod) object;
                return target.getMethodName();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    QueryMethod target = (QueryMethod) object;
                    target.setMethodName((java.lang.String) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _methodName
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);
        //-- _methodParams
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.apache.openejb.config.ejb11.MethodParams.class, "_methodParams", "method-params", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                QueryMethod target = (QueryMethod) object;
                return target.getMethodParams();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    QueryMethod target = (QueryMethod) object;
                    target.setMethodParams((org.apache.openejb.config.ejb11.MethodParams) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.apache.openejb.config.ejb11.MethodParams();
            }
        });
        desc.setHandler(handler);
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _methodParams
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
    } //-- org.openejb.config.ejb11.QueryMethodDescriptor()


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
        return org.apache.openejb.config.ejb11.QueryMethod.class;
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
