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
public class EjbJar implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _displayName;

    private java.lang.String _smallIcon;

    private java.lang.String _largeIcon;

    private EnterpriseBeans _enterpriseBeans;

    private AssemblyDescriptor _assemblyDescriptor;

    private java.lang.String _ejbClientJar;


      //----------------/
     //- Constructors -/
    //----------------/

    public EjbJar() {
        super();
    } //-- org.openejb.alt.config.ejb11.EjbJar()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public AssemblyDescriptor getAssemblyDescriptor()
    {
        return this._assemblyDescriptor;
    } //-- AssemblyDescriptor getAssemblyDescriptor() 

    /**
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
    **/
    public java.lang.String getDisplayName()
    {
        return this._displayName;
    } //-- java.lang.String getDisplayName() 

    /**
    **/
    public java.lang.String getEjbClientJar()
    {
        return this._ejbClientJar;
    } //-- java.lang.String getEjbClientJar() 

    /**
    **/
    public EnterpriseBeans getEnterpriseBeans()
    {
        return this._enterpriseBeans;
    } //-- EnterpriseBeans getEnterpriseBeans() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getLargeIcon()
    {
        return this._largeIcon;
    } //-- java.lang.String getLargeIcon() 

    /**
    **/
    public java.lang.String getSmallIcon()
    {
        return this._smallIcon;
    } //-- java.lang.String getSmallIcon() 

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
     * @param _assemblyDescriptor
    **/
    public void setAssemblyDescriptor(AssemblyDescriptor _assemblyDescriptor)
    {
        this._assemblyDescriptor = _assemblyDescriptor;
    } //-- void setAssemblyDescriptor(AssemblyDescriptor) 

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
     * @param _displayName
    **/
    public void setDisplayName(java.lang.String _displayName)
    {
        this._displayName = _displayName;
    } //-- void setDisplayName(java.lang.String) 

    /**
     * 
     * @param _ejbClientJar
    **/
    public void setEjbClientJar(java.lang.String _ejbClientJar)
    {
        this._ejbClientJar = _ejbClientJar;
    } //-- void setEjbClientJar(java.lang.String) 

    /**
     * 
     * @param _enterpriseBeans
    **/
    public void setEnterpriseBeans(EnterpriseBeans _enterpriseBeans)
    {
        this._enterpriseBeans = _enterpriseBeans;
    } //-- void setEnterpriseBeans(EnterpriseBeans) 

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
     * @param _largeIcon
    **/
    public void setLargeIcon(java.lang.String _largeIcon)
    {
        this._largeIcon = _largeIcon;
    } //-- void setLargeIcon(java.lang.String) 

    /**
     * 
     * @param _smallIcon
    **/
    public void setSmallIcon(java.lang.String _smallIcon)
    {
        this._smallIcon = _smallIcon;
    } //-- void setSmallIcon(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.EjbJar unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbJar) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbJar.class, reader);
    } //-- org.openejb.alt.config.ejb11.EjbJar unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
