package org.openejb.test.entity.bmp;

/**
 * Interface definition : BasicBmpObject
 * 
 * @author OpenORB Compiler
 */

public class _BasicBmpObject_Stub extends javax.rmi.CORBA.Stub
		implements BasicBmpObject
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.entity.bmp.BasicBmpObject:0000000000000000", 
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = BasicBmpObject.class;

	//
	// Attribute getPermissionsReport
	//
	public java.util.Properties getPermissionsReport()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_permissionsReport",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.util.Properties _arg_ret = ( java.util.Properties )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.util.Properties.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_permissionsReport",_opsClass);
				if ( _so == null )
				   return getPermissionsReport();
				try
				{
					java.util.Properties _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).getPermissionsReport();
					return (java.util.Properties)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
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
	// Operation businessMethod
	//
	public String businessMethod(String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("businessMethod",true);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("businessMethod",_opsClass);
				if ( _so == null )
				   return businessMethod( arg0);
				try
				{
					String arg0Copy = (String)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					String _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).businessMethod( arg0Copy);
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
	// Operation getAllowedOperationsReport
	//
	public org.openejb.test.object.OperationsPolicy getAllowedOperationsReport(String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("getAllowedOperationsReport",true);
					_output.write_value((java.io.Serializable)arg0,String.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.OperationsPolicy _arg_ret = ( org.openejb.test.object.OperationsPolicy )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.OperationsPolicy.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("getAllowedOperationsReport",_opsClass);
				if ( _so == null )
				   return getAllowedOperationsReport( arg0);
				try
				{
					String arg0Copy = (String)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					org.openejb.test.object.OperationsPolicy _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).getAllowedOperationsReport( arg0Copy);
					return (org.openejb.test.object.OperationsPolicy)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
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
					javax.ejb.EJBHome _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).getEJBHome();
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
					javax.ejb.Handle _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).getHandle();
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
					java.lang.Object _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).getPrimaryKey();
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
					((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).remove();
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
					boolean _arg_ret = ((org.openejb.test.entity.bmp.BasicBmpObject)_so.servant).isIdentical( arg0Copy);
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
