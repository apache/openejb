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
public class OpenejbJar implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _ejbDeploymentList;


      //----------------/
     //- Constructors -/
    //----------------/

    public OpenejbJar() {
        super();
        _ejbDeploymentList = new Vector();
    } //-- org.openejb.alt.config.ejb11.OpenejbJar()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vEjbDeployment
    **/
    public void addEjbDeployment(EjbDeployment vEjbDeployment)
        throws java.lang.IndexOutOfBoundsException
    {
        _ejbDeploymentList.addElement(vEjbDeployment);
    } //-- void addEjbDeployment(EjbDeployment) 

    /**
    **/
    public java.util.Enumeration enumerateEjbDeployment()
    {
        return _ejbDeploymentList.elements();
    } //-- java.util.Enumeration enumerateEjbDeployment() 

    /**
     * 
     * @param index
    **/
    public EjbDeployment getEjbDeployment(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbDeploymentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (EjbDeployment) _ejbDeploymentList.elementAt(index);
    } //-- EjbDeployment getEjbDeployment(int) 

    /**
    **/
    public EjbDeployment[] getEjbDeployment()
    {
        int size = _ejbDeploymentList.size();
        EjbDeployment[] mArray = new EjbDeployment[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (EjbDeployment) _ejbDeploymentList.elementAt(index);
        }
        return mArray;
    } //-- EjbDeployment[] getEjbDeployment() 

    /**
    **/
    public int getEjbDeploymentCount()
    {
        return _ejbDeploymentList.size();
    } //-- int getEjbDeploymentCount() 

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
    public void removeAllEjbDeployment()
    {
        _ejbDeploymentList.removeAllElements();
    } //-- void removeAllEjbDeployment() 

    /**
     * 
     * @param index
    **/
    public EjbDeployment removeEjbDeployment(int index)
    {
        Object obj = _ejbDeploymentList.elementAt(index);
        _ejbDeploymentList.removeElementAt(index);
        return (EjbDeployment) obj;
    } //-- EjbDeployment removeEjbDeployment(int) 

    /**
     * 
     * @param index
     * @param vEjbDeployment
    **/
    public void setEjbDeployment(int index, EjbDeployment vEjbDeployment)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbDeploymentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbDeploymentList.setElementAt(vEjbDeployment, index);
    } //-- void setEjbDeployment(int, EjbDeployment) 

    /**
     * 
     * @param ejbDeploymentArray
    **/
    public void setEjbDeployment(EjbDeployment[] ejbDeploymentArray)
    {
        //-- copy array
        _ejbDeploymentList.removeAllElements();
        for (int i = 0; i < ejbDeploymentArray.length; i++) {
            _ejbDeploymentList.addElement(ejbDeploymentArray[i]);
        }
    } //-- void setEjbDeployment(EjbDeployment) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.OpenejbJar unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.OpenejbJar) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.OpenejbJar.class, reader);
    } //-- org.openejb.alt.config.ejb11.OpenejbJar unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
