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
 * Class OpenejbJar.
 *
 * @version $Revision$ $Date$
 */
public class OpenejbJar implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _ejbDeploymentList
     */
    private java.util.Vector _ejbDeploymentList;


    //----------------/
    //- Constructors -/
    //----------------/

    public OpenejbJar() {
        super();
        _ejbDeploymentList = new Vector();
    } //-- org.openejb.config.ejb11.OpenejbJar()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addEjbDeployment
     *
     * @param vEjbDeployment
     */
    public void addEjbDeployment(org.openejb.config.ejb11.EjbDeployment vEjbDeployment)
            throws java.lang.IndexOutOfBoundsException {
        _ejbDeploymentList.addElement(vEjbDeployment);
    } //-- void addEjbDeployment(org.openejb.config.ejb11.EjbDeployment) 

    /**
     * Method addEjbDeployment
     *
     * @param index
     * @param vEjbDeployment
     */
    public void addEjbDeployment(int index, org.openejb.config.ejb11.EjbDeployment vEjbDeployment)
            throws java.lang.IndexOutOfBoundsException {
        _ejbDeploymentList.insertElementAt(vEjbDeployment, index);
    } //-- void addEjbDeployment(int, org.openejb.config.ejb11.EjbDeployment) 

    /**
     * Method enumerateEjbDeployment
     */
    public java.util.Enumeration enumerateEjbDeployment() {
        return _ejbDeploymentList.elements();
    } //-- java.util.Enumeration enumerateEjbDeployment() 

    /**
     * Method getEjbDeployment
     *
     * @param index
     */
    public org.openejb.config.ejb11.EjbDeployment getEjbDeployment(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbDeploymentList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.EjbDeployment) _ejbDeploymentList.elementAt(index);
    } //-- org.openejb.config.ejb11.EjbDeployment getEjbDeployment(int) 

    /**
     * Method getEjbDeployment
     */
    public org.openejb.config.ejb11.EjbDeployment[] getEjbDeployment() {
        int size = _ejbDeploymentList.size();
        org.openejb.config.ejb11.EjbDeployment[] mArray = new org.openejb.config.ejb11.EjbDeployment[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.EjbDeployment) _ejbDeploymentList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.EjbDeployment[] getEjbDeployment() 

    /**
     * Method getEjbDeploymentCount
     */
    public int getEjbDeploymentCount() {
        return _ejbDeploymentList.size();
    } //-- int getEjbDeploymentCount() 

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
     * Method removeAllEjbDeployment
     */
    public void removeAllEjbDeployment() {
        _ejbDeploymentList.removeAllElements();
    } //-- void removeAllEjbDeployment() 

    /**
     * Method removeEjbDeployment
     *
     * @param index
     */
    public org.openejb.config.ejb11.EjbDeployment removeEjbDeployment(int index) {
        java.lang.Object obj = _ejbDeploymentList.elementAt(index);
        _ejbDeploymentList.removeElementAt(index);
        return (org.openejb.config.ejb11.EjbDeployment) obj;
    } //-- org.openejb.config.ejb11.EjbDeployment removeEjbDeployment(int) 

    /**
     * Method setEjbDeployment
     *
     * @param index
     * @param vEjbDeployment
     */
    public void setEjbDeployment(int index, org.openejb.config.ejb11.EjbDeployment vEjbDeployment)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbDeploymentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbDeploymentList.setElementAt(vEjbDeployment, index);
    } //-- void setEjbDeployment(int, org.openejb.config.ejb11.EjbDeployment) 

    /**
     * Method setEjbDeployment
     *
     * @param ejbDeploymentArray
     */
    public void setEjbDeployment(org.openejb.config.ejb11.EjbDeployment[] ejbDeploymentArray) {
        //-- copy array
        _ejbDeploymentList.removeAllElements();
        for (int i = 0; i < ejbDeploymentArray.length; i++) {
            _ejbDeploymentList.addElement(ejbDeploymentArray[i]);
        }
    } //-- void setEjbDeployment(org.openejb.config.ejb11.EjbDeployment) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.config.ejb11.OpenejbJar) Unmarshaller.unmarshal(org.openejb.config.ejb11.OpenejbJar.class, reader);
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
