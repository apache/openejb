package org.openejb.alt.config;

/**
 * A zip handle contains information to access a zip ( for in and out access )
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 * @version $Revision$ $Date$ 
 */
 
/** This Class borrowed from OpenORB project */
public class ZipHandle
{
	public java.util.zip.ZipFile in;
	
	public java.util.zip.ZipOutputStream out;
}
