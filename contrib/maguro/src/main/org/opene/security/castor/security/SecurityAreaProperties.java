/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3</a>, using an
 * XML Schema.
 * $Id$
 */

package org.opene.security.castor.security;

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
public class SecurityAreaProperties implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private PropertiesFile _propertiesFile;

    private Lookup _lookup;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityAreaProperties() {
        super();
    } //-- org.opene.security.castor.security.SecurityAreaProperties()


      //-----------/
     //- Methods -/
    //-----------/

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
     * @param lookup
    **/
    public void setLookup(Lookup lookup)
    {
        this._lookup = lookup;
    } //-- void setLookup(Lookup) 

    /**
     * 
     * @param propertiesFile
    **/
    public void setPropertiesFile(PropertiesFile propertiesFile)
    {
        this._propertiesFile = propertiesFile;
    } //-- void setPropertiesFile(PropertiesFile) 

    /**
     * 
     * @param reader
    **/
    public static org.opene.security.castor.security.SecurityAreaProperties unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opene.security.castor.security.SecurityAreaProperties) Unmarshaller.unmarshal(org.opene.security.castor.security.SecurityAreaProperties.class, reader);
    } //-- org.opene.security.castor.security.SecurityAreaProperties unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
