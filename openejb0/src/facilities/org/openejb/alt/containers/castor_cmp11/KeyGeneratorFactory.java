package org.openejb.alt.containers.castor_cmp11;

import org.openejb.DeploymentInfo;
import org.exolab.castor.persist.spi.Complex;
import java.io.*;
import java.util.Properties;
import java.util.Vector;
import org.openejb.util.proxy.ProxyClassLoader;
import org.openejb.util.FileUtils;

public abstract class KeyGeneratorFactory {

    public final static ProxyClassLoader loader = new ProxyClassLoader();

    private static boolean DELETE_DEFINITIONS         = false;
    private static boolean CREATE_PACKAGE_DIRECTORIES = true;
    private static File    KEY_OUTPUT_DIRECTORY       = null;
    private static String  defaultDirectory           = "keys";

    protected static void setKeyOutputDirectory(String path) throws IOException{
        if ( path == null ) {
            path = defaultDirectory;
        }

        try{
            KEY_OUTPUT_DIRECTORY = FileUtils.getDirectory(path);
        }catch(java.io.IOException e) {
            throw new IOException(KeyGeneratorFactory.class.getName()+".init(): can't use directory for generated keys "+path+" : "+e);
        }
    }

    public static KeyGenerator createKeyGenerator(DeploymentInfo di)
    throws java.lang.InstantiationException, java.lang.IllegalAccessException{

        StringBuffer source = new StringBuffer();

        appendPackage(di, source);
        appendImports(di, source);
        appendClassDeclaration(di, source);
        source.append('{');
        appendMethod_getPrimaryKey(di,source);
        appendMethod_getJdoComplex(di, source);
        appendMethod_isKeyComplex(di, source);
        newLine(source);
        newLine(source);
        source.append('}');

        //System.out.println(source);
        //String className = "org.openejb.alt.containers.castor_cmp11."+getClassName(di);
        String className = getClassName(di);
        return(KeyGenerator)getKeyGeneratorClass(source.toString(), className).newInstance();

    }
    protected static void appendPackage(DeploymentInfo di, StringBuffer source) {
        //newLine(source);
        //source.append("package org.openejb.alt.containers.castor_cmp11;");
    }
    protected static void appendImports(DeploymentInfo di, StringBuffer source) {
        newLine(source);
        newLine(source);
        source.append("import org.exolab.castor.persist.spi.Complex;");
        newLine(source);
        source.append("import javax.ejb.EntityBean;");
        newLine(source);
        newLine(source);
        // Add all beans imports (necessary when no package is specified for the classes
        source.append("import ").append(di.getBeanClass().getName()).append(';');
        newLine(source);
        source.append("import ").append(di.getPrimaryKeyClass().getName()).append(';');
        newLine(source);
        newLine(source);

    }
    protected static void appendClassDeclaration(DeploymentInfo di, StringBuffer source) {
        newLine(source);
        newLine(source);
        source.append("public class ");
        source.append(getClassName(di));
        source.append(" implements org.openejb.alt.containers.castor_cmp11.KeyGenerator ");
    }

    static final char[] illegalClassNameCharacters = {
        '`', '~', '!', '@', '#', '%', '^', '&',
        '*', '(', ')', '-', '+', '=', '[', ']',
        '{', '}', ' ', '|', ';', ':', '<', '>',
        ',', '?', '/', '"', '\\', '\'','.'
    };
    protected static String getClassName(DeploymentInfo di) {
        String name = ""+di.getDeploymentID();
        for ( int i=0; i < illegalClassNameCharacters.length; i++ ) {
            name = name.replace(illegalClassNameCharacters[i], '_');
        }
        return "KeyGenerator_"+name;   
    }

    protected static void appendMethod_getPrimaryKey(DeploymentInfo di, StringBuffer source) {
        newLine(source);
        newLine(source);


        tab(source, 1);

        //  public Object getPrimaryKey(EntityBean bean) {
        source.append("public java.lang.Object getPrimaryKey(javax.ejb.EntityBean bean){");

        //      CustomerBean entityBean = (CustomerBean)bean;
        newLine(source); tab(source, 2);
        source.append(di.getBeanClass().getName());
        source.append(" entityBean = (");
        source.append(di.getBeanClass().getName());
        source.append(")bean;");

        if ( di.getPrimaryKeyField()==null ) {// key is complex

            //      CustomerPK primaryKey = new CustomerPK();
            newLine(source); tab(source, 2);
            source.append(di.getPrimaryKeyClass().getName());
            source.append(" primaryKey = new ");
            source.append(di.getPrimaryKeyClass().getName());
            source.append("();");


            //      primaryKey.id = entityBean.id;
            java.lang.reflect.Field[] keyFields  = di.getPrimaryKeyClass().getFields();
            java.lang.reflect.Field[] beanFields = di.getBeanClass().getFields();

            for ( int i = 0; i < keyFields.length; i++ ) {

                java.lang.reflect.Field keyField = keyFields[i];

                for ( int x = 0; x < beanFields.length; x++ ) {
                    java.lang.reflect.Field beanField = beanFields[x];

                    if ( keyField.getClass()== beanField.getClass() && keyField.getName().equals(beanField.getName()) ) {

                        newLine(source); tab(source, 2);
                        source.append("primaryKey.");
                        source.append(keyField.getName());
                        source.append(" = entityBean.");
                        source.append(beanField.getName());
                        source.append(';');
                        break;

                    }

                }

            }
            // return primaryKey;
            newLine(source);tab(source,2);
            source.append("return primaryKey;");

        } else {// simple primary key
            java.lang.reflect.Field field = di.getPrimaryKeyField();
            Class fieldType = field.getType();

            newLine(source); tab(source,2);

            if ( fieldType.isPrimitive() ) {
                // return new java.lang.Integer(entityBean.id);
                source.append("return new ");

                // java.lang.Integer
                appendPrimitiveWrapperName(source,fieldType);

                source.append("(entityBean.");
                source.append(field.getName());
                source.append(");");
            } else if ( fieldType == String.class || 
                        java.lang.Number.class.isAssignableFrom(fieldType) || 
                        fieldType == java.lang.Boolean.class ) {
                // return entityBean.id;
                source.append("return entityBean.");
                source.append(field.getName());
                source.append(';');
            } else {
                // return entityBean.id.clone();
                newLine(source); tab(source,2);
                source.append("return entityBean.");
                source.append(field.getName());
                source.append(".clone();");
            }

        }

        //  }
        newLine(source);tab(source,1);
        source.append('}');
    }
    protected static void appendPrimitiveWrapperName(StringBuffer source, Class fieldType) {
        if ( fieldType == Integer.TYPE ) {
            source.append("java.lang.Integer");                    
        } else if ( fieldType == Long.TYPE ) {
            source.append("java.lang.Long");  
        } else if ( fieldType == Double.TYPE ) {
            source.append("java.lang.Double"); 
        } else if ( fieldType == Float.TYPE ) {
            source.append("java.lang.Float");
        } else if ( fieldType == Short.TYPE ) {
            source.append("java.lang.Short");
        } else if ( fieldType == Byte.TYPE ) {
            source.append("java.lang.Byte");
        } else if ( fieldType == Character.TYPE ) {
            source.append("java.lang.Character");
        } else if ( fieldType == Boolean.TYPE ) {
            source.append("java.lang.Boolean");
        }
    }
    protected static void appendMethod_getJdoComplex(DeploymentInfo di, StringBuffer source) {
        newLine(source);
        newLine(source);tab(source,1);

        // public Complex getJdoComplex(Object primaryKey){
        source.append("public org.exolab.castor.persist.spi.Complex getJdoComplex(java.lang.Object primaryKey){");

        if ( di.getPrimaryKeyField()==null ) {// key is complex

            //      CustomerPK key = (CustomerPK)primaryKey;
            newLine(source); tab(source,2);
            source.append(di.getPrimaryKeyClass().getName());
            source.append(" key = (");
            source.append(di.getPrimaryKeyClass().getName());
            source.append(")primaryKey;");

            //      Object args = new Object[2];
            newLine(source); tab(source, 2);

            // only fields that are shared between the key and bean are considered primary key fields
            java.lang.reflect.Field [] keyFields = di.getPrimaryKeyClass().getFields();
            java.lang.reflect.Field [] beanFields = di.getBeanClass().getFields();
            java.util.Vector commonFields = new java.util.Vector();

            for ( int i = 0; i < keyFields.length; i++ ) {

                java.lang.reflect.Field keyField = keyFields[i];
                for ( int x = 0; x < beanFields.length; x++ ) {
                    java.lang.reflect.Field beanField = beanFields[x];
                    if ( keyField.getClass()== beanField.getClass() && keyField.getName().equals(beanField.getName()) ) {
                        commonFields.add(keyField);
                    }
                }
            }

            source.append("java.lang.Object [] args = new java.lang.Object[");
            source.append(commonFields.size());
            source.append("];");

            //      args[1] = new java.util.Integer(key.id);  
            //      args[2] = key.date;
            for ( int i = 0; i < commonFields.size(); i++ ) {
                newLine(source); tab(source,2);
                source.append("args[");
                source.append(i);
                source.append("] = ");
                boolean wrappered = applyWrapper(source, (java.lang.reflect.Field)commonFields.elementAt(i));
                source.append("key.").append(((java.lang.reflect.Field)commonFields.elementAt(i)).getName());
                if ( wrappered )source.append(')');
                source.append(';');
            }

            // return new Complex(args.length, args);
            newLine(source); tab(source, 2);
            source.append("return new org.exolab.castor.persist.spi.Complex(args.length, args);");
        } else {
            // make complex for simply key
            newLine(source); tab(source, 2);
            source.append("return new org.exolab.castor.persist.spi.Complex(primaryKey);");

        }

        //  }
        newLine(source);tab(source,1);
        source.append('}');

    }
    protected static boolean applyWrapper(StringBuffer source, java.lang.reflect.Field field) {
        if ( field.getType().isPrimitive() ) {
            source.append("new ");
            appendPrimitiveWrapperName(source,field.getType());
            source.append('(');
            return true;
        } else
            return false;

    }
    protected static void appendMethod_isKeyComplex(DeploymentInfo di, StringBuffer source) {
        // public boolean isKeyComplex( ){
        newLine(source);
        newLine(source); tab(source,1);

        source.append("public boolean isKeyComplex( ){");

        // return true; return false;
        newLine(source); tab(source,2);
        source.append("return ");
        source.append((di.getPrimaryKeyField()==null?true:false));
        source.append(';');

        //  }
        newLine(source);tab(source,1);
        source.append('}');
    }/*
    protected static boolean isPrimaryKeyComplex(DeploymentInfo di){
        return (di.getPrimaryKeyField()==null)? true:false;
    }*/
    protected static void newLine(StringBuffer source) {
        source.append('\n');
    }

    protected static void tab(StringBuffer source, int count) {
        for ( int i = 0; i < count; i++ ) {
            source.append('\t');
        }
    }

    protected static Class getKeyGeneratorClass(String source, String className) {
        try {
            return loader.defineClass( className , generateKeyByteCode( source, className ) );
        } catch ( ClassFormatError cfe ) {
            //cfe.printStackTrace();
            throw new IllegalArgumentException(cfe.getMessage());
        } catch ( IllegalAccessException iae ) {
            //iae.printStackTrace();
            throw new IllegalArgumentException(iae.getMessage());
        }
    }
    protected static byte[] generateKeyByteCode(String source, String className) throws IllegalAccessException {
        byte[] byteCode = null;
        // write source code to file
        try {
            File classFile = compileSourceCode(source, className);

            //=====================================
            // Load the .class and get it's bytes

            FileInputStream fis = new FileInputStream(classFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream( (int)classFile.length() );

            int b;
            while ( true ) {
                b = fis.read();
                if ( b==-1 ) break;
                baos.write(b);
            }

            byteCode = baos.toByteArray();

            //============================
            // Clean up before returning
            fis.close();
            baos.close();
            if ( DELETE_DEFINITIONS ) classFile.delete();

        } catch ( SecurityException se ) {
            throw new IllegalAccessException("Cant compile. SecurityManager restriction: "+se.getMessage());
        } catch ( IOException io ) {
            //io.printStackTrace();
            throw new IllegalAccessException("Cant write generated key: "+io.getMessage());
        } catch ( InstantiationException ie ) {
            throw new IllegalAccessException("Cant instatiate compiler: "+ie.getMessage());
        }
        return byteCode;
    }

    protected static File compileSourceCode(String sourceCode, String className) throws IllegalAccessException, InstantiationException {
        File classFile = null;
        File outputDir = null;
        File javaFile  = null;
        File keyDir    = KEY_OUTPUT_DIRECTORY;
        
        try {

            //Not supported at the moment.
            //if ( CREATE_PACKAGE_DIRECTORIES ) {
            if ( false ) {
                //System.out.println(className);
                String packageName = parsePackageName(className);
                //System.out.println(packageName);
                outputDir = new File(keyDir, packageName.replace('.', File.separatorChar));
            } else {
                String packageName = parsePackageName(className);
                outputDir = keyDir;
            }

            String partialClassName = parsePartialClassName( className );
            outputDir.mkdirs();
            javaFile  = new File(outputDir, partialClassName + ".java");
            classFile = new File(outputDir, partialClassName + ".class");

            //=======================
            // Write source to file
            try {
                FileOutputStream fos = new FileOutputStream( javaFile );
                fos.write(sourceCode.toString().getBytes());
                fos.flush();
                fos.close();
            } catch ( IOException io ) {
                throw new IllegalAccessException("Can't write generated key source code to file:\n" + io.getMessage());          
            }

            //======================
            // Compile source file
            Vector cargs = new Vector();
            //cargs.addElement("-d");
            //cargs.addElement(getSourcePath());
            cargs.addElement("-classpath");
            cargs.addElement( keyDir.getAbsolutePath() + File.pathSeparator + System.getProperty("java.class.path"));
            // cargs.addElement("-g");  debug off by default
            cargs.addElement("-O"); //optimize
            cargs.addElement(""+javaFile.getAbsoluteFile());

            String[] args = new String[cargs.size()];
            cargs.copyInto(args);

	    org.openejb.util.compiler.Compiler compiler = org.openejb.util.compiler.CompilerFactory.newCompilerInstance();
            compiler.compile(args);

            //=====================
            // Delete source file
            if ( DELETE_DEFINITIONS ) javaFile.delete();

        } catch ( SecurityException se ) {
            throw new IllegalAccessException("SecurityManager restriction. Can't compile "+classFile.getAbsoluteFile());
        } catch ( NoClassDefFoundError no ) {
            System.err.println("The java compiler class from JAVA_HOME/lib/tools.jar is missing. Please add it to the classpath.");
            throw no;
        }
        return classFile;
    }

    protected static String parsePartialClassName(String className) {
        if ( className.indexOf('.') < 1 ) return className;
        return className.substring( className.lastIndexOf('.')+1 );
    }

    protected static String parsePackageName(String className) {
        if ( className.indexOf('.') < 1 ) return null;
        return className.substring( 0, className.lastIndexOf('.') );
    }


}

