package org.openejb.test.stateless;

/**
 * Interface definition : BeanTxStatelessObject
 * 
 * @author OpenORB Compiler
 */

public class _BeanTxStatelessObject_Stub extends javax.rmi.CORBA.Stub
		implements BeanTxStatelessObject
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.stateless.BeanTxStatelessObject:0000000000000000", 
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = BeanTxStatelessObject.class;

	//
	// Attribute getUserTransaction
	//
	public org.openejb.test.object.Transaction getUserTransaction()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_userTransaction",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.Transaction _arg_ret = ( org.openejb.test.object.Transaction )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.Transaction.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_userTransaction",_opsClass);
				if ( _so == null )
				   return getUserTransaction();
				try
				{
					org.openejb.test.object.Transaction _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).getUserTransaction();
					return (org.openejb.test.object.Transaction)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
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
	// Operation jndiUserTransaction
	//
	public org.openejb.test.object.Transaction jndiUserTransaction()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("jndiUserTransaction",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.Transaction _arg_ret = ( org.openejb.test.object.Transaction )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.Transaction.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("jndiUserTransaction",_opsClass);
				if ( _so == null )
				   return jndiUserTransaction();
				try
				{
					org.openejb.test.object.Transaction _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).jndiUserTransaction();
					return (org.openejb.test.object.Transaction)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
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
	// Operation openAccount
	//
	public void openAccount(org.openejb.test.object.Account arg0, java.lang.Boolean arg1)
		throws javax.transaction.RollbackException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("openAccount",true);
					_output.write_value((java.io.Serializable)arg0,org.openejb.test.object.Account.class);
					_output.write_value((java.io.Serializable)arg1,java.lang.Boolean.class);
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
					if ( _exception_id.equals("IDL:javax/transaction/RollbackEx:1.0") )
					{
						_input.read_string();
						throw ( javax.transaction.RollbackException ) _input.read_value(javax.transaction.RollbackException.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("openAccount",_opsClass);
				if ( _so == null )
				   openAccount( arg0,  arg1);
				try
				{
					org.openejb.test.object.Account arg0Copy = (org.openejb.test.object.Account)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					java.lang.Boolean arg1Copy = (java.lang.Boolean)javax.rmi.CORBA.Util.copyObject(arg1, _orb());
					((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).openAccount( arg0Copy,  arg1Copy);
					return;
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					if ( ex2 instanceof javax.transaction.RollbackException )
						throw ( javax.transaction.RollbackException ) ex2;

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
	// Operation retreiveAccount
	//
	public org.openejb.test.object.Account retreiveAccount(String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("retreiveAccount",true);
					_output.write_value((java.io.Serializable)arg0,String.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.object.Account _arg_ret = ( org.openejb.test.object.Account )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(org.openejb.test.object.Account.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("retreiveAccount",_opsClass);
				if ( _so == null )
				   return retreiveAccount( arg0);
				try
				{
					String arg0Copy = (String)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					org.openejb.test.object.Account _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).retreiveAccount( arg0Copy);
					return (org.openejb.test.object.Account)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
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
					javax.ejb.EJBHome _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).getEJBHome();
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
					javax.ejb.Handle _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).getHandle();
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
					java.lang.Object _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).getPrimaryKey();
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
					((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).remove();
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
					boolean _arg_ret = ((org.openejb.test.stateless.BeanTxStatelessObject)_so.servant).isIdentical( arg0Copy);
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
