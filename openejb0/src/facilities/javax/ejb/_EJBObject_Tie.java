package javax.ejb;

//
// Interface definition : EJBObject
//
// @author OpenORB Compiler
//
public class _EJBObject_Tie extends org.omg.PortableServer.Servant
		implements javax.rmi.CORBA.Tie
{

	static final String[] _ids_list =
	{
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	public String[] _all_interfaces( org.omg.PortableServer.POA poa, byte [] oid )
	{
		return _ids_list;
	}

	//
	// Private reference to implementation object
	//
	private javax.ejb.EJBObject target;

	//
	// Private reference to the ORB
	//
	private org.omg.CORBA_2_3.ORB _orb;

	//
	// Set target object
	//
	public void setTarget( java.rmi.Remote targ )
	{
		target = (EJBObject) targ;
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
			_poa().deactivate_object(_object_id());
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
				System.out.println("Get primary key...");
				java.lang.Object arg = target.getPrimaryKey();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeAny(_output,arg);
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
			if ( opName.equals("remove") )
			{

				try
				{
					target.remove();

					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();

				}
				catch ( javax.ejb.RemoveException _exception )
				{
					String exid = "RMI:javax.ejb.RemoveException:000000004A49EB2E:0000000000000000";
					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createExceptionReply();
					_output.write_string(exid);
					_output.write_value(_exception);
				}
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
