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
public class EnterpriseBeans implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _items;


      //----------------/
     //- Constructors -/
    //----------------/

    public EnterpriseBeans() {
        super();
        _items = new Vector();
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeans()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vEnterpriseBeansItem
    **/
    public void addEnterpriseBeansItem(org.openejb.alt.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.addElement(vEnterpriseBeansItem);
    } //-- void addEnterpriseBeansItem(org.openejb.alt.config.ejb11.EnterpriseBeansItem) 

    /**
    **/
    public java.util.Enumeration enumerateEnterpriseBeansItem()
    {
        return _items.elements();
    } //-- java.util.Enumeration enumerateEnterpriseBeansItem() 

    /**
     * 
     * @param index
    **/
    public org.openejb.alt.config.ejb11.EnterpriseBeansItem getEnterpriseBeansItem(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.EnterpriseBeansItem) _items.elementAt(index);
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeansItem getEnterpriseBeansItem(int) 

    /**
    **/
    public org.openejb.alt.config.ejb11.EnterpriseBeansItem[] getEnterpriseBeansItem()
    {
        int size = _items.size();
        org.openejb.alt.config.ejb11.EnterpriseBeansItem[] mArray = new EnterpriseBeansItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (EnterpriseBeansItem) _items.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeansItem[] getEnterpriseBeansItem() 

    /**
    **/
    public int getEnterpriseBeansItemCount()
    {
        return _items.size();
    } //-- int getEnterpriseBeansItemCount() 

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
    public void removeAllEnterpriseBeansItem()
    {
        _items.removeAllElements();
    } //-- void removeAllEnterpriseBeansItem() 

    /**
     * 
     * @param index
    **/
    public org.openejb.alt.config.ejb11.EnterpriseBeansItem removeEnterpriseBeansItem(int index)
    {
        Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.EnterpriseBeansItem) obj;
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeansItem removeEnterpriseBeansItem(int) 

    /**
     * 
     * @param index
     * @param vEnterpriseBeansItem
    **/
    public void setEnterpriseBeansItem(int index, org.openejb.alt.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vEnterpriseBeansItem, index);
    } //-- void setEnterpriseBeansItem(int, org.openejb.alt.config.ejb11.EnterpriseBeansItem) 

    /**
     * 
     * @param enterpriseBeansItemArray
    **/
    public void setEnterpriseBeansItem(org.openejb.alt.config.ejb11.EnterpriseBeansItem[] enterpriseBeansItemArray)
    {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < enterpriseBeansItemArray.length; i++) {
            _items.addElement(enterpriseBeansItemArray[i]);
        }
    } //-- void setEnterpriseBeansItem(org.openejb.alt.config.ejb11.EnterpriseBeansItem) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.EnterpriseBeans unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EnterpriseBeans) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EnterpriseBeans.class, reader);
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeans unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
