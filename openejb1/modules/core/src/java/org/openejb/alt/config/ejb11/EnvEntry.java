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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision$ $Date$
**/
public class EnvEntry implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _envEntryName;

    private java.lang.String _envEntryType;

    private java.lang.String _envEntryValue;


      //----------------/
     //- Constructors -/
    //----------------/

    public EnvEntry() {
        super();
    } //-- org.openejb.alt.config.ejb11.EnvEntry()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
    **/
    public java.lang.String getEnvEntryName()
    {
        return this._envEntryName;
    } //-- java.lang.String getEnvEntryName() 

    /**
    **/
    public java.lang.String getEnvEntryType()
    {
        return this._envEntryType;
    } //-- java.lang.String getEnvEntryType() 

    /**
    **/
    public java.lang.String getEnvEntryValue()
    {
        return this._envEntryValue;
    } //-- java.lang.String getEnvEntryValue() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

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
     * @param _description
    **/
    public void setDescription(java.lang.String _description)
    {
        this._description = _description;
    } //-- void setDescription(java.lang.String) 

    /**
     * 
     * @param _envEntryName
    **/
    public void setEnvEntryName(java.lang.String _envEntryName)
    {
        this._envEntryName = _envEntryName;
    } //-- void setEnvEntryName(java.lang.String) 

    /**
     * 
     * @param _envEntryType
    **/
    public void setEnvEntryType(java.lang.String _envEntryType)
    {
        this._envEntryType = _envEntryType;
    } //-- void setEnvEntryType(java.lang.String) 

    /**
     * 
     * @param _envEntryValue
    **/
    public void setEnvEntryValue(java.lang.String _envEntryValue)
    {
        this._envEntryValue = _envEntryValue;
    } //-- void setEnvEntryValue(java.lang.String) 

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
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.EnvEntry unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EnvEntry) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EnvEntry.class, reader);
    } //-- org.openejb.alt.config.ejb11.EnvEntry unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
