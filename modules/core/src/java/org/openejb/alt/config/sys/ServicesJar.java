/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

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
public class ServicesJar implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _serviceProviderList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ServicesJar() {
        super();
        _serviceProviderList = new Vector();
    } //-- org.openejb.alt.config.sys.ServicesJar()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vServiceProvider
    **/
    public void addServiceProvider(ServiceProvider vServiceProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _serviceProviderList.addElement(vServiceProvider);
    } //-- void addServiceProvider(ServiceProvider) 

    /**
     * 
     * @param index
     * @param vServiceProvider
    **/
    public void addServiceProvider(int index, ServiceProvider vServiceProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _serviceProviderList.insertElementAt(vServiceProvider, index);
    } //-- void addServiceProvider(int, ServiceProvider) 

    /**
    **/
    public java.util.Enumeration enumerateServiceProvider()
    {
        return _serviceProviderList.elements();
    } //-- java.util.Enumeration enumerateServiceProvider() 

    /**
     * 
     * @param index
    **/
    public ServiceProvider getServiceProvider(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _serviceProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (ServiceProvider) _serviceProviderList.elementAt(index);
    } //-- ServiceProvider getServiceProvider(int) 

    /**
    **/
    public ServiceProvider[] getServiceProvider()
    {
        int size = _serviceProviderList.size();
        ServiceProvider[] mArray = new ServiceProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (ServiceProvider) _serviceProviderList.elementAt(index);
        }
        return mArray;
    } //-- ServiceProvider[] getServiceProvider() 

    /**
    **/
    public int getServiceProviderCount()
    {
        return _serviceProviderList.size();
    } //-- int getServiceProviderCount() 

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
    public void removeAllServiceProvider()
    {
        _serviceProviderList.removeAllElements();
    } //-- void removeAllServiceProvider() 

    /**
     * 
     * @param index
    **/
    public ServiceProvider removeServiceProvider(int index)
    {
        java.lang.Object obj = _serviceProviderList.elementAt(index);
        _serviceProviderList.removeElementAt(index);
        return (ServiceProvider) obj;
    } //-- ServiceProvider removeServiceProvider(int) 

    /**
     * 
     * @param index
     * @param vServiceProvider
    **/
    public void setServiceProvider(int index, ServiceProvider vServiceProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _serviceProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _serviceProviderList.setElementAt(vServiceProvider, index);
    } //-- void setServiceProvider(int, ServiceProvider) 

    /**
     * 
     * @param serviceProviderArray
    **/
    public void setServiceProvider(ServiceProvider[] serviceProviderArray)
    {
        //-- copy array
        _serviceProviderList.removeAllElements();
        for (int i = 0; i < serviceProviderArray.length; i++) {
            _serviceProviderList.addElement(serviceProviderArray[i]);
        }
    } //-- void setServiceProvider(ServiceProvider) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.sys.ServicesJar unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.ServicesJar) Unmarshaller.unmarshal(org.openejb.alt.config.sys.ServicesJar.class, reader);
    } //-- org.openejb.alt.config.sys.ServicesJar unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
