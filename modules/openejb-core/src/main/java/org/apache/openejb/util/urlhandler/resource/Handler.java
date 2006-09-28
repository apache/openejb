/**
* Redistribution and use of this software and associated
* documentation ("Software"), with or without modification, are
* permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain copyright statements
*    and notices.  Redistributions must also contain a copy of this
*    document.
*
* 2. Redistributions in binary form must reproduce the above
*    copyright notice, this list of conditions and the following
*    disclaimer in the documentation and/or other materials provided
*    with the distribution.
*
* 3. The name "Exolab" must not be used to endorse or promote
*    products derived from this Software without prior written
*    permission of Intalio Inc.  For written permission, please
*    contact info@exolab.org.
*
* 4. Products derived from this Software may not be called "Exolab"
*    nor may "Exolab" appear in their names without prior written
*    permission of Intalio Inc. Exolab is a registered trademark of
*    Intalio Inc.
*
* 5. Due credit should be given to the Exolab Project
*    (http://www.exolab.org/).
*
* THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
* ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
* THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
*
* Copyright 2000 (C) Intalio Inc. All Rights Reserved.
*
* $Id$
*
* Date     Author    Changes
* 22/Nov/2000 Chris Wood  Created
* 
*/

package org.apache.openejb.util.urlhandler.resource;

import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @version $Revision$ $Date$ 
 */
public class Handler extends java.net.URLStreamHandler {
    
    protected URLConnection openConnection( URL url ) throws java.io.IOException {
        String cln = url.getHost();

        String resrce = url.getFile().substring( 1 );

        URL realURL;

        if ( cln != null && cln.length() != 0 ) {
            Class clz;
            ClassLoader cl = getContextClassLoader();

            try {
                //clz = Class.forName( cln );
                clz = Class.forName( cln, true, cl );
            } catch ( ClassNotFoundException ex ) {
                throw new java.net.MalformedURLException( "Class " + cln + " cannot be found (" + ex + ")" );
            }

            realURL = cl.getResource( resrce );

            if ( realURL == null )
                throw new java.io.FileNotFoundException( "Class resource " + resrce + " of class " + cln + " cannot be found" );
        } else {
            ClassLoader cl = getContextClassLoader();
            realURL = cl.getResource( resrce );

            if ( realURL == null )
                throw new java.io.FileNotFoundException( "System resource " + resrce + " cannot be found" );
        }

        return realURL.openConnection();
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
        );
    }

}
