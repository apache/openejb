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
 *    please contact openejb@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
package org.openejb.server.soap;


import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.java.DefaultJavaService;
import org.openejb.server.httpd.HttpListener;
import org.openejb.server.httpd.HttpRequest;
import org.openejb.server.httpd.HttpResponse;

public class SoapHttpListener implements HttpListener {

    private final WSContainerIndex containerIndex;

    public SoapHttpListener(WSContainerIndex containerIndex) {
        this.containerIndex = containerIndex;
    }

    public void onMessage(HttpRequest req, HttpResponse res) throws IOException {


        String path = req.getURI().getPath();
        WSContainer container = containerIndex.getContainer(path);

        if (container == null) {
            res.setCode(404);
            res.setResponseString("No such web service");
            return;
        }

        res.setContentType("text/xml");

        if (req.getQueryParameter("wsdl") != null){
            doWSDLRequest(container, res);
        } else {
            doInvoke(res, req, container);
        }

    }

    private void doInvoke(HttpResponse res, HttpRequest req, WSContainer container) throws IOException {
        //  We have to set the context classloader or the StAX API
        //  won't be able to find it's implementation.
        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();

        try {
            thread.setContextClassLoader(container.getClass().getClassLoader());
            MessageContext context = new MessageContext("not-used", null, res.getOutputStream(), null, req.getURI().toString());
            context.setRequestStream(req.getInputStream());
            container.invoke(context);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }

    private void doWSDLRequest(WSContainer container, HttpResponse res) throws IOException {
        URL wsdlURL = container.getWsdlURL();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = wsdlURL.openStream();
            out = res.getOutputStream();
            byte[] buffer = new byte[1024];
            for (int read = in.read(buffer); read > 0; read = in.read(buffer) ) {
                System.out.write(buffer, 0, read);
                out.write(buffer, 0 ,read);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

}
