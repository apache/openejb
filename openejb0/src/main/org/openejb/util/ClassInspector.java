package org.openejb.util;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.jar.*;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/**
 * Extracts EJB interfaces and required classes to a client JAR.  This
 * JAR can be used by clients or web applications that depend on the
 * EJB JAR.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class ClassInspector {
    /**
     * Populates a client JAR with all the classes referred to in the specified
     * EJB interfaces.  Any manifest Class-Path entries will be respected.  This
     * methods returns an array of class names that could not be located (these
     * potentially represent classes erroneously left off the CLASSPATH of the
     * EJBs, but may also be in stale code that's never run, etc.).  If multiple
     * JAR files are provided to this method, they will all be processed together,
     * as if they're all parts of one application.  JARs from separate apps
     * should be passed in to separate calls.
     *
     * @param jarFiles      The EJB JAR(s) (at least one is required)
     * @param clientJarFile The name of the client JAR file to write to
     * @param ejbInterfaces The names of the component & home interfaces to check
     * @param checkCode     If true, the inspector will check all classes referred
     *                      to by loaded classes, in addition to those in the
     *                      field and method signatures, superclasses, and interfaces.
     *                      This is generally recommended.
     * @return              An array of classes that could not be loaded.  This may be
     *                      a problem if those classes need to be loaded at runtime.
     * @throws IOException  Indicates a problem reading a JAR file
     */
    public static String[] createClientJar(File jarFiles[], File clientJarFile, String[] ejbInterfaces, boolean checkCode) throws IOException {
        if(jarFiles == null || jarFiles.length == 0) {
            throw new IllegalArgumentException("Must specify at least 1 JAR file");
        }
        JarTracker tracker = new JarTracker();
        Set set = new HashSet();
        for(int i=0; i<jarFiles.length; i++) {
            set = openJars(jarFiles[i], tracker, set);
        }

        return processClasses(clientJarFile, ejbInterfaces, tracker, checkCode);
    }

    /**
     * Populates a client JAR with all the classes referred to in the specified
     * EJB interfaces.  The EJB JAR(s) and any supporting JARs are represented by
     * a single ClassLoader here.  This methods returns an array of class names
     * that could not be located (these potentially represent classes erroneously
     * left off the CLASSPATH of the EJBs, but may also be in stale code that's
     * never run, etc.).
     *
     * @param loader        A ClassLoader with access to all the necessary classes
     * @param clientJarFile The name of the client JAR file to write to
     * @param ejbInterfaces The component + home interfaces to check
     * @param checkCode     If true, the inspector will check all classes referred
     *                      to by loaded classes, in addition to those in the
     *                      field and method signatures, superclasses, and interfaces.
     *                      This is generally recommended.
     * @return              An array of classes that could not be loaded.  This may be
     *                      a problem if those classes need to be loaded at runtime.
     * @throws IOException  Indicates a problem reading a JAR file
     */
    public static String[] createClientJar(ClassLoader loader, File clientJarFile, String[] ejbInterfaces, boolean checkCode) throws IOException {
        CLTracker tracker = new CLTracker(loader);
        return processClasses(clientJarFile, ejbInterfaces, tracker, checkCode);
    }

    /**
     * Does the actual work of extracting classes, given a repository representing
     * one or more JARs and a list of interfaces to process.
     */
    private static String[] processClasses(File newFile, String[] ejbInterfaces, Repository tracker, boolean checkCode) throws IOException {
        List warnings = new ArrayList();
        JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(newFile)));
        out.setLevel(6);
        Set loaded = new HashSet();
        Set seen = new HashSet();
        byte[] buf = new byte[1024];
        List list = new LinkedList();
        list.addAll(Arrays.asList(ejbInterfaces));
        seen.addAll(list);
        InputStream in, dataIn;
        int count;
        Object token;
        for(ListIterator it = list.listIterator(); it.hasNext();) {
            String s = (String)it.next();
            if(loaded.contains(s)) {
                continue;
            }
            loaded.add(s);
            String classFile = s.replace('.', '/') + ".class";
            token = tracker.locateFile(classFile);
            if(token == null) {
                warnings.add(s);
                continue;
            }
            in = tracker.getInputStream(token);
            out.putNextEntry(tracker.getZipEntry(token));
            ByteArrayOutputStream data;
            if(token instanceof ZipEntry) {
                data = new ByteArrayOutputStream((int)((ZipEntry)token).getSize());
            } else {
                data = new ByteArrayOutputStream();
            }
            while((count = in.read(buf)) > -1) {
                out.write(buf, 0, count);
                data.write(buf, 0, count);
            }
            in.close();

            String[] others = getRequiredClasses(dataIn = new ByteArrayInputStream(data.toByteArray()), classFile, checkCode);
            dataIn.close();
            for(int i = 0; i < others.length; i++) {
                if(!seen.contains(others[i])) {
                    it.add(others[i]);
                    seen.add(others[i]);
                    it.previous();
                }
            }
        }
        out.close();
        tracker.close();
        return (String[])warnings.toArray(new String[warnings.size()]);
    }

    /**
     * Given a single JAR, loads it and all the other JARs in its manifest
     * Class-Path (and theirs, etc.) into a single JarTracker.  May be
     * called repeatedly, so long as the set supplied as the third argument
     * is the same.
     */
    private static Set openJars(File jarFile, JarTracker tracker, Set set) throws IOException {
        if(set.contains(jarFile.getAbsolutePath())) {
            return set;
        }
        if(!jarFile.exists() || !jarFile.canRead() || jarFile.isDirectory()) {
            System.err.println(jarFile.getAbsolutePath() + " is not a readable jar file");
            return set;
        }
        set.add(jarFile.getAbsolutePath());
        JarRecord record = new JarRecord();
        record.jarFile = jarFile;
        record.jar = new JarFile(record.jarFile);
        tracker.addJar(record);
        Manifest mf = record.jar.getManifest();
        Attributes att = mf.getMainAttributes();
        String path = att.getValue("Class-Path");
        if(path != null) {
            File base = record.jarFile.getParentFile();
            StringTokenizer tok = new StringTokenizer(path, " ", false);
            while(tok.hasMoreTokens()) {
                String name = tok.nextToken();
                openJars(new File(base, name), tracker, set);
            }
        }
        return set;
    }

    /**
     * Given an InputStream representing a class file, returns the names of all
     * the classes that the specified class refers to.  The last argument
     * controls whether the actual code is examined.
     */
    private static String[] getRequiredClasses(InputStream in, String name, boolean checkCode) {
        Set set = new HashSet();
        try {
            JavaClass cls = new ClassParser(in, name).parse();
            set.addAll(Arrays.asList(cls.getInterfaceNames()));
            set.add(cls.getSuperclassName());
            Field[] fields = cls.getFields();
            for(int i=0; i < fields.length; i++) {
                String type = getType(fields[i].getType());
                if(type != null) {
                    set.add(type);
                }
            }
            Method[] methods = cls.getMethods();
            for(int i = 0; i < methods.length; i++) {
                String type = getType(methods[i].getReturnType());
                if(type != null) {
                    set.add(type);
                }
                ExceptionTable table = methods[i].getExceptionTable();
                if(table != null) {
                    String[] exs = table.getExceptionNames();
                    for(int j = 0; j < exs.length; j++) {
                        String ex = exs[j];
                        set.add(ex);
                    }
                }
            }
            if(checkCode) {
                ConstantPool cp = cls.getConstantPool();
                Constant[] cs = cp.getConstantPool();
                for(int i = 0; i < cs.length; i++) {
                    Constant c = cs[i];
                    if(c instanceof ConstantClass) {
                        String s = (String)((ConstantClass)c).getConstantValue(cp);
                        s = s.replace('/', '.');
                        while(s.startsWith("[")) {
                            if(s.startsWith("[L")) {
                                s = s.substring(2);
                            } else {
                                s = s.substring(1);
                            }
                        }
                        while(s.endsWith(";")) {
                            s = s.substring(0, s.length()-1);
                        }
                        if(s.length() == 1) {
                            continue; //todo: limit to real 1-character primitive codes
                        }
                        if(!set.contains(s)) {
                            set.add(s);
                        }
                    }
                }
            }
            for(Iterator iterator = set.iterator(); iterator.hasNext();) {
                String s = (String)iterator.next();
                try {
                    Class.forName(s);
                    iterator.remove();
                } catch(ClassNotFoundException e) {}
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassFormatException e) {
            e.printStackTrace();
        }
        return (String[])set.toArray(new String[set.size()]);
    }

    private static String getType(Type type) {
        if(type instanceof ArrayType) {
            return getType(((ArrayType)type).getBasicType());
        }
        if(type instanceof BasicType) {
            return null;
        }
        return type.toString();
    }

    private static class JarRecord {
        public File jarFile;
        public JarFile jar;
    }

    private static interface Repository {
        public Object locateFile(String file);
        public InputStream getInputStream(Object token) throws IOException;
        public void close() throws IOException;
        public ZipEntry getZipEntry(Object token);
    }

    private static class CLToken {
        public URL url;
        public String file;

        public CLToken(String file, URL url) {
            this.file = file;
            this.url = url;
        }
    }

    private static class CLTracker implements Repository {
        private ClassLoader loader;

        public CLTracker(ClassLoader loader) {
            this.loader = loader;
        }

        public void close() throws IOException {
        }

        public InputStream getInputStream(Object token) throws IOException {
            return ((CLToken)token).url.openStream();
        }

        public ZipEntry getZipEntry(Object token) {
            CLToken tok = (CLToken) token;
            ZipEntry entry = new ZipEntry(tok.file);
            entry.setMethod(ZipEntry.DEFLATED);
            return entry;
        }

        public Object locateFile(String file) {
            CLToken tok = new CLToken(file, loader.getResource(file));
            return tok.url == null ? null : tok;
        }
    }

    private static class JarTracker implements Repository {
        private List records = new LinkedList();
        private JarRecord current;

        public void addJar(JarRecord jar) {
            records.add(jar);
        }

        public InputStream getInputStream(Object token) throws IOException {
            return current.jar.getInputStream((JarEntry)token);
        }

        public Object locateFile(String file) {
            current = null;
            for(Iterator it = records.iterator(); it.hasNext();) {
                JarRecord rec = (JarRecord)it.next();
                JarEntry entry = rec.jar.getJarEntry(file);
                if(entry != null) {
                    current = rec;
                    return entry;
                }
            }
            return null;
        }

        public ZipEntry getZipEntry(Object token) {
            return (JarEntry)token;
        }

        public void close() throws IOException {
            for(Iterator it = records.iterator(); it.hasNext();) {
                JarRecord jarRecord = (JarRecord)it.next();
                jarRecord.jar.close();
            }
        }
    }
}
