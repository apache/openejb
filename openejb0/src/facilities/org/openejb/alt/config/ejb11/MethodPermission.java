/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision$ $Date$
**/
public class MethodPermission implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.util.Vector _roleNameList;

    private java.util.Vector _methodList;


      //----------------/
     //- Constructors -/
    //----------------/

    public MethodPermission() {
        super();
        _roleNameList = new Vector();
        _methodList = new Vector();
    } //-- org.openejb.alt.config.ejb11.MethodPermission()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vMethod
    **/
    public void addMethod(Method vMethod)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodList.addElement(vMethod);
    } //-- void addMethod(Method) 

    /**
     * 
     * @param vRoleName
    **/
    public void addRoleName(java.lang.String vRoleName)
        throws java.lang.IndexOutOfBoundsException
    {
        _roleNameList.addElement(vRoleName);
    } //-- void addRoleName(java.lang.String) 

    /**
    **/
    public java.util.Enumeration enumerateMethod()
    {
        return _methodList.elements();
    } //-- java.util.Enumeration enumerateMethod() 

    /**
    **/
    public java.util.Enumeration enumerateRoleName()
    {
        return _roleNameList.elements();
    } //-- java.util.Enumeration enumerateRoleName() 

    /**
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * 
     * @param index
    **/
    public Method getMethod(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Method) _methodList.elementAt(index);
    } //-- Method getMethod(int) 

    /**
    **/
    public Method[] getMethod()
    {
        int size = _methodList.size();
        Method[] mArray = new Method[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Method) _methodList.elementAt(index);
        }
        return mArray;
    } //-- Method[] getMethod() 

    /**
    **/
    public int getMethodCount()
    {
        return _methodList.size();
    } //-- int getMethodCount() 

    /**
     * 
     * @param index
    **/
    public java.lang.String getRoleName(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _roleNameList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (String)_roleNameList.elementAt(index);
    } //-- java.lang.String getRoleName(int) 

    /**
    **/
    public java.lang.String[] getRoleName()
    {
        int size = _roleNameList.size();
        java.lang.String[] mArray = new String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String)_roleNameList.elementAt(index);
        }
        return mArray;
    } //-- java.lang.String[] getRoleName() 

    /**
    **/
    public int getRoleNameCount()
    {
        return _roleNameList.size();
    } //-- int getRoleNameCount() 

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
    public void removeAllMethod()
    {
        _methodList.removeAllElements();
    } //-- void removeAllMethod() 

    /**
    **/
    public void removeAllRoleName()
    {
        _roleNameList.removeAllElements();
    } //-- void removeAllRoleName() 

    /**
     * 
     * @param index
    **/
    public Method removeMethod(int index)
    {
        Object obj = _methodList.elementAt(index);
        _methodList.removeElementAt(index);
        return (Method) obj;
    } //-- Method removeMethod(int) 

    /**
     * 
     * @param index
    **/
    public java.lang.String removeRoleName(int index)
    {
        Object obj = _roleNameList.elementAt(index);
        _roleNameList.removeElementAt(index);
        return (String)obj;
    } //-- java.lang.String removeRoleName(int) 

    /**
     * 
     * @param _description
    **/
    public void setDescription(java.lang.String _description)
    {
        this._description = _description;
    } //-- void setDescription(java.lang.String) 

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
     * @param index
     * @param vMethod
    **/
    public void setMethod(int index, Method vMethod)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodList.setElementAt(vMethod, index);
    } //-- void setMethod(int, Method) 

    /**
     * 
     * @param methodArray
    **/
    public void setMethod(Method[] methodArray)
    {
        //-- copy array
        _methodList.removeAllElements();
        for (int i = 0; i < methodArray.length; i++) {
            _methodList.addElement(methodArray[i]);
        }
    } //-- void setMethod(Method) 

    /**
     * 
     * @param index
     * @param vRoleName
    **/
    public void setRoleName(int index, java.lang.String vRoleName)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _roleNameList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _roleNameList.setElementAt(vRoleName, index);
    } //-- void setRoleName(int, java.lang.String) 

    /**
     * 
     * @param roleNameArray
    **/
    public void setRoleName(java.lang.String[] roleNameArray)
    {
        //-- copy array
        _roleNameList.removeAllElements();
        for (int i = 0; i < roleNameArray.length; i++) {
            _roleNameList.addElement(roleNameArray[i]);
        }
    } //-- void setRoleName(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.MethodPermission unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.MethodPermission) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.MethodPermission.class, reader);
    } //-- org.openejb.alt.config.ejb11.MethodPermission unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
