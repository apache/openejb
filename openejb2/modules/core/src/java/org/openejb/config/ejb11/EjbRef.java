/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class EjbRef.
 *
 * @version $Revision$ $Date$
 */
public class EjbRef implements java.io.Serializable {


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
     * Field _ejbRefName
     */
    private java.lang.String _ejbRefName;

    /**
     * Field _ejbRefType
     */
    private java.lang.String _ejbRefType;

    /**
     * Field _home
     */
    private java.lang.String _home;

    /**
     * Field _remote
     */
    private java.lang.String _remote;

    /**
     * Field _ejbLink
     */
    private java.lang.String _ejbLink;


    //----------------/
    //- Constructors -/
    //----------------/

    public EjbRef() {
        super();
    } //-- org.openejb.config.ejb11.EjbRef()


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
     * Returns the value of field 'ejbLink'.
     *
     * @return the value of field 'ejbLink'.
     */
    public java.lang.String getEjbLink() {
        return this._ejbLink;
    } //-- java.lang.String getEjbLink() 

    /**
     * Returns the value of field 'ejbRefName'.
     *
     * @return the value of field 'ejbRefName'.
     */
    public java.lang.String getEjbRefName() {
        return this._ejbRefName;
    } //-- java.lang.String getEjbRefName() 

    /**
     * Returns the value of field 'ejbRefType'.
     *
     * @return the value of field 'ejbRefType'.
     */
    public java.lang.String getEjbRefType() {
        return this._ejbRefType;
    } //-- java.lang.String getEjbRefType() 

    /**
     * Returns the value of field 'home'.
     *
     * @return the value of field 'home'.
     */
    public java.lang.String getHome() {
        return this._home;
    } //-- java.lang.String getHome() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'remote'.
     *
     * @return the value of field 'remote'.
     */
    public java.lang.String getRemote() {
        return this._remote;
    } //-- java.lang.String getRemote() 

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
     * Sets the value of field 'ejbLink'.
     *
     * @param ejbLink the value of field 'ejbLink'.
     */
    public void setEjbLink(java.lang.String ejbLink) {
        this._ejbLink = ejbLink;
    } //-- void setEjbLink(java.lang.String) 

    /**
     * Sets the value of field 'ejbRefName'.
     *
     * @param ejbRefName the value of field 'ejbRefName'.
     */
    public void setEjbRefName(java.lang.String ejbRefName) {
        this._ejbRefName = ejbRefName;
    } //-- void setEjbRefName(java.lang.String) 

    /**
     * Sets the value of field 'ejbRefType'.
     *
     * @param ejbRefType the value of field 'ejbRefType'.
     */
    public void setEjbRefType(java.lang.String ejbRefType) {
        this._ejbRefType = ejbRefType;
    } //-- void setEjbRefType(java.lang.String) 

    /**
     * Sets the value of field 'home'.
     *
     * @param home the value of field 'home'.
     */
    public void setHome(java.lang.String home) {
        this._home = home;
    } //-- void setHome(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'remote'.
     *
     * @param remote the value of field 'remote'.
     */
    public void setRemote(java.lang.String remote) {
        this._remote = remote;
    } //-- void setRemote(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.config.ejb11.EjbRef) Unmarshaller.unmarshal(org.openejb.config.ejb11.EjbRef.class, reader);
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
