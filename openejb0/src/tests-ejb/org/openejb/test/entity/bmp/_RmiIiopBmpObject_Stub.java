package org.openejb.test.entity.bmp;

/**
 * Interface definition : RmiIiopBmpObject
 * 
 * @author OpenORB Compiler
 */

public class _RmiIiopBmpObject_Stub extends javax.rmi.CORBA.Stub
		implements RmiIiopBmpObject
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.entity.bmp.RmiIiopBmpObject:0000000000000000", 
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = RmiIiopBmpObject.class;

	//
	// Operation returnStringObject
	//
	public String returnStringObject(String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnStringObject",true);
					_output.write_value((java.io.Serializable)arg0,String.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					String _arg_ret = ( String )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(String.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnStringObject",_opsClass);
				if ( _so == null )
				   return returnStringObject( arg0);
				try
				{
					String arg0Copy = (String)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					String _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnStringObject( arg0Copy);
					return (String)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnStringObjectArray
	//
	public String[] returnStringObjectArray(String[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnStringObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),String[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					String[] _arg_ret = ( String[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(String[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnStringObjectArray",_opsClass);
				if ( _so == null )
				   return returnStringObjectArray( arg0);
				try
				{
					String[] arg0Copy = (String[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					String[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnStringObjectArray( arg0Copy);
					return (String[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnCharacterObject
	//
	public java.lang.Character returnCharacterObject(java.lang.Character arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnCharacterObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Character.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Character _arg_ret = ( java.lang.Character )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Character.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnCharacterObject",_opsClass);
				if ( _so == null )
				   return returnCharacterObject( arg0);
				try
				{
					java.lang.Character arg0Copy = (java.lang.Character)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Character _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnCharacterObject( arg0Copy);
					return (java.lang.Character)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnCharacterPrimitive
	//
	public char returnCharacterPrimitive(char arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnCharacterPrimitive",true);
					_output.write_wchar(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					char _arg_ret = _input.read_wchar();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnCharacterPrimitive",_opsClass);
				if ( _so == null )
				   return returnCharacterPrimitive( arg0);
				try
				{
					char arg0Copy = arg0;
					char _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnCharacterPrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnCharacterObjectArray
	//
	public java.lang.Character[] returnCharacterObjectArray(java.lang.Character[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnCharacterObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Character[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Character[] _arg_ret = ( java.lang.Character[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Character[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnCharacterObjectArray",_opsClass);
				if ( _so == null )
				   return returnCharacterObjectArray( arg0);
				try
				{
					java.lang.Character[] arg0Copy = (java.lang.Character[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Character[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnCharacterObjectArray( arg0Copy);
					return (java.lang.Character[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnCharacterPrimitiveArray
	//
	public char[] returnCharacterPrimitiveArray(char[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnCharacterPrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),char[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					char[] _arg_ret = ( char[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(char[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnCharacterPrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnCharacterPrimitiveArray( arg0);
				try
				{
					char[] arg0Copy = (char[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					char[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnCharacterPrimitiveArray( arg0Copy);
					return (char[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnBooleanObject
	//
	public java.lang.Boolean returnBooleanObject(java.lang.Boolean arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnBooleanObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Boolean.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Boolean _arg_ret = ( java.lang.Boolean )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Boolean.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnBooleanObject",_opsClass);
				if ( _so == null )
				   return returnBooleanObject( arg0);
				try
				{
					java.lang.Boolean arg0Copy = (java.lang.Boolean)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Boolean _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnBooleanObject( arg0Copy);
					return (java.lang.Boolean)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnBooleanPrimitive
	//
	public boolean returnBooleanPrimitive(boolean arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnBooleanPrimitive",true);
					_output.write_boolean(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					boolean _arg_ret = _input.read_boolean();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnBooleanPrimitive",_opsClass);
				if ( _so == null )
				   return returnBooleanPrimitive( arg0);
				try
				{
					boolean arg0Copy = arg0;
					boolean _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnBooleanPrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnBooleanObjectArray
	//
	public java.lang.Boolean[] returnBooleanObjectArray(java.lang.Boolean[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnBooleanObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Boolean[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Boolean[] _arg_ret = ( java.lang.Boolean[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Boolean[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnBooleanObjectArray",_opsClass);
				if ( _so == null )
				   return returnBooleanObjectArray( arg0);
				try
				{
					java.lang.Boolean[] arg0Copy = (java.lang.Boolean[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Boolean[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnBooleanObjectArray( arg0Copy);
					return (java.lang.Boolean[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnBooleanPrimitiveArray
	//
	public boolean[] returnBooleanPrimitiveArray(boolean[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnBooleanPrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),boolean[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					boolean[] _arg_ret = ( boolean[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(boolean[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnBooleanPrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnBooleanPrimitiveArray( arg0);
				try
				{
					boolean[] arg0Copy = (boolean[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					boolean[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnBooleanPrimitiveArray( arg0Copy);
					return (boolean[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnByteObject
	//
	public java.lang.Byte returnByteObject(java.lang.Byte arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnByteObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Byte.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Byte _arg_ret = ( java.lang.Byte )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Byte.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnByteObject",_opsClass);
				if ( _so == null )
				   return returnByteObject( arg0);
				try
				{
					java.lang.Byte arg0Copy = (java.lang.Byte)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Byte _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnByteObject( arg0Copy);
					return (java.lang.Byte)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnBytePrimitive
	//
	public byte returnBytePrimitive(byte arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnBytePrimitive",true);
					_output.write_octet(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					byte _arg_ret = _input.read_octet();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnBytePrimitive",_opsClass);
				if ( _so == null )
				   return returnBytePrimitive( arg0);
				try
				{
					byte arg0Copy = arg0;
					byte _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnBytePrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnByteObjectArray
	//
	public java.lang.Byte[] returnByteObjectArray(java.lang.Byte[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnByteObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Byte[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Byte[] _arg_ret = ( java.lang.Byte[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Byte[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnByteObjectArray",_opsClass);
				if ( _so == null )
				   return returnByteObjectArray( arg0);
				try
				{
					java.lang.Byte[] arg0Copy = (java.lang.Byte[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Byte[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnByteObjectArray( arg0Copy);
					return (java.lang.Byte[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnBytePrimitiveArray
	//
	public byte[] returnBytePrimitiveArray(byte[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnBytePrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),byte[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					byte[] _arg_ret = ( byte[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(byte[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnBytePrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnBytePrimitiveArray( arg0);
				try
				{
					byte[] arg0Copy = (byte[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					byte[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnBytePrimitiveArray( arg0Copy);
					return (byte[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnShortObject
	//
	public java.lang.Short returnShortObject(java.lang.Short arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnShortObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Short.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Short _arg_ret = ( java.lang.Short )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Short.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnShortObject",_opsClass);
				if ( _so == null )
				   return returnShortObject( arg0);
				try
				{
					java.lang.Short arg0Copy = (java.lang.Short)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Short _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnShortObject( arg0Copy);
					return (java.lang.Short)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnShortPrimitive
	//
	public short returnShortPrimitive(short arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnShortPrimitive",true);
					_output.write_short(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					short _arg_ret = _input.read_short();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnShortPrimitive",_opsClass);
				if ( _so == null )
				   return returnShortPrimitive( arg0);
				try
				{
					short arg0Copy = arg0;
					short _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnShortPrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnShortObjectArray
	//
	public java.lang.Short[] returnShortObjectArray(java.lang.Short[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnShortObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Short[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Short[] _arg_ret = ( java.lang.Short[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Short[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnShortObjectArray",_opsClass);
				if ( _so == null )
				   return returnShortObjectArray( arg0);
				try
				{
					java.lang.Short[] arg0Copy = (java.lang.Short[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Short[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnShortObjectArray( arg0Copy);
					return (java.lang.Short[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnShortPrimitiveArray
	//
	public short[] returnShortPrimitiveArray(short[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnShortPrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),short[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					short[] _arg_ret = ( short[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(short[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnShortPrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnShortPrimitiveArray( arg0);
				try
				{
					short[] arg0Copy = (short[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					short[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnShortPrimitiveArray( arg0Copy);
					return (short[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnIntegerObject
	//
	public java.lang.Integer returnIntegerObject(java.lang.Integer arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnIntegerObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Integer.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Integer _arg_ret = ( java.lang.Integer )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Integer.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnIntegerObject",_opsClass);
				if ( _so == null )
				   return returnIntegerObject( arg0);
				try
				{
					java.lang.Integer arg0Copy = (java.lang.Integer)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Integer _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnIntegerObject( arg0Copy);
					return (java.lang.Integer)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnIntegerPrimitive
	//
	public int returnIntegerPrimitive(int arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnIntegerPrimitive",true);
					_output.write_long(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					int _arg_ret = _input.read_long();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnIntegerPrimitive",_opsClass);
				if ( _so == null )
				   return returnIntegerPrimitive( arg0);
				try
				{
					int arg0Copy = arg0;
					int _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnIntegerPrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnIntegerObjectArray
	//
	public java.lang.Integer[] returnIntegerObjectArray(java.lang.Integer[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnIntegerObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Integer[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Integer[] _arg_ret = ( java.lang.Integer[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Integer[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnIntegerObjectArray",_opsClass);
				if ( _so == null )
				   return returnIntegerObjectArray( arg0);
				try
				{
					java.lang.Integer[] arg0Copy = (java.lang.Integer[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Integer[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnIntegerObjectArray( arg0Copy);
					return (java.lang.Integer[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnIntegerPrimitiveArray
	//
	public int[] returnIntegerPrimitiveArray(int[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnIntegerPrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),int[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					int[] _arg_ret = ( int[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(int[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnIntegerPrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnIntegerPrimitiveArray( arg0);
				try
				{
					int[] arg0Copy = (int[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					int[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnIntegerPrimitiveArray( arg0Copy);
					return (int[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnLongObject
	//
	public java.lang.Long returnLongObject(java.lang.Long arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnLongObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Long.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Long _arg_ret = ( java.lang.Long )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Long.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnLongObject",_opsClass);
				if ( _so == null )
				   return returnLongObject( arg0);
				try
				{
					java.lang.Long arg0Copy = (java.lang.Long)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Long _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnLongObject( arg0Copy);
					return (java.lang.Long)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnLongPrimitive
	//
	public long returnLongPrimitive(long arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnLongPrimitive",true);
					_output.write_longlong(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					long _arg_ret = _input.read_longlong();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnLongPrimitive",_opsClass);
				if ( _so == null )
				   return returnLongPrimitive( arg0);
				try
				{
					long arg0Copy = arg0;
					long _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnLongPrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnLongObjectArray
	//
	public java.lang.Long[] returnLongObjectArray(java.lang.Long[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnLongObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Long[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Long[] _arg_ret = ( java.lang.Long[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Long[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnLongObjectArray",_opsClass);
				if ( _so == null )
				   return returnLongObjectArray( arg0);
				try
				{
					java.lang.Long[] arg0Copy = (java.lang.Long[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Long[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnLongObjectArray( arg0Copy);
					return (java.lang.Long[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnLongPrimitiveArray
	//
	public long[] returnLongPrimitiveArray(long[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnLongPrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),long[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					long[] _arg_ret = ( long[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(long[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnLongPrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnLongPrimitiveArray( arg0);
				try
				{
					long[] arg0Copy = (long[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					long[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnLongPrimitiveArray( arg0Copy);
					return (long[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnFloatObject
	//
	public java.lang.Float returnFloatObject(java.lang.Float arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnFloatObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Float.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Float _arg_ret = ( java.lang.Float )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Float.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnFloatObject",_opsClass);
				if ( _so == null )
				   return returnFloatObject( arg0);
				try
				{
					java.lang.Float arg0Copy = (java.lang.Float)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Float _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnFloatObject( arg0Copy);
					return (java.lang.Float)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnFloatPrimitive
	//
	public float returnFloatPrimitive(float arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnFloatPrimitive",true);
					_output.write_float(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					float _arg_ret = _input.read_float();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnFloatPrimitive",_opsClass);
				if ( _so == null )
				   return returnFloatPrimitive( arg0);
				try
				{
					float arg0Copy = arg0;
					float _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnFloatPrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnFloatObjectArray
	//
	public java.lang.Float[] returnFloatObjectArray(java.lang.Float[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnFloatObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Float[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Float[] _arg_ret = ( java.lang.Float[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Float[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnFloatObjectArray",_opsClass);
				if ( _so == null )
				   return returnFloatObjectArray( arg0);
				try
				{
					java.lang.Float[] arg0Copy = (java.lang.Float[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Float[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnFloatObjectArray( arg0Copy);
					return (java.lang.Float[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnFloatPrimitiveArray
	//
	public float[] returnFloatPrimitiveArray(float[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnFloatPrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),float[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					float[] _arg_ret = ( float[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(float[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnFloatPrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnFloatPrimitiveArray( arg0);
				try
				{
					float[] arg0Copy = (float[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					float[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnFloatPrimitiveArray( arg0Copy);
					return (float[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnDoubleObject
	//
	public java.lang.Double returnDoubleObject(java.lang.Double arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnDoubleObject",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Double.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Double _arg_ret = ( java.lang.Double )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Double.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnDoubleObject",_opsClass);
				if ( _so == null )
				   return returnDoubleObject( arg0);
				try
				{
					java.lang.Double arg0Copy = (java.lang.Double)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Double _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnDoubleObject( arg0Copy);
					return (java.lang.Double)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnDoublePrimitive
	//
	public double returnDoublePrimitive(double arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnDoublePrimitive",true);
					_output.write_double(arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					double _arg_ret = _input.read_double();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnDoublePrimitive",_opsClass);
				if ( _so == null )
				   return returnDoublePrimitive( arg0);
				try
				{
					double arg0Copy = arg0;
					double _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnDoublePrimitive( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnDoubleObjectArray
	//
	public java.lang.Double[] returnDoubleObjectArray(java.lang.Double[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnDoubleObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),java.lang.Double[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Double[] _arg_ret = ( java.lang.Double[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.lang.Double[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnDoubleObjectArray",_opsClass);
				if ( _so == null )
				   return returnDoubleObjectArray( arg0);
				try
				{
					java.lang.Double[] arg0Copy = (java.lang.Double[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Double[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnDoubleObjectArray( arg0Copy);
					return (java.lang.Double[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnDoublePrimitiveArray
	//
	public double[] returnDoublePrimitiveArray(double[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnDoublePrimitiveArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),double[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					double[] _arg_ret = ( double[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(double[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnDoublePrimitiveArray",_opsClass);
				if ( _so == null )
				   return returnDoublePrimitiveArray( arg0);
				try
				{
					double[] arg0Copy = (double[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					double[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnDoublePrimitiveArray( arg0Copy);
					return (double[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBHome__
	//
	public javax.ejb.EJBHome returnEJBHome()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBHome__",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBHome _arg_ret = ( javax.ejb.EJBHome ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), javax.ejb.EJBHome.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBHome__",_opsClass);
				if ( _so == null )
				   return returnEJBHome();
				try
				{
					javax.ejb.EJBHome _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBHome();
					return (javax.ejb.EJBHome)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBHome__javax_ejb_EJBHome
	//
	public javax.ejb.EJBHome returnEJBHome(javax.ejb.EJBHome arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBHome__javax_ejb_EJBHome",true);
					javax.rmi.CORBA.Util.writeRemoteObject( _output, arg0 );
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBHome _arg_ret = ( javax.ejb.EJBHome ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), javax.ejb.EJBHome.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBHome__javax_ejb_EJBHome",_opsClass);
				if ( _so == null )
				   return returnEJBHome( arg0);
				try
				{
					javax.ejb.EJBHome arg0Copy = (javax.ejb.EJBHome)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.EJBHome _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBHome( arg0Copy);
					return (javax.ejb.EJBHome)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnNestedEJBHome
	//
	public org.openejb.test.object.ObjectGraph returnNestedEJBHome()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnNestedEJBHome",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.ObjectGraph _arg_ret = ( org.openejb.test.object.ObjectGraph )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.ObjectGraph.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnNestedEJBHome",_opsClass);
				if ( _so == null )
				   return returnNestedEJBHome();
				try
				{
					org.openejb.test.object.ObjectGraph _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnNestedEJBHome();
					return (org.openejb.test.object.ObjectGraph)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBHomeArray
	//
	public javax.ejb.EJBHome[] returnEJBHomeArray(javax.ejb.EJBHome[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBHomeArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),javax.ejb.EJBHome[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBHome[] _arg_ret = ( javax.ejb.EJBHome[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.EJBHome[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBHomeArray",_opsClass);
				if ( _so == null )
				   return returnEJBHomeArray( arg0);
				try
				{
					javax.ejb.EJBHome[] arg0Copy = (javax.ejb.EJBHome[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.EJBHome[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBHomeArray( arg0Copy);
					return (javax.ejb.EJBHome[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBObject__javax_ejb_EJBObject
	//
	public javax.ejb.EJBObject returnEJBObject(javax.ejb.EJBObject arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBObject__javax_ejb_EJBObject",true);
					javax.rmi.CORBA.Util.writeRemoteObject( _output, arg0 );
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBObject _arg_ret = ( javax.ejb.EJBObject ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), javax.ejb.EJBObject.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBObject__javax_ejb_EJBObject",_opsClass);
				if ( _so == null )
				   return returnEJBObject( arg0);
				try
				{
					javax.ejb.EJBObject arg0Copy = (javax.ejb.EJBObject)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.EJBObject _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBObject( arg0Copy);
					return (javax.ejb.EJBObject)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBObject__
	//
	public javax.ejb.EJBObject returnEJBObject()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBObject__",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBObject _arg_ret = ( javax.ejb.EJBObject ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), javax.ejb.EJBObject.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBObject__",_opsClass);
				if ( _so == null )
				   return returnEJBObject();
				try
				{
					javax.ejb.EJBObject _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBObject();
					return (javax.ejb.EJBObject)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnNestedEJBObject
	//
	public org.openejb.test.object.ObjectGraph returnNestedEJBObject()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnNestedEJBObject",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.ObjectGraph _arg_ret = ( org.openejb.test.object.ObjectGraph )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.ObjectGraph.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnNestedEJBObject",_opsClass);
				if ( _so == null )
				   return returnNestedEJBObject();
				try
				{
					org.openejb.test.object.ObjectGraph _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnNestedEJBObject();
					return (org.openejb.test.object.ObjectGraph)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBObjectArray
	//
	public javax.ejb.EJBObject[] returnEJBObjectArray(javax.ejb.EJBObject[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBObjectArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),javax.ejb.EJBObject[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBObject[] _arg_ret = ( javax.ejb.EJBObject[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.EJBObject[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBObjectArray",_opsClass);
				if ( _so == null )
				   return returnEJBObjectArray( arg0);
				try
				{
					javax.ejb.EJBObject[] arg0Copy = (javax.ejb.EJBObject[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.EJBObject[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBObjectArray( arg0Copy);
					return (javax.ejb.EJBObject[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBMetaData__
	//
	public javax.ejb.EJBMetaData returnEJBMetaData()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBMetaData__",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBMetaData _arg_ret = ( javax.ejb.EJBMetaData )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.EJBMetaData.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBMetaData__",_opsClass);
				if ( _so == null )
				   return returnEJBMetaData();
				try
				{
					javax.ejb.EJBMetaData _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBMetaData();
					return (javax.ejb.EJBMetaData)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBMetaData__javax_ejb_EJBMetaData
	//
	public javax.ejb.EJBMetaData returnEJBMetaData(javax.ejb.EJBMetaData arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBMetaData__javax_ejb_EJBMetaData",true);
					_output.write_value((java.io.Serializable)arg0,javax.ejb.EJBMetaData.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBMetaData _arg_ret = ( javax.ejb.EJBMetaData )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.EJBMetaData.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBMetaData__javax_ejb_EJBMetaData",_opsClass);
				if ( _so == null )
				   return returnEJBMetaData( arg0);
				try
				{
					javax.ejb.EJBMetaData arg0Copy = (javax.ejb.EJBMetaData)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.EJBMetaData _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBMetaData( arg0Copy);
					return (javax.ejb.EJBMetaData)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnNestedEJBMetaData
	//
	public org.openejb.test.object.ObjectGraph returnNestedEJBMetaData()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnNestedEJBMetaData",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.ObjectGraph _arg_ret = ( org.openejb.test.object.ObjectGraph )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.ObjectGraph.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnNestedEJBMetaData",_opsClass);
				if ( _so == null )
				   return returnNestedEJBMetaData();
				try
				{
					org.openejb.test.object.ObjectGraph _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnNestedEJBMetaData();
					return (org.openejb.test.object.ObjectGraph)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnEJBMetaDataArray
	//
	public javax.ejb.EJBMetaData[] returnEJBMetaDataArray(javax.ejb.EJBMetaData[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnEJBMetaDataArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),javax.ejb.EJBMetaData[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBMetaData[] _arg_ret = ( javax.ejb.EJBMetaData[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.EJBMetaData[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnEJBMetaDataArray",_opsClass);
				if ( _so == null )
				   return returnEJBMetaDataArray( arg0);
				try
				{
					javax.ejb.EJBMetaData[] arg0Copy = (javax.ejb.EJBMetaData[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.EJBMetaData[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnEJBMetaDataArray( arg0Copy);
					return (javax.ejb.EJBMetaData[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnHandle__javax_ejb_Handle
	//
	public javax.ejb.Handle returnHandle(javax.ejb.Handle arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnHandle__javax_ejb_Handle",true);
					javax.rmi.CORBA.Util.writeAbstractObject( _output, arg0 );
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.Handle _arg_ret = ( javax.ejb.Handle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_input).read_abstract_interface(), javax.ejb.Handle.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnHandle__javax_ejb_Handle",_opsClass);
				if ( _so == null )
				   return returnHandle( arg0);
				try
				{
					javax.ejb.Handle arg0Copy = (javax.ejb.Handle)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.Handle _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnHandle( arg0Copy);
					return (javax.ejb.Handle)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnHandle__
	//
	public javax.ejb.Handle returnHandle()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnHandle__",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.Handle _arg_ret = ( javax.ejb.Handle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_input).read_abstract_interface(), javax.ejb.Handle.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnHandle__",_opsClass);
				if ( _so == null )
				   return returnHandle();
				try
				{
					javax.ejb.Handle _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnHandle();
					return (javax.ejb.Handle)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnNestedHandle
	//
	public org.openejb.test.object.ObjectGraph returnNestedHandle()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnNestedHandle",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.ObjectGraph _arg_ret = ( org.openejb.test.object.ObjectGraph )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.ObjectGraph.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnNestedHandle",_opsClass);
				if ( _so == null )
				   return returnNestedHandle();
				try
				{
					org.openejb.test.object.ObjectGraph _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnNestedHandle();
					return (org.openejb.test.object.ObjectGraph)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnHandleArray
	//
	public javax.ejb.Handle[] returnHandleArray(javax.ejb.Handle[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnHandleArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),javax.ejb.Handle[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.Handle[] _arg_ret = ( javax.ejb.Handle[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.Handle[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnHandleArray",_opsClass);
				if ( _so == null )
				   return returnHandleArray( arg0);
				try
				{
					javax.ejb.Handle[] arg0Copy = (javax.ejb.Handle[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					javax.ejb.Handle[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnHandleArray( arg0Copy);
					return (javax.ejb.Handle[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnObjectGraph
	//
	public org.openejb.test.object.ObjectGraph returnObjectGraph(org.openejb.test.object.ObjectGraph arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnObjectGraph",true);
					_output.write_value((java.io.Serializable)arg0,org.openejb.test.object.ObjectGraph.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.ObjectGraph _arg_ret = ( org.openejb.test.object.ObjectGraph )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.ObjectGraph.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnObjectGraph",_opsClass);
				if ( _so == null )
				   return returnObjectGraph( arg0);
				try
				{
					org.openejb.test.object.ObjectGraph arg0Copy = (org.openejb.test.object.ObjectGraph)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					org.openejb.test.object.ObjectGraph _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnObjectGraph( arg0Copy);
					return (org.openejb.test.object.ObjectGraph)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation returnObjectGraphArray
	//
	public org.openejb.test.object.ObjectGraph[] returnObjectGraphArray(org.openejb.test.object.ObjectGraph[] arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("returnObjectGraphArray",true);
					_output.write_value((java.io.Serializable)((java.lang.Object)arg0),org.openejb.test.object.ObjectGraph[].class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.ObjectGraph[] _arg_ret = ( org.openejb.test.object.ObjectGraph[] )( java.lang.Object )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.ObjectGraph[].class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("returnObjectGraphArray",_opsClass);
				if ( _so == null )
				   return returnObjectGraphArray( arg0);
				try
				{
					org.openejb.test.object.ObjectGraph[] arg0Copy = (org.openejb.test.object.ObjectGraph[])javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					org.openejb.test.object.ObjectGraph[] _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).returnObjectGraphArray( arg0Copy);
					return (org.openejb.test.object.ObjectGraph[])javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Attribute getEJBHome
	//
	public javax.ejb.EJBHome getEJBHome()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_EJBHome",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBHome _arg_ret = ( javax.ejb.EJBHome ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), javax.ejb.EJBHome.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_EJBHome",_opsClass);
				if ( _so == null )
				   return getEJBHome();
				try
				{
					javax.ejb.EJBHome _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).getEJBHome();
					return (javax.ejb.EJBHome)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Attribute getHandle
	//
	public javax.ejb.Handle getHandle()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_handle",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.Handle _arg_ret = ( javax.ejb.Handle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_input).read_abstract_interface(), javax.ejb.Handle.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_handle",_opsClass);
				if ( _so == null )
				   return getHandle();
				try
				{
					javax.ejb.Handle _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).getHandle();
					return (javax.ejb.Handle)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Attribute getPrimaryKey
	//
	public java.lang.Object getPrimaryKey()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_primaryKey",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.lang.Object _arg_ret = javax.rmi.CORBA.Util.readAny(_input);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_primaryKey",_opsClass);
				if ( _so == null )
				   return getPrimaryKey();
				try
				{
					java.lang.Object _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).getPrimaryKey();
					return (java.lang.Object)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation remove
	//
	public void remove()
		throws javax.ejb.RemoveException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("remove",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					return;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					if ( _exception_id.equals("IDL:javax/ejb/RemoveEx:1.0") )
					{
						_input.read_string();
						throw ( javax.ejb.RemoveException ) _input.read_value(javax.ejb.RemoveException.class);
					}

					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("remove",_opsClass);
				if ( _so == null )
				   remove();
				try
				{
					((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).remove();
					return;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					if ( ex2 instanceof javax.ejb.RemoveException )
						throw ( javax.ejb.RemoveException ) ex2;

					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

	//
	// Operation isIdentical
	//
	public boolean isIdentical(javax.ejb.EJBObject arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("isIdentical",true);
					javax.rmi.CORBA.Util.writeRemoteObject( _output, arg0 );
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					boolean _arg_ret = _input.read_boolean();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _exception.getId();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
			}
			else
			{
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("isIdentical",_opsClass);
				if ( _so == null )
				   return isIdentical( arg0);
				try
				{
					javax.ejb.EJBObject arg0Copy = (javax.ejb.EJBObject)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					boolean _arg_ret = ((org.openejb.test.entity.bmp.RmiIiopBmpObject)_so.servant).isIdentical( arg0Copy);
					return _arg_ret;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					throw javax.rmi.CORBA.Util.wrapException(ex2);
				}
				finally
				{
					_servant_postinvoke(_so);
				}
			}
		}
	}

}
