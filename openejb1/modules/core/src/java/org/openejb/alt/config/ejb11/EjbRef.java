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
public class EjbRef implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _ejbRefName;

    private java.lang.String _ejbRefType;

    private java.lang.String _home;

    private java.lang.String _remote;

    private java.lang.String _ejbLink;


      //----------------/
     //- Constructors -/
    //----------------/

    public EjbRef() {
        super();
    } //-- org.openejb.alt.config.ejb11.EjbRef()


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
    public java.lang.String getEjbLink()
    {
        return this._ejbLink;
    } //-- java.lang.String getEjbLink() 

    /**
    **/
    public java.lang.String getEjbRefName()
    {
        return this._ejbRefName;
    } //-- java.lang.String getEjbRefName() 

    /**
    **/
    public java.lang.String getEjbRefType()
    {
        return this._ejbRefType;
    } //-- java.lang.String getEjbRefType() 

    /**
    **/
    public java.lang.String getHome()
    {
        return this._home;
    } //-- java.lang.String getHome() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getRemote()
    {
        return this._remote;
    } //-- java.lang.String getRemote() 

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
     * @param _ejbLink
    **/
    public void setEjbLink(java.lang.String _ejbLink)
    {
        this._ejbLink = _ejbLink;
    } //-- void setEjbLink(java.lang.String) 

    /**
     * 
     * @param _ejbRefName
    **/
    public void setEjbRefName(java.lang.String _ejbRefName)
    {
        this._ejbRefName = _ejbRefName;
    } //-- void setEjbRefName(java.lang.String) 

    /**
     * 
     * @param _ejbRefType
    **/
    public void setEjbRefType(java.lang.String _ejbRefType)
    {
        this._ejbRefType = _ejbRefType;
    } //-- void setEjbRefType(java.lang.String) 

    /**
     * 
     * @param _home
    **/
    public void setHome(java.lang.String _home)
    {
        this._home = _home;
    } //-- void setHome(java.lang.String) 

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
     * @param _remote
    **/
    public void setRemote(java.lang.String _remote)
    {
        this._remote = _remote;
    } //-- void setRemote(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.EjbRef unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbRef) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbRef.class, reader);
    } //-- org.openejb.alt.config.ejb11.EjbRef unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
