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
public class ResourceRef implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _resRefName;

    private java.lang.String _resType;

    private java.lang.String _resAuth;


      //----------------/
     //- Constructors -/
    //----------------/

    public ResourceRef() {
        super();
    } //-- org.openejb.alt.config.ejb11.ResourceRef()


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
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getResAuth()
    {
        return this._resAuth;
    } //-- java.lang.String getResAuth() 

    /**
    **/
    public java.lang.String getResRefName()
    {
        return this._resRefName;
    } //-- java.lang.String getResRefName() 

    /**
    **/
    public java.lang.String getResType()
    {
        return this._resType;
    } //-- java.lang.String getResType() 

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
     * @param _id
    **/
    public void setId(java.lang.String _id)
    {
        this._id = _id;
    } //-- void setId(java.lang.String) 

    /**
     * 
     * @param _resAuth
    **/
    public void setResAuth(java.lang.String _resAuth)
    {
        this._resAuth = _resAuth;
    } //-- void setResAuth(java.lang.String) 

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
     * @param _resType
    **/
    public void setResType(java.lang.String _resType)
    {
        this._resType = _resType;
    } //-- void setResType(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.ResourceRef unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.ResourceRef) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.ResourceRef.class, reader);
    } //-- org.openejb.alt.config.ejb11.ResourceRef unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
