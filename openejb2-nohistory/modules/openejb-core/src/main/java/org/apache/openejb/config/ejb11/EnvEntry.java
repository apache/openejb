/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EnvEntry.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class EnvEntry.
 *
 * @version $Revision$ $Date$
 */
public class EnvEntry implements java.io.Serializable {


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
     * Field _envEntryName
     */
    private java.lang.String _envEntryName;

    /**
     * Field _envEntryType
     */
    private java.lang.String _envEntryType;

    /**
     * Field _envEntryValue
     */
    private java.lang.String _envEntryValue;


    //----------------/
    //- Constructors -/
    //----------------/

    public EnvEntry() {
        super();
    } //-- org.openejb.config.ejb11.EnvEntry()


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
     * Returns the value of field 'envEntryName'.
     *
     * @return the value of field 'envEntryName'.
     */
    public java.lang.String getEnvEntryName() {
        return this._envEntryName;
    } //-- java.lang.String getEnvEntryName() 

    /**
     * Returns the value of field 'envEntryType'.
     *
     * @return the value of field 'envEntryType'.
     */
    public java.lang.String getEnvEntryType() {
        return this._envEntryType;
    } //-- java.lang.String getEnvEntryType() 

    /**
     * Returns the value of field 'envEntryValue'.
     *
     * @return the value of field 'envEntryValue'.
     */
    public java.lang.String getEnvEntryValue() {
        return this._envEntryValue;
    } //-- java.lang.String getEnvEntryValue() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

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
     * Sets the value of field 'envEntryName'.
     *
     * @param envEntryName the value of field 'envEntryName'.
     */
    public void setEnvEntryName(java.lang.String envEntryName) {
        this._envEntryName = envEntryName;
    } //-- void setEnvEntryName(java.lang.String) 

    /**
     * Sets the value of field 'envEntryType'.
     *
     * @param envEntryType the value of field 'envEntryType'.
     */
    public void setEnvEntryType(java.lang.String envEntryType) {
        this._envEntryType = envEntryType;
    } //-- void setEnvEntryType(java.lang.String) 

    /**
     * Sets the value of field 'envEntryValue'.
     *
     * @param envEntryValue the value of field 'envEntryValue'.
     */
    public void setEnvEntryValue(java.lang.String envEntryValue) {
        this._envEntryValue = envEntryValue;
    } //-- void setEnvEntryValue(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.EnvEntry) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.EnvEntry.class, reader);
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
