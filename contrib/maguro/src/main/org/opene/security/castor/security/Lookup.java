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
public class Lookup implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _jndiName;

    private java.lang.String _jndiId;


      //----------------/
     //- Constructors -/
    //----------------/

    public Lookup() {
        super();
    } //-- org.opene.security.castor.security.Lookup()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getJndiId()
    {
        return this._jndiId;
    } //-- java.lang.String getJndiId() 

    /**
    **/
    public java.lang.String getJndiName()
    {
        return this._jndiName;
    } //-- java.lang.String getJndiName() 

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
     * @param jndiId
    **/
    public void setJndiId(java.lang.String jndiId)
    {
        this._jndiId = jndiId;
    } //-- void setJndiId(java.lang.String) 

    /**
     * 
     * @param jndiName
    **/
    public void setJndiName(java.lang.String jndiName)
    {
        this._jndiName = jndiName;
    } //-- void setJndiName(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.opene.security.castor.security.Lookup unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opene.security.castor.security.Lookup) Unmarshaller.unmarshal(org.opene.security.castor.security.Lookup.class, reader);
    } //-- org.opene.security.castor.security.Lookup unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
