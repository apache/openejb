/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision$ $Date$
**/
public class Connector implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _jar;

    /**
     * internal content storage
    **/
    private java.lang.String _content = "";


      //----------------/
     //- Constructors -/
    //----------------/

    public Connector() {
        super();
    } //-- org.openejb.alt.config.sys.Connector()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getJar()
    {
        return this._jar;
    } //-- java.lang.String getJar() 

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
     * @param _content
    **/
    public void setContent(java.lang.String _content)
    {
        this._content = _content;
    } //-- void setContent(java.lang.String) 

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
     * @param _jar
    **/
    public void setJar(java.lang.String _jar)
    {
        this._jar = _jar;
    } //-- void setJar(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.sys.Connector unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.Connector) Unmarshaller.unmarshal(org.openejb.alt.config.sys.Connector.class, reader);
    } //-- org.openejb.alt.config.sys.Connector unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
