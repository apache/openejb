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
public class AssemblyDescriptor implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.util.Vector _securityRoleList;

    private java.util.Vector _methodPermissionList;

    private java.util.Vector _containerTransactionList;


      //----------------/
     //- Constructors -/
    //----------------/

    public AssemblyDescriptor() {
        super();
        _securityRoleList = new Vector();
        _methodPermissionList = new Vector();
        _containerTransactionList = new Vector();
    } //-- org.openejb.alt.config.ejb11.AssemblyDescriptor()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vContainerTransaction
    **/
    public void addContainerTransaction(ContainerTransaction vContainerTransaction)
        throws java.lang.IndexOutOfBoundsException
    {
        _containerTransactionList.addElement(vContainerTransaction);
    } //-- void addContainerTransaction(ContainerTransaction) 

    /**
     * 
     * @param vMethodPermission
    **/
    public void addMethodPermission(MethodPermission vMethodPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodPermissionList.addElement(vMethodPermission);
    } //-- void addMethodPermission(MethodPermission) 

    /**
     * 
     * @param vSecurityRole
    **/
    public void addSecurityRole(SecurityRole vSecurityRole)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRoleList.addElement(vSecurityRole);
    } //-- void addSecurityRole(SecurityRole) 

    /**
    **/
    public java.util.Enumeration enumerateContainerTransaction()
    {
        return _containerTransactionList.elements();
    } //-- java.util.Enumeration enumerateContainerTransaction() 

    /**
    **/
    public java.util.Enumeration enumerateMethodPermission()
    {
        return _methodPermissionList.elements();
    } //-- java.util.Enumeration enumerateMethodPermission() 

    /**
    **/
    public java.util.Enumeration enumerateSecurityRole()
    {
        return _securityRoleList.elements();
    } //-- java.util.Enumeration enumerateSecurityRole() 

    /**
     * 
     * @param index
    **/
    public ContainerTransaction getContainerTransaction(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _containerTransactionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (ContainerTransaction) _containerTransactionList.elementAt(index);
    } //-- ContainerTransaction getContainerTransaction(int) 

    /**
    **/
    public ContainerTransaction[] getContainerTransaction()
    {
        int size = _containerTransactionList.size();
        ContainerTransaction[] mArray = new ContainerTransaction[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (ContainerTransaction) _containerTransactionList.elementAt(index);
        }
        return mArray;
    } //-- ContainerTransaction[] getContainerTransaction() 

    /**
    **/
    public int getContainerTransactionCount()
    {
        return _containerTransactionList.size();
    } //-- int getContainerTransactionCount() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * 
     * @param index
    **/
    public MethodPermission getMethodPermission(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodPermissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (MethodPermission) _methodPermissionList.elementAt(index);
    } //-- MethodPermission getMethodPermission(int) 

    /**
    **/
    public MethodPermission[] getMethodPermission()
    {
        int size = _methodPermissionList.size();
        MethodPermission[] mArray = new MethodPermission[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (MethodPermission) _methodPermissionList.elementAt(index);
        }
        return mArray;
    } //-- MethodPermission[] getMethodPermission() 

    /**
    **/
    public int getMethodPermissionCount()
    {
        return _methodPermissionList.size();
    } //-- int getMethodPermissionCount() 

    /**
     * 
     * @param index
    **/
    public SecurityRole getSecurityRole(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (SecurityRole) _securityRoleList.elementAt(index);
    } //-- SecurityRole getSecurityRole(int) 

    /**
    **/
    public SecurityRole[] getSecurityRole()
    {
        int size = _securityRoleList.size();
        SecurityRole[] mArray = new SecurityRole[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (SecurityRole) _securityRoleList.elementAt(index);
        }
        return mArray;
    } //-- SecurityRole[] getSecurityRole() 

    /**
    **/
    public int getSecurityRoleCount()
    {
        return _securityRoleList.size();
    } //-- int getSecurityRoleCount() 

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
    public void removeAllContainerTransaction()
    {
        _containerTransactionList.removeAllElements();
    } //-- void removeAllContainerTransaction() 

    /**
    **/
    public void removeAllMethodPermission()
    {
        _methodPermissionList.removeAllElements();
    } //-- void removeAllMethodPermission() 

    /**
    **/
    public void removeAllSecurityRole()
    {
        _securityRoleList.removeAllElements();
    } //-- void removeAllSecurityRole() 

    /**
     * 
     * @param index
    **/
    public ContainerTransaction removeContainerTransaction(int index)
    {
        Object obj = _containerTransactionList.elementAt(index);
        _containerTransactionList.removeElementAt(index);
        return (ContainerTransaction) obj;
    } //-- ContainerTransaction removeContainerTransaction(int) 

    /**
     * 
     * @param index
    **/
    public MethodPermission removeMethodPermission(int index)
    {
        Object obj = _methodPermissionList.elementAt(index);
        _methodPermissionList.removeElementAt(index);
        return (MethodPermission) obj;
    } //-- MethodPermission removeMethodPermission(int) 

    /**
     * 
     * @param index
    **/
    public SecurityRole removeSecurityRole(int index)
    {
        Object obj = _securityRoleList.elementAt(index);
        _securityRoleList.removeElementAt(index);
        return (SecurityRole) obj;
    } //-- SecurityRole removeSecurityRole(int) 

    /**
     * 
     * @param index
     * @param vContainerTransaction
    **/
    public void setContainerTransaction(int index, ContainerTransaction vContainerTransaction)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _containerTransactionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _containerTransactionList.setElementAt(vContainerTransaction, index);
    } //-- void setContainerTransaction(int, ContainerTransaction) 

    /**
     * 
     * @param containerTransactionArray
    **/
    public void setContainerTransaction(ContainerTransaction[] containerTransactionArray)
    {
        //-- copy array
        _containerTransactionList.removeAllElements();
        for (int i = 0; i < containerTransactionArray.length; i++) {
            _containerTransactionList.addElement(containerTransactionArray[i]);
        }
    } //-- void setContainerTransaction(ContainerTransaction) 

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
     * @param index
     * @param vMethodPermission
    **/
    public void setMethodPermission(int index, MethodPermission vMethodPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodPermissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodPermissionList.setElementAt(vMethodPermission, index);
    } //-- void setMethodPermission(int, MethodPermission) 

    /**
     * 
     * @param methodPermissionArray
    **/
    public void setMethodPermission(MethodPermission[] methodPermissionArray)
    {
        //-- copy array
        _methodPermissionList.removeAllElements();
        for (int i = 0; i < methodPermissionArray.length; i++) {
            _methodPermissionList.addElement(methodPermissionArray[i]);
        }
    } //-- void setMethodPermission(MethodPermission) 

    /**
     * 
     * @param index
     * @param vSecurityRole
    **/
    public void setSecurityRole(int index, SecurityRole vSecurityRole)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _securityRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRoleList.setElementAt(vSecurityRole, index);
    } //-- void setSecurityRole(int, SecurityRole) 

    /**
     * 
     * @param securityRoleArray
    **/
    public void setSecurityRole(SecurityRole[] securityRoleArray)
    {
        //-- copy array
        _securityRoleList.removeAllElements();
        for (int i = 0; i < securityRoleArray.length; i++) {
            _securityRoleList.addElement(securityRoleArray[i]);
        }
    } //-- void setSecurityRole(SecurityRole) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.AssemblyDescriptor unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.AssemblyDescriptor) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.AssemblyDescriptor.class, reader);
    } //-- org.openejb.alt.config.ejb11.AssemblyDescriptor unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
