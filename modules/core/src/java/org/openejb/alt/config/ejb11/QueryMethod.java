/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
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
public class QueryMethod implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _methodName;

    private MethodParams _methodParams;


      //----------------/
     //- Constructors -/
    //----------------/

    public QueryMethod() {
        super();
    } //-- org.openejb.alt.config.ejb11.QueryMethod()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'methodName'.
     * @return the value of field 'methodName'.
    **/
    public java.lang.String getMethodName()
    {
        return this._methodName;
    } //-- java.lang.String getMethodName() 

    /**
     * Returns the value of field 'methodParams'.
     * @return the value of field 'methodParams'.
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
     * Sets the value of field 'methodName'.
     * @param methodName the value of field 'methodName'.
    **/
    public void setMethodName(java.lang.String methodName)
    {
        this._methodName = methodName;
    } //-- void setMethodName(java.lang.String) 

    /**
     * Sets the value of field 'methodParams'.
     * @param methodParams the value of field 'methodParams'.
    **/
    public void setMethodParams(MethodParams methodParams)
    {
        this._methodParams = methodParams;
    } //-- void setMethodParams(MethodParams) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.QueryMethod unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.QueryMethod) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.QueryMethod.class, reader);
    } //-- org.openejb.alt.config.ejb11.QueryMethod unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
