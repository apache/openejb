package org.openejb.test.entity.cmp;

/**
 * Interface definition : EncCmpHome
 * 
 * @author OpenORB Compiler
 */

public class _EncCmpHome_Stub extends javax.rmi.CORBA.Stub
		implements EncCmpHome
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.entity.cmp.EncCmpHome:0000000000000000", 
		"RMI:javax.ejb.EJBHome:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = EncCmpHome.class;

	//
	// Operation create
	//
	public org.openejb.test.entity.cmp.EncCmpObject create(String arg0)
		throws javax.ejb.CreateException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("create",true);
					_output.write_value((java.io.Serializable)arg0,String.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.entity.cmp.EncCmpObject _arg_ret = ( org.openejb.test.entity.cmp.EncCmpObject ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), org.openejb.test.entity.cmp.EncCmpObject.class);
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
					if ( _exception_id.equals("IDL:javax/ejb/CreateEx:1.0") )
					{
						_input.read_string();
						throw ( javax.ejb.CreateException ) _input.read_value(javax.ejb.CreateException.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("create",_opsClass);
				if ( _so == null )
				   return create( arg0);
				try
				{
					String arg0Copy = (String)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					org.openejb.test.entity.cmp.EncCmpObject _arg_ret = ((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).create( arg0Copy);
					return (org.openejb.test.entity.cmp.EncCmpObject)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					if ( ex2 instanceof javax.ejb.CreateException )
						throw ( javax.ejb.CreateException ) ex2;

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
	// Operation findByPrimaryKey
	//
	public org.openejb.test.entity.cmp.EncCmpObject findByPrimaryKey(java.lang.Integer arg0)
		throws javax.ejb.FinderException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("findByPrimaryKey",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Integer.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.entity.cmp.EncCmpObject _arg_ret = ( org.openejb.test.entity.cmp.EncCmpObject ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), org.openejb.test.entity.cmp.EncCmpObject.class);
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
					if ( _exception_id.equals("IDL:javax/ejb/FinderEx:1.0") )
					{
						_input.read_string();
						throw ( javax.ejb.FinderException ) _input.read_value(javax.ejb.FinderException.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("findByPrimaryKey",_opsClass);
				if ( _so == null )
				   return findByPrimaryKey( arg0);
				try
				{
					java.lang.Integer arg0Copy = (java.lang.Integer)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					org.openejb.test.entity.cmp.EncCmpObject _arg_ret = ((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).findByPrimaryKey( arg0Copy);
					return (org.openejb.test.entity.cmp.EncCmpObject)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					if ( ex2 instanceof javax.ejb.FinderException )
						throw ( javax.ejb.FinderException ) ex2;

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
	// Operation findEmptyCollection
	//
	public java.util.Collection findEmptyCollection()
		throws javax.ejb.FinderException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("findEmptyCollection",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.util.Collection _arg_ret = ( java.util.Collection )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.util.Collection.class);
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
					if ( _exception_id.equals("IDL:javax/ejb/FinderEx:1.0") )
					{
						_input.read_string();
						throw ( javax.ejb.FinderException ) _input.read_value(javax.ejb.FinderException.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("findEmptyCollection",_opsClass);
				if ( _so == null )
				   return findEmptyCollection();
				try
				{
					java.util.Collection _arg_ret = ((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).findEmptyCollection();
					return (java.util.Collection)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
				}
				catch ( Throwable ex )
				{
					Throwable ex2 = ( Throwable ) javax.rmi.CORBA.Util.copyObject(ex, _orb());
					if ( ex2 instanceof javax.ejb.FinderException )
						throw ( javax.ejb.FinderException ) ex2;

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
	// Attribute getEJBMetaData
	//
	public javax.ejb.EJBMetaData getEJBMetaData()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_EJBMetaData",true);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_EJBMetaData",_opsClass);
				if ( _so == null )
				   return getEJBMetaData();
				try
				{
					javax.ejb.EJBMetaData _arg_ret = ((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).getEJBMetaData();
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
	// Attribute getHomeHandle
	//
	public javax.ejb.HomeHandle getHomeHandle()
		throws java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_homeHandle",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.HomeHandle _arg_ret = ( javax.ejb.HomeHandle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_input).read_abstract_interface(), javax.ejb.HomeHandle.class);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("_get_homeHandle",_opsClass);
				if ( _so == null )
				   return getHomeHandle();
				try
				{
					javax.ejb.HomeHandle _arg_ret = ((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).getHomeHandle();
					return (javax.ejb.HomeHandle)javax.rmi.CORBA.Util.copyObject(_arg_ret, _orb());
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
	// Operation remove__javax_ejb_Handle
	//
	public void remove(javax.ejb.Handle arg0)
		throws javax.ejb.RemoveException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("remove__javax_ejb_Handle",true);
					javax.rmi.CORBA.Util.writeAbstractObject( _output, arg0 );
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("remove__javax_ejb_Handle",_opsClass);
				if ( _so == null )
				   remove( arg0);
				try
				{
					javax.ejb.Handle arg0Copy = (javax.ejb.Handle)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).remove( arg0Copy);
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
	// Operation remove__java_lang_Object
	//
	public void remove(java.lang.Object arg0)
		throws javax.ejb.RemoveException, java.rmi.RemoteException
	{
		while( true )
		{
			if (!javax.rmi.CORBA.Util.isLocal(this) )
			{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("remove__java_lang_Object",true);
					javax.rmi.CORBA.Util.writeAny(_output,arg0);
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
				org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("remove__java_lang_Object",_opsClass);
				if ( _so == null )
				   remove( arg0);
				try
				{
					java.lang.Object arg0Copy = (java.lang.Object)javax.rmi.CORBA.Util.copyObject(arg0, _orb());
					((org.openejb.test.entity.cmp.EncCmpHome)_so.servant).remove( arg0Copy);
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

}
