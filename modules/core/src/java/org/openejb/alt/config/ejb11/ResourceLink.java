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
public class ResourceLink implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _resRefName;

    private java.lang.String _resId;


      //----------------/
     //- Constructors -/
    //----------------/

    public ResourceLink() {
        super();
    } //-- org.openejb.alt.config.ejb11.ResourceLink()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getResId()
    {
        return this._resId;
    } //-- java.lang.String getResId() 

    /**
    **/
    public java.lang.String getResRefName()
    {
        return this._resRefName;
    } //-- java.lang.String getResRefName() 

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
     * @param _resId
    **/
    public void setResId(java.lang.String _resId)
    {
        this._resId = _resId;
    } //-- void setResId(java.lang.String) 

    /**
     * 
     * @param _resRefName
    **/
    public void setResRefName(java.lang.String _resRefName)
    {
        this._resRefName = _resRefName;
    } //-- void setResRefName(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.ResourceLink unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.ResourceLink) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.ResourceLink.class, reader);
    } //-- org.openejb.alt.config.ejb11.ResourceLink unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
