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
public class SecurityRealm implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _name;

    private java.lang.String _description;

    private java.util.Vector _principalList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityRealm() {
        super();
        _principalList = new Vector();
    } //-- org.openejb.alt.config.ejb11.SecurityRealm()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vPrincipal
    **/
    public void addPrincipal(Principal vPrincipal)
        throws java.lang.IndexOutOfBoundsException
    {
        _principalList.addElement(vPrincipal);
    } //-- void addPrincipal(Principal) 

    /**
     * 
     * @param index
     * @param vPrincipal
    **/
    public void addPrincipal(int index, Principal vPrincipal)
        throws java.lang.IndexOutOfBoundsException
    {
        _principalList.insertElementAt(vPrincipal, index);
    } //-- void addPrincipal(int, Principal) 

    /**
    **/
    public java.util.Enumeration enumeratePrincipal()
    {
        return _principalList.elements();
    } //-- java.util.Enumeration enumeratePrincipal() 

    /**
     * Returns the value of field 'description'.
     * @return the value of field 'description'.
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'name'.
     * @return the value of field 'name'.
    **/
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
     * 
     * @param index
    **/
    public Principal getPrincipal(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _principalList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Principal) _principalList.elementAt(index);
    } //-- Principal getPrincipal(int) 

    /**
    **/
    public Principal[] getPrincipal()
    {
        int size = _principalList.size();
        Principal[] mArray = new Principal[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Principal) _principalList.elementAt(index);
        }
        return mArray;
    } //-- Principal[] getPrincipal() 

    /**
    **/
    public int getPrincipalCount()
    {
        return _principalList.size();
    } //-- int getPrincipalCount() 

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
    public void removeAllPrincipal()
    {
        _principalList.removeAllElements();
    } //-- void removeAllPrincipal() 

    /**
     * 
     * @param index
    **/
    public Principal removePrincipal(int index)
    {
        java.lang.Object obj = _principalList.elementAt(index);
        _principalList.removeElementAt(index);
        return (Principal) obj;
    } //-- Principal removePrincipal(int) 

    /**
     * Sets the value of field 'description'.
     * @param description the value of field 'description'.
    **/
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'name'.
     * @param name the value of field 'name'.
    **/
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * 
     * @param index
     * @param vPrincipal
    **/
    public void setPrincipal(int index, Principal vPrincipal)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _principalList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _principalList.setElementAt(vPrincipal, index);
    } //-- void setPrincipal(int, Principal) 

    /**
     * 
     * @param principalArray
    **/
    public void setPrincipal(Principal[] principalArray)
    {
        //-- copy array
        _principalList.removeAllElements();
        for (int i = 0; i < principalArray.length; i++) {
            _principalList.addElement(principalArray[i]);
        }
    } //-- void setPrincipal(Principal) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.SecurityRealm unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.SecurityRealm) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.SecurityRealm.class, reader);
    } //-- org.openejb.alt.config.ejb11.SecurityRealm unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
