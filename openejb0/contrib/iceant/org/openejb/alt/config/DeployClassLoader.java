package org.openejb.alt.config;

/**
  This class use for reference from OpenORB project.
  Modified for OpenEJB deploy demand by pizer.chen iceant@21cn.com
*/
public class DeployClassLoader extends java.lang.ClassLoader {
    /** List of path */
    private java.util.ArrayList path_list;

    private java.util.HashMap archives;

    private java.lang.ClassLoader parent;

    /** This class used to load class for Deploy */
    public DeployClassLoader(java.lang.ClassLoader _parent)
    {
        path_list = new java.util.ArrayList();

        archives = new java.util.HashMap();

        this.parent = _parent;
    }

    /**
     * Add a class path to the path list.
     */
    public void addPath( String path )
    {
        java.util.StringTokenizer token = new java.util.StringTokenizer( path, java.io.File.pathSeparator );

        while ( token.hasMoreTokens() ) {
            String nextToken = token.nextToken();

            path_list.add( nextToken );

            // Support for manifest file referenced class path
            if ( nextToken.endsWith(".jar") ) addManifestReferences( nextToken );
        }
    }

    /**
     * This operation reads the manifest file of a jar archive,
     * and add the contents the the path list.
     *
     * All entries are added relative to the current jar file path.
     * I.e. if the entry reads foobar.jar, it will be added at the
     * same path level than the current path
     *  
     */
    private void addManifestReferences( String archive ) {

        try {

            // Open the jar file
            java.util.jar.Manifest m = ( new java.util.jar.JarFile( archive ) ).getManifest();

            String basePath = archive.substring( 0, archive.lastIndexOf( java.io.File.separator ) + 1 );

            if ( m == null ) return;

            java.lang.String paths = (java.lang.String)m.getMainAttributes().getValue("Class-Path");

            if ( paths == null ) return;

            java.util.StringTokenizer token = new java.util.StringTokenizer( paths, " ");

            while ( token.hasMoreTokens() )

                path_list.add( (basePath + token.nextToken()));      
        } catch ( java.lang.Exception ex ) {
        } 
    }

    /**
     * This operation is used to load a class
     * modified by Pizer.Chen -- iceant@21cn.com
     */
    public java.lang.Class loadClass( String name, boolean resolve )
    throws ClassNotFoundException
    {
        java.lang.Class clz = findLoadedClass( name );

        // Modified to suitable for Deploy demand.

        if ( clz == null )
            clz =  findClass( name );
        
        if ( clz == null ) {
            try {
                //clz = findSystemClass( name );
                clz = parent.loadClass(name);
            } catch ( ClassNotFoundException ex ) {
            }
        }

        if ( resolve  && clz != null )
            resolveClass( clz );

        if ( clz == null )
            throw new ClassNotFoundException();
       
        return clz;
    }

    /**
     * This operation is used to find a class 
     */
    protected java.lang.Class findClass( String name )
    {
        byte [] content = null;

        // We are going to parse all path
        for ( int i=0; i<path_list.size(); i++ ) {
            String path = ( String ) path_list.get( i );

            if ( path.endsWith(".zip") || path.endsWith(".jar") ) {
                content = loadClassFromArchive( path, name_to_archive_class( name ) );
                if ( content != null )
                    break;
            } else {
                content = loadClassFromPath( path, name );
                if ( content != null )
                    break;

            }
        }

        if ( content == null )
            return null;

        return defineClass( name, content, 0, content.length );
    }

    /**
     * This operation is used to return a class content ( as a byte stream ) from an archive ( ZIP or JAR )
     */
    private byte [] loadClassFromArchive( String archive_name, String name )
    {
        try {
            boolean new_archive = false;

            org.openejb.alt.config.ZipHandle archive = ( org.openejb.alt.config.ZipHandle ) archives.get( archive_name );

            if ( archive == null ) {
                // A new archive must be open

                try {
                    archive = org.openejb.alt.config.ZipTool.open( archive_name );

                    new_archive = true;
                } catch ( java.io.IOException ex ) {
                    org.openejb.alt.config.ZipTool.close( archive );

                    // Unable to open the zip file
                    return null;
                }
            }

            if ( !org.openejb.alt.config.ZipTool.containsFile( archive, name ) ) {
                if ( new_archive )
                    org.openejb.alt.config.ZipTool.close( archive );

                return null;
            }

            byte [] content = org.openejb.alt.config.ZipTool.getFileContent( archive, name );

            if ( new_archive )
                archives.put( archive_name, archive );

            return content;
        } catch ( java.io.IOException ex ) {
            return null;
        }
    }

    /**
     * Return a class name (for an archive)
     */
    private String name_to_archive_class( String name )
    {

        String file_name = name.replace( '.', '/' );

        return(  file_name + ".class" );
    }

    /**
     * Return a class name
     */
    private String name_to_class( String name )
    {

        String file_name = name.replace( '.', java.io.File.separatorChar );

        return(  file_name + ".class" );
    }

    /**
     * This operation is used to return
     */
    private byte [] loadClassFromPath( String path, String name )
    {
        if ( ! path.endsWith( java.io.File.separator ) )
            path = path + java.io.File.separator;

        java.io.File file = new java.io.File( path + name_to_class( name ) ); 

        if ( ! file.exists() )
            return null;

        try {
            java.io.FileInputStream input = new java.io.FileInputStream( file );

            long size = 0;
            java.util.Vector list = new java.util.Vector();
            byte [] packet = null;

            while ( true ) {
                packet = new byte[ 2048 ];

                long read = input.read( packet, 0, packet.length );

                if ( read == -1 )
                    break;

                size += read;

                byte[] correctPacket = new byte[(int)read];

                System.arraycopy( packet, 0, correctPacket, 0, (int)read );

                list.addElement( correctPacket );
            }

            input.close();      

            byte [] content = new byte[ ( int )size ];
            int index = 0;

            for ( int i=0; i<list.size(); i++ ) {
                packet = ( byte [] ) list.elementAt( i );

                System.arraycopy( packet, 0, content, index, packet.length );

                index += packet.length;
            }

            return content;
        } catch ( java.lang.Throwable ex ) {
            // An exception has been intercepted during the class loading

            return null;
        }

    }

    /**
       * Get system resource
       */
    public java.net.URL getResource( String name ) {

        try {
            // try by using the default ClassLoader  
            java.net.URL url = ClassLoader.getSystemClassLoader().getResource( name );

            if ( url != null ) return url;

            return findResource( name );
        } catch ( java.lang.Exception ex ) {
            return null;
        }
    }

    /**
       * This operation is used to find a class 
       */
    protected java.net.URL findResource( String name )
    {
        try {
            String resourcePath = null;

            // We are going to parse all path
            for ( int i=0; i<path_list.size(); i++ ) {
                String path = ( String ) path_list.get( i );

                if ( path.endsWith(".zip") || path.endsWith(".jar") ) {
                    resourcePath = loadResourceFromArchive( path, name );
                    if ( resourcePath != null )
                        break;
                } else {
                    resourcePath = loadResourceFromPath( path, name );
                    if ( resourcePath != null )
                        break;
                    break;
                }
            }

            if ( resourcePath == null )
                return null;

            return new java.net.URL( resourcePath );
        } catch ( java.lang.Exception ex ) {
            return null;
        }
    } 

    /**
     * This operation is used to return a class content ( as a byte stream ) from an archive ( ZIP or JAR )
     */
    private String loadResourceFromArchive( String archive_name, String name )
    {
        try {
            boolean new_archive = false;

            org.openejb.alt.config.ZipHandle archive = ( org.openejb.alt.config.ZipHandle ) archives.get( archive_name );

            if ( archive == null ) {
                // A new archive must be open

                try {
                    archive = org.openejb.alt.config.ZipTool.open( archive_name );

                    new_archive = true;
                } catch ( java.io.IOException ex ) {
                    org.openejb.alt.config.ZipTool.close( archive );

                    // Unable to open the zip file
                    return null;
                }
            }

            if ( !org.openejb.alt.config.ZipTool.containsFile( archive, name ) ) {
                if ( new_archive )
                    org.openejb.alt.config.ZipTool.close( archive );

                return null;
            }
            return new String( "jar:file:" + archive_name + "!/" + name );
        } catch ( java.io.IOException ex ) {
            return null;
        }
    }

    /**
     * Return a resource name
     */
    private String name_to_resource( String name )
    {

        String file_name = name.replace( '/', java.io.File.separatorChar );

        return(  file_name );
    }

    /**
     * This operation is used to return
     */
    private String loadResourceFromPath( String path, String name )
    {
        if ( ! path.endsWith( java.io.File.separator ) )
            path = path + java.io.File.separator;

        java.io.File file = new java.io.File( path + name ); 

        if ( ! file.exists() )
            return null;

        return new String( "file:" + file.getAbsolutePath() );

    }         
}
