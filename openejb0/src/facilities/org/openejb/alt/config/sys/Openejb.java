/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

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
public class Openejb implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
    **/
    private java.lang.String _content = "";

    private java.util.Vector _containerList;

    private java.util.Vector _jndiProviderList;

    private SecurityService _securityService;

    private TransactionService _transactionService;

    private ConnectionManager _connectionManager;

    private ProxyFactory _proxyFactory;

    private java.util.Vector _connectorList;

    private java.util.Vector _resourceList;

    private java.util.Vector _deploymentsList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Openejb() {
        super();
        _containerList = new Vector();
        _jndiProviderList = new Vector();
        _connectorList = new Vector();
        _resourceList = new Vector();
        _deploymentsList = new Vector();
    } //-- org.openejb.alt.config.sys.Openejb()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vConnector
    **/
    public void addConnector(Connector vConnector)
        throws java.lang.IndexOutOfBoundsException
    {
        _connectorList.addElement(vConnector);
    } //-- void addConnector(Connector) 

    /**
     * 
     * @param index
     * @param vConnector
    **/
    public void addConnector(int index, Connector vConnector)
        throws java.lang.IndexOutOfBoundsException
    {
        _connectorList.insertElementAt(vConnector, index);
    } //-- void addConnector(int, Connector) 

    /**
     * 
     * @param vContainer
    **/
    public void addContainer(Container vContainer)
        throws java.lang.IndexOutOfBoundsException
    {
        _containerList.addElement(vContainer);
    } //-- void addContainer(Container) 

    /**
     * 
     * @param index
     * @param vContainer
    **/
    public void addContainer(int index, Container vContainer)
        throws java.lang.IndexOutOfBoundsException
    {
        _containerList.insertElementAt(vContainer, index);
    } //-- void addContainer(int, Container) 

    /**
     * 
     * @param vDeployments
    **/
    public void addDeployments(Deployments vDeployments)
        throws java.lang.IndexOutOfBoundsException
    {
        _deploymentsList.addElement(vDeployments);
    } //-- void addDeployments(Deployments) 

    /**
     * 
     * @param index
     * @param vDeployments
    **/
    public void addDeployments(int index, Deployments vDeployments)
        throws java.lang.IndexOutOfBoundsException
    {
        _deploymentsList.insertElementAt(vDeployments, index);
    } //-- void addDeployments(int, Deployments) 

    /**
     * 
     * @param vJndiProvider
    **/
    public void addJndiProvider(JndiProvider vJndiProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _jndiProviderList.addElement(vJndiProvider);
    } //-- void addJndiProvider(JndiProvider) 

    /**
     * 
     * @param index
     * @param vJndiProvider
    **/
    public void addJndiProvider(int index, JndiProvider vJndiProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _jndiProviderList.insertElementAt(vJndiProvider, index);
    } //-- void addJndiProvider(int, JndiProvider) 

    /**
     * 
     * @param vResource
    **/
    public void addResource(Resource vResource)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceList.addElement(vResource);
    } //-- void addResource(Resource) 

    /**
     * 
     * @param index
     * @param vResource
    **/
    public void addResource(int index, Resource vResource)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceList.insertElementAt(vResource, index);
    } //-- void addResource(int, Resource) 

    /**
    **/
    public java.util.Enumeration enumerateConnector()
    {
        return _connectorList.elements();
    } //-- java.util.Enumeration enumerateConnector() 

    /**
    **/
    public java.util.Enumeration enumerateContainer()
    {
        return _containerList.elements();
    } //-- java.util.Enumeration enumerateContainer() 

    /**
    **/
    public java.util.Enumeration enumerateDeployments()
    {
        return _deploymentsList.elements();
    } //-- java.util.Enumeration enumerateDeployments() 

    /**
    **/
    public java.util.Enumeration enumerateJndiProvider()
    {
        return _jndiProviderList.elements();
    } //-- java.util.Enumeration enumerateJndiProvider() 

    /**
    **/
    public java.util.Enumeration enumerateResource()
    {
        return _resourceList.elements();
    } //-- java.util.Enumeration enumerateResource() 

    /**
     * Returns the value of field 'connectionManager'.
     * @return the value of field 'connectionManager'.
    **/
    public ConnectionManager getConnectionManager()
    {
        return this._connectionManager;
    } //-- ConnectionManager getConnectionManager() 

    /**
     * 
     * @param index
    **/
    public Connector getConnector(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _connectorList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Connector) _connectorList.elementAt(index);
    } //-- Connector getConnector(int) 

    /**
    **/
    public Connector[] getConnector()
    {
        int size = _connectorList.size();
        Connector[] mArray = new Connector[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Connector) _connectorList.elementAt(index);
        }
        return mArray;
    } //-- Connector[] getConnector() 

    /**
    **/
    public int getConnectorCount()
    {
        return _connectorList.size();
    } //-- int getConnectorCount() 

    /**
     * 
     * @param index
    **/
    public Container getContainer(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _containerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Container) _containerList.elementAt(index);
    } //-- Container getContainer(int) 

    /**
    **/
    public Container[] getContainer()
    {
        int size = _containerList.size();
        Container[] mArray = new Container[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Container) _containerList.elementAt(index);
        }
        return mArray;
    } //-- Container[] getContainer() 

    /**
    **/
    public int getContainerCount()
    {
        return _containerList.size();
    } //-- int getContainerCount() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * @return the value of field 'content'.
    **/
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * 
     * @param index
    **/
    public Deployments getDeployments(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _deploymentsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Deployments) _deploymentsList.elementAt(index);
    } //-- Deployments getDeployments(int) 

    /**
    **/
    public Deployments[] getDeployments()
    {
        int size = _deploymentsList.size();
        Deployments[] mArray = new Deployments[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Deployments) _deploymentsList.elementAt(index);
        }
        return mArray;
    } //-- Deployments[] getDeployments() 

    /**
    **/
    public int getDeploymentsCount()
    {
        return _deploymentsList.size();
    } //-- int getDeploymentsCount() 

    /**
     * 
     * @param index
    **/
    public JndiProvider getJndiProvider(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _jndiProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (JndiProvider) _jndiProviderList.elementAt(index);
    } //-- JndiProvider getJndiProvider(int) 

    /**
    **/
    public JndiProvider[] getJndiProvider()
    {
        int size = _jndiProviderList.size();
        JndiProvider[] mArray = new JndiProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (JndiProvider) _jndiProviderList.elementAt(index);
        }
        return mArray;
    } //-- JndiProvider[] getJndiProvider() 

    /**
    **/
    public int getJndiProviderCount()
    {
        return _jndiProviderList.size();
    } //-- int getJndiProviderCount() 

    /**
     * Returns the value of field 'proxyFactory'.
     * @return the value of field 'proxyFactory'.
    **/
    public ProxyFactory getProxyFactory()
    {
        return this._proxyFactory;
    } //-- ProxyFactory getProxyFactory() 

    /**
     * 
     * @param index
    **/
    public Resource getResource(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Resource) _resourceList.elementAt(index);
    } //-- Resource getResource(int) 

    /**
    **/
    public Resource[] getResource()
    {
        int size = _resourceList.size();
        Resource[] mArray = new Resource[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Resource) _resourceList.elementAt(index);
        }
        return mArray;
    } //-- Resource[] getResource() 

    /**
    **/
    public int getResourceCount()
    {
        return _resourceList.size();
    } //-- int getResourceCount() 

    /**
     * Returns the value of field 'securityService'.
     * @return the value of field 'securityService'.
    **/
    public SecurityService getSecurityService()
    {
        return this._securityService;
    } //-- SecurityService getSecurityService() 

    /**
     * Returns the value of field 'transactionService'.
     * @return the value of field 'transactionService'.
    **/
    public TransactionService getTransactionService()
    {
        return this._transactionService;
    } //-- TransactionService getTransactionService() 

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
    public void removeAllConnector()
    {
        _connectorList.removeAllElements();
    } //-- void removeAllConnector() 

    /**
    **/
    public void removeAllContainer()
    {
        _containerList.removeAllElements();
    } //-- void removeAllContainer() 

    /**
    **/
    public void removeAllDeployments()
    {
        _deploymentsList.removeAllElements();
    } //-- void removeAllDeployments() 

    /**
    **/
    public void removeAllJndiProvider()
    {
        _jndiProviderList.removeAllElements();
    } //-- void removeAllJndiProvider() 

    /**
    **/
    public void removeAllResource()
    {
        _resourceList.removeAllElements();
    } //-- void removeAllResource() 

    /**
     * 
     * @param index
    **/
    public Connector removeConnector(int index)
    {
        java.lang.Object obj = _connectorList.elementAt(index);
        _connectorList.removeElementAt(index);
        return (Connector) obj;
    } //-- Connector removeConnector(int) 

    /**
     * 
     * @param index
    **/
    public Container removeContainer(int index)
    {
        java.lang.Object obj = _containerList.elementAt(index);
        _containerList.removeElementAt(index);
        return (Container) obj;
    } //-- Container removeContainer(int) 

    /**
     * 
     * @param index
    **/
    public Deployments removeDeployments(int index)
    {
        java.lang.Object obj = _deploymentsList.elementAt(index);
        _deploymentsList.removeElementAt(index);
        return (Deployments) obj;
    } //-- Deployments removeDeployments(int) 

    /**
     * 
     * @param index
    **/
    public JndiProvider removeJndiProvider(int index)
    {
        java.lang.Object obj = _jndiProviderList.elementAt(index);
        _jndiProviderList.removeElementAt(index);
        return (JndiProvider) obj;
    } //-- JndiProvider removeJndiProvider(int) 

    /**
     * 
     * @param index
    **/
    public Resource removeResource(int index)
    {
        java.lang.Object obj = _resourceList.elementAt(index);
        _resourceList.removeElementAt(index);
        return (Resource) obj;
    } //-- Resource removeResource(int) 

    /**
     * Sets the value of field 'connectionManager'.
     * @param connectionManager the value of field
     * 'connectionManager'.
    **/
    public void setConnectionManager(ConnectionManager connectionManager)
    {
        this._connectionManager = connectionManager;
    } //-- void setConnectionManager(ConnectionManager) 

    /**
     * 
     * @param index
     * @param vConnector
    **/
    public void setConnector(int index, Connector vConnector)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _connectorList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _connectorList.setElementAt(vConnector, index);
    } //-- void setConnector(int, Connector) 

    /**
     * 
     * @param connectorArray
    **/
    public void setConnector(Connector[] connectorArray)
    {
        //-- copy array
        _connectorList.removeAllElements();
        for (int i = 0; i < connectorArray.length; i++) {
            _connectorList.addElement(connectorArray[i]);
        }
    } //-- void setConnector(Connector) 

    /**
     * 
     * @param index
     * @param vContainer
    **/
    public void setContainer(int index, Container vContainer)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _containerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _containerList.setElementAt(vContainer, index);
    } //-- void setContainer(int, Container) 

    /**
     * 
     * @param containerArray
    **/
    public void setContainer(Container[] containerArray)
    {
        //-- copy array
        _containerList.removeAllElements();
        for (int i = 0; i < containerArray.length; i++) {
            _containerList.addElement(containerArray[i]);
        }
    } //-- void setContainer(Container) 

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * @param content the value of field 'content'.
    **/
    public void setContent(java.lang.String content)
    {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * 
     * @param index
     * @param vDeployments
    **/
    public void setDeployments(int index, Deployments vDeployments)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _deploymentsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _deploymentsList.setElementAt(vDeployments, index);
    } //-- void setDeployments(int, Deployments) 

    /**
     * 
     * @param deploymentsArray
    **/
    public void setDeployments(Deployments[] deploymentsArray)
    {
        //-- copy array
        _deploymentsList.removeAllElements();
        for (int i = 0; i < deploymentsArray.length; i++) {
            _deploymentsList.addElement(deploymentsArray[i]);
        }
    } //-- void setDeployments(Deployments) 

    /**
     * 
     * @param index
     * @param vJndiProvider
    **/
    public void setJndiProvider(int index, JndiProvider vJndiProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _jndiProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _jndiProviderList.setElementAt(vJndiProvider, index);
    } //-- void setJndiProvider(int, JndiProvider) 

    /**
     * 
     * @param jndiProviderArray
    **/
    public void setJndiProvider(JndiProvider[] jndiProviderArray)
    {
        //-- copy array
        _jndiProviderList.removeAllElements();
        for (int i = 0; i < jndiProviderArray.length; i++) {
            _jndiProviderList.addElement(jndiProviderArray[i]);
        }
    } //-- void setJndiProvider(JndiProvider) 

    /**
     * Sets the value of field 'proxyFactory'.
     * @param proxyFactory the value of field 'proxyFactory'.
    **/
    public void setProxyFactory(ProxyFactory proxyFactory)
    {
        this._proxyFactory = proxyFactory;
    } //-- void setProxyFactory(ProxyFactory) 

    /**
     * 
     * @param index
     * @param vResource
    **/
    public void setResource(int index, Resource vResource)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceList.setElementAt(vResource, index);
    } //-- void setResource(int, Resource) 

    /**
     * 
     * @param resourceArray
    **/
    public void setResource(Resource[] resourceArray)
    {
        //-- copy array
        _resourceList.removeAllElements();
        for (int i = 0; i < resourceArray.length; i++) {
            _resourceList.addElement(resourceArray[i]);
        }
    } //-- void setResource(Resource) 

    /**
     * Sets the value of field 'securityService'.
     * @param securityService the value of field 'securityService'.
    **/
    public void setSecurityService(SecurityService securityService)
    {
        this._securityService = securityService;
    } //-- void setSecurityService(SecurityService) 

    /**
     * Sets the value of field 'transactionService'.
     * @param transactionService the value of field
     * 'transactionService'.
    **/
    public void setTransactionService(TransactionService transactionService)
    {
        this._transactionService = transactionService;
    } //-- void setTransactionService(TransactionService) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.sys.Openejb unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.sys.Openejb) Unmarshaller.unmarshal(org.openejb.alt.config.sys.Openejb.class, reader);
    } //-- org.openejb.alt.config.sys.Openejb unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
