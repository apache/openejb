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

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 *  The Server will call the following methods.
 * 
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 * 
 * All ServerService implementations must have a no argument 
 * constructor.
 * 
 */
public class ServicePool implements ServerService {
    private static final Log log = LogFactory.getLog(ServicePool.class);

    private final ServerService next;
    private final Executor executor;

    public ServicePool(ServerService next, final String name, final int threads, final long keepAliveTime){
        this.next = next;

        PooledExecutor p = new PooledExecutor(new LinkedQueue(), threads);
        p.setKeepAliveTime(keepAliveTime);
        p.setMinimumPoolSize(threads);
        p.setThreadFactory(new ThreadFactory() {
            private volatile int id = 0;
            public Thread newThread(Runnable arg0) {
                Thread thread = new Thread(arg0, name + " " + getNextID());
                return thread;
            }
            private int getNextID() {
                return id++;
            }

        });
        executor = p;
    }

    public ServicePool(ServerService next, Executor executor){
        this.next = next;
        this.executor = executor;
    }

    public void service(final Socket socket) throws ServiceException, IOException{
        final Runnable service = new Runnable() {
            public void run() {
                try {
                    next.service(socket);
                } catch (SecurityException e) {
                    log.error( "Security error: "+ e.getMessage(), e);
                } catch (Throwable e) {
                    log.error( "Unexpected error", e);
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (Throwable t) {
                        log.warn("Error while closing connection with client", t);
                    }
                }
            }
        };

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Runnable ctxCL = new Runnable() {
            public void run() {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(tccl);
                try {
                    service.run();
                } finally {
                    Thread.currentThread().setContextClassLoader(cl);
                }
            }
        };
        
        try {
            executor.execute(ctxCL);
        } catch (InterruptedException e) {
            log.error("Error while executing service", e);
        }
    }

    /**
     * Pulls out the access log information
     * 
     * @param props
     * 
     * @exception ServiceException
     */
    public void init(Properties props) throws Exception{
        // Do our stuff
        
        // Then call the next guy
        next.init(props);
    }
    
    public void start() throws ServiceException{
        // Do our stuff
        
        // Then call the next guy
        next.start();
    }
    
    public void stop() throws ServiceException{
        // Do our stuff
        
        // Then call the next guy
        next.stop();
    }


    /**
     * Gets the name of the service.
     * Used for display purposes only
     */ 
    public String getName(){
        return next.getName();
    }

    /**
     * Gets the ip number that the 
     * daemon is listening on.
     */
    public String getIP(){
        return next.getIP();
    }
    
    /**
     * Gets the port number that the 
     * daemon is listening on.
     */
    public int getPort(){
        return next.getPort();
    }

}
