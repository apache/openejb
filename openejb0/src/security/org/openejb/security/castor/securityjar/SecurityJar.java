/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.security.castor.securityjar;

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
public class SecurityJar implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _realmProviderList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityJar() {
        super();
        _realmProviderList = new Vector();
    } //-- org.openejb.security.castor.securityjar.SecurityJar()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vRealmProvider
    **/
    public void addRealmProvider(RealmProvider vRealmProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _realmProviderList.addElement(vRealmProvider);
    } //-- void addRealmProvider(RealmProvider) 

    /**
     * 
     * @param index
     * @param vRealmProvider
    **/
    public void addRealmProvider(int index, RealmProvider vRealmProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _realmProviderList.insertElementAt(vRealmProvider, index);
    } //-- void addRealmProvider(int, RealmProvider) 

    /**
    **/
    public java.util.Enumeration enumerateRealmProvider()
    {
        return _realmProviderList.elements();
    } //-- java.util.Enumeration enumerateRealmProvider() 

    /**
     * 
     * @param index
    **/
    public RealmProvider getRealmProvider(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _realmProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (RealmProvider) _realmProviderList.elementAt(index);
    } //-- RealmProvider getRealmProvider(int) 

    /**
    **/
    public RealmProvider[] getRealmProvider()
    {
        int size = _realmProviderList.size();
        RealmProvider[] mArray = new RealmProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (RealmProvider) _realmProviderList.elementAt(index);
        }
        return mArray;
    } //-- RealmProvider[] getRealmProvider() 

    /**
    **/
    public int getRealmProviderCount()
    {
        return _realmProviderList.size();
    } //-- int getRealmProviderCount() 

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
    public void removeAllRealmProvider()
    {
        _realmProviderList.removeAllElements();
    } //-- void removeAllRealmProvider() 

    /**
     * 
     * @param index
    **/
    public RealmProvider removeRealmProvider(int index)
    {
        java.lang.Object obj = _realmProviderList.elementAt(index);
        _realmProviderList.removeElementAt(index);
        return (RealmProvider) obj;
    } //-- RealmProvider removeRealmProvider(int) 

    /**
     * 
     * @param index
     * @param vRealmProvider
    **/
    public void setRealmProvider(int index, RealmProvider vRealmProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _realmProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _realmProviderList.setElementAt(vRealmProvider, index);
    } //-- void setRealmProvider(int, RealmProvider) 

    /**
     * 
     * @param realmProviderArray
    **/
    public void setRealmProvider(RealmProvider[] realmProviderArray)
    {
        //-- copy array
        _realmProviderList.removeAllElements();
        for (int i = 0; i < realmProviderArray.length; i++) {
            _realmProviderList.addElement(realmProviderArray[i]);
        }
    } //-- void setRealmProvider(RealmProvider) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.security.castor.securityjar.SecurityJar unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.security.castor.securityjar.SecurityJar) Unmarshaller.unmarshal(org.openejb.security.castor.securityjar.SecurityJar.class, reader);
    } //-- org.openejb.security.castor.securityjar.SecurityJar unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
