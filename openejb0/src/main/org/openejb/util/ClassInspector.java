package org.openejb.util;

import java.io.*;
import java.util.*;
import java.util.jar.*;
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
     * EJBs, but may also be in stale code that's never run, etc.).
     *
     * @param jarFileNames  The EJB JAR(s) (at least one is required)
     * @param ejbInterfaces The component + home interfaces to check
     * @param checkCode     If true, the inspector will check all classes referred
     *                      to by loaded classes, in addition to those in the
     *                      field and method signatures, superclasses, and interfaces.
     *                      This is generally recommended.
     * @return              An array of classes that could not be loaded.  This may be
     *                      a problem if those classes need to be loaded at runtime.
     * @throws IOException  Indicates a problem reading a JAR file
     */
    public static String[] createClientJar(String jarFileNames[], String[] ejbInterfaces, boolean checkCode) throws IOException {
        if(jarFileNames == null || jarFileNames.length == 0) {
            throw new IllegalArgumentException("Must specify at least 1 JAR file");
        }
        List warnings = new ArrayList();
        JarTracker tracker = new JarTracker();
        Set set = new HashSet();
        for(int i=0; i<jarFileNames.length; i++) {
            set = openJars(new File(jarFileNames[i]), tracker, set);
        }
        File jarFile = new File(jarFileNames[0]);
        String fileName = jarFile.getName();
        String newName = null;
        int pos = fileName.lastIndexOf('.');
        if(pos > -1) {
            newName = fileName.substring(0, pos) + "-client" + fileName.substring(pos);
        } else {
            newName = fileName + "-client";
        }
        JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(new File(jarFile.getParentFile(), newName))));
        out.setLevel(6);
        Set loaded = new HashSet();
        byte[] buf = new byte[1024];
        byte[] data;
        List list = new LinkedList();
        list.addAll(Arrays.asList(ejbInterfaces));
        JarEntry entry;
        InputStream in, dataIn;
        int count, offset;
        for(ListIterator it = list.listIterator(); it.hasNext();) {
            String s = (String)it.next();
            if(loaded.contains(s)) {
                continue;
            }
            loaded.add(s);
            String classFile = s.replace('.', '/') + ".class";
            entry = tracker.locateFile(classFile);
            if(entry == null) {
                warnings.add(s);
                continue;
            }
            in = tracker.getInputStream(entry);
            data = new byte[(int)entry.getSize()];
            out.putNextEntry(entry);
            offset = 0;
            while((count = in.read(buf)) > -1) {
                System.arraycopy(buf, 0, data, offset, count);
                offset += count;
                out.write(buf, 0, count);
            }
            in.close();
            tracker.done();

            String[] others = getRequiredClasses(dataIn = new ByteArrayInputStream(data), classFile, checkCode);
            dataIn.close();
            for(int i = 0; i < others.length; i++) {
                if(!loaded.contains(others[i])) {
                    it.add(others[i]);
                    it.previous();
                }
            }
        }
        out.close();
        tracker.close();
        return (String[])warnings.toArray(new String[warnings.size()]);
    }

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

    private static class JarTracker {
        private List records = new LinkedList();
        private JarRecord current;

        public void addJar(JarRecord jar) {
            records.add(jar);
        }

        public void done() {
            current = null;
        }

        public InputStream getInputStream(JarEntry entry) throws IOException {
            return current.jar.getInputStream(entry);
        }

        public JarEntry locateFile(String file) {
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

        public void close() throws IOException {
            for(Iterator it = records.iterator(); it.hasNext();) {
                JarRecord jarRecord = (JarRecord)it.next();
                jarRecord.jar.close();
            }
        }
    }
}
