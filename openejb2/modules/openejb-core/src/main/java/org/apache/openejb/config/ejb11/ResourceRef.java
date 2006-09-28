/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ResourceRef.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ResourceRef.
 *
 * @version $Revision$ $Date$
 */
public class ResourceRef implements java.io.Serializable {


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
     * Field _resRefName
     */
    private java.lang.String _resRefName;

    /**
     * Field _resType
     */
    private java.lang.String _resType;

    /**
     * Field _resAuth
     */
    private java.lang.String _resAuth;


    //----------------/
    //- Constructors -/
    //----------------/

    public ResourceRef() {
        super();
    } //-- org.openejb.config.ejb11.ResourceRef()


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
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'resAuth'.
     *
     * @return the value of field 'resAuth'.
     */
    public java.lang.String getResAuth() {
        return this._resAuth;
    } //-- java.lang.String getResAuth() 

    /**
     * Returns the value of field 'resRefName'.
     *
     * @return the value of field 'resRefName'.
     */
    public java.lang.String getResRefName() {
        return this._resRefName;
    } //-- java.lang.String getResRefName() 

    /**
     * Returns the value of field 'resType'.
     *
     * @return the value of field 'resType'.
     */
    public java.lang.String getResType() {
        return this._resType;
    } //-- java.lang.String getResType() 

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
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'resAuth'.
     *
     * @param resAuth the value of field 'resAuth'.
     */
    public void setResAuth(java.lang.String resAuth) {
        this._resAuth = resAuth;
    } //-- void setResAuth(java.lang.String) 

    /**
     * Sets the value of field 'resRefName'.
     *
     * @param resRefName the value of field 'resRefName'.
     */
    public void setResRefName(java.lang.String resRefName) {
        this._resRefName = resRefName;
    } //-- void setResRefName(java.lang.String) 

    /**
     * Sets the value of field 'resType'.
     *
     * @param resType the value of field 'resType'.
     */
    public void setResType(java.lang.String resType) {
        this._resType = resType;
    } //-- void setResType(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.ResourceRef) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.ResourceRef.class, reader);
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
