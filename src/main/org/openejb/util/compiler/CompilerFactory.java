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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util.compiler;


public class CompilerFactory {
    private static Class factory  = null;
    static {
        String version = null;
             
        try {
            version = System.getProperty("java.vm.version");
        } catch ( Exception e ) {
            throw new RuntimeException("Unable to determine the version of your VM.  No CompilerFactory Can be installed");
        }
        if ( version.startsWith("1.1") ) {
            throw new RuntimeException("This VM version is not supported: "+version);
        } else if ( version.startsWith("1.2") ) {
            ClassLoader cl = org.openejb.util.ClasspathUtils.getContextClassLoader();
            try {
                Class.forName("org.opentools.proxies.Proxy", true, cl);
            } catch ( Exception e ) {
                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.opentools.proxies.Proxy.  This class is needed for generating proxies in JDK 1.2 VMs.");
            }

            try {
                factory = Class.forName("org.openejb.util.compiler.Jdk12Compiler", true, cl);
            } catch ( Exception e ) {
                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.openejb.client.proxy.Jdk12ProxyFactory.");
            }
        } else {
            ClassLoader cl = org.openejb.util.ClasspathUtils.getContextClassLoader();
            try {
                factory = Class.forName("org.openejb.util.compiler.Jdk13Compiler", true, cl);
            } catch ( Exception e ) {
                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.openejb.client.proxy.Jdk13ProxyFactory.");
            }
        } 
    }

    public static Compiler newCompilerInstance() throws InstantiationException, IllegalAccessException {
	return (Compiler)factory.newInstance();
    }
}

