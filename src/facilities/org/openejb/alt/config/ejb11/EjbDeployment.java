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

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

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


      //----------------/
     //- Constructors -/
    //----------------/

    public EjbDeployment() {
        super();
        _resourceLinkList = new Vector();
    } //-- org.openejb.alt.config.ejb11.EjbDeployment()


      //-----------/
     //- Methods -/
    //-----------/

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
    **/
    public java.util.Enumeration enumerateResourceLink()
    {
        return _resourceLinkList.elements();
    } //-- java.util.Enumeration enumerateResourceLink() 

    /**
    **/
    public java.lang.String getContainerId()
    {
        return this._containerId;
    } //-- java.lang.String getContainerId() 

    /**
    **/
    public java.lang.String getDeploymentId()
    {
        return this._deploymentId;
    } //-- java.lang.String getDeploymentId() 

    /**
    **/
    public java.lang.String getEjbName()
    {
        return this._ejbName;
    } //-- java.lang.String getEjbName() 

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
    public void removeAllResourceLink()
    {
        _resourceLinkList.removeAllElements();
    } //-- void removeAllResourceLink() 

    /**
     * 
     * @param index
    **/
    public ResourceLink removeResourceLink(int index)
    {
        Object obj = _resourceLinkList.elementAt(index);
        _resourceLinkList.removeElementAt(index);
        return (ResourceLink) obj;
    } //-- ResourceLink removeResourceLink(int) 

    /**
     * 
     * @param _containerId
    **/
    public void setContainerId(java.lang.String _containerId)
    {
        this._containerId = _containerId;
    } //-- void setContainerId(java.lang.String) 

    /**
     * 
     * @param _deploymentId
    **/
    public void setDeploymentId(java.lang.String _deploymentId)
    {
        this._deploymentId = _deploymentId;
    } //-- void setDeploymentId(java.lang.String) 

    /**
     * 
     * @param _ejbName
    **/
    public void setEjbName(java.lang.String _ejbName)
    {
        this._ejbName = _ejbName;
    } //-- void setEjbName(java.lang.String) 

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
