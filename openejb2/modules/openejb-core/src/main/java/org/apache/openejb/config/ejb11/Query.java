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
 * Class Query.
 *
 * @version $Revision$ $Date$
 */
public class Query implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _queryMethod
     */
    private org.apache.openejb.config.ejb11.QueryMethod _queryMethod;

    /**
     * Field _objectQl
     */
    private java.lang.String _objectQl;


    //----------------/
    //- Constructors -/
    //----------------/

    public Query() {
        super();
    } //-- org.apache.openejb.config.ejb11.Query()


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
     * Returns the value of field 'objectQl'.
     *
     * @return the value of field 'objectQl'.
     */
    public java.lang.String getObjectQl() {
        return this._objectQl;
    } //-- java.lang.String getObjectQl() 

    /**
     * Returns the value of field 'queryMethod'.
     *
     * @return the value of field 'queryMethod'.
     */
    public org.apache.openejb.config.ejb11.QueryMethod getQueryMethod() {
        return this._queryMethod;
    } //-- org.apache.openejb.config.ejb11.QueryMethod getQueryMethod()

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
     * Sets the value of field 'objectQl'.
     *
     * @param objectQl the value of field 'objectQl'.
     */
    public void setObjectQl(java.lang.String objectQl) {
        this._objectQl = objectQl;
    } //-- void setObjectQl(java.lang.String) 

    /**
     * Sets the value of field 'queryMethod'.
     *
     * @param queryMethod the value of field 'queryMethod'.
     */
    public void setQueryMethod(org.apache.openejb.config.ejb11.QueryMethod queryMethod) {
        this._queryMethod = queryMethod;
    } //-- void setQueryMethod(org.apache.openejb.config.ejb11.QueryMethod)

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.Query) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.Query.class, reader);
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
