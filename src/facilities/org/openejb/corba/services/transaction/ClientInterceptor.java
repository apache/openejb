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
 */

package org.openejb.corba.services.transaction;

/**
 * The Client side interceptor is used to transfer the propagation context in transaction requests
 * @author <a href="mailto:mdaniel@intalio.com">Marina Daniel &lt;mdaniel@intalio.com&gt;</a>
 */
public class ClientInterceptor	
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ClientRequestInterceptor
{
	private org.omg.PortableInterceptor.ORBInitInfo info;
	private int t_slot;
	
	/**
	 * Constructor which initializes the ORBInitInfo and the Sender reference
	 */
	public ClientInterceptor( org.omg.PortableInterceptor.ORBInitInfo info, int t_slot)
	{
		this.info = info;
		this.t_slot = t_slot;
	}
	
	/**
	 * Adds a transaction propagation context to the service context list, to be passed to the server
	 * @param ri the client request
	 */
	public void send_request(org.omg.PortableInterceptor.ClientRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{		
		org.omg.CORBA.Any any = null;
		try
		{
			any = ri.get_slot(t_slot);
		}
		catch (org.omg.PortableInterceptor.InvalidSlot is) 
		{
			fatal("ClientInterceptor", "invalid slot : " + t_slot);
		}
		
		if ( ( any.type().kind().value() == org.omg.CORBA.TCKind._tk_null ) ||
		     ( any.type().kind().value() == org.omg.CORBA.TCKind._tk_void ) )
			return;
		
	 	String tid = any.extract_string();
	
		org.omg.IOP.ServiceContext serviceCtx = new org.omg.IOP.ServiceContext();
		serviceCtx.context_id = 1090; 
		
		try
		{
			org.omg.IOP.CodecFactory codecFactory = info.codec_factory();
			
			org.omg.IOP.Encoding encoding = new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, new Integer(1).byteValue(), new Integer(2).byteValue());
			org.omg.IOP.Codec codec = codecFactory.create_codec(encoding);
			org.omg.CORBA.Any pany = org.omg.CORBA.ORB.init().create_any();
			pany.insert_string(tid);
			serviceCtx.context_data = codec.encode_value(pany);
		}
		catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding ue) 
		{
			fatal("ClientInterceptor", "Unknown Encoding");
		}
		catch (org.omg.IOP.CodecPackage.InvalidTypeForEncoding it) 
		{
			fatal("ClientInterceptor", "Invalid Type for encoding");
		}
		
		ri.add_request_service_context(serviceCtx, true);
	}

	/**
	 * used to query information during a Time Independent Invocation polling get reply sequence ??
	 */
	public void send_poll(org.omg.PortableInterceptor.ClientRequestInfo ri)
	{}

	/**
	 * Called when the client receive a reply from the server
	 * get the transaction propagation context of the service context list, pass it to the sender
	 *	which will set the propagation context to the current object
	 */
	public void receive_reply(org.omg.PortableInterceptor.ClientRequestInfo ri)
	{}

	/**
	 *  Called when the client receive an exception from the server
	 */
	public void receive_exception(org.omg.PortableInterceptor.ClientRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{}

	/**
	 *  Called when the client receive a message from the server 
	 *		which is not a reply and not an exception
	 */
	public void receive_other(org.omg.PortableInterceptor.ClientRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{}
	
	/**
	 * return the name of the Transactional Client Interceptor
	 */
	public java.lang.String name()
	{
		return "OpenEJB-Transactional-Client-Interceptor";
	}

        public void destroy() {}

        /**
         * Displays a trace and throw a INTERNAL exception...
         */
        public void fatal( String from, String msg )
        {
	    org.openejb.corba.util.Verbose.print( from, msg );
            throw new org.omg.CORBA.INTERNAL(msg);
        }
}
