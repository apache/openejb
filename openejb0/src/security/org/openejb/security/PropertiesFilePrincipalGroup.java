package org.openejb.security;


public class PropertiesFilePrincipalGroup implements java.security.Principal, java.io.Serializable {
    protected String _groupName;

    public PropertiesFilePrincipalGroup( String groupName ) {
	_groupName = groupName;
    }

    public java.lang.String getName() {
	return _groupName;
    }

    public boolean equals( Object another ) {
	if ( another == null ) return false;

	if ( this == another ) return true;

	if ( !(another instanceof PropertiesFilePrincipalGroup) ) return false;

	PropertiesFilePrincipalGroup that = (PropertiesFilePrincipalGroup)another;
	if ( this._groupName.equals(that._groupName) ) return true;

	return false;
    }

    public String toString() {
	return "PropertiesFilePrincipalGroup: "+_groupName;
    }
}
