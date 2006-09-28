/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Openejb.java 444992 2004-10-25 09:46:56Z dblevins $
 */

package org.apache.openejb.config.sys;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Openejb.
 *
 * @version $Revision$ $Date$
 */
public class Openejb implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
    private java.lang.String _content = "";

    /**
     * Field _containerList
     */
    private java.util.Vector _containerList;

    /**
     * Field _jndiProviderList
     */
    private java.util.Vector _jndiProviderList;

    /**
     * Field _securityService
     */
    private org.apache.openejb.config.sys.SecurityService _securityService;

    /**
     * Field _transactionService
     */
    private org.apache.openejb.config.sys.TransactionService _transactionService;

    /**
     * Field _connectionManager
     */
    private org.apache.openejb.config.sys.ConnectionManager _connectionManager;

    /**
     * Field _proxyFactory
     */
    private org.apache.openejb.config.sys.ProxyFactory _proxyFactory;

    /**
     * Field _connectorList
     */
    private java.util.Vector _connectorList;

    /**
     * Field _resourceList
     */
    private java.util.Vector _resourceList;

    /**
     * Field _deploymentsList
     */
    private java.util.Vector _deploymentsList;


    //----------------/
    //- Constructors -/
    //----------------/

    public Openejb() {
        super();
        setContent("");
        _containerList = new Vector();
        _jndiProviderList = new Vector();
        _connectorList = new Vector();
        _resourceList = new Vector();
        _deploymentsList = new Vector();
    } //-- org.openejb.config.sys.Openejb()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addConnector
     *
     * @param vConnector
     */
    public void addConnector(org.apache.openejb.config.sys.Connector vConnector)
            throws java.lang.IndexOutOfBoundsException {
        _connectorList.addElement(vConnector);
    } //-- void addConnector(org.openejb.config.sys.Connector) 

    /**
     * Method addConnector
     *
     * @param index
     * @param vConnector
     */
    public void addConnector(int index, org.apache.openejb.config.sys.Connector vConnector)
            throws java.lang.IndexOutOfBoundsException {
        _connectorList.insertElementAt(vConnector, index);
    } //-- void addConnector(int, org.openejb.config.sys.Connector) 

    /**
     * Method addContainer
     *
     * @param vContainer
     */
    public void addContainer(org.apache.openejb.config.sys.Container vContainer)
            throws java.lang.IndexOutOfBoundsException {
        _containerList.addElement(vContainer);
    } //-- void addContainer(org.openejb.config.sys.Container) 

    /**
     * Method addContainer
     *
     * @param index
     * @param vContainer
     */
    public void addContainer(int index, org.apache.openejb.config.sys.Container vContainer)
            throws java.lang.IndexOutOfBoundsException {
        _containerList.insertElementAt(vContainer, index);
    } //-- void addContainer(int, org.openejb.config.sys.Container) 

    /**
     * Method addDeployments
     *
     * @param vDeployments
     */
    public void addDeployments(org.apache.openejb.config.sys.Deployments vDeployments)
            throws java.lang.IndexOutOfBoundsException {
        _deploymentsList.addElement(vDeployments);
    } //-- void addDeployments(org.openejb.config.sys.Deployments) 

    /**
     * Method addDeployments
     *
     * @param index
     * @param vDeployments
     */
    public void addDeployments(int index, org.apache.openejb.config.sys.Deployments vDeployments)
            throws java.lang.IndexOutOfBoundsException {
        _deploymentsList.insertElementAt(vDeployments, index);
    } //-- void addDeployments(int, org.openejb.config.sys.Deployments) 

    /**
     * Method addJndiProvider
     *
     * @param vJndiProvider
     */
    public void addJndiProvider(org.apache.openejb.config.sys.JndiProvider vJndiProvider)
            throws java.lang.IndexOutOfBoundsException {
        _jndiProviderList.addElement(vJndiProvider);
    } //-- void addJndiProvider(org.openejb.config.sys.JndiProvider) 

    /**
     * Method addJndiProvider
     *
     * @param index
     * @param vJndiProvider
     */
    public void addJndiProvider(int index, org.apache.openejb.config.sys.JndiProvider vJndiProvider)
            throws java.lang.IndexOutOfBoundsException {
        _jndiProviderList.insertElementAt(vJndiProvider, index);
    } //-- void addJndiProvider(int, org.openejb.config.sys.JndiProvider) 

    /**
     * Method addResource
     *
     * @param vResource
     */
    public void addResource(org.apache.openejb.config.sys.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        _resourceList.addElement(vResource);
    } //-- void addResource(org.openejb.config.sys.Resource) 

    /**
     * Method addResource
     *
     * @param index
     * @param vResource
     */
    public void addResource(int index, org.apache.openejb.config.sys.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        _resourceList.insertElementAt(vResource, index);
    } //-- void addResource(int, org.openejb.config.sys.Resource) 

    /**
     * Method enumerateConnector
     */
    public java.util.Enumeration enumerateConnector() {
        return _connectorList.elements();
    } //-- java.util.Enumeration enumerateConnector() 

    /**
     * Method enumerateContainer
     */
    public java.util.Enumeration enumerateContainer() {
        return _containerList.elements();
    } //-- java.util.Enumeration enumerateContainer() 

    /**
     * Method enumerateDeployments
     */
    public java.util.Enumeration enumerateDeployments() {
        return _deploymentsList.elements();
    } //-- java.util.Enumeration enumerateDeployments() 

    /**
     * Method enumerateJndiProvider
     */
    public java.util.Enumeration enumerateJndiProvider() {
        return _jndiProviderList.elements();
    } //-- java.util.Enumeration enumerateJndiProvider() 

    /**
     * Method enumerateResource
     */
    public java.util.Enumeration enumerateResource() {
        return _resourceList.elements();
    } //-- java.util.Enumeration enumerateResource() 

    /**
     * Returns the value of field 'connectionManager'.
     *
     * @return the value of field 'connectionManager'.
     */
    public org.apache.openejb.config.sys.ConnectionManager getConnectionManager() {
        return this._connectionManager;
    } //-- org.openejb.config.sys.ConnectionManager getConnectionManager() 

    /**
     * Method getConnector
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Connector getConnector(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _connectorList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.Connector) _connectorList.elementAt(index);
    } //-- org.openejb.config.sys.Connector getConnector(int) 

    /**
     * Method getConnector
     */
    public org.apache.openejb.config.sys.Connector[] getConnector() {
        int size = _connectorList.size();
        org.apache.openejb.config.sys.Connector[] mArray = new org.apache.openejb.config.sys.Connector[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.Connector) _connectorList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.sys.Connector[] getConnector() 

    /**
     * Method getConnectorCount
     */
    public int getConnectorCount() {
        return _connectorList.size();
    } //-- int getConnectorCount() 

    /**
     * Method getContainer
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Container getContainer(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _containerList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.Container) _containerList.elementAt(index);
    } //-- org.openejb.config.sys.Container getContainer(int) 

    /**
     * Method getContainer
     */
    public org.apache.openejb.config.sys.Container[] getContainer() {
        int size = _containerList.size();
        org.apache.openejb.config.sys.Container[] mArray = new org.apache.openejb.config.sys.Container[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.Container) _containerList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.sys.Container[] getContainer() 

    /**
     * Method getContainerCount
     */
    public int getContainerCount() {
        return _containerList.size();
    } //-- int getContainerCount() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     *
     * @return the value of field 'content'.
     */
    public java.lang.String getContent() {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * Method getDeployments
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Deployments getDeployments(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _deploymentsList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.Deployments) _deploymentsList.elementAt(index);
    } //-- org.openejb.config.sys.Deployments getDeployments(int) 

    /**
     * Method getDeployments
     */
    public org.apache.openejb.config.sys.Deployments[] getDeployments() {
        int size = _deploymentsList.size();
        org.apache.openejb.config.sys.Deployments[] mArray = new org.apache.openejb.config.sys.Deployments[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.Deployments) _deploymentsList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.sys.Deployments[] getDeployments() 

    /**
     * Method getDeploymentsCount
     */
    public int getDeploymentsCount() {
        return _deploymentsList.size();
    } //-- int getDeploymentsCount() 

    /**
     * Method getJndiProvider
     *
     * @param index
     */
    public org.apache.openejb.config.sys.JndiProvider getJndiProvider(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _jndiProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.JndiProvider) _jndiProviderList.elementAt(index);
    } //-- org.openejb.config.sys.JndiProvider getJndiProvider(int) 

    /**
     * Method getJndiProvider
     */
    public org.apache.openejb.config.sys.JndiProvider[] getJndiProvider() {
        int size = _jndiProviderList.size();
        org.apache.openejb.config.sys.JndiProvider[] mArray = new org.apache.openejb.config.sys.JndiProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.JndiProvider) _jndiProviderList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.sys.JndiProvider[] getJndiProvider() 

    /**
     * Method getJndiProviderCount
     */
    public int getJndiProviderCount() {
        return _jndiProviderList.size();
    } //-- int getJndiProviderCount() 

    /**
     * Returns the value of field 'proxyFactory'.
     *
     * @return the value of field 'proxyFactory'.
     */
    public org.apache.openejb.config.sys.ProxyFactory getProxyFactory() {
        return this._proxyFactory;
    } //-- org.openejb.config.sys.ProxyFactory getProxyFactory() 

    /**
     * Method getResource
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Resource getResource(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.Resource) _resourceList.elementAt(index);
    } //-- org.openejb.config.sys.Resource getResource(int) 

    /**
     * Method getResource
     */
    public org.apache.openejb.config.sys.Resource[] getResource() {
        int size = _resourceList.size();
        org.apache.openejb.config.sys.Resource[] mArray = new org.apache.openejb.config.sys.Resource[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.Resource) _resourceList.elementAt(index);
        }
        return mArray;
    } //-- org.openejb.config.sys.Resource[] getResource() 

    /**
     * Method getResourceCount
     */
    public int getResourceCount() {
        return _resourceList.size();
    } //-- int getResourceCount() 

    /**
     * Returns the value of field 'securityService'.
     *
     * @return the value of field 'securityService'.
     */
    public org.apache.openejb.config.sys.SecurityService getSecurityService() {
        return this._securityService;
    } //-- org.openejb.config.sys.SecurityService getSecurityService() 

    /**
     * Returns the value of field 'transactionService'.
     *
     * @return the value of field 'transactionService'.
     */
    public org.apache.openejb.config.sys.TransactionService getTransactionService() {
        return this._transactionService;
    } //-- org.openejb.config.sys.TransactionService getTransactionService() 

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
     * Method removeAllConnector
     */
    public void removeAllConnector() {
        _connectorList.removeAllElements();
    } //-- void removeAllConnector() 

    /**
     * Method removeAllContainer
     */
    public void removeAllContainer() {
        _containerList.removeAllElements();
    } //-- void removeAllContainer() 

    /**
     * Method removeAllDeployments
     */
    public void removeAllDeployments() {
        _deploymentsList.removeAllElements();
    } //-- void removeAllDeployments() 

    /**
     * Method removeAllJndiProvider
     */
    public void removeAllJndiProvider() {
        _jndiProviderList.removeAllElements();
    } //-- void removeAllJndiProvider() 

    /**
     * Method removeAllResource
     */
    public void removeAllResource() {
        _resourceList.removeAllElements();
    } //-- void removeAllResource() 

    /**
     * Method removeConnector
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Connector removeConnector(int index) {
        java.lang.Object obj = _connectorList.elementAt(index);
        _connectorList.removeElementAt(index);
        return (org.apache.openejb.config.sys.Connector) obj;
    } //-- org.openejb.config.sys.Connector removeConnector(int) 

    /**
     * Method removeContainer
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Container removeContainer(int index) {
        java.lang.Object obj = _containerList.elementAt(index);
        _containerList.removeElementAt(index);
        return (org.apache.openejb.config.sys.Container) obj;
    } //-- org.openejb.config.sys.Container removeContainer(int) 

    /**
     * Method removeDeployments
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Deployments removeDeployments(int index) {
        java.lang.Object obj = _deploymentsList.elementAt(index);
        _deploymentsList.removeElementAt(index);
        return (org.apache.openejb.config.sys.Deployments) obj;
    } //-- org.openejb.config.sys.Deployments removeDeployments(int) 

    /**
     * Method removeJndiProvider
     *
     * @param index
     */
    public org.apache.openejb.config.sys.JndiProvider removeJndiProvider(int index) {
        java.lang.Object obj = _jndiProviderList.elementAt(index);
        _jndiProviderList.removeElementAt(index);
        return (org.apache.openejb.config.sys.JndiProvider) obj;
    } //-- org.openejb.config.sys.JndiProvider removeJndiProvider(int) 

    /**
     * Method removeResource
     *
     * @param index
     */
    public org.apache.openejb.config.sys.Resource removeResource(int index) {
        java.lang.Object obj = _resourceList.elementAt(index);
        _resourceList.removeElementAt(index);
        return (org.apache.openejb.config.sys.Resource) obj;
    } //-- org.openejb.config.sys.Resource removeResource(int) 

    /**
     * Sets the value of field 'connectionManager'.
     *
     * @param connectionManager the value of field
     *                          'connectionManager'.
     */
    public void setConnectionManager(org.apache.openejb.config.sys.ConnectionManager connectionManager) {
        this._connectionManager = connectionManager;
    } //-- void setConnectionManager(org.openejb.config.sys.ConnectionManager) 

    /**
     * Method setConnector
     *
     * @param index
     * @param vConnector
     */
    public void setConnector(int index, org.apache.openejb.config.sys.Connector vConnector)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _connectorList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _connectorList.setElementAt(vConnector, index);
    } //-- void setConnector(int, org.openejb.config.sys.Connector) 

    /**
     * Method setConnector
     *
     * @param connectorArray
     */
    public void setConnector(org.apache.openejb.config.sys.Connector[] connectorArray) {
        //-- copy array
        _connectorList.removeAllElements();
        for (int i = 0; i < connectorArray.length; i++) {
            _connectorList.addElement(connectorArray[i]);
        }
    } //-- void setConnector(org.openejb.config.sys.Connector) 

    /**
     * Method setContainer
     *
     * @param index
     * @param vContainer
     */
    public void setContainer(int index, org.apache.openejb.config.sys.Container vContainer)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _containerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _containerList.setElementAt(vContainer, index);
    } //-- void setContainer(int, org.openejb.config.sys.Container) 

    /**
     * Method setContainer
     *
     * @param containerArray
     */
    public void setContainer(org.apache.openejb.config.sys.Container[] containerArray) {
        //-- copy array
        _containerList.removeAllElements();
        for (int i = 0; i < containerArray.length; i++) {
            _containerList.addElement(containerArray[i]);
        }
    } //-- void setContainer(org.openejb.config.sys.Container) 

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     *
     * @param content the value of field 'content'.
     */
    public void setContent(java.lang.String content) {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * Method setDeployments
     *
     * @param index
     * @param vDeployments
     */
    public void setDeployments(int index, org.apache.openejb.config.sys.Deployments vDeployments)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _deploymentsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _deploymentsList.setElementAt(vDeployments, index);
    } //-- void setDeployments(int, org.openejb.config.sys.Deployments) 

    /**
     * Method setDeployments
     *
     * @param deploymentsArray
     */
    public void setDeployments(org.apache.openejb.config.sys.Deployments[] deploymentsArray) {
        //-- copy array
        _deploymentsList.removeAllElements();
        for (int i = 0; i < deploymentsArray.length; i++) {
            _deploymentsList.addElement(deploymentsArray[i]);
        }
    } //-- void setDeployments(org.openejb.config.sys.Deployments) 

    /**
     * Method setJndiProvider
     *
     * @param index
     * @param vJndiProvider
     */
    public void setJndiProvider(int index, org.apache.openejb.config.sys.JndiProvider vJndiProvider)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _jndiProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _jndiProviderList.setElementAt(vJndiProvider, index);
    } //-- void setJndiProvider(int, org.openejb.config.sys.JndiProvider) 

    /**
     * Method setJndiProvider
     *
     * @param jndiProviderArray
     */
    public void setJndiProvider(org.apache.openejb.config.sys.JndiProvider[] jndiProviderArray) {
        //-- copy array
        _jndiProviderList.removeAllElements();
        for (int i = 0; i < jndiProviderArray.length; i++) {
            _jndiProviderList.addElement(jndiProviderArray[i]);
        }
    } //-- void setJndiProvider(org.openejb.config.sys.JndiProvider) 

    /**
     * Sets the value of field 'proxyFactory'.
     *
     * @param proxyFactory the value of field 'proxyFactory'.
     */
    public void setProxyFactory(org.apache.openejb.config.sys.ProxyFactory proxyFactory) {
        this._proxyFactory = proxyFactory;
    } //-- void setProxyFactory(org.openejb.config.sys.ProxyFactory) 

    /**
     * Method setResource
     *
     * @param index
     * @param vResource
     */
    public void setResource(int index, org.apache.openejb.config.sys.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceList.setElementAt(vResource, index);
    } //-- void setResource(int, org.openejb.config.sys.Resource) 

    /**
     * Method setResource
     *
     * @param resourceArray
     */
    public void setResource(org.apache.openejb.config.sys.Resource[] resourceArray) {
        //-- copy array
        _resourceList.removeAllElements();
        for (int i = 0; i < resourceArray.length; i++) {
            _resourceList.addElement(resourceArray[i]);
        }
    } //-- void setResource(org.openejb.config.sys.Resource) 

    /**
     * Sets the value of field 'securityService'.
     *
     * @param securityService the value of field 'securityService'.
     */
    public void setSecurityService(org.apache.openejb.config.sys.SecurityService securityService) {
        this._securityService = securityService;
    } //-- void setSecurityService(org.openejb.config.sys.SecurityService) 

    /**
     * Sets the value of field 'transactionService'.
     *
     * @param transactionService the value of field
     *                           'transactionService'.
     */
    public void setTransactionService(org.apache.openejb.config.sys.TransactionService transactionService) {
        this._transactionService = transactionService;
    } //-- void setTransactionService(org.openejb.config.sys.TransactionService) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.Openejb) Unmarshaller.unmarshal(org.apache.openejb.config.sys.Openejb.class, reader);
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
