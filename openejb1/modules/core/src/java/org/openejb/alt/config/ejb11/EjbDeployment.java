/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
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
public class EjbDeployment implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _ejbName;

    private java.lang.String _deploymentId;

    private java.lang.String _containerId;

    private java.util.Vector _resourceLinkList;

    private java.util.Vector _queryList;


      //----------------/
     //- Constructors -/
    //----------------/

    public EjbDeployment() {
        super();
        _resourceLinkList = new Vector();
        _queryList = new Vector();
    } //-- org.openejb.alt.config.ejb11.EjbDeployment()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vQuery
    **/
    public void addQuery(Query vQuery)
        throws java.lang.IndexOutOfBoundsException
    {
        _queryList.addElement(vQuery);
    } //-- void addQuery(Query) 

    /**
     * 
     * @param index
     * @param vQuery
    **/
    public void addQuery(int index, Query vQuery)
        throws java.lang.IndexOutOfBoundsException
    {
        _queryList.insertElementAt(vQuery, index);
    } //-- void addQuery(int, Query) 

    /**
     * 
     * @param vResourceLink
    **/
    public void addResourceLink(ResourceLink vResourceLink)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceLinkList.addElement(vResourceLink);
    } //-- void addResourceLink(ResourceLink) 

    /**
     * 
     * @param index
     * @param vResourceLink
    **/
    public void addResourceLink(int index, ResourceLink vResourceLink)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceLinkList.insertElementAt(vResourceLink, index);
    } //-- void addResourceLink(int, ResourceLink) 

    /**
    **/
    public java.util.Enumeration enumerateQuery()
    {
        return _queryList.elements();
    } //-- java.util.Enumeration enumerateQuery() 

    /**
    **/
    public java.util.Enumeration enumerateResourceLink()
    {
        return _resourceLinkList.elements();
    } //-- java.util.Enumeration enumerateResourceLink() 

    /**
     * Returns the value of field 'containerId'.
     * @return the value of field 'containerId'.
    **/
    public java.lang.String getContainerId()
    {
        return this._containerId;
    } //-- java.lang.String getContainerId() 

    /**
     * Returns the value of field 'deploymentId'.
     * @return the value of field 'deploymentId'.
    **/
    public java.lang.String getDeploymentId()
    {
        return this._deploymentId;
    } //-- java.lang.String getDeploymentId() 

    /**
     * Returns the value of field 'ejbName'.
     * @return the value of field 'ejbName'.
    **/
    public java.lang.String getEjbName()
    {
        return this._ejbName;
    } //-- java.lang.String getEjbName() 

    /**
     * 
     * @param index
    **/
    public Query getQuery(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _queryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Query) _queryList.elementAt(index);
    } //-- Query getQuery(int) 

    /**
    **/
    public Query[] getQuery()
    {
        int size = _queryList.size();
        Query[] mArray = new Query[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Query) _queryList.elementAt(index);
        }
        return mArray;
    } //-- Query[] getQuery() 

    /**
    **/
    public int getQueryCount()
    {
        return _queryList.size();
    } //-- int getQueryCount() 

    /**
     * 
     * @param index
    **/
    public ResourceLink getResourceLink(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceLinkList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (ResourceLink) _resourceLinkList.elementAt(index);
    } //-- ResourceLink getResourceLink(int) 

    /**
    **/
    public ResourceLink[] getResourceLink()
    {
        int size = _resourceLinkList.size();
        ResourceLink[] mArray = new ResourceLink[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (ResourceLink) _resourceLinkList.elementAt(index);
        }
        return mArray;
    } //-- ResourceLink[] getResourceLink() 

    /**
    **/
    public int getResourceLinkCount()
    {
        return _resourceLinkList.size();
    } //-- int getResourceLinkCount() 

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
    public void removeAllQuery()
    {
        _queryList.removeAllElements();
    } //-- void removeAllQuery() 

    /**
    **/
    public void removeAllResourceLink()
    {
        _resourceLinkList.removeAllElements();
    } //-- void removeAllResourceLink() 

    /**
     * 
     * @param index
    **/
    public Query removeQuery(int index)
    {
        java.lang.Object obj = _queryList.elementAt(index);
        _queryList.removeElementAt(index);
        return (Query) obj;
    } //-- Query removeQuery(int) 

    /**
     * 
     * @param index
    **/
    public ResourceLink removeResourceLink(int index)
    {
        java.lang.Object obj = _resourceLinkList.elementAt(index);
        _resourceLinkList.removeElementAt(index);
        return (ResourceLink) obj;
    } //-- ResourceLink removeResourceLink(int) 

    /**
     * Sets the value of field 'containerId'.
     * @param containerId the value of field 'containerId'.
    **/
    public void setContainerId(java.lang.String containerId)
    {
        this._containerId = containerId;
    } //-- void setContainerId(java.lang.String) 

    /**
     * Sets the value of field 'deploymentId'.
     * @param deploymentId the value of field 'deploymentId'.
    **/
    public void setDeploymentId(java.lang.String deploymentId)
    {
        this._deploymentId = deploymentId;
    } //-- void setDeploymentId(java.lang.String) 

    /**
     * Sets the value of field 'ejbName'.
     * @param ejbName the value of field 'ejbName'.
    **/
    public void setEjbName(java.lang.String ejbName)
    {
        this._ejbName = ejbName;
    } //-- void setEjbName(java.lang.String) 

    /**
     * 
     * @param index
     * @param vQuery
    **/
    public void setQuery(int index, Query vQuery)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _queryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _queryList.setElementAt(vQuery, index);
    } //-- void setQuery(int, Query) 

    /**
     * 
     * @param queryArray
    **/
    public void setQuery(Query[] queryArray)
    {
        //-- copy array
        _queryList.removeAllElements();
        for (int i = 0; i < queryArray.length; i++) {
            _queryList.addElement(queryArray[i]);
        }
    } //-- void setQuery(Query) 

    /**
     * 
     * @param index
     * @param vResourceLink
    **/
    public void setResourceLink(int index, ResourceLink vResourceLink)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceLinkList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceLinkList.setElementAt(vResourceLink, index);
    } //-- void setResourceLink(int, ResourceLink) 

    /**
     * 
     * @param resourceLinkArray
    **/
    public void setResourceLink(ResourceLink[] resourceLinkArray)
    {
        //-- copy array
        _resourceLinkList.removeAllElements();
        for (int i = 0; i < resourceLinkArray.length; i++) {
            _resourceLinkList.addElement(resourceLinkArray[i]);
        }
    } //-- void setResourceLink(ResourceLink) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.EjbDeployment unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbDeployment) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbDeployment.class, reader);
    } //-- org.openejb.alt.config.ejb11.EjbDeployment unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
