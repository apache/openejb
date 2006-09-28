/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ContainerTransaction.
 *
 * @version $Revision$ $Date$
 */
public class ContainerTransaction implements java.io.Serializable {


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
     * Field _methodList
     */
    private java.util.Vector _methodList;

    /**
     * Field _transAttribute
     */
    private java.lang.String _transAttribute;


    //----------------/
    //- Constructors -/
    //----------------/

    public ContainerTransaction() {
        super();
        _methodList = new Vector();
    } //-- org.apache.openejb.config.ejb11.ContainerTransaction()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addMethod
     *
     * @param vMethod
     */
    public void addMethod(org.apache.openejb.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        _methodList.addElement(vMethod);
    } //-- void addMethod(org.apache.openejb.config.ejb11.Method)

    /**
     * Method addMethod
     *
     * @param index
     * @param vMethod
     */
    public void addMethod(int index, org.apache.openejb.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        _methodList.insertElementAt(vMethod, index);
    } //-- void addMethod(int, org.apache.openejb.config.ejb11.Method)

    /**
     * Method enumerateMethod
     */
    public java.util.Enumeration enumerateMethod() {
        return _methodList.elements();
    } //-- java.util.Enumeration enumerateMethod() 

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
    public org.apache.openejb.config.ejb11.Method getMethod(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.Method) _methodList.elementAt(index);
    } //-- org.apache.openejb.config.ejb11.Method getMethod(int)

    /**
     * Method getMethod
     */
    public org.apache.openejb.config.ejb11.Method[] getMethod() {
        int size = _methodList.size();
        org.apache.openejb.config.ejb11.Method[] mArray = new org.apache.openejb.config.ejb11.Method[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.Method) _methodList.elementAt(index);
        }
        return mArray;
    } //-- org.apache.openejb.config.ejb11.Method[] getMethod()

    /**
     * Method getMethodCount
     */
    public int getMethodCount() {
        return _methodList.size();
    } //-- int getMethodCount() 

    /**
     * Returns the value of field 'transAttribute'.
     *
     * @return the value of field 'transAttribute'.
     */
    public java.lang.String getTransAttribute() {
        return this._transAttribute;
    } //-- java.lang.String getTransAttribute() 

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
     * Method removeMethod
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.Method removeMethod(int index) {
        java.lang.Object obj = _methodList.elementAt(index);
        _methodList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.Method) obj;
    } //-- org.apache.openejb.config.ejb11.Method removeMethod(int)

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
    public void setMethod(int index, org.apache.openejb.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodList.setElementAt(vMethod, index);
    } //-- void setMethod(int, org.apache.openejb.config.ejb11.Method)

    /**
     * Method setMethod
     *
     * @param methodArray
     */
    public void setMethod(org.apache.openejb.config.ejb11.Method[] methodArray) {
        //-- copy array
        _methodList.removeAllElements();
        for (int i = 0; i < methodArray.length; i++) {
            _methodList.addElement(methodArray[i]);
        }
    } //-- void setMethod(org.apache.openejb.config.ejb11.Method)

    /**
     * Sets the value of field 'transAttribute'.
     *
     * @param transAttribute the value of field 'transAttribute'.
     */
    public void setTransAttribute(java.lang.String transAttribute) {
        this._transAttribute = transAttribute;
    } //-- void setTransAttribute(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.ContainerTransaction) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.ContainerTransaction.class, reader);
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
