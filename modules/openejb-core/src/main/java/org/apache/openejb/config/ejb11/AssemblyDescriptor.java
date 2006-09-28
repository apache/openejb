/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class AssemblyDescriptor.
 *
 * @version $Revision$ $Date$
 */
public class AssemblyDescriptor implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _securityRoleList
     */
    private java.util.Vector _securityRoleList;

    /**
     * Field _methodPermissionList
     */
    private java.util.Vector _methodPermissionList;

    /**
     * Field _containerTransactionList
     */
    private java.util.Vector _containerTransactionList;


    //----------------/
    //- Constructors -/
    //----------------/

    public AssemblyDescriptor() {
        super();
        _securityRoleList = new Vector();
        _methodPermissionList = new Vector();
        _containerTransactionList = new Vector();
    } //-- org.apache.openejb.config.ejb11.AssemblyDescriptor()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addContainerTransaction
     *
     * @param vContainerTransaction
     */
    public void addContainerTransaction(org.apache.openejb.config.ejb11.ContainerTransaction vContainerTransaction)
            throws java.lang.IndexOutOfBoundsException {
        _containerTransactionList.addElement(vContainerTransaction);
    } //-- void addContainerTransaction(org.apache.openejb.config.ejb11.ContainerTransaction)

    /**
     * Method addContainerTransaction
     *
     * @param index
     * @param vContainerTransaction
     */
    public void addContainerTransaction(int index, org.apache.openejb.config.ejb11.ContainerTransaction vContainerTransaction)
            throws java.lang.IndexOutOfBoundsException {
        _containerTransactionList.insertElementAt(vContainerTransaction, index);
    } //-- void addContainerTransaction(int, org.apache.openejb.config.ejb11.ContainerTransaction)

    /**
     * Method addMethodPermission
     *
     * @param vMethodPermission
     */
    public void addMethodPermission(org.apache.openejb.config.ejb11.MethodPermission vMethodPermission)
            throws java.lang.IndexOutOfBoundsException {
        _methodPermissionList.addElement(vMethodPermission);
    } //-- void addMethodPermission(org.apache.openejb.config.ejb11.MethodPermission)

    /**
     * Method addMethodPermission
     *
     * @param index
     * @param vMethodPermission
     */
    public void addMethodPermission(int index, org.apache.openejb.config.ejb11.MethodPermission vMethodPermission)
            throws java.lang.IndexOutOfBoundsException {
        _methodPermissionList.insertElementAt(vMethodPermission, index);
    } //-- void addMethodPermission(int, org.apache.openejb.config.ejb11.MethodPermission)

    /**
     * Method addSecurityRole
     *
     * @param vSecurityRole
     */
    public void addSecurityRole(org.apache.openejb.config.ejb11.SecurityRole vSecurityRole)
            throws java.lang.IndexOutOfBoundsException {
        _securityRoleList.addElement(vSecurityRole);
    } //-- void addSecurityRole(org.apache.openejb.config.ejb11.SecurityRole)

    /**
     * Method addSecurityRole
     *
     * @param index
     * @param vSecurityRole
     */
    public void addSecurityRole(int index, org.apache.openejb.config.ejb11.SecurityRole vSecurityRole)
            throws java.lang.IndexOutOfBoundsException {
        _securityRoleList.insertElementAt(vSecurityRole, index);
    } //-- void addSecurityRole(int, org.apache.openejb.config.ejb11.SecurityRole)

    /**
     * Method enumerateContainerTransaction
     */
    public java.util.Enumeration enumerateContainerTransaction() {
        return _containerTransactionList.elements();
    } //-- java.util.Enumeration enumerateContainerTransaction() 

    /**
     * Method enumerateMethodPermission
     */
    public java.util.Enumeration enumerateMethodPermission() {
        return _methodPermissionList.elements();
    } //-- java.util.Enumeration enumerateMethodPermission() 

    /**
     * Method enumerateSecurityRole
     */
    public java.util.Enumeration enumerateSecurityRole() {
        return _securityRoleList.elements();
    } //-- java.util.Enumeration enumerateSecurityRole() 

    /**
     * Method getContainerTransaction
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.ContainerTransaction getContainerTransaction(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _containerTransactionList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.ContainerTransaction) _containerTransactionList.elementAt(index);
    } //-- org.apache.openejb.config.ejb11.ContainerTransaction getContainerTransaction(int)

    /**
     * Method getContainerTransaction
     */
    public org.apache.openejb.config.ejb11.ContainerTransaction[] getContainerTransaction() {
        int size = _containerTransactionList.size();
        org.apache.openejb.config.ejb11.ContainerTransaction[] mArray = new org.apache.openejb.config.ejb11.ContainerTransaction[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.ContainerTransaction) _containerTransactionList.elementAt(index);
        }
        return mArray;
    } //-- org.apache.openejb.config.ejb11.ContainerTransaction[] getContainerTransaction()

    /**
     * Method getContainerTransactionCount
     */
    public int getContainerTransactionCount() {
        return _containerTransactionList.size();
    } //-- int getContainerTransactionCount() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Method getMethodPermission
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.MethodPermission getMethodPermission(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodPermissionList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.MethodPermission) _methodPermissionList.elementAt(index);
    } //-- org.apache.openejb.config.ejb11.MethodPermission getMethodPermission(int)

    /**
     * Method getMethodPermission
     */
    public org.apache.openejb.config.ejb11.MethodPermission[] getMethodPermission() {
        int size = _methodPermissionList.size();
        org.apache.openejb.config.ejb11.MethodPermission[] mArray = new org.apache.openejb.config.ejb11.MethodPermission[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.MethodPermission) _methodPermissionList.elementAt(index);
        }
        return mArray;
    } //-- org.apache.openejb.config.ejb11.MethodPermission[] getMethodPermission()

    /**
     * Method getMethodPermissionCount
     */
    public int getMethodPermissionCount() {
        return _methodPermissionList.size();
    } //-- int getMethodPermissionCount() 

    /**
     * Method getSecurityRole
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.SecurityRole getSecurityRole(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.ejb11.SecurityRole) _securityRoleList.elementAt(index);
    } //-- org.apache.openejb.config.ejb11.SecurityRole getSecurityRole(int)

    /**
     * Method getSecurityRole
     */
    public org.apache.openejb.config.ejb11.SecurityRole[] getSecurityRole() {
        int size = _securityRoleList.size();
        org.apache.openejb.config.ejb11.SecurityRole[] mArray = new org.apache.openejb.config.ejb11.SecurityRole[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.ejb11.SecurityRole) _securityRoleList.elementAt(index);
        }
        return mArray;
    } //-- org.apache.openejb.config.ejb11.SecurityRole[] getSecurityRole()

    /**
     * Method getSecurityRoleCount
     */
    public int getSecurityRoleCount() {
        return _securityRoleList.size();
    } //-- int getSecurityRoleCount() 

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
     * Method removeAllContainerTransaction
     */
    public void removeAllContainerTransaction() {
        _containerTransactionList.removeAllElements();
    } //-- void removeAllContainerTransaction() 

    /**
     * Method removeAllMethodPermission
     */
    public void removeAllMethodPermission() {
        _methodPermissionList.removeAllElements();
    } //-- void removeAllMethodPermission() 

    /**
     * Method removeAllSecurityRole
     */
    public void removeAllSecurityRole() {
        _securityRoleList.removeAllElements();
    } //-- void removeAllSecurityRole() 

    /**
     * Method removeContainerTransaction
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.ContainerTransaction removeContainerTransaction(int index) {
        java.lang.Object obj = _containerTransactionList.elementAt(index);
        _containerTransactionList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.ContainerTransaction) obj;
    } //-- org.apache.openejb.config.ejb11.ContainerTransaction removeContainerTransaction(int)

    /**
     * Method removeMethodPermission
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.MethodPermission removeMethodPermission(int index) {
        java.lang.Object obj = _methodPermissionList.elementAt(index);
        _methodPermissionList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.MethodPermission) obj;
    } //-- org.apache.openejb.config.ejb11.MethodPermission removeMethodPermission(int)

    /**
     * Method removeSecurityRole
     *
     * @param index
     */
    public org.apache.openejb.config.ejb11.SecurityRole removeSecurityRole(int index) {
        java.lang.Object obj = _securityRoleList.elementAt(index);
        _securityRoleList.removeElementAt(index);
        return (org.apache.openejb.config.ejb11.SecurityRole) obj;
    } //-- org.apache.openejb.config.ejb11.SecurityRole removeSecurityRole(int)

    /**
     * Method setContainerTransaction
     *
     * @param index
     * @param vContainerTransaction
     */
    public void setContainerTransaction(int index, org.apache.openejb.config.ejb11.ContainerTransaction vContainerTransaction)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _containerTransactionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _containerTransactionList.setElementAt(vContainerTransaction, index);
    } //-- void setContainerTransaction(int, org.apache.openejb.config.ejb11.ContainerTransaction)

    /**
     * Method setContainerTransaction
     *
     * @param containerTransactionArray
     */
    public void setContainerTransaction(org.apache.openejb.config.ejb11.ContainerTransaction[] containerTransactionArray) {
        //-- copy array
        _containerTransactionList.removeAllElements();
        for (int i = 0; i < containerTransactionArray.length; i++) {
            _containerTransactionList.addElement(containerTransactionArray[i]);
        }
    } //-- void setContainerTransaction(org.apache.openejb.config.ejb11.ContainerTransaction)

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Method setMethodPermission
     *
     * @param index
     * @param vMethodPermission
     */
    public void setMethodPermission(int index, org.apache.openejb.config.ejb11.MethodPermission vMethodPermission)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodPermissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodPermissionList.setElementAt(vMethodPermission, index);
    } //-- void setMethodPermission(int, org.apache.openejb.config.ejb11.MethodPermission)

    /**
     * Method setMethodPermission
     *
     * @param methodPermissionArray
     */
    public void setMethodPermission(org.apache.openejb.config.ejb11.MethodPermission[] methodPermissionArray) {
        //-- copy array
        _methodPermissionList.removeAllElements();
        for (int i = 0; i < methodPermissionArray.length; i++) {
            _methodPermissionList.addElement(methodPermissionArray[i]);
        }
    } //-- void setMethodPermission(org.apache.openejb.config.ejb11.MethodPermission)

    /**
     * Method setSecurityRole
     *
     * @param index
     * @param vSecurityRole
     */
    public void setSecurityRole(int index, org.apache.openejb.config.ejb11.SecurityRole vSecurityRole)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRoleList.setElementAt(vSecurityRole, index);
    } //-- void setSecurityRole(int, org.apache.openejb.config.ejb11.SecurityRole)

    /**
     * Method setSecurityRole
     *
     * @param securityRoleArray
     */
    public void setSecurityRole(org.apache.openejb.config.ejb11.SecurityRole[] securityRoleArray) {
        //-- copy array
        _securityRoleList.removeAllElements();
        for (int i = 0; i < securityRoleArray.length; i++) {
            _securityRoleList.addElement(securityRoleArray[i]);
        }
    } //-- void setSecurityRole(org.apache.openejb.config.ejb11.SecurityRole)

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.AssemblyDescriptor) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.AssemblyDescriptor.class, reader);
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
