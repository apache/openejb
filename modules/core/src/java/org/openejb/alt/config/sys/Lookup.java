/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

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
     * Returns the value of field 'jndiName'.
     * @return the value of field 'jndiName'.
    **/
    public java.lang.String getJndiName()
    {
        return this._jndiName;
    } //-- java.lang.String getJndiName() 

    /**
     * Returns the value of field 'jndiProviderId'.
     * @return the value of field 'jndiProviderId'.
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
     * Sets the value of field 'jndiName'.
     * @param jndiName the value of field 'jndiName'.
    **/
    public void setJndiName(java.lang.String jndiName)
    {
        this._jndiName = jndiName;
    } //-- void setJndiName(java.lang.String) 

    /**
     * Sets the value of field 'jndiProviderId'.
     * @param jndiProviderId the value of field 'jndiProviderId'.
    **/
    public void setJndiProviderId(java.lang.String jndiProviderId)
    {
        this._jndiProviderId = jndiProviderId;
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
