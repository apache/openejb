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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server;

import java.io.*;
import java.net.*;
import java.util.*;
import org.openejb.*;
import org.openejb.util.*;


/**
 * This is the base class for orcistrating the other daemons 
 * which actually accept and react to calls coming in from 
 * different protocols or channels.
 * 
 * To perform this task, this class will
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 * 
 * 
 */
public class ServiceManager {

    static Messages messages = new Messages( "org.openejb.server" );
    static Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server" );

    private static ServiceManager manager;
    
    private static HashMap propsByFile = new HashMap();
    private static HashMap fileByProps = new HashMap();

    private static ServerService[] daemons;

    private boolean stop = false;

    private ServiceManager(){}

    public static ServiceManager getManager(){
        if (manager == null) {
            manager = new ServiceManager();
        }

        return manager;
    }

    // Have properties files (like xinet.d) that specifies what daemons to 
    // Look into the xinet.d file structure again
    // conf/server.d/
    //    admin.properties
    //    ejbd.properties
    //    webadmin.properties
    //    telnet.properties
    //    corba.properties
    //    soap.properties
    //    xmlrpc.properties
    //    httpejb.properties
    //    webejb.properties
    //    xmlejb.properties
    // Each contains the class name of the daemon implamentation
    // The port to use
    // whether it's turned on
    public void init() throws Exception {
        try{
            org.apache.log4j.MDC.put("SERVER", "main");
            InetAddress localhost = InetAddress.getLocalHost();
            org.apache.log4j.MDC.put("HOST", localhost.getHostName());
        } catch (Exception e){
        }

        
        // Get the properties files
        //  - hard coded for now -
        String[] serviceFiles = new String[] {
            "admin.properties",
            "ejbd.properties",
            "telnet.properties",
            "webadmin.properties"
        };
        
        Vector enabledServers = new Vector();

        for (int i=0; i < serviceFiles.length; i++){
            try{
                //Properties props = getProperties("conf/server.d/"+serviceFiles[i]);
                Properties props = getProperties(serviceFiles[i]);
                if (isEnabled(props)){
                    ServerService server = createService(props);
                    server = wrapService(server);
                    server.init(props);
                    enabledServers.add(server);
                }
            } catch (Throwable e){
                logger.i18n.error("service.not.loaded", serviceFiles[i], e.getMessage());
            }
        }


        daemons = new ServerService[enabledServers.size()];
        enabledServers.copyInto(daemons);


        //--This is a temp solution ---//
//        daemons = new Service[]{
//            new AdminService(),
//            new EjbService(),
//            new TelnetService(),
//            new WebAdminService()
//            new XmlRpcService(),
//            new EjbXmlService(),
//            new WebEjbService(),
//        };

    }

    private static Properties getProperties(String file) throws ServiceException{
        Properties props = (Properties)propsByFile.get(file);

        if (props == null) {
            props = loadProperties(file);
            propsByFile.put(file,props);
            fileByProps.put(props,file);
        }
        
        return props;
    }

    private static Properties loadProperties(String file) throws ServiceException{
        Properties props = new Properties();
        try{
            URL url = new URL("resource:/"+file);
            props.load(url.openStream());
        } catch (Exception e){
            //e.printStackTrace();
            throw new ServiceException("Cannot load properties",e);
        }
        return props;
    }
    
    private ServerService createService(Properties props) throws ServiceException {
        ServerService service = null;
        
        String serviceClassName = getRequiredProperty("server",props);
        Class serviceClass = loadClass(serviceClassName);
        checkImplementation(serviceClass);
        service = instantiateService(serviceClass);
        
        return service;
    }
    
    private ServerService wrapService(ServerService service){
        service = new ServiceLogger(service);
        service = new ServiceAccessController(service);
        service = new ServiceDaemon(service);
        return service;
    }

    public synchronized void start() throws ServiceException {
        System.out.println("  ** Starting Services **");
        printRow("NAME", "IP", "PORT");
        
        for (int i=0; i < daemons.length; i++){
            ServerService d = daemons[i];
            try{
                d.start();
                printRow(d.getName(), d.getIP(), d.getPort()+"");
            } catch (Exception e){
                logger.error(d.getName()+" "+d.getIP()+" "+d.getPort()+": "+e.getMessage());
                printRow(d.getName(), "----", "FAILED");
            }
        }

        System.out.println("-------");
        System.out.println("Ready!");
        /*
         * This will cause the user thread (the thread that keeps the
         *  vm alive) to go into a state of constant waiting.
         *  Each time the thread is woken up, it checks to see if
         *  it should continue waiting.
         *
         *  To stop the thread (and the VM), just call the stop method
         *  which will set 'stop' to true and notify the user thread.
         */
        try{
            while ( !stop ) {
                //System.out.println("[] waiting to stop \t["+Thread.currentThread().getName()+"]");
                this.wait(Long.MAX_VALUE);
            }
        } catch (Throwable t){
            logger.fatal("Unable to keep the server thread alive. Received exception: "+t.getClass().getName()+" : "+t.getMessage());
        }
        System.out.println("[] exiting vm");
        logger.info("Stopping Remote Server");

    }

    public synchronized void stop() throws ServiceException {
        System.out.println("[] received stop signal");
        stop = true;
        for (int i=0; i < daemons.length; i++){
            daemons[i].stop();
        }
        notifyAll();
    }

    public void printRow(String col1, String col2, String col3){

        // column 1 is 20 chars long
        col1 += "                    ";
        col1 = col1.substring(0,20);
        
        // column 2 is 15 chars long
        col2 += "                    ";
        col2 = col2.substring(0,15);
        
        // column 3 is 6 chars long
        col3 += "                    ";
        col3 = col3.substring(0,6);

        StringBuffer sb = new StringBuffer(50);
        sb.append("  ").append(col1);
        sb.append(" ").append(col2);
        sb.append(" ").append(col3);

        System.out.println(sb.toString());
    }

    /**
     * Loads the service class passed in
     * 
     * @param className
     * 
     * @return 
     * @exception ServiceException
     */
    private Class loadClass(String className) throws ServiceException{
        ClassLoader loader = org.openejb.util.ClasspathUtils.getContextClassLoader();
        Class clazz = null;
        try{
            clazz = Class.forName(className,true,loader);
        } 
        catch (ClassNotFoundException cnfe){
            String msg = messages.format("service.no.class", className);
            throw new ServiceException( msg );
        } 
        return clazz;
    }

    /**
     * Does this class implement the ServerService interface?
     * 
     * @param clazz
     * 
     * @exception ServiceException
     */
    private void checkImplementation(Class clazz) throws ServiceException{
        Class intrfce = org.openejb.server.ServerService.class;

        if ( !intrfce.isAssignableFrom(clazz) ){
            String msg = messages.format("service.bad.impl", clazz.getName(),intrfce.getName());
            throw new ServiceException(msg);
        }
    }


    /**
     * Instantiates the specified service
     * 
     * @param clazz
     * 
     * @return 
     * @exception ServiceException
     */
    private ServerService instantiateService(Class clazz) throws ServiceException{
        ServerService service = null;
        
        try{
            service = (ServerService)clazz.newInstance();
        } 
        catch (Throwable t){
            String msg = messages.format(
                "service.instantiation.err", 
                clazz.getName(),
                t.getClass().getName(),
                t.getMessage());

            throw new ServiceException( msg, t );
        } 

        return service;
    }

    private boolean isEnabled(Properties props) throws ServiceException{
         // if it should be started, continue
        String dissabled = props.getProperty("dissabled","");
        
        if (dissabled.equalsIgnoreCase("yes") || dissabled.equalsIgnoreCase("true")) {
            return false;
        } else {
            return true;
        }
    }
 
    

    public static String getRequiredProperty(String name, Properties props) throws ServiceException{
         
        String value = props.getProperty(name);
        if (value == null) {
            String msg = messages.format(
                "service.missing.property", 
                name, fileByProps.get(props));

            throw new ServiceException( msg );
        }

        return value;
    }
    

}
