/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.security.castor.security;

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

    private java.util.Vector _realmList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Security() {
        super();
        _realmList = new Vector();
    } //-- org.openejb.security.castor.security.Security()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vRealm
    **/
    public void addRealm(Realm vRealm)
        throws java.lang.IndexOutOfBoundsException
    {
        _realmList.addElement(vRealm);
    } //-- void addRealm(Realm) 

    /**
     * 
     * @param index
     * @param vRealm
    **/
    public void addRealm(int index, Realm vRealm)
        throws java.lang.IndexOutOfBoundsException
    {
        _realmList.insertElementAt(vRealm, index);
    } //-- void addRealm(int, Realm) 

    /**
    **/
    public java.util.Enumeration enumerateRealm()
    {
        return _realmList.elements();
    } //-- java.util.Enumeration enumerateRealm() 

    /**
     * 
     * @param index
    **/
    public Realm getRealm(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _realmList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Realm) _realmList.elementAt(index);
    } //-- Realm getRealm(int) 

    /**
    **/
    public Realm[] getRealm()
    {
        int size = _realmList.size();
        Realm[] mArray = new Realm[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Realm) _realmList.elementAt(index);
        }
        return mArray;
    } //-- Realm[] getRealm() 

    /**
    **/
    public int getRealmCount()
    {
        return _realmList.size();
    } //-- int getRealmCount() 

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
    public void removeAllRealm()
    {
        _realmList.removeAllElements();
    } //-- void removeAllRealm() 

    /**
     * 
     * @param index
    **/
    public Realm removeRealm(int index)
    {
        java.lang.Object obj = _realmList.elementAt(index);
        _realmList.removeElementAt(index);
        return (Realm) obj;
    } //-- Realm removeRealm(int) 

    /**
     * 
     * @param index
     * @param vRealm
    **/
    public void setRealm(int index, Realm vRealm)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _realmList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _realmList.setElementAt(vRealm, index);
    } //-- void setRealm(int, Realm) 

    /**
     * 
     * @param realmArray
    **/
    public void setRealm(Realm[] realmArray)
    {
        //-- copy array
        _realmList.removeAllElements();
        for (int i = 0; i < realmArray.length; i++) {
            _realmList.addElement(realmArray[i]);
        }
    } //-- void setRealm(Realm) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.security.castor.security.Security unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.security.castor.security.Security) Unmarshaller.unmarshal(org.openejb.security.castor.security.Security.class, reader);
    } //-- org.openejb.security.castor.security.Security unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
