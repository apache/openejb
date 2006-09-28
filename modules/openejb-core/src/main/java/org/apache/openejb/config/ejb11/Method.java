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
 * Class Method.
 *
 * @version $Revision$ $Date$
 */
public class Method implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _ejbName
     */
    private java.lang.String _ejbName;

    /**
     * Field _methodIntf
     */
    private java.lang.String _methodIntf;

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

    public Method() {
        super();
    } //-- org.apache.openejb.config.ejb11.Method()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'description'.
     *
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription() {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'ejbName'.
     *
     * @return the value of field 'ejbName'.
     */
    public java.lang.String getEjbName() {
        return this._ejbName;
    } //-- java.lang.String getEjbName() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'methodIntf'.
     *
     * @return the value of field 'methodIntf'.
     */
    public java.lang.String getMethodIntf() {
        return this._methodIntf;
    } //-- java.lang.String getMethodIntf() 

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
     * Sets the value of field 'description'.
     *
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description) {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'ejbName'.
     *
     * @param ejbName the value of field 'ejbName'.
     */
    public void setEjbName(java.lang.String ejbName) {
        this._ejbName = ejbName;
    } //-- void setEjbName(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'methodIntf'.
     *
     * @param methodIntf the value of field 'methodIntf'.
     */
    public void setMethodIntf(java.lang.String methodIntf) {
        this._methodIntf = methodIntf;
    } //-- void setMethodIntf(java.lang.String) 

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
        return (org.apache.openejb.config.ejb11.Method) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.Method.class, reader);
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
