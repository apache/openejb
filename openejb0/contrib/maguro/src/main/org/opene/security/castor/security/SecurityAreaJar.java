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
public class SecurityAreaJar implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _jar;

    /**
     * internal content storage
    **/
    private java.lang.String _content = "";

    private SecurityAreaProperties _securityAreaProperties;

    private SecurityAreaMap _securityAreaMap;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityAreaJar() {
        super();
    } //-- org.opene.security.castor.security.SecurityAreaJar()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getJar()
    {
        return this._jar;
    } //-- java.lang.String getJar() 

    /**
    **/
    public SecurityAreaMap getSecurityAreaMap()
    {
        return this._securityAreaMap;
    } //-- SecurityAreaMap getSecurityAreaMap() 

    /**
    **/
    public SecurityAreaProperties getSecurityAreaProperties()
    {
        return this._securityAreaProperties;
    } //-- SecurityAreaProperties getSecurityAreaProperties() 

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
     * @param content
    **/
    public void setContent(java.lang.String content)
    {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * 
     * @param id
    **/
    public void setId(java.lang.String id)
    {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * 
     * @param jar
    **/
    public void setJar(java.lang.String jar)
    {
        this._jar = jar;
    } //-- void setJar(java.lang.String) 

    /**
     * 
     * @param securityAreaMap
    **/
    public void setSecurityAreaMap(SecurityAreaMap securityAreaMap)
    {
        this._securityAreaMap = securityAreaMap;
    } //-- void setSecurityAreaMap(SecurityAreaMap) 

    /**
     * 
     * @param securityAreaProperties
    **/
    public void setSecurityAreaProperties(SecurityAreaProperties securityAreaProperties)
    {
        this._securityAreaProperties = securityAreaProperties;
    } //-- void setSecurityAreaProperties(SecurityAreaProperties) 

    /**
     * 
     * @param reader
    **/
    public static org.opene.security.castor.security.SecurityAreaJar unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opene.security.castor.security.SecurityAreaJar) Unmarshaller.unmarshal(org.opene.security.castor.security.SecurityAreaJar.class, reader);
    } //-- org.opene.security.castor.security.SecurityAreaJar unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
