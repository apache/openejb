/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3</a>, using an
 * XML Schema.
 * $Id$
 */

package org.opene.security.castor.security;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision$ $Date$
**/
public class Security implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
    **/
    private java.lang.String _content = "";

    private java.util.Vector _securityAreaJarList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Security() {
        super();
        _securityAreaJarList = new Vector();
    } //-- org.opene.security.castor.security.Security()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vSecurityAreaJar
    **/
    public void addSecurityAreaJar(SecurityAreaJar vSecurityAreaJar)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityAreaJarList.addElement(vSecurityAreaJar);
    } //-- void addSecurityAreaJar(SecurityAreaJar) 

    /**
     * 
     * @param index
     * @param vSecurityAreaJar
    **/
    public void addSecurityAreaJar(int index, SecurityAreaJar vSecurityAreaJar)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityAreaJarList.insertElementAt(vSecurityAreaJar, index);
    } //-- void addSecurityAreaJar(int, SecurityAreaJar) 

    /**
    **/
    public java.util.Enumeration enumerateSecurityAreaJar()
    {
        return _securityAreaJarList.elements();
    } //-- java.util.Enumeration enumerateSecurityAreaJar() 

    /**
    **/
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * 
     * @param index
    **/
    public SecurityAreaJar getSecurityAreaJar(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityAreaJarList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (SecurityAreaJar) _securityAreaJarList.elementAt(index);
    } //-- SecurityAreaJar getSecurityAreaJar(int) 

    /**
    **/
    public SecurityAreaJar[] getSecurityAreaJar()
    {
        int size = _securityAreaJarList.size();
        SecurityAreaJar[] mArray = new SecurityAreaJar[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (SecurityAreaJar) _securityAreaJarList.elementAt(index);
        }
        return mArray;
    } //-- SecurityAreaJar[] getSecurityAreaJar() 

    /**
    **/
    public int getSecurityAreaJarCount()
    {
        return _securityAreaJarList.size();
    } //-- int getSecurityAreaJarCount() 

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
    **/
    public void removeAllSecurityAreaJar()
    {
        _securityAreaJarList.removeAllElements();
    } //-- void removeAllSecurityAreaJar() 

    /**
     * 
     * @param index
    **/
    public SecurityAreaJar removeSecurityAreaJar(int index)
    {
        Object obj = _securityAreaJarList.elementAt(index);
        _securityAreaJarList.removeElementAt(index);
        return (SecurityAreaJar) obj;
    } //-- SecurityAreaJar removeSecurityAreaJar(int) 

    /**
     * 
     * @param content
    **/
    public void setContent(java.lang.String content)
    {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * 
     * @param index
     * @param vSecurityAreaJar
    **/
    public void setSecurityAreaJar(int index, SecurityAreaJar vSecurityAreaJar)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityAreaJarList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityAreaJarList.setElementAt(vSecurityAreaJar, index);
    } //-- void setSecurityAreaJar(int, SecurityAreaJar) 

    /**
     * 
     * @param securityAreaJarArray
    **/
    public void setSecurityAreaJar(SecurityAreaJar[] securityAreaJarArray)
    {
        //-- copy array
        _securityAreaJarList.removeAllElements();
        for (int i = 0; i < securityAreaJarArray.length; i++) {
            _securityAreaJarList.addElement(securityAreaJarArray[i]);
        }
    } //-- void setSecurityAreaJar(SecurityAreaJar) 

    /**
     * 
     * @param reader
    **/
    public static org.opene.security.castor.security.Security unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opene.security.castor.security.Security) Unmarshaller.unmarshal(org.opene.security.castor.security.Security.class, reader);
    } //-- org.opene.security.castor.security.Security unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
