package org.openejb.util.io;

import java.io.File;

public class FilesystemUtilities{
    private static final java.util.Random _random = new java.util.Random();

    /** Creates a string for a temporary directory
	@param pathPrefix the path prefix to for the directory, e.g. /tmp/openejb
	@returns the file object associated with the unique name
	@throws java.io.IOException if it can't find a unique directory name after many iterations
    */
    public static File createTempDirectory(String pathPrefix) throws java.io.IOException{
	for(int maxAttempts=100; maxAttempts>0; --maxAttempts){
	    String path=pathPrefix+_random.nextLong();
	    java.io.File tmpDir = new java.io.File(path);
	    if(tmpDir.exists()) {
		continue;
	    } else {
		return tmpDir;
	    }
	}
	throw new java.io.IOException("Can't create temporary directory.");
    }	

    /** Creates a string for a temporary directory
	The path prefix is chosen from the system property "java.io.tmpdir" plus a file separator plus the string "openejb"
	@returns the file object associated with the unique name
	@throws java.io.IOException if it can't find a unique directory name after many iterations
    */
    public static File createTempDirectory() throws java.io.IOException{
	String prefix = System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator+"openejb";
	return createTempDirectory(prefix);
    }	

}
