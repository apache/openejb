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
public class SecurityAreaMap implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private SecurityMapFile _securityMapFile;

    private SecurityMapLookup _securityMapLookup;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityAreaMap() {
        super();
    } //-- org.opene.security.castor.security.SecurityAreaMap()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public SecurityMapFile getSecurityMapFile()
    {
        return this._securityMapFile;
    } //-- SecurityMapFile getSecurityMapFile() 

    /**
    **/
    public SecurityMapLookup getSecurityMapLookup()
    {
        return this._securityMapLookup;
    } //-- SecurityMapLookup getSecurityMapLookup() 

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
     * @param securityMapFile
    **/
    public void setSecurityMapFile(SecurityMapFile securityMapFile)
    {
        this._securityMapFile = securityMapFile;
    } //-- void setSecurityMapFile(SecurityMapFile) 

    /**
     * 
     * @param securityMapLookup
    **/
    public void setSecurityMapLookup(SecurityMapLookup securityMapLookup)
    {
        this._securityMapLookup = securityMapLookup;
    } //-- void setSecurityMapLookup(SecurityMapLookup) 

    /**
     * 
     * @param reader
    **/
    public static org.opene.security.castor.security.SecurityAreaMap unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opene.security.castor.security.SecurityAreaMap) Unmarshaller.unmarshal(org.opene.security.castor.security.SecurityAreaMap.class, reader);
    } //-- org.opene.security.castor.security.SecurityAreaMap unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
