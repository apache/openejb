package org.openejb.alt.config;


public class ZipTool {
    /**
     * Create a Zip file
     */
    public static org.openejb.alt.config.ZipHandle create( String name )
    throws java.io.IOException
    {
        org.openejb.alt.config.ZipHandle handle = new org.openejb.alt.config.ZipHandle();

        java.io.FileOutputStream out = new java.io.FileOutputStream( name );

        handle.out = new java.util.zip.ZipOutputStream( out );

        return handle;
    }

    /**
     * Open a zip file
     */
    public static org.openejb.alt.config.ZipHandle open( String name )
    throws java.io.IOException
    {
        org.openejb.alt.config.ZipHandle handle = new org.openejb.alt.config.ZipHandle();

        handle.in = new java.util.zip.ZipFile( new java.io.File(name) );

        return handle;
    }               

    /**
     * Close a zip file
     */
    public static void close( org.openejb.alt.config.ZipHandle handle )
    throws java.io.IOException
    {
        /** Modified by pizer.chen iceant@21cn.com */
        if (handle!=null) {

            if ( handle.in != null )
                handle.in.close();

            if ( handle.out != null )
                handle.out.close();
        }
    }

    /**
     * This function builds a new Zip file entry to add a file
     */
    private static java.util.zip.ZipEntry buildEntry( String name )
    {
        return new java.util.zip.ZipEntry( name );
    }

    /**
     * This function adds a file to a Zip file
     */
    public static void insert( String src_name, String dst_name, org.openejb.alt.config.ZipHandle dest )
    throws java.io.IOException      
    {
        byte[] buffer = new byte[5000];
        java.io.File file;
        int read;

        java.util.zip.ZipOutputStream outputZip = dest.out;

        java.util.zip.ZipEntry entry = buildEntry( dst_name );

        outputZip.putNextEntry( entry );

        java.io.FileInputStream input = new java.io.FileInputStream( new java.io.File( src_name ) );

        do {
            read = input.read(buffer);
            outputZip.write(buffer,0,read);
        }
        while ( read == 5000 );

        outputZip.closeEntry();                 

        input.close();                          
    }

    /**
     * This function adds a Zip File to another Zip
     */
    public static void addZipFileToZip( java.util.zip.ZipEntry src_entry, 
                                        java.util.zip.ZipFile inputZip, 
                                        String dst_name, 
                                        java.util.zip.ZipOutputStream outputZip )       
    throws java.io.IOException
    {
        byte[] buffer = new byte[500];
        long read;                               

        java.util.zip.ZipEntry dst_entry = buildEntry( dst_name );      

        outputZip.putNextEntry( dst_entry );

        java.io.InputStream input = inputZip.getInputStream( src_entry );

        long size = 0;
        long init = src_entry.getSize();
        if ( !src_entry.isDirectory() ) {
            do {
                read = input.read(buffer);      
                outputZip.write(buffer,0,(int)read);
                size += read;
            }
            while ( size != init );
        }

        outputZip.closeEntry();                     

        input.close();                              
    }

    /**
     * Make a Zip copy
     */
    public static void copy( org.openejb.alt.config.ZipHandle src, org.openejb.alt.config.ZipHandle dst, String [] no_copy_list )       
    throws java.io.IOException
    {
        java.util.Enumeration enum =  src.in.entries();

        java.util.zip.ZipEntry entry_src = null;        

        while ( enum.hasMoreElements() ) {
            entry_src = ( java.util.zip.ZipEntry ) enum.nextElement();

            if ( notInList( no_copy_list, entry_src ) ) {
                addZipFileToZip( entry_src, src.in, entry_src.getName(), dst.out );                 
            }
        }               
    }

    /**
     * This function tests if a Zip contains a file specified as parameter
     */
    public static boolean containsFile( org.openejb.alt.config.ZipHandle handle, String file )
    {       
        if ( handle.in.getEntry( file ) != null )
            return true;

        return false;
    }

    /**
     * Extract a file and return its content
     */
    public static byte [] getFileContent( org.openejb.alt.config.ZipHandle handle, String file )
    {
        java.util.zip.ZipEntry entry = handle.in.getEntry( file );

        byte [] content = null;
        try {
            java.io.InputStream input = handle.in.getInputStream( entry );

            content = new byte[ ( int ) entry.getSize() ];

            int b;
            int i = 0;
            while ( ( b = input.read() ) != -1 ) content[i++]=(byte)b; 

            //input.read( content, 0, content.length );

            input.close();
        } catch ( java.io.IOException ex ) {
        }

        return content;
    }

    /**
     * This function tests if a entry is in a list
     */
    private static boolean notInList( String [] list, java.util.zip.ZipEntry entry )
    {
        String name = entry.getName();

        if ( list == null )
            return true;

        for ( int i=0; i<list.length; i++ ) {
            if ( list[i].equals( name ) )
                return false;
        }

        return true;
    }

}
