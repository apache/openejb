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
public class Lookup implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _jndiName;

    private java.lang.String _jndiProviderId;


      //----------------/
     //- Constructors -/
    //----------------/

    public Lookup() {
        super();
    } //-- org.openejb.alt.config.sys.Lookup()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getJndiName()
    {
        return this._jndiName;
    } //-- java.lang.String getJndiName() 

    /**
    **/
    public java.lang.String getJndiProviderId()
    {
        return this._jndiProviderId;
    } //-- java.lang.String getJndiProviderId() 

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
     * @param _jndiName
    **/
    public void setJndiName(java.lang.String _jndiName)
    {
        this._jndiName = _jndiName;
    } //-- void setJndiName(java.lang.String) 

    /**
     * 
     * @param _jndiProviderId
    **/
    public void setJndiProviderId(java.lang.String _jndiProviderId)
    {
        this._jndiProviderId = _jndiProviderId;
    } //-- void setJndiProviderId(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.sys.Lookup unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.Lookup) Unmarshaller.unmarshal(org.openejb.alt.config.sys.Lookup.class, reader);
    } //-- org.openejb.alt.config.sys.Lookup unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
