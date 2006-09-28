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
 * Class JndiProvider.
 *
 * @version $Revision$ $Date$
 */
public class JndiProvider implements java.io.Serializable, org.apache.openejb.config.Service {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _provider
     */
    private java.lang.String _provider;

    /**
     * Field _jar
     */
    private java.lang.String _jar;

    /**
     * internal content storage
     */
    private java.lang.String _content = "";


    //----------------/
    //- Constructors -/
    //----------------/

    public JndiProvider() {
        super();
        setContent("");
    } //-- org.apache.openejb.config.sys.JndiProvider()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     *
     * @return the value of field 'content'.
     */
    public java.lang.String getContent() {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'jar'.
     *
     * @return the value of field 'jar'.
     */
    public java.lang.String getJar() {
        return this._jar;
    } //-- java.lang.String getJar() 

    /**
     * Returns the value of field 'provider'.
     *
     * @return the value of field 'provider'.
     */
    public java.lang.String getProvider() {
        return this._provider;
    } //-- java.lang.String getProvider() 

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
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     *
     * @param content the value of field 'content'.
     */
    public void setContent(java.lang.String content) {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'jar'.
     *
     * @param jar the value of field 'jar'.
     */
    public void setJar(java.lang.String jar) {
        this._jar = jar;
    } //-- void setJar(java.lang.String) 

    /**
     * Sets the value of field 'provider'.
     *
     * @param provider the value of field 'provider'.
     */
    public void setProvider(java.lang.String provider) {
        this._provider = provider;
    } //-- void setProvider(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.JndiProvider) Unmarshaller.unmarshal(org.apache.openejb.config.sys.JndiProvider.class, reader);
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
