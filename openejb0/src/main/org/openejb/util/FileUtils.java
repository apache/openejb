package org.openejb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.SecurityException;

public class FileUtils{

    private static final java.util.Random _random = new java.util.Random();

    private static File home;

    
    /**
     * Resolves the specifed path reletive to the openejb.home variable
     * 
     * @param path
     * @return 
     * @exception java.io.IOException
     */
    public static File getDirectory(String path) throws java.io.IOException{
        File dir = null;
        
        if ( home == null ) resolveOpenEjbHome();
        
        dir = new File(home, path);
        dir = dir.getCanonicalFile();


        if( !dir.exists() ) {
            try{
                if (!dir.mkdirs()) throw new IOException("Cannot create the directory "+dir.getPath());
            } catch (SecurityException e){
                throw new IOException("Permission denied: Cannot create the directory "+dir.getPath()+" : "+e.getMessage());
            }
        } else if ( dir.exists() && !dir.isDirectory() ) {
            throw new IOException("The path specified is not a valid directory: "+dir.getPath());
        }

        return dir;
    }	

    public static File getFile(String path) throws java.io.FileNotFoundException, java.io.IOException{
        return FileUtils.getFile(path, true);
    }	

    public static File getFile(String path, boolean validate) throws java.io.FileNotFoundException, java.io.IOException{
        File file = null;
        
        if ( home == null ) resolveOpenEjbHome();
        
        file = new File(home, path);
        file = file.getCanonicalFile();


        if( validate && !file.exists() ) {
            throw new FileNotFoundException("The path specified is not a valid file: "+file.getPath());
        } else if ( validate && file.isDirectory() ) {
            throw new FileNotFoundException("The path specified is a directory, not a file: "+file.getPath());
        }

        return file;
    }	

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
    
    private static void resolveOpenEjbHome() throws IOException{
        String openejb =  null;
        try{
            openejb = (String)System.getProperty("openejb.home", System.getProperty("user.dir"));
            
            home = new File(openejb);
            
            if( !home.exists() ) {
                String userDir = System.getProperty("user.dir");
                //log.warn("The OpenEJB Home directory specified at startup doesn't exist, using current working directory instead: openejb.home = "+openejb+" current dir = "+userDir);
                openejb = userDir;
                home = new File(openejb);
            } else if ( home.exists() && !home.isDirectory() ) {
                String userDir = System.getProperty("user.dir");
                //log.warn("The OpenEJB Home directory specified at startup is not a valid directory, using current working directory instead: openejb.home = "+openejb+" current dir = "+userDir);
                openejb = userDir;
                home = new File(openejb);
            }
        } catch (SecurityException e){
            throw new IOException("Cannot resolve the OpenEJB directory: "+openejb+" : "+e.getMessage());
        }
    }
}
