/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * Class ServiceProvider.
 * 
 * @version $Revision$ $Date$
 */
public class ServiceProvider implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _providerType
     */
    private java.lang.String _providerType;

    /**
     * Field _displayName
     */
    private java.lang.String _displayName;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _className
     */
    private java.lang.String _className;

    /**
     * internal content storage
     */
    private java.lang.String _content = "";

    /**
     * Field _propertiesFile
     */
    private org.openejb.alt.config.sys.PropertiesFile _propertiesFile;

    /**
     * Field _lookup
     */
    private org.openejb.alt.config.sys.Lookup _lookup;


      //----------------/
     //- Constructors -/
    //----------------/

    public ServiceProvider() {
        super();
        setContent("");
    } //-- org.openejb.alt.config.sys.ServiceProvider()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'className'.
     * 
     * @return the value of field 'className'.
     */
    public java.lang.String getClassName()
    {
        return this._className;
    } //-- java.lang.String getClassName() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * 
     * @return the value of field 'content'.
     */
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * Returns the value of field 'description'.
     * 
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'displayName'.
     * 
     * @return the value of field 'displayName'.
     */
    public java.lang.String getDisplayName()
    {
        return this._displayName;
    } //-- java.lang.String getDisplayName() 

    /**
     * Returns the value of field 'id'.
     * 
     * @return the value of field 'id'.
     */
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'lookup'.
     * 
     * @return the value of field 'lookup'.
     */
    public org.openejb.alt.config.sys.Lookup getLookup()
    {
        return this._lookup;
    } //-- org.openejb.alt.config.sys.Lookup getLookup() 

    /**
     * Returns the value of field 'propertiesFile'.
     * 
     * @return the value of field 'propertiesFile'.
     */
    public org.openejb.alt.config.sys.PropertiesFile getPropertiesFile()
    {
        return this._propertiesFile;
    } //-- org.openejb.alt.config.sys.PropertiesFile getPropertiesFile() 

    /**
     * Returns the value of field 'providerType'.
     * 
     * @return the value of field 'providerType'.
     */
    public java.lang.String getProviderType()
    {
        return this._providerType;
    } //-- java.lang.String getProviderType() 

    /**
     * Method isValid
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'className'.
     * 
     * @param className the value of field 'className'.
     */
    public void setClassName(java.lang.String className)
    {
        this._className = className;
    } //-- void setClassName(java.lang.String) 

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(java.lang.String content)
    {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * Sets the value of field 'description'.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'displayName'.
     * 
     * @param displayName the value of field 'displayName'.
     */
    public void setDisplayName(java.lang.String displayName)
    {
        this._displayName = displayName;
    } //-- void setDisplayName(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     * 
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id)
    {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'lookup'.
     * 
     * @param lookup the value of field 'lookup'.
     */
    public void setLookup(org.openejb.alt.config.sys.Lookup lookup)
    {
        this._lookup = lookup;
    } //-- void setLookup(org.openejb.alt.config.sys.Lookup) 

    /**
     * Sets the value of field 'propertiesFile'.
     * 
     * @param propertiesFile the value of field 'propertiesFile'.
     */
    public void setPropertiesFile(org.openejb.alt.config.sys.PropertiesFile propertiesFile)
    {
        this._propertiesFile = propertiesFile;
    } //-- void setPropertiesFile(org.openejb.alt.config.sys.PropertiesFile) 

    /**
     * Sets the value of field 'providerType'.
     * 
     * @param providerType the value of field 'providerType'.
     */
    public void setProviderType(java.lang.String providerType)
    {
        this._providerType = providerType;
    } //-- void setProviderType(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.ServiceProvider) Unmarshaller.unmarshal(org.openejb.alt.config.sys.ServiceProvider.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
