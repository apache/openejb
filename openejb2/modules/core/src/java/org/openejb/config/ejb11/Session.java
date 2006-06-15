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
 * Class Session.
 *
 * @version $Revision$ $Date$
 */
public class Session implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _displayName
     */
    private java.lang.String _displayName;

    /**
     * Field _smallIcon
     */
    private java.lang.String _smallIcon;

    /**
     * Field _largeIcon
     */
    private java.lang.String _largeIcon;

    /**
     * Field _ejbName
     */
    private java.lang.String _ejbName;

    /**
     * Field _home
     */
    private java.lang.String _home;

    /**
     * Field _remote
     */
    private java.lang.String _remote;

    /**
     * Field _localHome
     */
    private java.lang.String _localHome;

    /**
     * Field _local
     */
    private java.lang.String _local;

    /**
     * Field _ejbClass
     */
    private java.lang.String _ejbClass;

    /**
     * Field _sessionType
     */
    private java.lang.String _sessionType;

    /**
     * Field _transactionType
     */
    private java.lang.String _transactionType;

    /**
     * Field _envEntryList
     */
    private java.util.Vector _envEntryList;

    /**
     * Field _ejbRefList
     */
    private java.util.Vector _ejbRefList;

    /**
     * Field _ejbLocalRefList
     */
    private java.util.Vector _ejbLocalRefList;

    /**
     * Field _securityRoleRefList
     */
    private java.util.Vector _securityRoleRefList;

    /**
     * Field _resourceRefList
     */
    private java.util.Vector _resourceRefList;


    //----------------/
    //- Constructors -/
    //----------------/

    public Session() {
        super();
        _envEntryList = new Vector();
        _ejbRefList = new Vector();
        _ejbLocalRefList = new Vector();
        _securityRoleRefList = new Vector();
        _resourceRefList = new Vector();
    } //-- org.openejb.config.ejb11.Session()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addEjbLocalRef
     *
     * @param vEjbLocalRef
     */
    public void addEjbLocalRef(org.openejb.config.ejb11.EjbLocalRef vEjbLocalRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbLocalRefList.addElement(vEjbLocalRef);
    } //-- void addEjbLocalRef(org.openejb.config.ejb11.EjbLocalRef) 

    /**
     * Method addEjbLocalRef
     *
     * @param index
     * @param vEjbLocalRef
     */
    public void addEjbLocalRef(int index, org.openejb.config.ejb11.EjbLocalRef vEjbLocalRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbLocalRefList.insertElementAt(vEjbLocalRef, index);
    } //-- void addEjbLocalRef(int, org.openejb.config.ejb11.EjbLocalRef) 

    /**
     * Method addEjbRef
     *
     * @param vEjbRef
     */
    public void addEjbRef(org.openejb.config.ejb11.EjbRef vEjbRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbRefList.addElement(vEjbRef);
    } //-- void addEjbRef(org.openejb.config.ejb11.EjbRef) 

    /**
     * Method addEjbRef
     *
     * @param index
     * @param vEjbRef
     */
    public void addEjbRef(int index, org.openejb.config.ejb11.EjbRef vEjbRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbRefList.insertElementAt(vEjbRef, index);
    } //-- void addEjbRef(int, org.openejb.config.ejb11.EjbRef) 

    /**
     * Method addEnvEntry
     *
     * @param vEnvEntry
     */
    public void addEnvEntry(org.openejb.config.ejb11.EnvEntry vEnvEntry)
            throws java.lang.IndexOutOfBoundsException {
        _envEntryList.addElement(vEnvEntry);
    } //-- void addEnvEntry(org.openejb.config.ejb11.EnvEntry) 

    /**
     * Method addEnvEntry
     *
     * @param index
     * @param vEnvEntry
     */
    public void addEnvEntry(int index, org.openejb.config.ejb11.EnvEntry vEnvEntry)
            throws java.lang.IndexOutOfBoundsException {
        _envEntryList.insertElementAt(vEnvEntry, index);
    } //-- void addEnvEntry(int, org.openejb.config.ejb11.EnvEntry) 

    /**
     * Method addResourceRef
     *
     * @param vResourceRef
     */
    public void addResourceRef(org.openejb.config.ejb11.ResourceRef vResourceRef)
            throws java.lang.IndexOutOfBoundsException {
        _resourceRefList.addElement(vResourceRef);
    } //-- void addResourceRef(org.openejb.config.ejb11.ResourceRef) 

    /**
     * Method addResourceRef
     *
     * @param index
     * @param vResourceRef
     */
    public void addResourceRef(int index, org.openejb.config.ejb11.ResourceRef vResourceRef)
            throws java.lang.IndexOutOfBoundsException {
        _resourceRefList.insertElementAt(vResourceRef, index);
    } //-- void addResourceRef(int, org.openejb.config.ejb11.ResourceRef) 

    /**
     * Method addSecurityRoleRef
     *
     * @param vSecurityRoleRef
     */
    public void addSecurityRoleRef(org.openejb.config.ejb11.SecurityRoleRef vSecurityRoleRef)
            throws java.lang.IndexOutOfBoundsException {
        _securityRoleRefList.addElement(vSecurityRoleRef);
    } //-- void addSecurityRoleRef(org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Method addSecurityRoleRef
     *
     * @param index
     * @param vSecurityRoleRef
     */
    public void addSecurityRoleRef(int index, org.openejb.config.ejb11.SecurityRoleRef vSecurityRoleRef)
            throws java.lang.IndexOutOfBoundsException {
        _securityRoleRefList.insertElementAt(vSecurityRoleRef, index);
    } //-- void addSecurityRoleRef(int, org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Method enumerateEjbLocalRef
     *
     * @return Enumeration
     */
    public java.util.Enumeration enumerateEjbLocalRef() {
        return _ejbLocalRefList.elements();
    } //-- java.util.Enumeration enumerateEjbLocalRef() 

    /**
     * Method enumerateEjbRef
     */
    public java.util.Enumeration enumerateEjbRef() {
        return _ejbRefList.elements();
    } //-- java.util.Enumeration enumerateEjbRef() 

    /**
     * Method enumerateEnvEntry
     */
    public java.util.Enumeration enumerateEnvEntry() {
        return _envEntryList.elements();
    } //-- java.util.Enumeration enumerateEnvEntry() 

    /**
     * Method enumerateResourceRef
     */
    public java.util.Enumeration enumerateResourceRef() {
        return _resourceRefList.elements();
    } //-- java.util.Enumeration enumerateResourceRef() 

    /**
     * Method enumerateSecurityRoleRef
     */
    public java.util.Enumeration enumerateSecurityRoleRef() {
        return _securityRoleRefList.elements();
    } //-- java.util.Enumeration enumerateSecurityRoleRef() 

    /**
     * Returns the value of field 'description'.
     *
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription() {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'displayName'.
     *
     * @return the value of field 'displayName'.
     */
    public java.lang.String getDisplayName() {
        return this._displayName;
    } //-- java.lang.String getDisplayName() 

    /**
     * Returns the value of field 'ejbClass'.
     *
     * @return the value of field 'ejbClass'.
     */
    public java.lang.String getEjbClass() {
        return this._ejbClass;
    } //-- java.lang.String getEjbClass() 

    /**
     * Method getEjbLocalRef
     *
     * @param index
     * @return EjbLocalRef
     */
    public org.openejb.config.ejb11.EjbLocalRef getEjbLocalRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbLocalRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.EjbLocalRef) _ejbLocalRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.EjbLocalRef getEjbLocalRef(int) 

    /**
     * Method getEjbLocalRef
     *
     * @return EjbLocalRef
     */
    public org.openejb.config.ejb11.EjbLocalRef[] getEjbLocalRef() {
        int size = _ejbLocalRefList.size();
        org.openejb.config.ejb11.EjbLocalRef[] mArray = new org.openejb.config.ejb11.EjbLocalRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.EjbLocalRef) _ejbLocalRefList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.EjbLocalRef[] getEjbLocalRef() 

    /**
     * Method getEjbLocalRefCount
     *
     * @return int
     */
    public int getEjbLocalRefCount() {
        return _ejbLocalRefList.size();
    } //-- int getEjbLocalRefCount() 

    /**
     * Returns the value of field 'ejbName'.
     *
     * @return the value of field 'ejbName'.
     */
    public java.lang.String getEjbName() {
        return this._ejbName;
    } //-- java.lang.String getEjbName() 

    /**
     * Method getEjbRef
     *
     * @param index
     */
    public org.openejb.config.ejb11.EjbRef getEjbRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.EjbRef) _ejbRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.EjbRef getEjbRef(int) 

    /**
     * Method getEjbRef
     */
    public org.openejb.config.ejb11.EjbRef[] getEjbRef() {
        int size = _ejbRefList.size();
        org.openejb.config.ejb11.EjbRef[] mArray = new org.openejb.config.ejb11.EjbRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.EjbRef) _ejbRefList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.EjbRef[] getEjbRef() 

    /**
     * Method getEjbRefCount
     */
    public int getEjbRefCount() {
        return _ejbRefList.size();
    } //-- int getEjbRefCount() 

    /**
     * Method getEnvEntry
     *
     * @param index
     */
    public org.openejb.config.ejb11.EnvEntry getEnvEntry(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.EnvEntry) _envEntryList.elementAt(index);
    } //-- org.openejb.config.ejb11.EnvEntry getEnvEntry(int) 

    /**
     * Method getEnvEntry
     */
    public org.openejb.config.ejb11.EnvEntry[] getEnvEntry() {
        int size = _envEntryList.size();
        org.openejb.config.ejb11.EnvEntry[] mArray = new org.openejb.config.ejb11.EnvEntry[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.EnvEntry) _envEntryList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.EnvEntry[] getEnvEntry() 

    /**
     * Method getEnvEntryCount
     */
    public int getEnvEntryCount() {
        return _envEntryList.size();
    } //-- int getEnvEntryCount() 

    /**
     * Returns the value of field 'home'.
     *
     * @return the value of field 'home'.
     */
    public java.lang.String getHome() {
        return this._home;
    } //-- java.lang.String getHome() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'largeIcon'.
     *
     * @return the value of field 'largeIcon'.
     */
    public java.lang.String getLargeIcon() {
        return this._largeIcon;
    } //-- java.lang.String getLargeIcon() 

    /**
     * Returns the value of field 'local'.
     *
     * @return the value of field 'local'.
     */
    public java.lang.String getLocal() {
        return this._local;
    } //-- java.lang.String getLocal() 

    /**
     * Returns the value of field 'localHome'.
     *
     * @return the value of field 'localHome'.
     */
    public java.lang.String getLocalHome() {
        return this._localHome;
    } //-- java.lang.String getLocalHome() 

    /**
     * Returns the value of field 'remote'.
     *
     * @return the value of field 'remote'.
     */
    public java.lang.String getRemote() {
        return this._remote;
    } //-- java.lang.String getRemote() 

    /**
     * Method getResourceRef
     *
     * @param index
     */
    public org.openejb.config.ejb11.ResourceRef getResourceRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.ResourceRef) _resourceRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.ResourceRef getResourceRef(int) 

    /**
     * Method getResourceRef
     */
    public org.openejb.config.ejb11.ResourceRef[] getResourceRef() {
        int size = _resourceRefList.size();
        org.openejb.config.ejb11.ResourceRef[] mArray = new org.openejb.config.ejb11.ResourceRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.ResourceRef) _resourceRefList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.ResourceRef[] getResourceRef() 

    /**
     * Method getResourceRefCount
     */
    public int getResourceRefCount() {
        return _resourceRefList.size();
    } //-- int getResourceRefCount() 

    /**
     * Method getSecurityRoleRef
     *
     * @param index
     */
    public org.openejb.config.ejb11.SecurityRoleRef getSecurityRoleRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.config.ejb11.SecurityRoleRef) _securityRoleRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.SecurityRoleRef getSecurityRoleRef(int) 

    /**
     * Method getSecurityRoleRef
     */
    public org.openejb.config.ejb11.SecurityRoleRef[] getSecurityRoleRef() {
        int size = _securityRoleRefList.size();
        org.openejb.config.ejb11.SecurityRoleRef[] mArray = new org.openejb.config.ejb11.SecurityRoleRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.config.ejb11.SecurityRoleRef) _securityRoleRefList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.SecurityRoleRef[] getSecurityRoleRef() 

    /**
     * Method getSecurityRoleRefCount
     */
    public int getSecurityRoleRefCount() {
        return _securityRoleRefList.size();
    } //-- int getSecurityRoleRefCount() 

    /**
     * Returns the value of field 'sessionType'.
     *
     * @return the value of field 'sessionType'.
     */
    public java.lang.String getSessionType() {
        return this._sessionType;
    } //-- java.lang.String getSessionType() 

    /**
     * Returns the value of field 'smallIcon'.
     *
     * @return the value of field 'smallIcon'.
     */
    public java.lang.String getSmallIcon() {
        return this._smallIcon;
    } //-- java.lang.String getSmallIcon() 

    /**
     * Returns the value of field 'transactionType'.
     *
     * @return the value of field 'transactionType'.
     */
    public java.lang.String getTransactionType() {
        return this._transactionType;
    } //-- java.lang.String getTransactionType() 

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
     * Method removeAllEjbLocalRef
     */
    public void removeAllEjbLocalRef() {
        _ejbLocalRefList.removeAllElements();
    } //-- void removeAllEjbLocalRef() 

    /**
     * Method removeAllEjbRef
     */
    public void removeAllEjbRef() {
        _ejbRefList.removeAllElements();
    } //-- void removeAllEjbRef() 

    /**
     * Method removeAllEnvEntry
     */
    public void removeAllEnvEntry() {
        _envEntryList.removeAllElements();
    } //-- void removeAllEnvEntry() 

    /**
     * Method removeAllResourceRef
     */
    public void removeAllResourceRef() {
        _resourceRefList.removeAllElements();
    } //-- void removeAllResourceRef() 

    /**
     * Method removeAllSecurityRoleRef
     */
    public void removeAllSecurityRoleRef() {
        _securityRoleRefList.removeAllElements();
    } //-- void removeAllSecurityRoleRef() 

    /**
     * Method removeEjbLocalRef
     *
     * @param index
     * @return EjbLocalRef
     */
    public org.openejb.config.ejb11.EjbLocalRef removeEjbLocalRef(int index) {
        java.lang.Object obj = _ejbLocalRefList.elementAt(index);
        _ejbLocalRefList.removeElementAt(index);
        return (org.openejb.config.ejb11.EjbLocalRef) obj;
    } //-- org.openejb.config.ejb11.EjbLocalRef removeEjbLocalRef(int) 

    /**
     * Method removeEjbRef
     *
     * @param index
     */
    public org.openejb.config.ejb11.EjbRef removeEjbRef(int index) {
        java.lang.Object obj = _ejbRefList.elementAt(index);
        _ejbRefList.removeElementAt(index);
        return (org.openejb.config.ejb11.EjbRef) obj;
    } //-- org.openejb.config.ejb11.EjbRef removeEjbRef(int) 

    /**
     * Method removeEnvEntry
     *
     * @param index
     */
    public org.openejb.config.ejb11.EnvEntry removeEnvEntry(int index) {
        java.lang.Object obj = _envEntryList.elementAt(index);
        _envEntryList.removeElementAt(index);
        return (org.openejb.config.ejb11.EnvEntry) obj;
    } //-- org.openejb.config.ejb11.EnvEntry removeEnvEntry(int) 

    /**
     * Method removeResourceRef
     *
     * @param index
     */
    public org.openejb.config.ejb11.ResourceRef removeResourceRef(int index) {
        java.lang.Object obj = _resourceRefList.elementAt(index);
        _resourceRefList.removeElementAt(index);
        return (org.openejb.config.ejb11.ResourceRef) obj;
    } //-- org.openejb.config.ejb11.ResourceRef removeResourceRef(int) 

    /**
     * Method removeSecurityRoleRef
     *
     * @param index
     */
    public org.openejb.config.ejb11.SecurityRoleRef removeSecurityRoleRef(int index) {
        java.lang.Object obj = _securityRoleRefList.elementAt(index);
        _securityRoleRefList.removeElementAt(index);
        return (org.openejb.config.ejb11.SecurityRoleRef) obj;
    } //-- org.openejb.config.ejb11.SecurityRoleRef removeSecurityRoleRef(int) 

    /**
     * Sets the value of field 'description'.
     *
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description) {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'displayName'.
     *
     * @param displayName the value of field 'displayName'.
     */
    public void setDisplayName(java.lang.String displayName) {
        this._displayName = displayName;
    } //-- void setDisplayName(java.lang.String) 

    /**
     * Sets the value of field 'ejbClass'.
     *
     * @param ejbClass the value of field 'ejbClass'.
     */
    public void setEjbClass(java.lang.String ejbClass) {
        this._ejbClass = ejbClass;
    } //-- void setEjbClass(java.lang.String) 

    /**
     * Method setEjbLocalRef
     *
     * @param index
     * @param vEjbLocalRef
     */
    public void setEjbLocalRef(int index, org.openejb.config.ejb11.EjbLocalRef vEjbLocalRef)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbLocalRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbLocalRefList.setElementAt(vEjbLocalRef, index);
    } //-- void setEjbLocalRef(int, org.openejb.config.ejb11.EjbLocalRef) 

    /**
     * Method setEjbLocalRef
     *
     * @param ejbLocalRefArray
     */
    public void setEjbLocalRef(org.openejb.config.ejb11.EjbLocalRef[] ejbLocalRefArray) {
        //-- copy array
        _ejbLocalRefList.removeAllElements();
        for (int i = 0; i < ejbLocalRefArray.length; i++) {
            _ejbLocalRefList.addElement(ejbLocalRefArray[i]);
        }
    } //-- void setEjbLocalRef(org.openejb.config.ejb11.EjbLocalRef) 

    /**
     * Sets the value of field 'ejbName'.
     *
     * @param ejbName the value of field 'ejbName'.
     */
    public void setEjbName(java.lang.String ejbName) {
        this._ejbName = ejbName;
    } //-- void setEjbName(java.lang.String) 

    /**
     * Method setEjbRef
     *
     * @param index
     * @param vEjbRef
     */
    public void setEjbRef(int index, org.openejb.config.ejb11.EjbRef vEjbRef)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbRefList.setElementAt(vEjbRef, index);
    } //-- void setEjbRef(int, org.openejb.config.ejb11.EjbRef) 

    /**
     * Method setEjbRef
     *
     * @param ejbRefArray
     */
    public void setEjbRef(org.openejb.config.ejb11.EjbRef[] ejbRefArray) {
        //-- copy array
        _ejbRefList.removeAllElements();
        for (int i = 0; i < ejbRefArray.length; i++) {
            _ejbRefList.addElement(ejbRefArray[i]);
        }
    } //-- void setEjbRef(org.openejb.config.ejb11.EjbRef) 

    /**
     * Method setEnvEntry
     *
     * @param index
     * @param vEnvEntry
     */
    public void setEnvEntry(int index, org.openejb.config.ejb11.EnvEntry vEnvEntry)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _envEntryList.setElementAt(vEnvEntry, index);
    } //-- void setEnvEntry(int, org.openejb.config.ejb11.EnvEntry) 

    /**
     * Method setEnvEntry
     *
     * @param envEntryArray
     */
    public void setEnvEntry(org.openejb.config.ejb11.EnvEntry[] envEntryArray) {
        //-- copy array
        _envEntryList.removeAllElements();
        for (int i = 0; i < envEntryArray.length; i++) {
            _envEntryList.addElement(envEntryArray[i]);
        }
    } //-- void setEnvEntry(org.openejb.config.ejb11.EnvEntry) 

    /**
     * Sets the value of field 'home'.
     *
     * @param home the value of field 'home'.
     */
    public void setHome(java.lang.String home) {
        this._home = home;
    } //-- void setHome(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'largeIcon'.
     *
     * @param largeIcon the value of field 'largeIcon'.
     */
    public void setLargeIcon(java.lang.String largeIcon) {
        this._largeIcon = largeIcon;
    } //-- void setLargeIcon(java.lang.String) 

    /**
     * Sets the value of field 'local'.
     *
     * @param local the value of field 'local'.
     */
    public void setLocal(java.lang.String local) {
        this._local = local;
    } //-- void setLocal(java.lang.String) 

    /**
     * Sets the value of field 'localHome'.
     *
     * @param localHome the value of field 'localHome'.
     */
    public void setLocalHome(java.lang.String localHome) {
        this._localHome = localHome;
    } //-- void setLocalHome(java.lang.String) 

    /**
     * Sets the value of field 'remote'.
     *
     * @param remote the value of field 'remote'.
     */
    public void setRemote(java.lang.String remote) {
        this._remote = remote;
    } //-- void setRemote(java.lang.String) 

    /**
     * Method setResourceRef
     *
     * @param index
     * @param vResourceRef
     */
    public void setResourceRef(int index, org.openejb.config.ejb11.ResourceRef vResourceRef)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceRefList.setElementAt(vResourceRef, index);
    } //-- void setResourceRef(int, org.openejb.config.ejb11.ResourceRef) 

    /**
     * Method setResourceRef
     *
     * @param resourceRefArray
     */
    public void setResourceRef(org.openejb.config.ejb11.ResourceRef[] resourceRefArray) {
        //-- copy array
        _resourceRefList.removeAllElements();
        for (int i = 0; i < resourceRefArray.length; i++) {
            _resourceRefList.addElement(resourceRefArray[i]);
        }
    } //-- void setResourceRef(org.openejb.config.ejb11.ResourceRef) 

    /**
     * Method setSecurityRoleRef
     *
     * @param index
     * @param vSecurityRoleRef
     */
    public void setSecurityRoleRef(int index, org.openejb.config.ejb11.SecurityRoleRef vSecurityRoleRef)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRoleRefList.setElementAt(vSecurityRoleRef, index);
    } //-- void setSecurityRoleRef(int, org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Method setSecurityRoleRef
     *
     * @param securityRoleRefArray
     */
    public void setSecurityRoleRef(org.openejb.config.ejb11.SecurityRoleRef[] securityRoleRefArray) {
        //-- copy array
        _securityRoleRefList.removeAllElements();
        for (int i = 0; i < securityRoleRefArray.length; i++) {
            _securityRoleRefList.addElement(securityRoleRefArray[i]);
        }
    } //-- void setSecurityRoleRef(org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Sets the value of field 'sessionType'.
     *
     * @param sessionType the value of field 'sessionType'.
     */
    public void setSessionType(java.lang.String sessionType) {
        this._sessionType = sessionType;
    } //-- void setSessionType(java.lang.String) 

    /**
     * Sets the value of field 'smallIcon'.
     *
     * @param smallIcon the value of field 'smallIcon'.
     */
    public void setSmallIcon(java.lang.String smallIcon) {
        this._smallIcon = smallIcon;
    } //-- void setSmallIcon(java.lang.String) 

    /**
     * Sets the value of field 'transactionType'.
     *
     * @param transactionType the value of field 'transactionType'.
     */
    public void setTransactionType(java.lang.String transactionType) {
        this._transactionType = transactionType;
    } //-- void setTransactionType(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.config.ejb11.Session) Unmarshaller.unmarshal(org.openejb.config.ejb11.Session.class, reader);
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
