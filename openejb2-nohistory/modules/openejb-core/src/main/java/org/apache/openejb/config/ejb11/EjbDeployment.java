/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EjbDeployment.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class EjbDeployment.
 *
 * @version $Revision$ $Date$
 */
public class EjbDeployment implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _ejbName
     */
    private java.lang.String _ejbName;

    /**
     * Field _deploymentId
     */
    private java.lang.String _deploymentId;

    /**
     * Field _containerId
     */
    private java.lang.String _containerId;

    /**
     * Field _resourceLinkList
     */
    private java.util.Vector _resourceLinkList;

    /**
     * Field _queryList
     */
    private java.util.Vector _queryList;


    //----------------/
    //- Constructors -/
    //----------------/

    public EjbDeployment() {
        super();
        _resourceLinkList = new Vector();
        _queryList = new Vector();
    } //-- org.openejb.config.ejb11.EjbDeployment()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addQuery
     *
     * @param vQuery
     */
    public void addQuery(org.apache.openejb.config.ejb11.Query vQuery)
            throws java.lang.IndexOutOfBoundsException {
        _queryList.addElement(vQuery);
    } //-- void addQuery(org.openejb.config.ejb11.Query) 

    /**
     * Method addQuery
     *
     * @param index
     * @param vQuery
     */
    public void addQuery(int index, org.apache.openejb.config.ejb11.Query vQuery)
            throws java.lang.IndexOutOfBoundsException {
        _queryList.insertElementAt(vQuery, index);
    } //-- void addQuery(int, org.openejb.config.ejb11.Query) 

    /**
     * Method addResourceLink
     *
     * @param vResourceLink
     */
    public void addResourceLink(org.apache.openejb.config.ejb11.ResourceLink vResourceLink)
            throws java.lang.IndexOutOfBoundsException {
        _resourceLinkList.addElement(vResourceLink);
    } //-- void addResourceLink(org.openejb.config.ejb11.ResourceLink) 

    /**
     * Method addResourceLink
     *
     * @param index
     * @param vResourceLink
     */
    public void addResourceLink(int index, org.apache.openejb.config.ejb11.ResourceLink vResourceLink)
            throws java.lang.IndexOutOfBoundsException {
        _resourceLinkList.insertElementAt(vResourceLink, index);
    } //-- void addResourceLink(int, org.openejb.config.ejb11.ResourceLink) 

    /**
     * Method enumerateQuery
     */
    public java.util.Enumeration enumerateQuery() {
        return _queryList.elements();
    } //-- java.util.Enumeration enumerateQuery() 

    /**
     * Method enumerateResourceLink
     */
    public java.util.Enumeration enumerateResourceLink() {
        return _resourceLinkList.elements();
    } //-- java.util.Enumeration enumerateResourceLink() 

    /**
     * Returns the value of field 'containerId'.
     *
     * @return the value of field 'containerId'.
     */
    public java.lang.String getContainerId() {
        return this._containerId;
    } //-- java.lang.String getContainerId() 

    /**
     * Returns the value of field 'deploymentId'.
     *
     * @return the value of field 'deploymentId'.
     */
    public java.lang.String getDeploymentId() {
        return this._deploymentId;
    } //-- java.lang.String getDeploymentId() 

    /**
     * Returns the value of field 'ejbName'.
     *
     * @return the value of field 'ejbName'.
     */
    public java.lang.String getEjbName() {
        return this._ejbName;
    } //-- java.lang.String getEjbName() 

    /**
     * Method getQuery
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.Query getQuery(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _queryList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.Query) _queryList.elementAt(index);
    } //-- org.openejb.config.ejb11.Query getQuery(int) 

    /**
     * Method getQuery
     */
    public org.apache.openejb.config.ejb11.Query[] getQuery() {
        int size = _queryList.size();
        org.apache.openejb.config.ejb11.Query[] mArray = new org.apache.openejb.config.ejb11.Query[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.Query) _queryList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.Query[] getQuery() 

    /**
     * Method getQueryCount
     */
    public int getQueryCount() {
        return _queryList.size();
    } //-- int getQueryCount() 

    /**
     * Method getResourceLink
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.ResourceLink getResourceLink(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceLinkList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.ResourceLink) _resourceLinkList.elementAt(index);
    } //-- org.openejb.config.ejb11.ResourceLink getResourceLink(int) 

    /**
     * Method getResourceLink
     */
    public org.apache.openejb.config.ejb11.ResourceLink[] getResourceLink() {
        int size = _resourceLinkList.size();
        org.apache.openejb.config.ejb11.ResourceLink[] mArray = new org.apache.openejb.config.ejb11.ResourceLink[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.ResourceLink) _resourceLinkList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.ResourceLink[] getResourceLink() 

    /**
     * Method getResourceLinkCount
     */
    public int getResourceLinkCount() {
        return _resourceLinkList.size();
    } //-- int getResourceLinkCount() 

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
     * Method removeAllQuery
     */
    public void removeAllQuery() {
        _queryList.removeAllElements();
    } //-- void removeAllQuery() 

    /**
     * Method removeAllResourceLink
     */
    public void removeAllResourceLink() {
        _resourceLinkList.removeAllElements();
    } //-- void removeAllResourceLink() 

    /**
     * Method removeQuery
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.Query removeQuery(int index) {
        java.lang.Object obj = _queryList.elementAt(index);
        _queryList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.Query) obj;
    } //-- org.openejb.config.ejb11.Query removeQuery(int) 

    /**
     * Method removeResourceLink
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.ResourceLink removeResourceLink(int index) {
        java.lang.Object obj = _resourceLinkList.elementAt(index);
        _resourceLinkList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.ResourceLink) obj;
    } //-- org.openejb.config.ejb11.ResourceLink removeResourceLink(int) 

    /**
     * Sets the value of field 'containerId'.
     *
     * @param containerId the value of field 'containerId'.
     */
    public void setContainerId(java.lang.String containerId) {
        this._containerId = containerId;
    } //-- void setContainerId(java.lang.String) 

    /**
     * Sets the value of field 'deploymentId'.
     *
     * @param deploymentId the value of field 'deploymentId'.
     */
    public void setDeploymentId(java.lang.String deploymentId) {
        this._deploymentId = deploymentId;
    } //-- void setDeploymentId(java.lang.String) 

    /**
     * Sets the value of field 'ejbName'.
     *
     * @param ejbName the value of field 'ejbName'.
     */
    public void setEjbName(java.lang.String ejbName) {
        this._ejbName = ejbName;
    } //-- void setEjbName(java.lang.String) 

    /**
     * Method setQuery
     *
     * @param index
     * @param vQuery
     */
    public void setQuery(int index, org.apache.openejb.config.ejb11.Query vQuery)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _queryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _queryList.setElementAt(vQuery, index);
    } //-- void setQuery(int, org.openejb.config.ejb11.Query) 

    /**
     * Method setQuery
     *
     * @param queryArray
     */
    public void setQuery(org.apache.openejb.config.ejb11.Query[] queryArray) {
        //-- copy array
        _queryList.removeAllElements();
        for (int i = 0; i < queryArray.length; i++) {
            _queryList.addElement(queryArray[i]);
        }
    } //-- void setQuery(org.openejb.config.ejb11.Query) 

    /**
     * Method setResourceLink
     *
     * @param index
     * @param vResourceLink
     */
    public void setResourceLink(int index, org.apache.openejb.config.ejb11.ResourceLink vResourceLink)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceLinkList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceLinkList.setElementAt(vResourceLink, index);
    } //-- void setResourceLink(int, org.openejb.config.ejb11.ResourceLink) 

    /**
     * Method setResourceLink
     *
     * @param resourceLinkArray
     */
    public void setResourceLink(org.apache.openejb.config.ejb11.ResourceLink[] resourceLinkArray) {
        //-- copy array
        _resourceLinkList.removeAllElements();
        for (int i = 0; i < resourceLinkArray.length; i++) {
            _resourceLinkList.addElement(resourceLinkArray[i]);
        }
    } //-- void setResourceLink(org.openejb.config.ejb11.ResourceLink) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.EjbDeployment) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.EjbDeployment.class, reader);
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
