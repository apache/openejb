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
 */

package org.openejb.corba.services.transaction;

/**
 * The Server side interceptor is used to retrieve the service context for the transaction service
 *   The service context is added to the corresponding current object
 * @author <a href="mailto:mdaniel@intalio.com">Marina Daniel &lt;mdaniel@intalio.com&gt;</a>
 */
public class ServerInterceptor 
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ServerRequestInterceptor
{
 	private org.omg.PortableInterceptor.ORBInitInfo info;
	private int t_slot;
	
	/**
	 * Constructor which initializes the ORBInitInfo and the slot reference
	 */
	public ServerInterceptor( org.omg.PortableInterceptor.ORBInitInfo info, int t_slot)
	{
		this.info = info;
		this.t_slot = t_slot;
	}
	
	/**
	 * get the transaction propagation context from the service context list and save it to the PICurrent
	 * @param ri the client request
	 */
	public void receive_request_service_contexts(org.omg.PortableInterceptor.ServerRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{
                //see if there is a propagation context
		org.omg.IOP.ServiceContext serviceCtx = null;
		try
		{
			serviceCtx = ri.get_request_service_context(1090);
		}
		catch (org.omg.CORBA.BAD_PARAM bp){return;}
		
		// save the propagation context in the PICurrent for TS Use
		try
		{
			org.omg.IOP.CodecFactory codecFactory = info.codec_factory();
			org.omg.IOP.Encoding encoding = new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, new Integer(1).byteValue(), new Integer(2).byteValue());
			org.omg.IOP.Codec codec = codecFactory.create_codec(encoding);
			
			org.omg.CORBA.Any any = codec.decode_value(serviceCtx.context_data, org.omg.CORBA.ORB.init().get_primitive_tc( org.omg.CORBA.TCKind.tk_string ) );
			
			ri.set_slot(t_slot, any);						
		}
		catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding ue) 
		{
			fatal("ServerInterceptor", "Unknown encoding");
		}
		catch (org.omg.PortableInterceptor.InvalidSlot is) 
		{
			fatal("ServerInterceptor", "Invalid Slot : " + t_slot);
		}
		catch (org.omg.IOP.CodecPackage.FormatMismatch fm) 
		{
			fatal("ServerInterceptor", "Format Mismatch");
		}
		catch (org.omg.IOP.CodecPackage.TypeMismatch fm) 
		{
			fatal("ServerInterceptor", "Type Mismatch");
		}
		
		
	}

	/**
	 * receive request operation
	 */
	public void receive_request(org.omg.PortableInterceptor.ServerRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{
        }

	/**
	 * send reply operation
	 */
	public void send_reply(org.omg.PortableInterceptor.ServerRequestInfo ri)
	{}

	/**
	 * send exception operation
	 */
	public void send_exception(org.omg.PortableInterceptor.ServerRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{}

	/**
	 * send other operation
	 */
	public void send_other(org.omg.PortableInterceptor.ServerRequestInfo ri)
		throws org.omg.PortableInterceptor.ForwardRequest
	{}
	
	/**
	 * return the name of the Transactional Server Interceptor
	 */
	public java.lang.String name()
	{
		return "OpenEJB-Transactional-Server-Interceptor";
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

