package org.openejb.test.entity.bmp;

//
// Interface definition : BasicBmp2DataSourcesObject
//
// @author OpenORB Compiler
//
public class _BasicBmp2DataSourcesObject_Stub extends javax.rmi.CORBA.Stub
		implements BasicBmp2DataSourcesObject
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.entity.bmp.BasicBmp2DataSourcesObject:0000000000000000", 
		"RMI:javax.ejb.EJBObject:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = BasicBmp2DataSourcesObject.class;

	//
	// Attribute getPermissionsReport
	//
	public java.util.Properties getPermissionsReport()
		throws java.rmi.RemoteException
	{
		while( true )
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
	// Operation businessMethod
	//
	public String businessMethod(String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
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
	// Operation getAllowedOperationsReport
	//
	public org.openejb.test.object.OperationsPolicy getAllowedOperationsReport(String arg0)
		throws java.rmi.RemoteException
	{
		while( true )
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
					if ( _exception_id.equals("IDL:javax/ejb/RemoveEx:1.0") )
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
