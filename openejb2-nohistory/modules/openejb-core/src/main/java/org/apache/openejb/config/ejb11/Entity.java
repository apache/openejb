/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Entity.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Entity.
 *
 * @version $Revision$ $Date$
 */
public class Entity implements java.io.Serializable {


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
     * Field _persistenceType
     */
    private java.lang.String _persistenceType;

    /**
     * Field _primKeyClass
     */
    private java.lang.String _primKeyClass;

    /**
     * Field _reentrant
     */
    private boolean _reentrant;

    /**
     * keeps track of state for field: _reentrant
     */
    private boolean _has_reentrant;

    /**
     * Field _cmpFieldList
     */
    private java.util.Vector _cmpFieldList;

    /**
     * Field _primkeyField
     */
    private java.lang.String _primkeyField;

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

    public Entity() {
        super();
        _cmpFieldList = new Vector();
        _envEntryList = new Vector();
        _ejbRefList = new Vector();
        _ejbLocalRefList = new Vector();
        _securityRoleRefList = new Vector();
        _resourceRefList = new Vector();
    } //-- org.openejb.config.ejb11.Entity()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addCmpField
     *
     * @param vCmpField
     */
    public void addCmpField(org.apache.openejb.config.ejb11.CmpField vCmpField)
            throws java.lang.IndexOutOfBoundsException {
        _cmpFieldList.addElement(vCmpField);
    } //-- void addCmpField(org.openejb.config.ejb11.CmpField) 

    /**
     * Method addCmpField
     *
     * @param index
     * @param vCmpField
     */
    public void addCmpField(int index, org.apache.openejb.config.ejb11.CmpField vCmpField)
            throws java.lang.IndexOutOfBoundsException {
        _cmpFieldList.insertElementAt(vCmpField, index);
    } //-- void addCmpField(int, org.openejb.config.ejb11.CmpField) 

    /**
     * Method addEjbLocalRef
     *
     * @param vEjbLocalRef
     */
    public void addEjbLocalRef(org.apache.openejb.config.ejb11.EjbLocalRef vEjbLocalRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbLocalRefList.addElement(vEjbLocalRef);
    } //-- void addEjbLocalRef(org.openejb.config.ejb11.EjbLocalRef) 

    /**
     * Method addEjbLocalRef
     *
     * @param index
     * @param vEjbLocalRef
     */
    public void addEjbLocalRef(int index, org.apache.openejb.config.ejb11.EjbLocalRef vEjbLocalRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbLocalRefList.insertElementAt(vEjbLocalRef, index);
    } //-- void addEjbLocalRef(int, org.openejb.config.ejb11.EjbLocalRef) 

    /**
     * Method addEjbRef
     *
     * @param vEjbRef
     */
    public void addEjbRef(org.apache.openejb.config.ejb11.EjbRef vEjbRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbRefList.addElement(vEjbRef);
    } //-- void addEjbRef(org.openejb.config.ejb11.EjbRef) 

    /**
     * Method addEjbRef
     *
     * @param index
     * @param vEjbRef
     */
    public void addEjbRef(int index, org.apache.openejb.config.ejb11.EjbRef vEjbRef)
            throws java.lang.IndexOutOfBoundsException {
        _ejbRefList.insertElementAt(vEjbRef, index);
    } //-- void addEjbRef(int, org.openejb.config.ejb11.EjbRef) 

    /**
     * Method addEnvEntry
     *
     * @param vEnvEntry
     */
    public void addEnvEntry(org.apache.openejb.config.ejb11.EnvEntry vEnvEntry)
            throws java.lang.IndexOutOfBoundsException {
        _envEntryList.addElement(vEnvEntry);
    } //-- void addEnvEntry(org.openejb.config.ejb11.EnvEntry) 

    /**
     * Method addEnvEntry
     *
     * @param index
     * @param vEnvEntry
     */
    public void addEnvEntry(int index, org.apache.openejb.config.ejb11.EnvEntry vEnvEntry)
            throws java.lang.IndexOutOfBoundsException {
        _envEntryList.insertElementAt(vEnvEntry, index);
    } //-- void addEnvEntry(int, org.openejb.config.ejb11.EnvEntry) 

    /**
     * Method addResourceRef
     *
     * @param vResourceRef
     */
    public void addResourceRef(org.apache.openejb.config.ejb11.ResourceRef vResourceRef)
            throws java.lang.IndexOutOfBoundsException {
        _resourceRefList.addElement(vResourceRef);
    } //-- void addResourceRef(org.openejb.config.ejb11.ResourceRef) 

    /**
     * Method addResourceRef
     *
     * @param index
     * @param vResourceRef
     */
    public void addResourceRef(int index, org.apache.openejb.config.ejb11.ResourceRef vResourceRef)
            throws java.lang.IndexOutOfBoundsException {
        _resourceRefList.insertElementAt(vResourceRef, index);
    } //-- void addResourceRef(int, org.openejb.config.ejb11.ResourceRef) 

    /**
     * Method addSecurityRoleRef
     *
     * @param vSecurityRoleRef
     */
    public void addSecurityRoleRef(org.apache.openejb.config.ejb11.SecurityRoleRef vSecurityRoleRef)
            throws java.lang.IndexOutOfBoundsException {
        _securityRoleRefList.addElement(vSecurityRoleRef);
    } //-- void addSecurityRoleRef(org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Method addSecurityRoleRef
     *
     * @param index
     * @param vSecurityRoleRef
     */
    public void addSecurityRoleRef(int index, org.apache.openejb.config.ejb11.SecurityRoleRef vSecurityRoleRef)
            throws java.lang.IndexOutOfBoundsException {
        _securityRoleRefList.insertElementAt(vSecurityRoleRef, index);
    } //-- void addSecurityRoleRef(int, org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Method deleteReentrant
     */
    public void deleteReentrant() {
        this._has_reentrant = false;
    } //-- void deleteReentrant() 

    /**
     * Method enumerateCmpField
     */
    public java.util.Enumeration enumerateCmpField() {
        return _cmpFieldList.elements();
    } //-- java.util.Enumeration enumerateCmpField() 

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
     * Method getCmpField
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.CmpField getCmpField(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _cmpFieldList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.CmpField) _cmpFieldList.elementAt(index);
    } //-- org.openejb.config.ejb11.CmpField getCmpField(int) 

    /**
     * Method getCmpField
     */
    public org.apache.openejb.config.ejb11.CmpField[] getCmpField() {
        int size = _cmpFieldList.size();
        org.apache.openejb.config.ejb11.CmpField[] mArray = new org.apache.openejb.config.ejb11.CmpField[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.CmpField) _cmpFieldList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.ejb11.CmpField[] getCmpField() 

    /**
     * Method getCmpFieldCount
     */
    public int getCmpFieldCount() {
        return _cmpFieldList.size();
    } //-- int getCmpFieldCount() 

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
    public org.apache.openejb.config.ejb11.EjbLocalRef getEjbLocalRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbLocalRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.EjbLocalRef) _ejbLocalRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.EjbLocalRef getEjbLocalRef(int) 

    /**
     * Method getEjbLocalRef
     *
     * @return EjbLocalRef
     */
    public org.apache.openejb.config.ejb11.EjbLocalRef[] getEjbLocalRef() {
        int size = _ejbLocalRefList.size();
        org.apache.openejb.config.ejb11.EjbLocalRef[] mArray = new org.apache.openejb.config.ejb11.EjbLocalRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.EjbLocalRef) _ejbLocalRefList.elementAt(index);
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
    public org.apache.openejb.config.ejb11.EjbRef getEjbRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.EjbRef) _ejbRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.EjbRef getEjbRef(int) 

    /**
     * Method getEjbRef
     */
    public org.apache.openejb.config.ejb11.EjbRef[] getEjbRef() {
        int size = _ejbRefList.size();
        org.apache.openejb.config.ejb11.EjbRef[] mArray = new org.apache.openejb.config.ejb11.EjbRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.EjbRef) _ejbRefList.elementAt(index);
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
    public org.apache.openejb.config.ejb11.EnvEntry getEnvEntry(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.EnvEntry) _envEntryList.elementAt(index);
    } //-- org.openejb.config.ejb11.EnvEntry getEnvEntry(int) 

    /**
     * Method getEnvEntry
     */
    public org.apache.openejb.config.ejb11.EnvEntry[] getEnvEntry() {
        int size = _envEntryList.size();
        org.apache.openejb.config.ejb11.EnvEntry[] mArray = new org.apache.openejb.config.ejb11.EnvEntry[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.EnvEntry) _envEntryList.elementAt(index);
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
     * Returns the value of field 'persistenceType'.
     *
     * @return the value of field 'persistenceType'.
     */
    public java.lang.String getPersistenceType() {
        return this._persistenceType;
    } //-- java.lang.String getPersistenceType() 

    /**
     * Returns the value of field 'primKeyClass'.
     *
     * @return the value of field 'primKeyClass'.
     */
    public java.lang.String getPrimKeyClass() {
        return this._primKeyClass;
    } //-- java.lang.String getPrimKeyClass() 

    /**
     * Returns the value of field 'primkeyField'.
     *
     * @return the value of field 'primkeyField'.
     */
    public java.lang.String getPrimkeyField() {
        return this._primkeyField;
    } //-- java.lang.String getPrimkeyField() 

    /**
     * Returns the value of field 'reentrant'.
     *
     * @return the value of field 'reentrant'.
     */
    public boolean getReentrant() {
        return this._reentrant;
    } //-- boolean getReentrant() 

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
    public org.apache.openejb.config.ejb11.ResourceRef getResourceRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.ResourceRef) _resourceRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.ResourceRef getResourceRef(int) 

    /**
     * Method getResourceRef
     */
    public org.apache.openejb.config.ejb11.ResourceRef[] getResourceRef() {
        int size = _resourceRefList.size();
        org.apache.openejb.config.ejb11.ResourceRef[] mArray = new org.apache.openejb.config.ejb11.ResourceRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.ResourceRef) _resourceRefList.elementAt(index);
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
    public org.apache.openejb.config.ejb11.SecurityRoleRef getSecurityRoleRef(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.SecurityRoleRef) _securityRoleRefList.elementAt(index);
    } //-- org.openejb.config.ejb11.SecurityRoleRef getSecurityRoleRef(int) 

    /**
     * Method getSecurityRoleRef
     */
    public org.apache.openejb.config.ejb11.SecurityRoleRef[] getSecurityRoleRef() {
        int size = _securityRoleRefList.size();
        org.apache.openejb.config.ejb11.SecurityRoleRef[] mArray = new org.apache.openejb.config.ejb11.SecurityRoleRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.SecurityRoleRef) _securityRoleRefList.elementAt(index);
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
     * Returns the value of field 'smallIcon'.
     *
     * @return the value of field 'smallIcon'.
     */
    public java.lang.String getSmallIcon() {
        return this._smallIcon;
    } //-- java.lang.String getSmallIcon() 

    /**
     * Method hasReentrant
     */
    public boolean hasReentrant() {
        return this._has_reentrant;
    } //-- boolean hasReentrant() 

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
     * Method removeAllCmpField
     */
    public void removeAllCmpField() {
        _cmpFieldList.removeAllElements();
    } //-- void removeAllCmpField() 

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
     * Method removeCmpField
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.CmpField removeCmpField(int index) {
        java.lang.Object obj = _cmpFieldList.elementAt(index);
        _cmpFieldList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.CmpField) obj;
    } //-- org.openejb.config.ejb11.CmpField removeCmpField(int) 

    /**
     * Method removeEjbLocalRef
     *
     * @param index
     * @return EjbLocalRef
     */
    public org.apache.openejb.config.ejb11.EjbLocalRef removeEjbLocalRef(int index) {
        java.lang.Object obj = _ejbLocalRefList.elementAt(index);
        _ejbLocalRefList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.EjbLocalRef) obj;
    } //-- org.openejb.config.ejb11.EjbLocalRef removeEjbLocalRef(int) 

    /**
     * Method removeEjbRef
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.EjbRef removeEjbRef(int index) {
        java.lang.Object obj = _ejbRefList.elementAt(index);
        _ejbRefList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.EjbRef) obj;
    } //-- org.openejb.config.ejb11.EjbRef removeEjbRef(int) 

    /**
     * Method removeEnvEntry
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.EnvEntry removeEnvEntry(int index) {
        java.lang.Object obj = _envEntryList.elementAt(index);
        _envEntryList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.EnvEntry) obj;
    } //-- org.openejb.config.ejb11.EnvEntry removeEnvEntry(int) 

    /**
     * Method removeResourceRef
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.ResourceRef removeResourceRef(int index) {
        java.lang.Object obj = _resourceRefList.elementAt(index);
        _resourceRefList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.ResourceRef) obj;
    } //-- org.openejb.config.ejb11.ResourceRef removeResourceRef(int) 

    /**
     * Method removeSecurityRoleRef
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.SecurityRoleRef removeSecurityRoleRef(int index) {
        java.lang.Object obj = _securityRoleRefList.elementAt(index);
        _securityRoleRefList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.SecurityRoleRef) obj;
    } //-- org.openejb.config.ejb11.SecurityRoleRef removeSecurityRoleRef(int) 

    /**
     * Method setCmpField
     *
     * @param index
     * @param vCmpField
     */
    public void setCmpField(int index, org.apache.openejb.config.ejb11.CmpField vCmpField)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _cmpFieldList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _cmpFieldList.setElementAt(vCmpField, index);
    } //-- void setCmpField(int, org.openejb.config.ejb11.CmpField) 

    /**
     * Method setCmpField
     *
     * @param cmpFieldArray
     */
    public void setCmpField(org.apache.openejb.config.ejb11.CmpField[] cmpFieldArray) {
        //-- copy array
        _cmpFieldList.removeAllElements();
        for (int i = 0; i < cmpFieldArray.length; i++) {
            _cmpFieldList.addElement(cmpFieldArray[i]);
        }
    } //-- void setCmpField(org.openejb.config.ejb11.CmpField) 

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
    public void setEjbLocalRef(int index, org.apache.openejb.config.ejb11.EjbLocalRef vEjbLocalRef)
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
    public void setEjbLocalRef(org.apache.openejb.config.ejb11.EjbLocalRef[] ejbLocalRefArray) {
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
    public void setEjbRef(int index, org.apache.openejb.config.ejb11.EjbRef vEjbRef)
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
    public void setEjbRef(org.apache.openejb.config.ejb11.EjbRef[] ejbRefArray) {
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
    public void setEnvEntry(int index, org.apache.openejb.config.ejb11.EnvEntry vEnvEntry)
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
    public void setEnvEntry(org.apache.openejb.config.ejb11.EnvEntry[] envEntryArray) {
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
     * Sets the value of field 'persistenceType'.
     *
     * @param persistenceType the value of field 'persistenceType'.
     */
    public void setPersistenceType(java.lang.String persistenceType) {
        this._persistenceType = persistenceType;
    } //-- void setPersistenceType(java.lang.String) 

    /**
     * Sets the value of field 'primKeyClass'.
     *
     * @param primKeyClass the value of field 'primKeyClass'.
     */
    public void setPrimKeyClass(java.lang.String primKeyClass) {
        this._primKeyClass = primKeyClass;
    } //-- void setPrimKeyClass(java.lang.String) 

    /**
     * Sets the value of field 'primkeyField'.
     *
     * @param primkeyField the value of field 'primkeyField'.
     */
    public void setPrimkeyField(java.lang.String primkeyField) {
        this._primkeyField = primkeyField;
    } //-- void setPrimkeyField(java.lang.String) 

    /**
     * Sets the value of field 'reentrant'.
     *
     * @param reentrant the value of field 'reentrant'.
     */
    public void setReentrant(boolean reentrant) {
        this._reentrant = reentrant;
        this._has_reentrant = true;
    } //-- void setReentrant(boolean) 

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
    public void setResourceRef(int index, org.apache.openejb.config.ejb11.ResourceRef vResourceRef)
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
    public void setResourceRef(org.apache.openejb.config.ejb11.ResourceRef[] resourceRefArray) {
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
    public void setSecurityRoleRef(int index, org.apache.openejb.config.ejb11.SecurityRoleRef vSecurityRoleRef)
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
    public void setSecurityRoleRef(org.apache.openejb.config.ejb11.SecurityRoleRef[] securityRoleRefArray) {
        //-- copy array
        _securityRoleRefList.removeAllElements();
        for (int i = 0; i < securityRoleRefArray.length; i++) {
            _securityRoleRefList.addElement(securityRoleRefArray[i]);
        }
    } //-- void setSecurityRoleRef(org.openejb.config.ejb11.SecurityRoleRef) 

    /**
     * Sets the value of field 'smallIcon'.
     *
     * @param smallIcon the value of field 'smallIcon'.
     */
    public void setSmallIcon(java.lang.String smallIcon) {
        this._smallIcon = smallIcon;
    } //-- void setSmallIcon(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.Entity) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.Entity.class, reader);
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
