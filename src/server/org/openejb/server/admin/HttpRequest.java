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
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.naming.*;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class HttpRequest {
    
    
    /** 5.1   Request-Line */
    String line;


    /** 5.1.1    Method */
    int method;
         
    public static int OPTIONS = 0; // Section 9.2
    public static int GET     = 1; // Section 9.3
    public static int HEAD    = 2; // Section 9.4
    public static int POST    = 3; // Section 9.5
    public static int PUT     = 4; // Section 9.6
    public static int DELETE  = 5; // Section 9.7
    public static int TRACE   = 6; // Section 9.8
    public static int CONNECT = 7; // Section 9.9
    public static int UNSUPPORTED = 8;

    /** 5.1.2    Request-URI */
    URL uri;

    HashMap headers;
    HashMap params;
    byte[] body;
    
    /**
     */
    public void readExternal(InputStream input) throws IOException{
        DataInput in = new DataInputStream(input);
        readRequestLine(in);
        readHeaders(in);
        readBody(in);
    }
    
    /**
     */
    public void writeExternal(OutputStream out) throws IOException{

    }

    public String getHeader(String name){
        return (String)headers.get(name);
    }
    
    private void readRequestLine(DataInput in) throws IOException{
        try{
            line = in.readLine();
            System.out.println(line);
        } catch (Exception e){
            throw new IOException("Could not read the HTTP Request Line :"+ e.getClass().getName()+" : "+e.getMessage());
        }

        StringTokenizer lineParts = new StringTokenizer(line, " ");
        /* [1] Parse the method */
        parseMethod(lineParts);

        /* [2] Parse the URI */
        parseURI(lineParts);

    }

    private void parseMethod(StringTokenizer lineParts) throws IOException{
        String token = null;
        try{
            token = lineParts.nextToken();
        } catch (Exception e){
            throw new IOException("Could not parse the HTTP Request Method :"+ e.getClass().getName()+" : "+e.getMessage());
        }
        
        if ( token.equalsIgnoreCase("GET") ) {
            method = GET;
        } else if ( token.equalsIgnoreCase("POST") ) {
            method = POST;
        } else {
            method = UNSUPPORTED;
            throw new IOException("Unsupported HTTP Request Method :"+ token);
        }
    }
    
    private void parseURI(StringTokenizer lineParts) throws IOException{
        String token = null;
        try{
            token = lineParts.nextToken();
        } catch (Exception e){
            throw new IOException("Could not parse the HTTP Request Method :"+ e.getClass().getName()+" : "+e.getMessage());
        }
        
        try {
            uri = new URL("http","localhost", token );
        } catch (java.net.MalformedURLException e){
            throw new IOException("Malformed URL :"+ token +" Exception: "+e.getMessage());
        }
    }

    private void readHeaders(DataInput in) throws IOException{
        headers = new HashMap();
        while (true) {
            // Header Field
            String hf = null;
            try{
                hf = in.readLine();
                System.out.println(hf);
            } catch (Exception e){
                throw new IOException("Could not read the HTTP Request Header Field :"+ e.getClass().getName()+" : "+e.getMessage());
            }
                
            if ( hf == null || hf.equals("") ) {
                break;
            }

            StringTokenizer field = new StringTokenizer(hf,":");
            /* [1] Parse the Name */
            String name = field.nextToken();
            if (name == null) break;
            
            /* [2] Parse the Value */
            String value = field.nextToken();
            if (value == null) break;
            value = value.trim();

            headers.put(name, value);
        }
    }

    private void readBody(DataInput in) throws IOException{
        readRequestBody(in);
        
        // Content-type: application/x-www-form-urlencoded
        String type = getHeader("Content-type");
        if (type != null && type.equals("application/x-www-form-urlencoded")) {
            parseFormParams();
        }
    }
        
    private void readRequestBody(DataInput in) throws IOException{
        // Content-length: 384
        String len  = getHeader("Content-length");
        int length = -1;

        if (len != null) {
            try{
                length = Integer.parseInt(len);
            } catch (Exception e){
                //don't care
            }
        } 

        if (length < 1) {
            this.body = new byte[0];
        } else if (length > 0) {
            this.body = new byte[length];
            try {
                in.readFully( body );
            } catch (Exception e){
                throw new IOException("Could not read the HTTP Request Body :"+ e.getClass().getName()+" : "+e.getMessage());
            }
        }
    }

    private void parseFormParams() throws IOException{
        String rawParams = new String( body );
        StringTokenizer parameters = new StringTokenizer(rawParams, "&");

        while (parameters.hasMoreTokens()) {
            StringTokenizer param = new StringTokenizer(parameters.nextToken(), "=");    
            /* [1] Parse the Name */
            String name = parameters.nextToken();
            if (name == null) break;
            
            /* [2] Parse the Value */
            String value = parameters.nextToken();
            if (value == null) value = "";

            params.put(name, value);
        }
    }
}
