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
package org.openejb.server;

import org.openejb.OpenEJBException;
import org.openejb.client.AuthenticationRequest;
import org.openejb.client.AuthenticationResponse;
import org.openejb.client.CallbackTransport;
import org.openejb.client.ResponseCodes;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;


/**
 * This adapter is used to isulate the CallbackHandler from having to know the
 * internals of the application server.
 * 
 * @author Alan D. Cabrera
 */
public class RemoteCallbackTransport implements CallbackTransport {

    protected ObjectInputStream _in;
    protected ObjectOutputStream _out;

    public RemoteCallbackTransport( ObjectInputStream in, ObjectOutputStream out ) {
	_in = in;
	_out = out;
    }

    public Object getCredential( String key, Properties properties ) throws OpenEJBException {

	Object value =null;
	
	try {
	    AuthenticationResponse res = new AuthenticationResponse();
    
	    res.setResponseCode( ResponseCodes.AUTH_NEEDCREDENTIALPARAMS );
	    res.setCredentialName( key );
	    res.setCredentialProperties( properties );
    
	    res.writeExternal( _out );
    
	    AuthenticationRequest req = new AuthenticationRequest();

	    byte requestCode = _in.readByte();
	    req.readExternal( _in );

	    value = req.getCredentials();

	} catch( java.io.IOException ioe ) {
	    throw new OpenEJBException( ioe.toString() );
	} catch( java.lang.ClassNotFoundException cnfe ) {
	    throw new OpenEJBException( cnfe.toString() );
	}

	return value;
    }

    public Object getCredential( String key ) throws OpenEJBException {

	Object value =null;
	
	try {
	    AuthenticationResponse res = new AuthenticationResponse();
    
	    res.setResponseCode( ResponseCodes.AUTH_NEEDCREDENTIAL );
	    res.setCredentialName( key );
    
	    res.writeExternal( _out );
    
	    AuthenticationRequest req = new AuthenticationRequest();

	    byte requestCode = _in.readByte();
	    req.readExternal( _in );

	    value = req.getCredentials();

	} catch( java.io.IOException ioe ) {
	    throw new OpenEJBException( "Z["+key+"]: "+ioe.toString() );
	} catch( java.lang.ClassNotFoundException cnfe ) {
	    throw new OpenEJBException( cnfe.toString() );
	}

	return value;
    }

}
