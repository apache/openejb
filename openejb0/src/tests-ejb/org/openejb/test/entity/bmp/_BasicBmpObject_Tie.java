package org.openejb.test.entity.bmp;

//
// Interface definition : BasicBmpObject
//
// @author OpenORB Compiler
//
public class _BasicBmpObject_Tie extends org.omg.PortableServer.Servant
		implements javax.rmi.CORBA.Tie
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.entity.bmp.BasicBmpObject:0000000000000000", 
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	private org.omg.PortableServer.POA _poa;
	private byte [] _oid;

	public String[] _all_interfaces( org.omg.PortableServer.POA poa, byte [] oid )
	{
		_poa = poa;
		_oid = oid;
		return _ids_list;
	}

	//
	// Private reference to implementation object
	//
	private org.openejb.test.entity.bmp.BasicBmpObject target;

	//
	// Private reference to the ORB
	//
	private org.omg.CORBA_2_3.ORB _orb;

	//
	// Set target object
	//
	public void setTarget( java.rmi.Remote targ )
	{
		target = (BasicBmpObject) targ;
	}

	//
	// Get target object
	//
	public java.rmi.Remote getTarget()
	{
		return target;
	}

	//
	// Returns an object reference for the target object
	//
	public org.omg.CORBA.Object thisObject()
	{
		return _this_object();
	}

	//
	// Deactivate the target object
	//
	public void deactivate()
	{
		try
		{
			_poa.deactivate_object( _oid );
		}
		catch ( org.omg.PortableServer.POAPackage.ObjectNotActive ex )
		{
		}
		catch ( org.omg.PortableServer.POAPackage.WrongPolicy ex )
		{
		}
		target = null;
	}

	//
	// Return the ORB
	//
	public org.omg.CORBA.ORB orb()
	{
		return _orb;
	}

	//
	// Set the ORB
	//
	public void orb( org.omg.CORBA.ORB orb )
	{
		_orb = ( org.omg.CORBA_2_3.ORB ) orb;
		_orb.set_delegate( this );
	}

	//
	// Invoke method ( for remote call )
	//
	public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream _is, org.omg.CORBA.portable.ResponseHandler handler)
	{
		org.omg.CORBA_2_3.portable.OutputStream _output = null;
		try
		{
			if ( opName.equals("_get_permissionsReport") )
			{
				java.util.Properties arg = target.getPermissionsReport();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)arg,java.util.Properties.class);
				return _output;
			}
			else
			if ( opName.equals("businessMethod") )
			{
				java.lang.String arg0_in = ( java.lang.String )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.String.class);

				java.lang.String _arg_result = target.businessMethod(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.String.class);

				return _output;
			}
			else
			if ( opName.equals("getAllowedOperationsReport") )
			{
				java.lang.String arg0_in = ( java.lang.String )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.String.class);

				org.openejb.test.object.OperationsPolicy _arg_result = target.getAllowedOperationsReport(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,org.openejb.test.object.OperationsPolicy.class);

				return _output;
			}
			else
			if ( opName.equals("_get_EJBHome") )
			{
				javax.ejb.EJBHome arg = target.getEJBHome();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeRemoteObject( _output, arg );
				return _output;
			}
			else
			if ( opName.equals("_get_handle") )
			{
				javax.ejb.Handle arg = target.getHandle();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeAbstractObject( _output, arg );
				return _output;
			}
			else
			if ( opName.equals("_get_primaryKey") )
			{
				java.lang.Object arg = target.getPrimaryKey();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeAny(_output,arg);
				return _output;
			}
			else
			if ( opName.equals("remove") )
			{

				try
				{
					target.remove();

					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();

				}
				catch ( javax.ejb.RemoveException _exception )
				{
					String exid = "RMI:javax.ejb.RemoveException:000000000064C34E:0000000000000000";
					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createExceptionReply();
					_output.write_string(exid);
					_output.write_value(_exception);
				}
				return _output;
			}
			else
			if ( opName.equals("isIdentical") )
			{
				javax.ejb.EJBObject arg0_in = ( javax.ejb.EJBObject ) javax.rmi.PortableRemoteObject.narrow(_is.read_Object(), javax.ejb.EJBObject.class);

				boolean _arg_result = target.isIdentical(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_boolean(_arg_result);

				return _output;
			}
			else
				throw new org.omg.CORBA.BAD_OPERATION();
		}
		catch ( org.omg.CORBA.SystemException ex )
		{
			throw ex;
		}
		catch ( Throwable ex )
		{
			throw new org.omg.CORBA.portable.UnknownException(ex);
		}
	}
}
