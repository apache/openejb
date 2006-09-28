/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class QueryMethod.
 *
 * @version $Revision$ $Date$
 */
public class QueryMethod implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _methodName
     */
    private java.lang.String _methodName;

    /**
     * Field _methodParams
     */
    private org.apache.openejb.config.ejb11.MethodParams _methodParams;


    //----------------/
    //- Constructors -/
    //----------------/

    public QueryMethod() {
        super();
    } //-- org.apache.openejb.config.ejb11.QueryMethod()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'methodName'.
     *
     * @return the value of field 'methodName'.
     */
    public java.lang.String getMethodName() {
        return this._methodName;
    } //-- java.lang.String getMethodName() 

    /**
     * Returns the value of field 'methodParams'.
     *
     * @return the value of field 'methodParams'.
     */
    public org.apache.openejb.config.ejb11.MethodParams getMethodParams() {
        return this._methodParams;
    } //-- org.apache.openejb.config.ejb11.MethodParams getMethodParams()

    /**
     * Method isValid
     */
    public boolean isValid() {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     *
     * @param out
     */
    public void marshal(java.io.Writer out)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     *
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
            throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'methodName'.
     *
     * @param methodName the value of field 'methodName'.
     */
    public void setMethodName(java.lang.String methodName) {
        this._methodName = methodName;
    } //-- void setMethodName(java.lang.String) 

    /**
     * Sets the value of field 'methodParams'.
     *
     * @param methodParams the value of field 'methodParams'.
     */
    public void setMethodParams(org.apache.openejb.config.ejb11.MethodParams methodParams) {
        this._methodParams = methodParams;
    } //-- void setMethodParams(org.apache.openejb.config.ejb11.MethodParams)

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.QueryMethod) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.QueryMethod.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     */
    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
