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
package org.openejb.server.admin;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import org.openejb.util.JarUtils;
import javax.naming.*;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class HttpResponse {
    
    
    /** Response string */
    String responseString = "OK";

    /** Code */
    int code = 200;
         
    public static final String HTTP_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    public static final String SP = " ";
    public static final String CSP = ": ";
    public static String server;

    HashMap headers;
        
    private byte[] body = new byte[0];
    PrintWriter writer;
    ByteArrayOutputStream baos;

    public HttpResponse(){
        this(200, "OK", "text/html");
        
    }
    
    public HttpResponse(int code, String responseString, String contentType){
        this.code = code;
        this.responseString = responseString;
        headers = new HashMap();
        
        // Default headers
        setHeader("Server", getServerName());
        setHeader("Connection","close");
        setHeader("Content-Type",contentType);

        // create the body.
        baos = new ByteArrayOutputStream();
        writer = new PrintWriter( baos );
    }
    
    public void setHeader(String name, String value){
        headers.put(name, value);
    }
    
    public String getHeader(String name){
        return (String) headers.get(name);
    }
    
    public PrintWriter getPrintWriter(){
        return writer;
    }
    
    /**
     */
    public void readExternal(InputStream in) throws IOException{
    }
    
    
    /**
     *  HTTP/1.1 200 OK
     *  Server: Netscape-Enterprise/3.6 SP3
     *  Date: Thu, 07 Jun 2001 17:30:42 GMT
     *  Content-type: text/html
     *  Connection: close
     *
     */
    public void writeExternal(OutputStream output) throws IOException{
        DataOutput out = new DataOutputStream(output);
        DataOutput log = new DataOutputStream(System.out);
        closeMessage();
        writeResponseLine(out);
        writeHeaders(out);
        writeBody(out);
        
        writeResponseLine(log);
        writeHeaders(log);
        writeBody(log);
    }

    private void closeMessage() throws IOException{
        writer.flush();
        writer.close();
        body = baos.toByteArray();
        setHeader("Content-length", body.length+"");
    }
    
    
    /**
     *  HTTP/1.1 200 OK
     */
    public void writeResponseLine(DataOutput out) throws IOException{
        out.writeBytes(HTTP_VERSION);
        out.writeBytes(SP);
        out.writeBytes(code+"");
        out.writeBytes(responseString);
        out.writeBytes(CRLF);
    }
    
    public void writeHeaders(DataOutput out) throws IOException{
        Iterator it =  headers.entrySet().iterator();

        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            out.writeBytes(""+entry.getKey());
            out.writeBytes(CSP);
            out.writeBytes(""+entry.getValue());
            out.writeBytes(CRLF);
        }
    }
    
    public void writeBody(DataOutput out) throws IOException{
        out.writeBytes(CRLF);
        out.write(body);
    }


    public static String getServerName(){
        if (server == null) {
            String version = "???";
            String os = "(unknown os)";
            try {
                Properties versionInfo = new Properties();
                JarUtils.setHandlerSystemProperty();
                versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
                version = versionInfo.getProperty( "version" );
                os = System.getProperty("os.name")+"/"+System.getProperty("os.version")+" ("+System.getProperty("os.arch")+")";
            } catch (java.io.IOException e) {
            }
            server = "OpenEJB/" +version+ " "+os;
        }
        return server;
    }    

}
