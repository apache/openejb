/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.openejb.ri.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.ejb.EJBObject;

import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.EnvProps;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.proxy.ProxyManager;

public class Server implements Runnable, org.openejb.spi.ApplicationServer {
    
    /**
    * The SYSTEM_STABLE variable is used in the run method of the SocketHanlder inner class. 
    * It allows the SocketHanlder objects to continue to process requests when its value is true.
    * The SYSTEM_STABLE variable is set to false if a org.openejb.SystemException is thrown by the container System.
    * This allows all current requests to finish processing but will not allow new requests to be processed.
    */
    static boolean SYSTEM_STABLE = true;
    
    
    NamingServer namingServer;
    private SafeToolkit toolkit = SafeToolkit.getToolkit("RI Server");

    public static final String OPENEJB_CONFIG_FILE = "openejb_config_file";
    public static final String RI_HOST_IP = "ri_host_ip";
    public static final String RI_HOST_PORT = "ri_host_port";
    public static final String NAMING_SERVER_PROPERTIES_FILE = "naming_server_properties_file";

    static Method removeMethod;
    static{
        try {
            removeMethod = EJBObject.class.getMethod("remove", new Class[0]);
        } catch ( NoSuchMethodException nsme ) {
            // never happen
        }
    }

    ServerSocket serverSocket = null;
    Vector clientSockets = new Vector();
    HashMap proxyMap = new HashMap();
    int port;
    String ip =  "127.0.0.1";
    Thread myThread;
    Properties props;

    public Server(Properties props)throws Exception{
        println("*********************************************");
        println("");
        println("OpenEJB Reference Implementation (RI) Server");
        println("  warning: this server is not intended for"); 
        println("  production use.");
        println("");
        println("The Ri server is intended to demonstrate");
        println("how the OpenEJB container system can be");
        println("intagrated into any server platform.");
        println("");
        println("mailing list: openejb-dev@exolab.org");
        println("web site: http://www.openejb.org");
        println("");
        println("*********************************************");
        println("Starting Ri Server...");
        SafeProperties safeProps = toolkit.getSafeProperties(props);
        this.props = props;

        props.setProperty(EnvProps.CONFIGURATION,props.getProperty(OPENEJB_CONFIG_FILE));

        port = safeProps.getPropertyAsInt(RI_HOST_PORT);
        ip = safeProps.getProperty(RI_HOST_IP);
        println("ip: "+ip+"...");
        println("port: "+port+"...");
        println("Initializing OpenEJB container system...");
        OpenEJB.init(props, this);

        

        //FIXME: change this to just use the containerMap (deployID:container)
        println("Starting JNDI naming server...");
        namingServer = new NamingServer(1098, loadNameMap(), ip, port);
        println("Ready!");
        myThread = new Thread(this);
        myThread.start();
    }

    private HashMap loadNameMap() throws Exception {
        HashMap nameMap = new HashMap();
        String CURRENT_OPPERATION = "Initializing JNDI name space...";

        try{
            
            CURRENT_OPPERATION += "reading properties file...";
            File propsFile = new File(this.props.getProperty(NAMING_SERVER_PROPERTIES_FILE));
            FileInputStream fis = new FileInputStream(propsFile.getAbsoluteFile());
            
            Properties props = new Properties();
            props.load(fis);
            fis.close();
            
            CURRENT_OPPERATION += "retreiving deployments...";
            DeploymentInfo [] deployments = OpenEJB.deployments();
            
            String ITERATIVE_OPPERATION = "";
            for ( int i = 0; i < deployments.length; i++ ) {
                
                String entry = (String)props.get(deployments[i].getDeploymentID());
                ITERATIVE_OPPERATION = "deployment="+deployments[i].getDeploymentID()+"...";
                if ( entry!=null ) {
                    ITERATIVE_OPPERATION += "parsing namespace entry...";
                    String path = entry;
                    ITERATIVE_OPPERATION += "JNDI name="+path+"...";
                    ITERATIVE_OPPERATION += "home interface name="+deployments[i].getHomeInterface()+"...";
                    
                    try {
                        ITERATIVE_OPPERATION += "loading proxy class...";
                        Class homeClass = ProxyManager.getProxyClass(deployments[i].getHomeInterface());
                        if ( path!=null ) {
                            ITERATIVE_OPPERATION += "binding entry in namespace...";
                            RiBinding binding = new RiBinding(deployments[i].getContainer(), deployments[i], path, homeClass, null);
                            nameMap.put(path,binding);
                            proxyMap.put(deployments[i].getDeploymentID(),binding);
                            ITERATIVE_OPPERATION += "OPERATION COMPLETED.";
                        }
                    } catch ( Exception e ) {
                        println("[Ri Server] "+CURRENT_OPPERATION+ITERATIVE_OPPERATION+"ERROR...OPERATION TERMINATED.");
                        //e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            println("[Ri Server] "+CURRENT_OPPERATION+"ERROR...OPERATION TERMINATED.");
            //e.printStackTrace();
            throw e;
        }
        return nameMap;
    }

    public void run( ) {
        try {
            serverSocket = new ServerSocket(port);
            while ( true ) {
                Socket socket = serverSocket.accept();
                clientSockets.addElement(new SocketHandler(socket));
            }

        } catch ( Exception e ) {
            println("ERROR: " +e.getMessage());
            //e.printStackTrace();
        }

    }
    protected void dereference(SocketHandler hndlr) {
        try {
            hndlr.mySocket.close();
        } catch ( Exception x ) {
        }
        clientSockets.remove(hndlr);
    }
    
    private void log(InvalidateReferenceException ire) {
        println("InvalidateReferenceException: Nested exception is = ");
        Throwable t = ire.getRootCause();
        if ( t!=null )
            t.printStackTrace();
        else
            println("No nested exception");

    }

    public static void main(String [] args) {
        try {
            if ( args.length != 1 ) {
                println("Usage: java org.openejb.ri.server.Server FILE");
                println("\texample: java org.openejb.ri.server.Server RiServer.properties");
                System.exit(-1);
            }
            File propsFile = new File(args[0]);
            FileInputStream input = new FileInputStream(propsFile.getAbsoluteFile());
            Properties props = new Properties();
            props.load(input);
            String configDirectory = props.getProperty("configuration_directory");
            if(configDirectory!=null){
                System.setProperty("user.dir",configDirectory);
            }
            Server myServer = new Server(props);
            // dont allow the server to exit
            while ( true ) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch ( InterruptedException e ) {
                }
            }
        } catch ( Exception re ) {
            System.out.println("[RI Server] FATAL ERROR: "+ re.getMessage());
            re.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Checks the authorization of the client requesting access to the Ri server.
     * 
     * @param methodInvocation
     * @exception org.openejb.OpenEJBException
     */
    public void checkAuthorization(MethodInvocation methodInvocation) throws org.openejb.OpenEJBException{

        DeploymentInfo deployInfo = methodInvocation.getDeploymentInfo();
        boolean authorized = OpenEJB.getSecurityService().isCallerAuthorized(methodInvocation.getPrincipal(), deployInfo.getAuthorizedRoles(methodInvocation.getMethod()));
        if ( !authorized )
            throw new org.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));

    }

    public Object invokeMethod(MethodInvocation mi) throws OpenEJBException{
        DeploymentInfo di = mi.getDeploymentInfo();
        RpcContainer container = (RpcContainer)di.getContainer();
        return container.invoke(di.getDeploymentID(),mi.getMethod(),mi.getArguments(), mi.getPrimaryKey(), mi.getPrincipal());
    }

    /**
     * This inner class defines objects that manage a connection from a EJBHome or 
     * EJBObject proxy on a client. Its dedicated to one proxy and process all 
     * requests from that proxy.  This object runs in its own thread responding to
     * data streamed to it from the proxy.
     */
    public class SocketHandler extends Thread {
               
        /**
         * The socket that is dedicated to this SocketHandler
         */
        Socket mySocket;
        /**
         * The ObjectInputStream used to receive incoming messages from the client.
         */
        ObjectInputStream ois;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        ObjectOutputStream oos;
        /**
         * Constructs a SocketHandler with an ObjectOutputStream and an ObjectInputStream 
         * for receiving and responding to requests made by the proxy connected to the socket.  
         * The constructor starts the thread.
         * 
         * @param socket
         * @exception Exception
         */
        public SocketHandler(Socket socket)throws Exception{
            mySocket = socket;
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());

            this.start();
        }     

        /**
         * Convers the RPC messge sent from the client into a MethodInvocation object for
         * easier use inside the SocketHandler. 
         * 
         * @param message
         * @return 
         * @exception Exception
         */
        private MethodInvocation convertRPCMessageToMethodInvocation(RPCMessage message)
        throws Exception{
            try {
                Class intrfc = Class.forName(message.interfaceName);
                Class [] paramTypes = message.paramTypes;
                for ( int i = 0; i < message.args.length; i++ ) {
                    if ( message.args[i] instanceof RPCMessage.PrimitiveArg ) {
                        RPCMessage.PrimitiveArg primArg = (RPCMessage.PrimitiveArg)message.args[i];
                        paramTypes[i] = primArg.getPrimitiveClass();
                        message.args[i] = primArg.argument;
                    }
                }
                Method mthd = intrfc.getDeclaredMethod(message.methodName, paramTypes);

                java.security.Principal caller = (java.security.Principal)namingServer.principalMap.get(message.securityToken);

                MethodInvocation mi = new MethodInvocation(mthd, message.args, message.primaryKey, OpenEJB.getDeploymentInfo(message.deploymentID), caller);

                return mi;
            } catch ( Exception e ) {
                throw e;
            }
        }
        /**
         * Responds to requests made by the proxy. Request are method invocations made
         * on the proxy and packaged into a MethodInvocation object which is serialized
         * through the socket and handled hear.  All EJBObject.class and EJBHome.class
         * methods are partially processed by the server and partially by the container.  
         * All business methods are simply delegated to the container.
         */
        public void run( ) {
            try{
            String CURRENT_OPERATION = "";
            Object retValue = null;
            while ( true ) {
                try {
                    if(!SYSTEM_STABLE){
                        /**
                        * The SYSTEM_STABLE variable is a static variable declared by the first class org.openejb.ri.Server class.
                        * It allows the SocketHanlder objects to continue to process requests while its value is true.
                        * The SYSTEM_STABLE variable is set to false if a org.openejb.SystemException is thrown by the container System.
                        * This allows all current requests to finish processing but will not allow new requests to be processed.
                        */
                        retValue = new InvalidateReferenceException(new RemoteException("OpenEJB System failure. Container system should be shut down"));
                        break;
                    }
                    // the MethodInvocation represents the method invoked on the proxy.
                    CURRENT_OPERATION = "";
                    RPCMessage message = (RPCMessage)ois.readObject();
                    CURRENT_OPERATION = "Reading rpc message...";

                    // convert RPCMessage to MethodInvocation
                    CURRENT_OPERATION += "method="+message.interfaceName+"."+message.methodName+"...";
                    MethodInvocation mi = convertRPCMessageToMethodInvocation(message);
                    try {
                        // obtain the deployment information for the bean type represented by the proxy
                        DeploymentInfo deploymentInfo = mi.getDeploymentInfo();

                        // locate the container that services the proxy
                        Container container = deploymentInfo.getContainer();

                            /** Home interface method invocation **/
                        if ( mi.getMethod().getDeclaringClass() == deploymentInfo.getHomeInterface() ) {
                            CURRENT_OPERATION += "processing home interface method...";
                            retValue = EjbHomeIntfcProcessor.processMethod(mi, message.securityToken, Server.this);
                        
                            /** EJBHome method invocation (methods defined in the javax.ejb.EJBHome class) **/
                        } else if ( mi.getMethod().getDeclaringClass() == javax.ejb.EJBHome.class ) {
                            CURRENT_OPERATION += "processing EJBHome method...";
                            retValue = EjbHomeProcessor.processMethod(mi, Server.this);
                        
                            /** EJBObject method invocation (method defined in the javax.ejb.EJBObject class) **/
                        } else if ( mi.getMethod().getDeclaringClass() == javax.ejb.EJBObject.class ) {
                            CURRENT_OPERATION += "processing EJBObject method...";
                            retValue = EjbObjectProcessor.processMethod(mi, message.securityToken, Server.this);
                        
                            /** Remote interface invocation (business methods defined in the bean's remote interface) **/
                        } else {
                            CURRENT_OPERATION += "processing remote interface method...";
                            retValue = EjbRemoteIntfcProcessor.processMethod(mi, message.securityToken, Server.this);
                        }
                                                    
                        CURRENT_OPERATION += "COMPLETED...";
                    } catch ( InvalidateReferenceException ire ) {
                        // if exception occured, do not break loop. Allow client to disconnect socket (supports shared sockets at client)
                        retValue = ire;
                        CURRENT_OPERATION += "SYSTEM ERROR...Invalidating Remote refernce...";
                        println(""+CURRENT_OPERATION+"root cause="+ire.getRootCause()+"...message="+ire.getRootCause().getMessage());
                    }  catch (org.openejb.ApplicationException ae){
                        /* application exceptions wrapper an exception that should be thrown by the client
                           proxy or stub.  In this case we extract the client exception and send it to the stub.
                           FIXME: The client exception is not really the root exception it should be extraced and using a different method
                                  maybe getClientException()
                         */
                        CURRENT_OPERATION += "APPLICATION ERROR...";
                        println(ae.getClass().getName());
                        retValue = ae.getRootCause();
                        println(""+CURRENT_OPERATION+"root cause="+ae.getRootCause()+"...message="+ae.getRootCause().getMessage());

                        ae.getRootCause().printStackTrace();
                        //ae.printStackTrace();

                    }  catch ( org.openejb.SystemException st ) {
                        /*
                          This exception indicates a serious exception occurred in the OpenEJB core classes or 
                          one of the subsystems (transaction manager, or security system, or connector).  When
                          a system exception is thrown the OpenEJB container system is considered unstable.
                        */
                        CURRENT_OPERATION += "UNEXPECTED SYSTEM ERROR... SHUT DOWN OpenEJB REQUIRED";
                        retValue = new InvalidateReferenceException(new RemoteException("OpenEJB System failure. The Server should be shut down"));
                        /**
                        * The SYSTEM_STABLE variable is a global static variable used in the run method of the SocketHanlder inner class. 
                        * It allows the SocketHanlder objects to continue to process requests when its value is true.
                        * The SYSTEM_STABLE variable is set to false if a org.openejb.SystemException is thrown by the container System.
                        * This allows all current requests to finish processing but will not allow new requests to be processed.
                        */
                        SYSTEM_STABLE = false;
                        println(""+CURRENT_OPERATION);
                    }  catch ( Exception re ) {
                        /*
                          OpenEJB only throws SystemException, ApplicationException, and InvalidateReferenceException
                          any other exception would be thrown by the RI Server and would indicate that the server is not
                          stable.
                        */
                        CURRENT_OPERATION += "UNEXPECTED SYSTEM ERROR... SHUT DOWN OpenEJB REQUIRED";
                        retValue = new InvalidateReferenceException(new RemoteException("RI Server failure. The Server should be shut down"));
                        /**
                        * The SYSTEM_STABLE variable is a global static variable used in the run method of the SocketHanlder inner class. 
                        * It allows the SocketHanlder objects to continue to process requests when its value is true.
                        * The SYSTEM_STABLE variable is set to false if a org.openejb.SystemException is thrown by the container System.
                        * This allows all current requests to finish processing but will not allow new requests to be processed.
                        */
                        SYSTEM_STABLE = false;
                        println(""+CURRENT_OPERATION);
                    } 

                    if ( retValue instanceof InvalidateReferenceException ) {
                        println(""+CURRENT_OPERATION);
                        //Server.this.log((InvalidateReferenceException)retValue);
                        break;
                    }

                    CURRENT_OPERATION = "sending response: "+retValue;
                    oos.writeObject(retValue);
                    oos.flush();
                    oos.reset();

                } catch ( SocketException se ) {
                    //Logger.getSystemLogger().println(Messages.format("sa0001", "RMH Naming Server"));
                    println(""+CURRENT_OPERATION+"Connection reset by peer...OPERATION TERMINATED.");
                    return;
                } catch ( Exception e ) {
                    println(""+CURRENT_OPERATION+ "UNEXPLAINABLE ERROR...OPERATION TERMINATED...Invalidating remote reference.  "+e.getMessage());
                    e.getMessage();
                    e.printStackTrace();
                    retValue = new InvalidateReferenceException(new RemoteException());
                    break;
                }
            }
            sendReturnValue(retValue);
            }finally{
                    try{
                        mySocket.close();
                    }catch(Exception e){
                        // do nothing
                    }
            }
        }
        
        private void sendReturnValue(Object retValue){
            try {
                if ( retValue instanceof InvalidateReferenceException ) {
                    log((InvalidateReferenceException)retValue);
                }
                oos.writeObject(retValue);
                oos.flush();
                oos.reset();
            } catch ( Exception e ) {
                e.printStackTrace();
            } finally {
                Server.this.dereference(this);
                try {
                    if ( oos!=null )oos.close();
                } catch ( Exception e ) {
                }
                try {
                    if ( ois!=null )ois.close();
                } catch ( Exception e2 ) {
                }
            }

        }
    }

    public static void println(String str){
        System.out.println("[RI Server] "+str);
    }
    
    //=============================================================
    //  ApplicationServer interface methods
    //=============================================================
    
    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo proxyInfo){
        byte type = 0;
        org.openejb.DeploymentInfo deployment = proxyInfo.getDeploymentInfo();
        switch(deployment.getComponentType()){
            case DeploymentInfo.STATEFUL:
                type = RiMetaData.STATEFUL;
                break;
            case DeploymentInfo.STATELESS:
                type = RiMetaData.STATELESS;
                break;
            case DeploymentInfo.BMP_ENTITY:
            case DeploymentInfo.CMP_ENTITY:
                type = RiMetaData.ENTITY;
        }
        return new RiMetaData(deployment.getHomeInterface(), deployment.getRemoteInterface(), deployment.getPrimaryKeyClass(), type);
        
    }
    
    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo proxyInfo){
        return null;
    }
    
    public javax.ejb.Handle getHandle(ProxyInfo proxyInfo){
        javax.ejb.EJBObject ejbObject = getEJBObject(proxyInfo);
        RiBaseHandle handle = new RiBaseHandle();
        handle.setProxy(ejbObject);
        return handle;
    }
    public javax.ejb.EJBObject getEJBObject(ProxyInfo proxyInfo){
        try{
            // FIXME: What should be used for the security token, if the thread context is empty.
            org.openejb.util.proxy.InvocationHandler handler = new EjbProxyHandler(port, ip, proxyInfo.getPrimaryKey(),proxyInfo.getDeploymentInfo().getDeploymentID(), null);
            return (javax.ejb.EJBObject)ProxyManager.newProxyInstance(proxyInfo.getDeploymentInfo().getRemoteInterface(), handler);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ApplicationServer impl could not replace EJBObject");
        }
    }
    
    public javax.ejb.EJBHome getEJBHome(ProxyInfo proxyInfo){
        try{
            // FIXME: What should be used for the security token, if the thread context is empty.
            org.openejb.util.proxy.InvocationHandler handler = new EjbProxyHandler(port, ip, null,proxyInfo.getDeploymentInfo().getDeploymentID(), null);
            return (javax.ejb.EJBHome)ProxyManager.newProxyInstance(proxyInfo.getDeploymentInfo().getHomeInterface(), handler);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ApplicationServer impl could not replace EJBObject");
        }
    }
    
    
    //=============================================================
    //  Inner class for sending commands to the server at runtime
    //
    // Server commands.
    public static final String[] COMMANDS = {"help", "stop", "start", "restart", "pause", "exit"};
    public static final int HELP = 0;
    public static final int STOP = 1;
    public static final int START = 2;
    public static final int RESTART = 3;
    public static final int PAUSE = 4;
    public static final int EXIT = 5;

    public class ServerControl extends Thread {

        public void run(){
            while (true) {
                try{
                    DataInputStream commandLineIn = new DataInputStream( System.in );
                    String command = commandLineIn.readLine();
                    command = command.trim();
                    int currentCommand = -1;
                    for (int i=0; i< COMMANDS.length; i++) {
                        if (COMMANDS[i].equalsIgnoreCase(command)){
                            currentCommand = i;
                            break;
                        }
                    }
    
                    switch( currentCommand ) {
                        case HELP: 
                            // Diplay commands
                            break;
                        case STOP: 
                            // Stop the server. Don't exit the VM.
                            break;    
                        case START: 
                            // Start the the server.
                            break;
                        case RESTART: 
                            // Stop than start the server.
                            break;
                        case PAUSE: 
                            // Pause the the server.
                            break;
                            // Stop the server. Exit the VM.
                        case EXIT: break;
                    }
                } catch (Exception e){
                    // Unrecognized command.  Display help.
                }
            }
        }

    }

    //
    //  Inner class for sending commands to the server at runtime
    //=============================================================

}

