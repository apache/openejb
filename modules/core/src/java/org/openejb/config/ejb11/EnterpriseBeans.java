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
 * Class EnterpriseBeans.
 *
 * @version $Revision$ $Date$
 */
public class EnterpriseBeans implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _items
     */
    private java.util.Vector _items;


    //----------------/
    //- Constructors -/
    //----------------/

    public EnterpriseBeans() {
        super();
        _items = new Vector();
    } //-- org.openejb.config.ejb11.EnterpriseBeans()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addEnterpriseBeansItem
     *
     * @param vEnterpriseBeansItem
     */
    public void addEnterpriseBeansItem(org.openejb.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.addElement(vEnterpriseBeansItem);
    } //-- void addEnterpriseBeansItem(org.openejb.config.ejb11.EnterpriseBeansItem) 

    /**
     * Method addEnterpriseBeansItem
     *
     * @param index
     * @param vEnterpriseBeansItem
     */
    public void addEnterpriseBeansItem(int index, org.openejb.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.insertElementAt(vEnterpriseBeansItem, index);
    } //-- void addEnterpriseBeansItem(int, org.openejb.config.ejb11.EnterpriseBeansItem) 

    /**
     * Method enumerateEnterpriseBeansItem
     */
    public java.util.Enumeration enumerateEnterpriseBeansItem() {
        return _items.elements();
    } //-- java.util.Enumeration enumerateEnterpriseBeansItem() 

    /**
     * Method getEnterpriseBeansItem
     *
     * @param index
     */
    public org.openejb.config.ejb11.EnterpriseBeansItem getEnterpriseBeansItem(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.EnterpriseBeansItem) _items.elementAt(index);
    } //-- org.openejb.config.ejb11.EnterpriseBeansItem getEnterpriseBeansItem(int) 

    /**
     * Method getEnterpriseBeansItem
     */
    public org.openejb.config.ejb11.EnterpriseBeansItem[] getEnterpriseBeansItem() {
        int size = _items.size();
        org.openejb.config.ejb11.EnterpriseBeansItem[] mArray = new org.openejb.config.ejb11.EnterpriseBeansItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.EnterpriseBeansItem) _items.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.EnterpriseBeansItem[] getEnterpriseBeansItem() 

    /**
     * Method getEnterpriseBeansItemCount
     */
    public int getEnterpriseBeansItemCount() {
        return _items.size();
    } //-- int getEnterpriseBeansItemCount() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

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
     * Method removeAllEnterpriseBeansItem
     */
    public void removeAllEnterpriseBeansItem() {
        _items.removeAllElements();
    } //-- void removeAllEnterpriseBeansItem() 

    /**
     * Method removeEnterpriseBeansItem
     *
     * @param index
     */
    public org.openejb.config.ejb11.EnterpriseBeansItem removeEnterpriseBeansItem(int index) {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (org.openejb.config.ejb11.EnterpriseBeansItem) obj;
    } //-- org.openejb.config.ejb11.EnterpriseBeansItem removeEnterpriseBeansItem(int) 

    /**
     * Method setEnterpriseBeansItem
     *
     * @param index
     * @param vEnterpriseBeansItem
     */
    public void setEnterpriseBeansItem(int index, org.openejb.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vEnterpriseBeansItem, index);
    } //-- void setEnterpriseBeansItem(int, org.openejb.config.ejb11.EnterpriseBeansItem) 

    /**
     * Method setEnterpriseBeansItem
     *
     * @param enterpriseBeansItemArray
     */
    public void setEnterpriseBeansItem(org.openejb.config.ejb11.EnterpriseBeansItem[] enterpriseBeansItemArray) {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < enterpriseBeansItemArray.length; i++) {
            _items.addElement(enterpriseBeansItemArray[i]);
        }
    } //-- void setEnterpriseBeansItem(org.openejb.config.ejb11.EnterpriseBeansItem) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.config.ejb11.EnterpriseBeans) Unmarshaller.unmarshal(org.openejb.config.ejb11.EnterpriseBeans.class, reader);
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
