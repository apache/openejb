package org.openejb.security;


public class PropertiesFilePrincipalUser implements java.security.Principal, java.io.Serializable {
    protected String _userName;

    public PropertiesFilePrincipalUser( String userName ) {
	_userName = userName;
    }

    public java.lang.String getName() {
	return _userName;
    }

    public boolean equals( Object another ) {
	if ( another == null ) return false;

	if ( this == another ) return true;

	if ( !(another instanceof PropertiesFilePrincipalUser) ) return false;

	PropertiesFilePrincipalUser that = (PropertiesFilePrincipalUser)another;
	if ( this._userName.equals(that._userName) ) return true;

	return false;
    }

    public String toString() {
	return "PropertiesFilePrincipalUser: "+_userName;
    }
}
