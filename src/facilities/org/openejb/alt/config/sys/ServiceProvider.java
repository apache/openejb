/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision$ $Date$
**/
public class ServiceProvider implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _providerType;

    private java.lang.String _displayName;

    private java.lang.String _description;

    private java.lang.String _className;

    /**
     * internal content storage
    **/
    private java.lang.String _content = "";

    private PropertiesFile _propertiesFile;

    private Lookup _lookup;


      //----------------/
     //- Constructors -/
    //----------------/

    public ServiceProvider() {
        super();
    } //-- org.openejb.alt.config.sys.ServiceProvider()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getClassName()
    {
        return this._className;
    } //-- java.lang.String getClassName() 

    /**
    **/
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
    **/
    public java.lang.String getDisplayName()
    {
        return this._displayName;
    } //-- java.lang.String getDisplayName() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public Lookup getLookup()
    {
        return this._lookup;
    } //-- Lookup getLookup() 

    /**
    **/
    public PropertiesFile getPropertiesFile()
    {
        return this._propertiesFile;
    } //-- PropertiesFile getPropertiesFile() 

    /**
    **/
    public java.lang.String getProviderType()
    {
        return this._providerType;
    } //-- java.lang.String getProviderType() 

    /**
    **/
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
     * 
     * @param out
    **/
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * 
     * @param handler
    **/
    public void marshal(org.xml.sax.DocumentHandler handler)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.DocumentHandler) 

    /**
     * 
     * @param _className
    **/
    public void setClassName(java.lang.String _className)
    {
        this._className = _className;
    } //-- void setClassName(java.lang.String) 

    /**
     * 
     * @param _content
    **/
    public void setContent(java.lang.String _content)
    {
        this._content = _content;
    } //-- void setContent(java.lang.String) 

    /**
     * 
     * @param _description
    **/
    public void setDescription(java.lang.String _description)
    {
        this._description = _description;
    } //-- void setDescription(java.lang.String) 

    /**
     * 
     * @param _displayName
    **/
    public void setDisplayName(java.lang.String _displayName)
    {
        this._displayName = _displayName;
    } //-- void setDisplayName(java.lang.String) 

    /**
     * 
     * @param _id
    **/
    public void setId(java.lang.String _id)
    {
        this._id = _id;
    } //-- void setId(java.lang.String) 

    /**
     * 
     * @param _lookup
    **/
    public void setLookup(Lookup _lookup)
    {
        this._lookup = _lookup;
    } //-- void setLookup(Lookup) 

    /**
     * 
     * @param _propertiesFile
    **/
    public void setPropertiesFile(PropertiesFile _propertiesFile)
    {
        this._propertiesFile = _propertiesFile;
    } //-- void setPropertiesFile(PropertiesFile) 

    /**
     * 
     * @param _providerType
    **/
    public void setProviderType(java.lang.String _providerType)
    {
        this._providerType = _providerType;
    } //-- void setProviderType(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.sys.ServiceProvider unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.ServiceProvider) Unmarshaller.unmarshal(org.openejb.alt.config.sys.ServiceProvider.class, reader);
    } //-- org.openejb.alt.config.sys.ServiceProvider unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
