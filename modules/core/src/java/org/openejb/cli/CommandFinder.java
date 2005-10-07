package org.openejb.cli;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CommandFinder {
	private String path;
	private Map classMap = Collections.synchronizedMap(new HashMap());
	
	public CommandFinder(String path) {
		this.path = path;
	}
	
    public Properties doFindCommandProperies(String key) throws IOException {
        String uri = path + key;
        
        // lets try the thread context class loader first
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);
        if (in == null) {
            in = CommandFinder.class.getClassLoader().getResourceAsStream(uri);
            if (in == null) {
                throw new IOException("Could not find factory class for resource: " + uri);
            }
        }

        // lets load the file
        BufferedInputStream reader = null;
        try {
            reader = new BufferedInputStream(in);
            Properties properties = new Properties();
            properties.load(reader);
            
            //All is well, set openejb.home
            URL propsURL = Thread.currentThread().getContextClassLoader().getResource(uri);
            String propsString = propsURL.getFile();
            URL jarURL;
            File jarFile;
            
            propsString = propsString.substring(0, propsString.indexOf("!"));
            
            jarURL = new URL(propsString);
            jarFile = new File(jarURL.getFile());
            
            if (jarFile.getName().indexOf("openejb-core") > -1) {
            	File lib = jarFile.getParentFile();
            	File home = lib.getParentFile();
            	
            	System.setProperty("openejb.home", home.getAbsolutePath());
            }
            
            return properties;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }
    
    public Enumeration doFindCommands() throws IOException {
    	return Thread.currentThread().getContextClassLoader().getResources(path);
    }
}