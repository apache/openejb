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
public class Session implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _displayName;

    private java.lang.String _smallIcon;

    private java.lang.String _largeIcon;

    private java.lang.String _ejbName;

    private java.lang.String _home;

    private java.lang.String _remote;

    private java.lang.String _ejbClass;

    private java.lang.String _sessionType;

    private java.lang.String _transactionType;

    private java.util.Vector _envEntryList;

    private java.util.Vector _ejbRefList;

    private java.util.Vector _securityRoleRefList;

    private java.util.Vector _resourceRefList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Session() {
        super();
        _envEntryList = new Vector();
        _ejbRefList = new Vector();
        _securityRoleRefList = new Vector();
        _resourceRefList = new Vector();
    } //-- org.openejb.alt.config.ejb11.Session()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vEjbRef
    **/
    public void addEjbRef(EjbRef vEjbRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _ejbRefList.addElement(vEjbRef);
    } //-- void addEjbRef(EjbRef) 

    /**
     * 
     * @param vEnvEntry
    **/
    public void addEnvEntry(EnvEntry vEnvEntry)
        throws java.lang.IndexOutOfBoundsException
    {
        _envEntryList.addElement(vEnvEntry);
    } //-- void addEnvEntry(EnvEntry) 

    /**
     * 
     * @param vResourceRef
    **/
    public void addResourceRef(ResourceRef vResourceRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceRefList.addElement(vResourceRef);
    } //-- void addResourceRef(ResourceRef) 

    /**
     * 
     * @param vSecurityRoleRef
    **/
    public void addSecurityRoleRef(SecurityRoleRef vSecurityRoleRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRoleRefList.addElement(vSecurityRoleRef);
    } //-- void addSecurityRoleRef(SecurityRoleRef) 

    /**
    **/
    public java.util.Enumeration enumerateEjbRef()
    {
        return _ejbRefList.elements();
    } //-- java.util.Enumeration enumerateEjbRef() 

    /**
    **/
    public java.util.Enumeration enumerateEnvEntry()
    {
        return _envEntryList.elements();
    } //-- java.util.Enumeration enumerateEnvEntry() 

    /**
    **/
    public java.util.Enumeration enumerateResourceRef()
    {
        return _resourceRefList.elements();
    } //-- java.util.Enumeration enumerateResourceRef() 

    /**
    **/
    public java.util.Enumeration enumerateSecurityRoleRef()
    {
        return _securityRoleRefList.elements();
    } //-- java.util.Enumeration enumerateSecurityRoleRef() 

    /**
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
    **/
    public java.lang.String getDisplayName()
    {
        return this._displayName;
    } //-- java.lang.String getDisplayName() 

    /**
    **/
    public java.lang.String getEjbClass()
    {
        return this._ejbClass;
    } //-- java.lang.String getEjbClass() 

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
    public EjbRef getEjbRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (EjbRef) _ejbRefList.elementAt(index);
    } //-- EjbRef getEjbRef(int) 

    /**
    **/
    public EjbRef[] getEjbRef()
    {
        int size = _ejbRefList.size();
        EjbRef[] mArray = new EjbRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (EjbRef) _ejbRefList.elementAt(index);
        }
        return mArray;
    } //-- EjbRef[] getEjbRef() 

    /**
    **/
    public int getEjbRefCount()
    {
        return _ejbRefList.size();
    } //-- int getEjbRefCount() 

    /**
     * 
     * @param index
    **/
    public EnvEntry getEnvEntry(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (EnvEntry) _envEntryList.elementAt(index);
    } //-- EnvEntry getEnvEntry(int) 

    /**
    **/
    public EnvEntry[] getEnvEntry()
    {
        int size = _envEntryList.size();
        EnvEntry[] mArray = new EnvEntry[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (EnvEntry) _envEntryList.elementAt(index);
        }
        return mArray;
    } //-- EnvEntry[] getEnvEntry() 

    /**
    **/
    public int getEnvEntryCount()
    {
        return _envEntryList.size();
    } //-- int getEnvEntryCount() 

    /**
    **/
    public java.lang.String getHome()
    {
        return this._home;
    } //-- java.lang.String getHome() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public java.lang.String getLargeIcon()
    {
        return this._largeIcon;
    } //-- java.lang.String getLargeIcon() 

    /**
    **/
    public java.lang.String getRemote()
    {
        return this._remote;
    } //-- java.lang.String getRemote() 

    /**
     * 
     * @param index
    **/
    public ResourceRef getResourceRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (ResourceRef) _resourceRefList.elementAt(index);
    } //-- ResourceRef getResourceRef(int) 

    /**
    **/
    public ResourceRef[] getResourceRef()
    {
        int size = _resourceRefList.size();
        ResourceRef[] mArray = new ResourceRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (ResourceRef) _resourceRefList.elementAt(index);
        }
        return mArray;
    } //-- ResourceRef[] getResourceRef() 

    /**
    **/
    public int getResourceRefCount()
    {
        return _resourceRefList.size();
    } //-- int getResourceRefCount() 

    /**
     * 
     * @param index
    **/
    public SecurityRoleRef getSecurityRoleRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (SecurityRoleRef) _securityRoleRefList.elementAt(index);
    } //-- SecurityRoleRef getSecurityRoleRef(int) 

    /**
    **/
    public SecurityRoleRef[] getSecurityRoleRef()
    {
        int size = _securityRoleRefList.size();
        SecurityRoleRef[] mArray = new SecurityRoleRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (SecurityRoleRef) _securityRoleRefList.elementAt(index);
        }
        return mArray;
    } //-- SecurityRoleRef[] getSecurityRoleRef() 

    /**
    **/
    public int getSecurityRoleRefCount()
    {
        return _securityRoleRefList.size();
    } //-- int getSecurityRoleRefCount() 

    /**
    **/
    public java.lang.String getSessionType()
    {
        return this._sessionType;
    } //-- java.lang.String getSessionType() 

    /**
    **/
    public java.lang.String getSmallIcon()
    {
        return this._smallIcon;
    } //-- java.lang.String getSmallIcon() 

    /**
    **/
    public java.lang.String getTransactionType()
    {
        return this._transactionType;
    } //-- java.lang.String getTransactionType() 

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
    public void removeAllEjbRef()
    {
        _ejbRefList.removeAllElements();
    } //-- void removeAllEjbRef() 

    /**
    **/
    public void removeAllEnvEntry()
    {
        _envEntryList.removeAllElements();
    } //-- void removeAllEnvEntry() 

    /**
    **/
    public void removeAllResourceRef()
    {
        _resourceRefList.removeAllElements();
    } //-- void removeAllResourceRef() 

    /**
    **/
    public void removeAllSecurityRoleRef()
    {
        _securityRoleRefList.removeAllElements();
    } //-- void removeAllSecurityRoleRef() 

    /**
     * 
     * @param index
    **/
    public EjbRef removeEjbRef(int index)
    {
        Object obj = _ejbRefList.elementAt(index);
        _ejbRefList.removeElementAt(index);
        return (EjbRef) obj;
    } //-- EjbRef removeEjbRef(int) 

    /**
     * 
     * @param index
    **/
    public EnvEntry removeEnvEntry(int index)
    {
        Object obj = _envEntryList.elementAt(index);
        _envEntryList.removeElementAt(index);
        return (EnvEntry) obj;
    } //-- EnvEntry removeEnvEntry(int) 

    /**
     * 
     * @param index
    **/
    public ResourceRef removeResourceRef(int index)
    {
        Object obj = _resourceRefList.elementAt(index);
        _resourceRefList.removeElementAt(index);
        return (ResourceRef) obj;
    } //-- ResourceRef removeResourceRef(int) 

    /**
     * 
     * @param index
    **/
    public SecurityRoleRef removeSecurityRoleRef(int index)
    {
        Object obj = _securityRoleRefList.elementAt(index);
        _securityRoleRefList.removeElementAt(index);
        return (SecurityRoleRef) obj;
    } //-- SecurityRoleRef removeSecurityRoleRef(int) 

    /**
     * 
     * @param _description
    **/
    public void setDescription(java.lang.String _description)
    {
        this._description = _description;
    } //-- void setDescription(java.lang.String) 

    /**
     * 
     * @param _displayName
    **/
    public void setDisplayName(java.lang.String _displayName)
    {
        this._displayName = _displayName;
    } //-- void setDisplayName(java.lang.String) 

    /**
     * 
     * @param _ejbClass
    **/
    public void setEjbClass(java.lang.String _ejbClass)
    {
        this._ejbClass = _ejbClass;
    } //-- void setEjbClass(java.lang.String) 

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
     * @param vEjbRef
    **/
    public void setEjbRef(int index, EjbRef vEjbRef)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbRefList.setElementAt(vEjbRef, index);
    } //-- void setEjbRef(int, EjbRef) 

    /**
     * 
     * @param ejbRefArray
    **/
    public void setEjbRef(EjbRef[] ejbRefArray)
    {
        //-- copy array
        _ejbRefList.removeAllElements();
        for (int i = 0; i < ejbRefArray.length; i++) {
            _ejbRefList.addElement(ejbRefArray[i]);
        }
    } //-- void setEjbRef(EjbRef) 

    /**
     * 
     * @param index
     * @param vEnvEntry
    **/
    public void setEnvEntry(int index, EnvEntry vEnvEntry)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _envEntryList.setElementAt(vEnvEntry, index);
    } //-- void setEnvEntry(int, EnvEntry) 

    /**
     * 
     * @param envEntryArray
    **/
    public void setEnvEntry(EnvEntry[] envEntryArray)
    {
        //-- copy array
        _envEntryList.removeAllElements();
        for (int i = 0; i < envEntryArray.length; i++) {
            _envEntryList.addElement(envEntryArray[i]);
        }
    } //-- void setEnvEntry(EnvEntry) 

    /**
     * 
     * @param _home
    **/
    public void setHome(java.lang.String _home)
    {
        this._home = _home;
    } //-- void setHome(java.lang.String) 

    /**
     * 
     * @param _id
    **/
    public void setId(java.lang.String _id)
    {
        this._id = _id;
    } //-- void setId(java.lang.String) 

    /**
     * 
     * @param _largeIcon
    **/
    public void setLargeIcon(java.lang.String _largeIcon)
    {
        this._largeIcon = _largeIcon;
    } //-- void setLargeIcon(java.lang.String) 

    /**
     * 
     * @param _remote
    **/
    public void setRemote(java.lang.String _remote)
    {
        this._remote = _remote;
    } //-- void setRemote(java.lang.String) 

    /**
     * 
     * @param index
     * @param vResourceRef
    **/
    public void setResourceRef(int index, ResourceRef vResourceRef)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceRefList.setElementAt(vResourceRef, index);
    } //-- void setResourceRef(int, ResourceRef) 

    /**
     * 
     * @param resourceRefArray
    **/
    public void setResourceRef(ResourceRef[] resourceRefArray)
    {
        //-- copy array
        _resourceRefList.removeAllElements();
        for (int i = 0; i < resourceRefArray.length; i++) {
            _resourceRefList.addElement(resourceRefArray[i]);
        }
    } //-- void setResourceRef(ResourceRef) 

    /**
     * 
     * @param index
     * @param vSecurityRoleRef
    **/
    public void setSecurityRoleRef(int index, SecurityRoleRef vSecurityRoleRef)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRoleRefList.setElementAt(vSecurityRoleRef, index);
    } //-- void setSecurityRoleRef(int, SecurityRoleRef) 

    /**
     * 
     * @param securityRoleRefArray
    **/
    public void setSecurityRoleRef(SecurityRoleRef[] securityRoleRefArray)
    {
        //-- copy array
        _securityRoleRefList.removeAllElements();
        for (int i = 0; i < securityRoleRefArray.length; i++) {
            _securityRoleRefList.addElement(securityRoleRefArray[i]);
        }
    } //-- void setSecurityRoleRef(SecurityRoleRef) 

    /**
     * 
     * @param _sessionType
    **/
    public void setSessionType(java.lang.String _sessionType)
    {
        this._sessionType = _sessionType;
    } //-- void setSessionType(java.lang.String) 

    /**
     * 
     * @param _smallIcon
    **/
    public void setSmallIcon(java.lang.String _smallIcon)
    {
        this._smallIcon = _smallIcon;
    } //-- void setSmallIcon(java.lang.String) 

    /**
     * 
     * @param _transactionType
    **/
    public void setTransactionType(java.lang.String _transactionType)
    {
        this._transactionType = _transactionType;
    } //-- void setTransactionType(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.Session unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.Session) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.Session.class, reader);
    } //-- org.openejb.alt.config.ejb11.Session unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
