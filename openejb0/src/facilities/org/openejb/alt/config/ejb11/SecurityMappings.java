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
public class SecurityMappings implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _description;

    private java.util.Vector _securityMapList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SecurityMappings() {
        super();
        _securityMapList = new Vector();
    } //-- org.openejb.alt.config.ejb11.SecurityMappings()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vSecurityMap
    **/
    public void addSecurityMap(SecurityMap vSecurityMap)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityMapList.addElement(vSecurityMap);
    } //-- void addSecurityMap(SecurityMap) 

    /**
     * 
     * @param index
     * @param vSecurityMap
    **/
    public void addSecurityMap(int index, SecurityMap vSecurityMap)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityMapList.insertElementAt(vSecurityMap, index);
    } //-- void addSecurityMap(int, SecurityMap) 

    /**
    **/
    public java.util.Enumeration enumerateSecurityMap()
    {
        return _securityMapList.elements();
    } //-- java.util.Enumeration enumerateSecurityMap() 

    /**
     * Returns the value of field 'description'.
     * @return the value of field 'description'.
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * 
     * @param index
    **/
    public SecurityMap getSecurityMap(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityMapList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (SecurityMap) _securityMapList.elementAt(index);
    } //-- SecurityMap getSecurityMap(int) 

    /**
    **/
    public SecurityMap[] getSecurityMap()
    {
        int size = _securityMapList.size();
        SecurityMap[] mArray = new SecurityMap[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (SecurityMap) _securityMapList.elementAt(index);
        }
        return mArray;
    } //-- SecurityMap[] getSecurityMap() 

    /**
    **/
    public int getSecurityMapCount()
    {
        return _securityMapList.size();
    } //-- int getSecurityMapCount() 

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
    public void removeAllSecurityMap()
    {
        _securityMapList.removeAllElements();
    } //-- void removeAllSecurityMap() 

    /**
     * 
     * @param index
    **/
    public SecurityMap removeSecurityMap(int index)
    {
        java.lang.Object obj = _securityMapList.elementAt(index);
        _securityMapList.removeElementAt(index);
        return (SecurityMap) obj;
    } //-- SecurityMap removeSecurityMap(int) 

    /**
     * Sets the value of field 'description'.
     * @param description the value of field 'description'.
    **/
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * 
     * @param index
     * @param vSecurityMap
    **/
    public void setSecurityMap(int index, SecurityMap vSecurityMap)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityMapList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityMapList.setElementAt(vSecurityMap, index);
    } //-- void setSecurityMap(int, SecurityMap) 

    /**
     * 
     * @param securityMapArray
    **/
    public void setSecurityMap(SecurityMap[] securityMapArray)
    {
        //-- copy array
        _securityMapList.removeAllElements();
        for (int i = 0; i < securityMapArray.length; i++) {
            _securityMapList.addElement(securityMapArray[i]);
        }
    } //-- void setSecurityMap(SecurityMap) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.SecurityMappings unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.SecurityMappings) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.SecurityMappings.class, reader);
    } //-- org.openejb.alt.config.ejb11.SecurityMappings unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
