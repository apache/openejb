/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.apache.openejb.config.sys;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Deployments.
 *
 * @version $Revision$ $Date$
 */
public class Deployments implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _jar
     */
    private java.lang.String _jar;

    /**
     * Field _dir
     */
    private java.lang.String _dir;


    //----------------/
    //- Constructors -/
    //----------------/

    public Deployments() {
        super();
    } //-- org.apache.openejb.config.sys.Deployments()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'dir'.
     *
     * @return the value of field 'dir'.
     */
    public java.lang.String getDir() {
        return this._dir;
    } //-- java.lang.String getDir() 

    /**
     * Returns the value of field 'jar'.
     *
     * @return the value of field 'jar'.
     */
    public java.lang.String getJar() {
        return this._jar;
    } //-- java.lang.String getJar() 

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
     * Sets the value of field 'dir'.
     *
     * @param dir the value of field 'dir'.
     */
    public void setDir(java.lang.String dir) {
        this._dir = dir;
    } //-- void setDir(java.lang.String) 

    /**
     * Sets the value of field 'jar'.
     *
     * @param jar the value of field 'jar'.
     */
    public void setJar(java.lang.String jar) {
        this._jar = jar;
    } //-- void setJar(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.Deployments) Unmarshaller.unmarshal(org.apache.openejb.config.sys.Deployments.class, reader);
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
