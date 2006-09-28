/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ServicesJar.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.sys;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ServicesJar.
 *
 * @version $Revision$ $Date$
 */
public class ServicesJar implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _serviceProviderList
     */
    private java.util.Vector _serviceProviderList;


    //----------------/
    //- Constructors -/
    //----------------/

    public ServicesJar() {
        super();
        _serviceProviderList = new Vector();
    } //-- org.openejb.config.sys.ServicesJar()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addServiceProvider
     *
     * @param vServiceProvider
     */
    public void addServiceProvider(org.apache.openejb.config.sys.ServiceProvider vServiceProvider)
            throws java.lang.IndexOutOfBoundsException {
        _serviceProviderList.addElement(vServiceProvider);
    } //-- void addServiceProvider(org.openejb.config.sys.ServiceProvider) 

    /**
     * Method addServiceProvider
     *
     * @param index
     * @param vServiceProvider
     */
    public void addServiceProvider(int index, org.apache.openejb.config.sys.ServiceProvider vServiceProvider)
            throws java.lang.IndexOutOfBoundsException {
        _serviceProviderList.insertElementAt(vServiceProvider, index);
    } //-- void addServiceProvider(int, org.openejb.config.sys.ServiceProvider) 

    /**
     * Method enumerateServiceProvider
     */
    public java.util.Enumeration enumerateServiceProvider() {
        return _serviceProviderList.elements();
    } //-- java.util.Enumeration enumerateServiceProvider() 

    /**
     * Method getServiceProvider
     *
     * @param index
     */
    public org.apache.openejb.config.sys.ServiceProvider getServiceProvider(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _serviceProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.ServiceProvider) _serviceProviderList.elementAt(index);
    } //-- org.openejb.config.sys.ServiceProvider getServiceProvider(int) 

    /**
     * Method getServiceProvider
     */
    public org.apache.openejb.config.sys.ServiceProvider[] getServiceProvider() {
        int size = _serviceProviderList.size();
        org.apache.openejb.config.sys.ServiceProvider[] mArray = new org.apache.openejb.config.sys.ServiceProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.ServiceProvider) _serviceProviderList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.sys.ServiceProvider[] getServiceProvider() 

    /**
     * Method getServiceProviderCount
     */
    public int getServiceProviderCount() {
        return _serviceProviderList.size();
    } //-- int getServiceProviderCount() 

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
     * Method removeAllServiceProvider
     */
    public void removeAllServiceProvider() {
        _serviceProviderList.removeAllElements();
    } //-- void removeAllServiceProvider() 

    /**
     * Method removeServiceProvider
     *
     * @param index
     */
    public org.apache.openejb.config.sys.ServiceProvider removeServiceProvider(int index) {
        java.lang.Object obj = _serviceProviderList.elementAt(index);
        _serviceProviderList.removeElementAt(index);
        return (org.apache.openejb.config.sys.ServiceProvider) obj;
    } //-- org.openejb.config.sys.ServiceProvider removeServiceProvider(int) 

    /**
     * Method setServiceProvider
     *
     * @param index
     * @param vServiceProvider
     */
    public void setServiceProvider(int index, org.apache.openejb.config.sys.ServiceProvider vServiceProvider)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _serviceProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _serviceProviderList.setElementAt(vServiceProvider, index);
    } //-- void setServiceProvider(int, org.openejb.config.sys.ServiceProvider) 

    /**
     * Method setServiceProvider
     *
     * @param serviceProviderArray
     */
    public void setServiceProvider(org.apache.openejb.config.sys.ServiceProvider[] serviceProviderArray) {
        //-- copy array
        _serviceProviderList.removeAllElements();
        for (int i = 0; i < serviceProviderArray.length; i++) {
            _serviceProviderList.addElement(serviceProviderArray[i]);
        }
    } //-- void setServiceProvider(org.openejb.config.sys.ServiceProvider) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.ServicesJar) Unmarshaller.unmarshal(org.apache.openejb.config.sys.ServicesJar.class, reader);
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
