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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.NameClassPair;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.util.Messages;
import org.openejb.util.proxy.DynamicProxyFactory;
import org.openejb.util.proxy.ProxyManager;


public class NamingServer implements Runnable {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    Thread thread = new Thread(this);
    HashMap nameMap;
    HashMap principalMap = new HashMap();
    final static byte REQUESTING_LIST = (byte)1;
    final static byte REQUESTING_BINDINGS = (byte)2;
    final static byte REQUESTING_CLASS = (byte)3;
    final static byte CLOSE_CONNECTION = (byte)4;

    String serverIP;
    int serverPort;
    int namingPort = 0;

    public NamingServer(int namingPort, HashMap nameBindings, String serverIP, int serverPort)throws Exception{
        this.namingPort = namingPort;
        this.serverPort = serverPort;
        this.serverIP = serverIP;
        nameMap = nameBindings;
        thread.start();
    }

    public void run( ) {
        try {
            ServerSocket serverSocket = new ServerSocket(namingPort);
            while ( true ) {
                Socket socket = serverSocket.accept();
                new SocketHandler(socket);

            }
        } catch ( IOException ioe ) {
            //ioe.printStackTrace();
        }
    }

    //==========================================
    // Inner class for handling sockets opened
    // with clients in a dedicated thread.
    //
    public class SocketHandler extends Thread {
        Socket mySocket;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        public SocketHandler(Socket socket) throws IOException{
            mySocket = socket;

            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());

            this.start();
        }

        private Object createProxy(AuthenticationRequest authRequest,RiBinding binding ) throws Exception{
            Object proxy = binding.getEJBHome();
            EjbProxyHandler handler = new EjbProxyHandler(serverPort, serverIP, null,binding.getDeploymentInfo().getDeploymentID(),authRequest.toString());
            principalMap.put(authRequest, authRequest);
            ProxyManager.setInvocationHandler(proxy, handler);
            return proxy;
        }

        public void run( ) {
            String CURRENT_OPERATION ="";
            try {
                CURRENT_OPERATION = _messages.message( "namingServer.authenticatingReading" );
                AuthenticationRequest authRequest = (AuthenticationRequest)ois.readObject();

                CURRENT_OPERATION = _messages.message( "namingServer.authenticatingWriting" );
                oos.writeObject("RECEIVED");
                oos.flush();

                while ( true ) {
                    CURRENT_OPERATION = "";
                    Object retValue = null;
                    Object obj = ois.readObject();
                    CURRENT_OPERATION = _messages.format( "namingServer.incomingReading", authRequest.getUserID() );
                    if ( obj instanceof Byte ) {
                        byte code = ((Byte)obj).byteValue();
                        Iterator names = null;
                        Vector pairs = null;
                        RiBinding binding = null;
                        Object clientID = null;
                        switch ( code ) {
                            case REQUESTING_LIST:
                                CURRENT_OPERATION += _messages.message( "namingServer.requestingList" );
                                names = nameMap.keySet().iterator();
                                pairs = new Vector();
                                while ( names.hasNext() ) {
                                    String name = (String)names.next();
                                    binding = (RiBinding)nameMap.get(name);
                                    DeploymentInfo info = binding.getDeploymentInfo();
                                    org.openejb.spi.SecurityService security = OpenEJB.getSecurityService();

                                    boolean authorized = security.isCallerAuthorized(authRequest, new String[]{DeploymentInfo.AC_CREATE_EJBHOME});
                                    if ( authorized )
                                        pairs.addElement(new NameClassPair(name,info.getHomeInterface().getName()));
                                }
                                CURRENT_OPERATION += _messages.message( "namingServer.sendingList" );
                                oos.writeObject(pairs);
                                oos.flush();
                                CURRENT_OPERATION += _messages.message( "namingServer.operationComplete" );
                            case REQUESTING_BINDINGS:
                                CURRENT_OPERATION += _messages.message( "namingServer.requestingBindings" );
                                names = nameMap.keySet().iterator();
                                pairs = new Vector();
                                while ( names.hasNext() ) {
                                    String name = (String)names.next();
                                    binding = (RiBinding)nameMap.get(name);
                                    Object proxy = this.createProxy(authRequest, binding);
                                    DeploymentInfo dInfo = binding.getDeploymentInfo();
                                    pairs.addElement(new javax.naming.Binding(name,dInfo.getHomeInterface().getName(),proxy));
                                }
                                CURRENT_OPERATION += _messages.message( "namingServer.sendingBindings" );
                                oos.writeObject(pairs);
                                oos.flush();
                                CURRENT_OPERATION += _messages.message( "namingServer.operationComplete" );
                                break;
                            case REQUESTING_CLASS:
                                CURRENT_OPERATION += _messages.message( "namingServer.requestingClass" );
                                String className = "";

                                CURRENT_OPERATION += _messages.message( "namingServer.readingClassName" );
                                className = ois.readUTF();
                                CURRENT_OPERATION += _messages.format( "namingServer.loadingClass", className );
                                // FIXME: Doesn't handle ClassLoaders for hot-deploy JARs
                                Class clazz = DynamicProxyFactory.loader.loadClass(className);
                                CURRENT_OPERATION += _messages.format( "namingServer.class", clazz.getName() );
                                className = "/"+clazz.getName().replace('.','/')+".class";
                                CURRENT_OPERATION += _messages.format( "lookingUpResource.loadingClass", className );

                                InputStream in = clazz.getResourceAsStream(className);
                                CURRENT_OPERATION += _messages.format( "lookingUpResource.inputstream", in );
                                ByteArrayOutputStream baos = new ByteArrayOutputStream(8000);

                                int b=0;
                                while(true){
                                    b = in.read();
                                    if ( b == -1 ) break;
                                    baos.write(b);
                                }
                                byte[] byteCode = baos.toByteArray();
                                oos.writeInt( byteCode.length );
                                oos.write( byteCode );
                                oos.flush();
                                CURRENT_OPERATION += _messages.message( "namingServer.operationComplete" );
                                break;
                            case CLOSE_CONNECTION:
                                CURRENT_OPERATION += _messages.message( "namingServer.closeConnection" );
                                CURRENT_OPERATION += _messages.message( "namingServer.closeInputStream" );
                                ois.close();
                                CURRENT_OPERATION += _messages.message( "namingServer.closeOutputStream" );
                                oos.close();
                                CURRENT_OPERATION += _messages.message( "namingServer.closeSocket" );
                                mySocket.close();
                                ois = null;
                                oos = null;
                                mySocket = null;
                                CURRENT_OPERATION += _messages.message( "namingServer.operationComplete" );
                                break;
                        }
                    } else {
                        String name = (String)obj;
                        RiBinding binding = (RiBinding)nameMap.get(name);
                        Object proxy = this.createProxy(authRequest, binding);
                        oos.writeObject(proxy);
                        oos.flush();
                    }
                }

            } catch ( SocketException cnfe ) {
                //Logger.getSystemLogger().println(Messages.format("sa0001", "RMH Naming Server"));
                System.out.println(  _messages.format( "namingServer.connectionResetByPeer", CURRENT_OPERATION ) );

            } catch ( Exception cnfe ) {
                System.out.println(  _messages.format( "namingServer.error", CURRENT_OPERATION ) );
                System.out.println("\n"+cnfe+"\n");
                // do something interesting
                //cnfe.printStackTrace();
            } finally {
                try {
                    if ( mySocket!=null )mySocket.close();
                } catch ( Exception e ) {
                }
            }
        }
    }

    //
    // Inner class for handling sockets opened
    // with clients in a dedicated thread.
    //==========================================

    public static void main(String [] args)throws Exception{


    }


}

