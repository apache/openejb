/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

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
public class SecurityMap implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _roleName;

    private java.lang.String _description;

    private java.util.Vector _securityRealmList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityMap() {
        super();
        _securityRealmList = new Vector();
    } //-- org.openejb.alt.config.ejb11.SecurityMap()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vSecurityRealm
    **/
    public void addSecurityRealm(SecurityRealm vSecurityRealm)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRealmList.addElement(vSecurityRealm);
    } //-- void addSecurityRealm(SecurityRealm) 

    /**
     * 
     * @param index
     * @param vSecurityRealm
    **/
    public void addSecurityRealm(int index, SecurityRealm vSecurityRealm)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRealmList.insertElementAt(vSecurityRealm, index);
    } //-- void addSecurityRealm(int, SecurityRealm) 

    /**
    **/
    public java.util.Enumeration enumerateSecurityRealm()
    {
        return _securityRealmList.elements();
    } //-- java.util.Enumeration enumerateSecurityRealm() 

    /**
     * Returns the value of field 'description'.
     * @return the value of field 'description'.
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'roleName'.
     * @return the value of field 'roleName'.
    **/
    public java.lang.String getRoleName()
    {
        return this._roleName;
    } //-- java.lang.String getRoleName() 

    /**
     * 
     * @param index
    **/
    public SecurityRealm getSecurityRealm(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRealmList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (SecurityRealm) _securityRealmList.elementAt(index);
    } //-- SecurityRealm getSecurityRealm(int) 

    /**
    **/
    public SecurityRealm[] getSecurityRealm()
    {
        int size = _securityRealmList.size();
        SecurityRealm[] mArray = new SecurityRealm[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (SecurityRealm) _securityRealmList.elementAt(index);
        }
        return mArray;
    } //-- SecurityRealm[] getSecurityRealm() 

    /**
    **/
    public int getSecurityRealmCount()
    {
        return _securityRealmList.size();
    } //-- int getSecurityRealmCount() 

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
    public void removeAllSecurityRealm()
    {
        _securityRealmList.removeAllElements();
    } //-- void removeAllSecurityRealm() 

    /**
     * 
     * @param index
    **/
    public SecurityRealm removeSecurityRealm(int index)
    {
        java.lang.Object obj = _securityRealmList.elementAt(index);
        _securityRealmList.removeElementAt(index);
        return (SecurityRealm) obj;
    } //-- SecurityRealm removeSecurityRealm(int) 

    /**
     * Sets the value of field 'description'.
     * @param description the value of field 'description'.
    **/
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'roleName'.
     * @param roleName the value of field 'roleName'.
    **/
    public void setRoleName(java.lang.String roleName)
    {
        this._roleName = roleName;
    } //-- void setRoleName(java.lang.String) 

    /**
     * 
     * @param index
     * @param vSecurityRealm
    **/
    public void setSecurityRealm(int index, SecurityRealm vSecurityRealm)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRealmList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRealmList.setElementAt(vSecurityRealm, index);
    } //-- void setSecurityRealm(int, SecurityRealm) 

    /**
     * 
     * @param securityRealmArray
    **/
    public void setSecurityRealm(SecurityRealm[] securityRealmArray)
    {
        //-- copy array
        _securityRealmList.removeAllElements();
        for (int i = 0; i < securityRealmArray.length; i++) {
            _securityRealmList.addElement(securityRealmArray[i]);
        }
    } //-- void setSecurityRealm(SecurityRealm) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.SecurityMap unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.SecurityMap) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.SecurityMap.class, reader);
    } //-- org.openejb.alt.config.ejb11.SecurityMap unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
