/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class MethodPermission.
 *
 * @version $Revision$ $Date$
 */
public class MethodPermission implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _roleNameList
     */
    private java.util.Vector _roleNameList;

    /**
     * Field _methodList
     */
    private java.util.Vector _methodList;


    //----------------/
    //- Constructors -/
    //----------------/

    public MethodPermission() {
        super();
        _roleNameList = new Vector();
        _methodList = new Vector();
    } //-- org.openejb.config.ejb11.MethodPermission()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addMethod
     *
     * @param vMethod
     */
    public void addMethod(org.openejb.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        _methodList.addElement(vMethod);
    } //-- void addMethod(org.openejb.config.ejb11.Method) 

    /**
     * Method addMethod
     *
     * @param index
     * @param vMethod
     */
    public void addMethod(int index, org.openejb.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        _methodList.insertElementAt(vMethod, index);
    } //-- void addMethod(int, org.openejb.config.ejb11.Method) 

    /**
     * Method addRoleName
     *
     * @param vRoleName
     */
    public void addRoleName(java.lang.String vRoleName)
            throws java.lang.IndexOutOfBoundsException {
        _roleNameList.addElement(vRoleName);
    } //-- void addRoleName(java.lang.String) 

    /**
     * Method addRoleName
     *
     * @param index
     * @param vRoleName
     */
    public void addRoleName(int index, java.lang.String vRoleName)
            throws java.lang.IndexOutOfBoundsException {
        _roleNameList.insertElementAt(vRoleName, index);
    } //-- void addRoleName(int, java.lang.String) 

    /**
     * Method enumerateMethod
     */
    public java.util.Enumeration enumerateMethod() {
        return _methodList.elements();
    } //-- java.util.Enumeration enumerateMethod() 

    /**
     * Method enumerateRoleName
     */
    public java.util.Enumeration enumerateRoleName() {
        return _roleNameList.elements();
    } //-- java.util.Enumeration enumerateRoleName() 

    /**
     * Returns the value of field 'description'.
     *
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription() {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Method getMethod
     *
     * @param index
     */
    public org.openejb.config.ejb11.Method getMethod(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.Method) _methodList.elementAt(index);
    } //-- org.openejb.config.ejb11.Method getMethod(int) 

    /**
     * Method getMethod
     */
    public org.openejb.config.ejb11.Method[] getMethod() {
        int size = _methodList.size();
        org.openejb.config.ejb11.Method[] mArray = new org.openejb.config.ejb11.Method[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.Method) _methodList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.Method[] getMethod() 

    /**
     * Method getMethodCount
     */
    public int getMethodCount() {
        return _methodList.size();
    } //-- int getMethodCount() 

    /**
     * Method getRoleName
     *
     * @param index
     */
    public java.lang.String getRoleName(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _roleNameList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (String) _roleNameList.elementAt(index);
    } //-- java.lang.String getRoleName(int) 

    /**
     * Method getRoleName
     */
    public java.lang.String[] getRoleName() {
        int size = _roleNameList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) _roleNameList.elementAt(index);
        }
        return mArray;
    } //-- java.lang.String[] getRoleName() 

    /**
     * Method getRoleNameCount
     */
    public int getRoleNameCount() {
        return _roleNameList.size();
    } //-- int getRoleNameCount() 

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
     * Method removeAllMethod
     */
    public void removeAllMethod() {
        _methodList.removeAllElements();
    } //-- void removeAllMethod() 

    /**
     * Method removeAllRoleName
     */
    public void removeAllRoleName() {
        _roleNameList.removeAllElements();
    } //-- void removeAllRoleName() 

    /**
     * Method removeMethod
     *
     * @param index
     */
    public org.openejb.config.ejb11.Method removeMethod(int index) {
        java.lang.Object obj = _methodList.elementAt(index);
        _methodList.removeElementAt(index);
        return (org.openejb.config.ejb11.Method) obj;
    } //-- org.openejb.config.ejb11.Method removeMethod(int) 

    /**
     * Method removeRoleName
     *
     * @param index
     */
    public java.lang.String removeRoleName(int index) {
        java.lang.Object obj = _roleNameList.elementAt(index);
        _roleNameList.removeElementAt(index);
        return (String) obj;
    } //-- java.lang.String removeRoleName(int) 

    /**
     * Sets the value of field 'description'.
     *
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description) {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Method setMethod
     *
     * @param index
     * @param vMethod
     */
    public void setMethod(int index, org.openejb.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodList.setElementAt(vMethod, index);
    } //-- void setMethod(int, org.openejb.config.ejb11.Method) 

    /**
     * Method setMethod
     *
     * @param methodArray
     */
    public void setMethod(org.openejb.config.ejb11.Method[] methodArray) {
        //-- copy array
        _methodList.removeAllElements();
        for (int i = 0; i < methodArray.length; i++) {
            _methodList.addElement(methodArray[i]);
        }
    } //-- void setMethod(org.openejb.config.ejb11.Method) 

    /**
     * Method setRoleName
     *
     * @param index
     * @param vRoleName
     */
    public void setRoleName(int index, java.lang.String vRoleName)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _roleNameList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _roleNameList.setElementAt(vRoleName, index);
    } //-- void setRoleName(int, java.lang.String) 

    /**
     * Method setRoleName
     *
     * @param roleNameArray
     */
    public void setRoleName(java.lang.String[] roleNameArray) {
        //-- copy array
        _roleNameList.removeAllElements();
        for (int i = 0; i < roleNameArray.length; i++) {
            _roleNameList.addElement(roleNameArray[i]);
        }
    } //-- void setRoleName(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.config.ejb11.MethodPermission) Unmarshaller.unmarshal(org.openejb.config.ejb11.MethodPermission.class, reader);
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
