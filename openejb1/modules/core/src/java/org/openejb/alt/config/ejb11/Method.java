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
public class Method implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _ejbName;

    private java.lang.String _methodIntf;

    private java.lang.String _methodName;

    private MethodParams _methodParams;


      //----------------/
     //- Constructors -/
    //----------------/

    public Method() {
        super();
    } //-- org.openejb.alt.config.ejb11.Method()


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
    public java.lang.String getEjbName()
    {
        return this._ejbName;
    } //-- java.lang.String getEjbName() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getMethodIntf()
    {
        return this._methodIntf;
    } //-- java.lang.String getMethodIntf() 

    /**
    **/
    public java.lang.String getMethodName()
    {
        return this._methodName;
    } //-- java.lang.String getMethodName() 

    /**
    **/
    public MethodParams getMethodParams()
    {
        return this._methodParams;
    } //-- MethodParams getMethodParams() 

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
     * @param _ejbName
    **/
    public void setEjbName(java.lang.String _ejbName)
    {
        this._ejbName = _ejbName;
    } //-- void setEjbName(java.lang.String) 

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
     * @param _methodIntf
    **/
    public void setMethodIntf(java.lang.String _methodIntf)
    {
        this._methodIntf = _methodIntf;
    } //-- void setMethodIntf(java.lang.String) 

    /**
     * 
     * @param _methodName
    **/
    public void setMethodName(java.lang.String _methodName)
    {
        this._methodName = _methodName;
    } //-- void setMethodName(java.lang.String) 

    /**
     * 
     * @param _methodParams
    **/
    public void setMethodParams(MethodParams _methodParams)
    {
        this._methodParams = _methodParams;
    } //-- void setMethodParams(MethodParams) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.Method unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.Method) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.Method.class, reader);
    } //-- org.openejb.alt.config.ejb11.Method unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
