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
public class Deployments implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _jar;

    private java.lang.String _dir;


      //----------------/
     //- Constructors -/
    //----------------/

    public Deployments() {
        super();
    } //-- org.openejb.alt.config.sys.Deployments()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getDir()
    {
        return this._dir;
    } //-- java.lang.String getDir() 

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
     * @param _dir
    **/
    public void setDir(java.lang.String _dir)
    {
        this._dir = _dir;
    } //-- void setDir(java.lang.String) 

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
    public static org.openejb.alt.config.sys.Deployments unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.Deployments) Unmarshaller.unmarshal(org.openejb.alt.config.sys.Deployments.class, reader);
    } //-- org.openejb.alt.config.sys.Deployments unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
