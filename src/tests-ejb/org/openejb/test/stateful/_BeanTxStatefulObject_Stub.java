package org.openejb.test.stateful;

//
// Interface definition : BeanTxStatefulObject
//
// @author OpenORB Compiler
//
public class _BeanTxStatefulObject_Stub extends javax.rmi.CORBA.Stub
		implements BeanTxStatefulObject
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.stateful.BeanTxStatefulObject:0000000000000000", 
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = BeanTxStatefulObject.class;

	//
	// Attribute getUserTransaction
	//
	public org.openejb.test.object.Transaction getUserTransaction()
		throws java.rmi.RemoteException
	{
		while( true )
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
					throw new org.omg.CORBA.UNKNOWN("Unexcepected User Exception: "+ _exception_id);
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
	}

	//
	// Operation jndiUserTransaction
	//
	public org.openejb.test.object.Transaction jndiUserTransaction()
		throws java.rmi.RemoteException
	{
		while( true )
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
					java.lang.String _exception_id = _input.read_string();
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
	}

	//
	// Operation openAccount
	//
	public void openAccount(org.openejb.test.object.Account arg0, java.lang.Boolean arg1)
		throws javax.transaction.RollbackException, java.rmi.RemoteException
	{
		while( true )
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
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.transaction.RollbackException:000000000017A37C:0000000000000000") )
					{
						throw ( javax.transaction.RollbackException ) _input.read_value();
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
	}

	//
	// Operation retreiveAccount
	//
	public org.openejb.test.object.Account retreiveAccount(java.lang.String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("retreiveAccount",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.String.class);
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
					java.lang.String _exception_id = _input.read_string();
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
	}

	//
	// Attribute getEJBHome
	//
	public javax.ejb.EJBHome getEJBHome()
		throws java.rmi.RemoteException
	{
		while( true )
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
					throw new org.omg.CORBA.UNKNOWN("Unexcepected User Exception: "+ _exception_id);
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
	}

	//
	// Attribute getHandle
	//
	public javax.ejb.Handle getHandle()
		throws java.rmi.RemoteException
	{
		while( true )
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
					throw new org.omg.CORBA.UNKNOWN("Unexcepected User Exception: "+ _exception_id);
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
	}

	//
	// Attribute getPrimaryKey
	//
	public java.lang.Object getPrimaryKey()
		throws java.rmi.RemoteException
	{
		while( true )
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
					throw new org.omg.CORBA.UNKNOWN("Unexcepected User Exception: "+ _exception_id);
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
	}

	//
	// Operation remove
	//
	public void remove()
		throws javax.ejb.RemoveException, java.rmi.RemoteException
	{
		while( true )
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
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.ejb.RemoveException:00000000000ECD7E:0000000000000000") )
					{
						throw ( javax.ejb.RemoveException ) _input.read_value();
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
	}

	//
	// Operation isIdentical
	//
	public boolean isIdentical(javax.ejb.EJBObject arg0)
		throws java.rmi.RemoteException
	{
		while( true )
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
					java.lang.String _exception_id = _input.read_string();
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
	}

}
