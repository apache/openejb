package org.openejb.test.stateful;

/**
 * Interface definition : RmiIiopStatefulObject
 * 
 * @author OpenORB Compiler
 */

public class _RmiIiopStatefulObject_Tie extends org.omg.PortableServer.Servant
		implements javax.rmi.CORBA.Tie
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.stateful.RmiIiopStatefulObject:0000000000000000", 
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
	private org.openejb.test.stateful.RmiIiopStatefulObject target;

	//
	// Private reference to the ORB
	//
	private org.omg.CORBA_2_3.ORB _orb;

	//
	// Set target object
	//
	public void setTarget( java.rmi.Remote targ )
	{
		target = (RmiIiopStatefulObject) targ;
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
	public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream is, org.omg.CORBA.portable.ResponseHandler handler)
	{
		org.omg.CORBA_2_3.portable.InputStream _is = (org.omg.CORBA_2_3.portable.InputStream)is;
		org.omg.CORBA_2_3.portable.OutputStream _output = null;
		try
		{
			if ( opName.equals("returnStringObject") )
			{
				String arg0_in = ( String )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(String.class);

				String _arg_result = target.returnStringObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,String.class);

				return _output;
			}
			else
			if ( opName.equals("returnStringObjectArray") )
			{
				String[] arg0_in = ( String[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(String[].class);

				String[] _arg_result = target.returnStringObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),String[].class);

				return _output;
			}
			else
			if ( opName.equals("returnCharacterObject") )
			{
				java.lang.Character arg0_in = ( java.lang.Character )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Character.class);

				java.lang.Character _arg_result = target.returnCharacterObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Character.class);

				return _output;
			}
			else
			if ( opName.equals("returnCharacterPrimitive") )
			{
				char arg0_in = _is.read_wchar();

				char _arg_result = target.returnCharacterPrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_wchar(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnCharacterObjectArray") )
			{
				java.lang.Character[] arg0_in = ( java.lang.Character[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Character[].class);

				java.lang.Character[] _arg_result = target.returnCharacterObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Character[].class);

				return _output;
			}
			else
			if ( opName.equals("returnCharacterPrimitiveArray") )
			{
				char[] arg0_in = ( char[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(char[].class);

				char[] _arg_result = target.returnCharacterPrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),char[].class);

				return _output;
			}
			else
			if ( opName.equals("returnBooleanObject") )
			{
				java.lang.Boolean arg0_in = ( java.lang.Boolean )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Boolean.class);

				java.lang.Boolean _arg_result = target.returnBooleanObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Boolean.class);

				return _output;
			}
			else
			if ( opName.equals("returnBooleanPrimitive") )
			{
				boolean arg0_in = _is.read_boolean();

				boolean _arg_result = target.returnBooleanPrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_boolean(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnBooleanObjectArray") )
			{
				java.lang.Boolean[] arg0_in = ( java.lang.Boolean[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Boolean[].class);

				java.lang.Boolean[] _arg_result = target.returnBooleanObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Boolean[].class);

				return _output;
			}
			else
			if ( opName.equals("returnBooleanPrimitiveArray") )
			{
				boolean[] arg0_in = ( boolean[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(boolean[].class);

				boolean[] _arg_result = target.returnBooleanPrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),boolean[].class);

				return _output;
			}
			else
			if ( opName.equals("returnByteObject") )
			{
				java.lang.Byte arg0_in = ( java.lang.Byte )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Byte.class);

				java.lang.Byte _arg_result = target.returnByteObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Byte.class);

				return _output;
			}
			else
			if ( opName.equals("returnBytePrimitive") )
			{
				byte arg0_in = _is.read_octet();

				byte _arg_result = target.returnBytePrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_octet(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnByteObjectArray") )
			{
				java.lang.Byte[] arg0_in = ( java.lang.Byte[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Byte[].class);

				java.lang.Byte[] _arg_result = target.returnByteObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Byte[].class);

				return _output;
			}
			else
			if ( opName.equals("returnBytePrimitiveArray") )
			{
				byte[] arg0_in = ( byte[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(byte[].class);

				byte[] _arg_result = target.returnBytePrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),byte[].class);

				return _output;
			}
			else
			if ( opName.equals("returnShortObject") )
			{
				java.lang.Short arg0_in = ( java.lang.Short )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Short.class);

				java.lang.Short _arg_result = target.returnShortObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Short.class);

				return _output;
			}
			else
			if ( opName.equals("returnShortPrimitive") )
			{
				short arg0_in = _is.read_short();

				short _arg_result = target.returnShortPrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_short(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnShortObjectArray") )
			{
				java.lang.Short[] arg0_in = ( java.lang.Short[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Short[].class);

				java.lang.Short[] _arg_result = target.returnShortObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Short[].class);

				return _output;
			}
			else
			if ( opName.equals("returnShortPrimitiveArray") )
			{
				short[] arg0_in = ( short[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(short[].class);

				short[] _arg_result = target.returnShortPrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),short[].class);

				return _output;
			}
			else
			if ( opName.equals("returnIntegerObject") )
			{
				java.lang.Integer arg0_in = ( java.lang.Integer )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Integer.class);

				java.lang.Integer _arg_result = target.returnIntegerObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Integer.class);

				return _output;
			}
			else
			if ( opName.equals("returnIntegerPrimitive") )
			{
				int arg0_in = _is.read_long();

				int _arg_result = target.returnIntegerPrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_long(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnIntegerObjectArray") )
			{
				java.lang.Integer[] arg0_in = ( java.lang.Integer[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Integer[].class);

				java.lang.Integer[] _arg_result = target.returnIntegerObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Integer[].class);

				return _output;
			}
			else
			if ( opName.equals("returnIntegerPrimitiveArray") )
			{
				int[] arg0_in = ( int[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(int[].class);

				int[] _arg_result = target.returnIntegerPrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),int[].class);

				return _output;
			}
			else
			if ( opName.equals("returnLongObject") )
			{
				java.lang.Long arg0_in = ( java.lang.Long )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Long.class);

				java.lang.Long _arg_result = target.returnLongObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Long.class);

				return _output;
			}
			else
			if ( opName.equals("returnLongPrimitive") )
			{
				long arg0_in = _is.read_longlong();

				long _arg_result = target.returnLongPrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_longlong(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnLongObjectArray") )
			{
				java.lang.Long[] arg0_in = ( java.lang.Long[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Long[].class);

				java.lang.Long[] _arg_result = target.returnLongObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Long[].class);

				return _output;
			}
			else
			if ( opName.equals("returnLongPrimitiveArray") )
			{
				long[] arg0_in = ( long[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(long[].class);

				long[] _arg_result = target.returnLongPrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),long[].class);

				return _output;
			}
			else
			if ( opName.equals("returnFloatObject") )
			{
				java.lang.Float arg0_in = ( java.lang.Float )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Float.class);

				java.lang.Float _arg_result = target.returnFloatObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Float.class);

				return _output;
			}
			else
			if ( opName.equals("returnFloatPrimitive") )
			{
				float arg0_in = _is.read_float();

				float _arg_result = target.returnFloatPrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_float(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnFloatObjectArray") )
			{
				java.lang.Float[] arg0_in = ( java.lang.Float[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Float[].class);

				java.lang.Float[] _arg_result = target.returnFloatObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Float[].class);

				return _output;
			}
			else
			if ( opName.equals("returnFloatPrimitiveArray") )
			{
				float[] arg0_in = ( float[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(float[].class);

				float[] _arg_result = target.returnFloatPrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),float[].class);

				return _output;
			}
			else
			if ( opName.equals("returnDoubleObject") )
			{
				java.lang.Double arg0_in = ( java.lang.Double )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Double.class);

				java.lang.Double _arg_result = target.returnDoubleObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,java.lang.Double.class);

				return _output;
			}
			else
			if ( opName.equals("returnDoublePrimitive") )
			{
				double arg0_in = _is.read_double();

				double _arg_result = target.returnDoublePrimitive(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_double(_arg_result);

				return _output;
			}
			else
			if ( opName.equals("returnDoubleObjectArray") )
			{
				java.lang.Double[] arg0_in = ( java.lang.Double[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(java.lang.Double[].class);

				java.lang.Double[] _arg_result = target.returnDoubleObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),java.lang.Double[].class);

				return _output;
			}
			else
			if ( opName.equals("returnDoublePrimitiveArray") )
			{
				double[] arg0_in = ( double[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(double[].class);

				double[] _arg_result = target.returnDoublePrimitiveArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),double[].class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBHome__") )
			{

				javax.ejb.EJBHome _arg_result = target.returnEJBHome();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeRemoteObject( _output, _arg_result );

				return _output;
			}
			else
			if ( opName.equals("returnEJBHome__javax_ejb_EJBHome") )
			{
				javax.ejb.EJBHome arg0_in = ( javax.ejb.EJBHome ) javax.rmi.PortableRemoteObject.narrow(_is.read_Object(), javax.ejb.EJBHome.class);

				javax.ejb.EJBHome _arg_result = target.returnEJBHome(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeRemoteObject( _output, _arg_result );

				return _output;
			}
			else
			if ( opName.equals("returnNestedEJBHome") )
			{

				org.openejb.test.object.ObjectGraph _arg_result = target.returnNestedEJBHome();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,org.openejb.test.object.ObjectGraph.class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBHomeArray") )
			{
				javax.ejb.EJBHome[] arg0_in = ( javax.ejb.EJBHome[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(javax.ejb.EJBHome[].class);

				javax.ejb.EJBHome[] _arg_result = target.returnEJBHomeArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),javax.ejb.EJBHome[].class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBObject__javax_ejb_EJBObject") )
			{
				javax.ejb.EJBObject arg0_in = ( javax.ejb.EJBObject ) javax.rmi.PortableRemoteObject.narrow(_is.read_Object(), javax.ejb.EJBObject.class);

				javax.ejb.EJBObject _arg_result = target.returnEJBObject(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeRemoteObject( _output, _arg_result );

				return _output;
			}
			else
			if ( opName.equals("returnEJBObject__") )
			{

				javax.ejb.EJBObject _arg_result = target.returnEJBObject();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeRemoteObject( _output, _arg_result );

				return _output;
			}
			else
			if ( opName.equals("returnNestedEJBObject") )
			{

				org.openejb.test.object.ObjectGraph _arg_result = target.returnNestedEJBObject();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,org.openejb.test.object.ObjectGraph.class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBObjectArray") )
			{
				javax.ejb.EJBObject[] arg0_in = ( javax.ejb.EJBObject[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(javax.ejb.EJBObject[].class);

				javax.ejb.EJBObject[] _arg_result = target.returnEJBObjectArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),javax.ejb.EJBObject[].class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBMetaData__") )
			{

				javax.ejb.EJBMetaData _arg_result = target.returnEJBMetaData();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,javax.ejb.EJBMetaData.class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBMetaData__javax_ejb_EJBMetaData") )
			{
				javax.ejb.EJBMetaData arg0_in = ( javax.ejb.EJBMetaData )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(javax.ejb.EJBMetaData.class);

				javax.ejb.EJBMetaData _arg_result = target.returnEJBMetaData(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,javax.ejb.EJBMetaData.class);

				return _output;
			}
			else
			if ( opName.equals("returnNestedEJBMetaData") )
			{

				org.openejb.test.object.ObjectGraph _arg_result = target.returnNestedEJBMetaData();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,org.openejb.test.object.ObjectGraph.class);

				return _output;
			}
			else
			if ( opName.equals("returnEJBMetaDataArray") )
			{
				javax.ejb.EJBMetaData[] arg0_in = ( javax.ejb.EJBMetaData[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(javax.ejb.EJBMetaData[].class);

				javax.ejb.EJBMetaData[] _arg_result = target.returnEJBMetaDataArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),javax.ejb.EJBMetaData[].class);

				return _output;
			}
			else
			if ( opName.equals("returnHandle__javax_ejb_Handle") )
			{
				javax.ejb.Handle arg0_in = ( javax.ejb.Handle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_is).read_abstract_interface(), javax.ejb.Handle.class);

				javax.ejb.Handle _arg_result = target.returnHandle(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeAbstractObject( _output, _arg_result );

				return _output;
			}
			else
			if ( opName.equals("returnHandle__") )
			{

				javax.ejb.Handle _arg_result = target.returnHandle();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeAbstractObject( _output, _arg_result );

				return _output;
			}
			else
			if ( opName.equals("returnNestedHandle") )
			{

				org.openejb.test.object.ObjectGraph _arg_result = target.returnNestedHandle();

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,org.openejb.test.object.ObjectGraph.class);

				return _output;
			}
			else
			if ( opName.equals("returnHandleArray") )
			{
				javax.ejb.Handle[] arg0_in = ( javax.ejb.Handle[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(javax.ejb.Handle[].class);

				javax.ejb.Handle[] _arg_result = target.returnHandleArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),javax.ejb.Handle[].class);

				return _output;
			}
			else
			if ( opName.equals("returnObjectGraph") )
			{
				org.openejb.test.object.ObjectGraph arg0_in = ( org.openejb.test.object.ObjectGraph )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(org.openejb.test.object.ObjectGraph.class);

				org.openejb.test.object.ObjectGraph _arg_result = target.returnObjectGraph(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)_arg_result,org.openejb.test.object.ObjectGraph.class);

				return _output;
			}
			else
			if ( opName.equals("returnObjectGraphArray") )
			{
				org.openejb.test.object.ObjectGraph[] arg0_in = ( org.openejb.test.object.ObjectGraph[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(org.openejb.test.object.ObjectGraph[].class);

				org.openejb.test.object.ObjectGraph[] _arg_result = target.returnObjectGraphArray(arg0_in);

				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)((java.lang.Object)_arg_result),org.openejb.test.object.ObjectGraph[].class);

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
					String exid = "IDL:javax/ejb/RemoveEx:1.0";
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
