/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.openejb.naming;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import com.sun.naming.internal.ResourceManager;

/*
* This class wrappers a specific NameNode which is the data model for the JNDI
* name space. This class provides javax.naming.Context specific functionality
* to the NameNode so that it can be used by beans the JNDI ENC.
*
*/
public class IvmContext implements Context, java.io.Serializable{
    Hashtable myEnv;
    boolean readOnly = false;
    HashMap fastCache = new HashMap();
    public NameNode mynode;

    /**
     * Creates and returns a IvmContext object that is a root context.
     */
    public static IvmContext createRootContext() {
	//return new IvmContext(new NameNode(null,new NameTokenizer("/"),null));
        return new IvmContext("/");
    }

    public IvmContext(String name){
        this(new NameNode(null, new NameTokenizer(name), null));
    }
    public IvmContext(){
        this("root");
        //this(new NameNode(null, new NameTokenizer("root"), null));
    }

    public IvmContext(NameNode node){
        mynode = node;
    }
    public IvmContext(Hashtable environment) throws NamingException{
        this();
        if(environment ==null)
            throw new NamingException("Invalid Argument");
        else
            myEnv = (Hashtable)environment.clone();

    }
    public Object lookup(String compositName) throws NamingException {
        if (compositName.equals("")) {
            return this;
        }

        // Special case for UserTransaction
        // This is to give transaction support for non-bean ejb clients using the IntraVm Server
//        if ( compositName.equals("java:comp/UserTransaction") && ThreadContext.getThreadContext().getDeploymentInfo() == null ) {
//            EJBUserTransaction userTx = new EJBUserTransaction();
//            userTx.setUp(OpenEJB.getTransactionManager(), null);
//            userTx.setOnline(true);
//            return userTx;
//        }


        String compoundName = null;
        int indx = compositName.indexOf(":");
        if(indx>-1){
            /*
             The ':' character will be in the path if its an absolute path name starting with the schema
             'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            compoundName = compositName.substring(indx+1);
        }else{
            /*
              the resolve method always starts with the comparison assuming that the first
              component of the name is a context of a peer node or the same node, so we have
              to prepend the current context name to the relative lookup path.
            */
	    compoundName = mynode.atomicName+'/'+compositName;
        }

        /*
           If the object has been resolved in the past from this context and the specified path (name)
           it will be in the fastCache which is significantly faster then peruse the Node graph.
           80 ms compared to 300 ms for a full node path search.
        */
        Object obj = fastCache.get(compoundName);
        if(obj==null){
            // this method will transverse the node graph to locate the bound context or object
            try{
            obj = mynode.resolve(new NameTokenizer(compoundName));
            }catch(NameNotFoundException nnfe){
                obj = federate(compositName);
            }
            // cache the lookup path and object for faster resolution next time
            fastCache.put(compoundName,obj);
        }
        if(obj.getClass() == IvmContext.class)
            ((IvmContext)obj).myEnv = myEnv;
        else if(obj instanceof Reference){
            /*
             EJB references and resource references are wrapped in special
             org.apache.openejb.naming.Reference types that check to
             see if the current operation is allowed access to the entry (See EJB 1.1/2.0 Allowed Operations)
             If the operation is not allowed, a javax.naming.NameNotFoundException is thrown.

             A Reference type can also carry out dynamic resolution of references if necessary.
            */
            obj = ((Reference)obj).getObject();
        }
        return obj;
    }
    protected Object federate(String compositName)throws NamingException{
        ObjectFactory factories [] = getFederatedFactories();
        for(int i =0; i < factories.length; i++){
            try{
            javax.naming.CompositeName name = new javax.naming.CompositeName(compositName);
            Object obj = factories[i].getObjectInstance(null, name, null,null);

            if(obj instanceof Context)
                return ((Context)obj).lookup(compositName);
            else if(obj!=null)
                return obj;
            }catch(Exception nnfe){
                // do nothing; this is expected
            }
        }
        // if a return never happened then the federated factories didn't contain
        //   anything bound to the name so the exception is thrown.
        throw new javax.naming.NameNotFoundException("Name \""+compositName+"\" not found.");
    }

    static ObjectFactory [] federatedFactories = null;

    public static ObjectFactory [] getFederatedFactories( ) throws NamingException{
        if(federatedFactories == null){
            Set factories = new HashSet();
            Hashtable jndiProps = ResourceManager.getInitialEnvironment(null);
            String pkgs = (String)jndiProps.get(Context.URL_PKG_PREFIXES);
	    if( pkgs == null) {
		return new ObjectFactory[0];
	    }
            StringTokenizer parser = new StringTokenizer(pkgs, ":");

            while (parser.hasMoreTokens()) {
                String className = parser.nextToken() + ".java.javaURLContextFactory";
                if(className.equals("org.apache.openejb.naming.java.javaURLContextFactory"))
                    continue;
                try {
                    ClassLoader cl = org.apache.openejb.util.ClasspathUtils.getContextClassLoader();
                    Class factoryClass = Class.forName(className, true, cl);
                    ObjectFactory factoryInstance = (ObjectFactory)factoryClass.newInstance();
                    factories.add(factoryInstance);
                }catch (ClassNotFoundException cnfe){
                    //do nothing; this is expected
                }catch (Throwable e) {
                    NamingException ne =
                    new NamingException("Federation failed: Cannot instantiate " + className);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            Object [] temp = factories.toArray();
            federatedFactories = new ObjectFactory[temp.length];
            System.arraycopy(temp, 0, federatedFactories, 0, federatedFactories.length);
        }
        return federatedFactories;
    }


    public Object lookup(Name compositName) throws NamingException {
        return lookup(compositName.toString());
    }
    /**
     * WARNING: this function does not provide support for relative names.
     */
    public void bind(String name, Object obj) throws NamingException {
        checkReadOnly();
        int indx = name.indexOf(":");
        if(indx>-1){
            /*
             The ':' character will be in the path if its an absolute path name starting with the schema
             'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx+1);
        }
        if(fastCache.containsKey(name))
            throw new javax.naming.NameAlreadyBoundException();
        else
            mynode.bind(new NameTokenizer(name), obj);
    }
    /**
     * WARNING: this function does not provide support for relative names.
     */
    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }
    public void rebind(String name, Object obj) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }
    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }
    public void unbind(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }
    public void rename(String oldname, String newname)
    throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }
    public void rename(Name oldname, Name newname)
    throws NamingException {
        rename(oldname.toString(), newname.toString());
    }
    public NamingEnumeration list(String name)
    throws NamingException {
        Object obj = lookup(name);
        if(obj.getClass() == IvmContext.class)
            return new MyListEnumeration(((IvmContext)obj).mynode);
        else {
            return null; // should be simple enum of one element
        }
    }
    public NamingEnumeration list(Name name)
    throws NamingException {
        return list(name.toString());
    }
    public NamingEnumeration listBindings(String name)
    throws NamingException {
        Object obj = lookup(name);
        if(obj.getClass() == IvmContext.class)
            return new MyListEnumeration(((IvmContext)obj).mynode);
        else {
            return null; // should be simple enum of one element
        }
    }
    public NamingEnumeration listBindings(Name name)
    throws NamingException {
        return listBindings(name.toString());
    }
    public void destroySubcontext(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }
    /**
     * WARNING: this function does not provide support for relative names.
     */
    public Context createSubcontext(String name) throws NamingException {
        checkReadOnly();
        int indx = name.indexOf(":");
        if(indx>-1){
            /*
	      The ':' character will be in the path if its an absolute path name starting with the schema
	      'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx+1);
        }
        if(fastCache.containsKey(name))
            throw new javax.naming.NameAlreadyBoundException();
        else
            return mynode.createSubcontext(new NameTokenizer(name));
    }
    /**
     * WARNING: this function does not provide support for relative names.
     */
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }
    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }
    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }
    public NameParser getNameParser(String name)
    throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }
    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }
    public String composeName(String name, String prefix)
    throws NamingException {
        Name result = composeName(new CompositeName(name),
        new CompositeName(prefix));
        return result.toString();
    }
    public Name composeName(Name name, Name prefix)
    throws NamingException {
        Name result = (Name)(prefix.clone());
        result.addAll(name);
        return result;
    }
    public Object addToEnvironment(String propName, Object propVal)
    throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable(5, 0.75f);
        }
        return myEnv.put(propName, propVal);
    }
    public Object removeFromEnvironment(String propName)
    throws NamingException {
        if (myEnv == null)
            return null;
        return myEnv.remove(propName);
    }
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            // Must return non-null
            return new Hashtable(3, 0.75f);
        } else {
            return (Hashtable)myEnv.clone();
        }
    }
    public String getNameInNamespace() throws NamingException {
        return "";
    }
    public void close() throws NamingException {
    }

    protected void checkReadOnly( )throws javax.naming.OperationNotSupportedException{
        if(readOnly)throw new javax.naming.OperationNotSupportedException();
    }

    // NamingEnumeration implemenations

    protected class MyBindingEnumeration extends MyNamingEnumeration {
         public MyBindingEnumeration(NameNode parentNode){
            super(parentNode);
         }
         protected void buildEnumeration(Vector vect){
            for(int i = 0; i < vect.size(); i++){
                NameNode node = (NameNode)vect.elementAt(i);
                String className = node.getBinding().getClass().getName();
                vect.setElementAt(new Binding(node.atomicName,className, node.getBinding()), i);
            }
            myEnum = vect.elements();
         }

    }

    protected class MyListEnumeration extends MyNamingEnumeration {
         public MyListEnumeration(NameNode parentNode){
            super(parentNode);
         }
         protected void buildEnumeration(Vector vect){
            for(int i = 0; i < vect.size(); i++){
                NameNode node = (NameNode)vect.elementAt(i);
                String className = node.getBinding().getClass().getName();
                vect.setElementAt(new NameClassPair(node.atomicName,className), i);
            }
            myEnum = vect.elements();
         }

    }
    protected abstract class MyNamingEnumeration implements javax.naming.NamingEnumeration {
         Enumeration myEnum;

         public MyNamingEnumeration(NameNode parentNode){
            Vector vect = new Vector();
          //System.out.println("[] node "+ parentNode);
          //System.out.println("[] node.atatomicName "+ parentNode.atomicName);
          //System.out.println("[] node.atomicHash   "+ parentNode.atomicHash);
          //System.out.println("[] node.grtrTree     "+ parentNode.grtrTree);
          //System.out.println("[] node.lessTree     "+ parentNode.lessTree);
          //System.out.println("[] node.myContext    "+ parentNode.myContext);
          //System.out.println("[] node.myObject     "+ parentNode.myObject);
          //System.out.println("[] node.parent       "+ parentNode.parent);
          //System.out.println("[] node.subTree      "+ parentNode.subTree);
            NameNode node = parentNode.subTree;

            //<DMB> Not sure about this code
            if ( node == null ) {
                node = parentNode;
            } else {
                vect.addElement(node);
            }
            //</DMB> Not sure about this code

            gatherNodes(node,vect);

            buildEnumeration(vect);
         }
         abstract protected void buildEnumeration(Vector vect);

         protected void gatherNodes(NameNode node, Vector vect){
            if(node.lessTree!=null){
                vect.addElement(node.lessTree);
                gatherNodes(node.lessTree,vect);
            }
            if(node.grtrTree!=null){
                vect.addElement(node.grtrTree);
                gatherNodes(node.grtrTree,vect);
            }
         }

         public void close(){
            myEnum = null;
         }

         public boolean hasMore() {
            return hasMoreElements();
         }
         public Object next() {
            return nextElement();
         }
         public boolean hasMoreElements(){
            return myEnum.hasMoreElements();
         }
         public Object nextElement() {
            return myEnum.nextElement();
         }
    }

    static class NameNode implements java.io.Serializable {
        public String atomicName;
        public int atomicHash;
        public NameNode lessTree;
        public NameNode grtrTree;
        public NameNode subTree;
        public NameNode parent;
        public Object myObject;
        public transient IvmContext myContext;

        public NameNode(NameNode parent, NameTokenizer name,  Object obj){
            atomicName = name.getComponent();
            atomicHash = name.getComponentHashCode();
            this.parent = parent;
            if(name.next())
                subTree = new NameNode(this, name, obj);
            else
                myObject = obj;
        }
        public Object getBinding(){
            if(myObject != null)
                return myObject;// if NameNode has an object it must be a binding
            else{
                if(myContext == null)
                    myContext = new IvmContext(this);
                return myContext;
            }
        }
        public Object resolve(NameTokenizer name)throws javax.naming.NameNotFoundException{
            int compareResult = name.compareTo(atomicHash);

            if(compareResult == NameTokenizer.IS_EQUAL && name.getComponent().equals(atomicName)){// hashcodes and String valuse are equal
                if(name.next()){
                    if(subTree == null) throw new NameNotFoundException("Can not resolve "+name);
                    return subTree.resolve(name);
                }else
                    return getBinding();
            }else if(compareResult == NameTokenizer.IS_LESS){// parsed hash is less than
                if(lessTree == null) throw new NameNotFoundException("Can not resolve "+name);
                return lessTree.resolve(name);

            }else{//NameTokenizer.IS_GREATER
                //...or NameTokenizer.IS_EQUAL but components are not the same string (hash code collision)
                if(grtrTree == null) throw new NameNotFoundException("Can not resolve "+name);
                return grtrTree.resolve(name);
            }
        }

        public void bind(NameTokenizer name, Object obj) throws NameAlreadyBoundException {
            int compareResult = name.compareTo(atomicHash);
            if(compareResult == NameTokenizer.IS_EQUAL && name.getComponent().equals(atomicName)){
                if(name.next()){
                    if( myObject != null) {
                        throw new NameAlreadyBoundException();
                    }
                    if(subTree==null)
                        subTree = new NameNode(this, name,obj);
                    else
                        subTree.bind(name,obj);
                }
                else {
                    if( subTree != null) {
                        throw new NameAlreadyBoundException();
                    }
                    myObject = obj;// bind the object to this node
                }
            }else if(compareResult == NameTokenizer.IS_LESS){
                if(lessTree == null)
                    lessTree = new NameNode(this.parent, name, obj);
                else
                    lessTree.bind(name,obj);
            }else{//NameTokenizer.IS_GREATER ...
                //...or NameTokenizer.IS_EQUAL but components are not the same string (hash code collision)
                if(grtrTree == null)
                    grtrTree = new NameNode(this.parent, name, obj);
                else
                    grtrTree.bind(name,obj);
            }
        }

        public IvmContext createSubcontext( NameTokenizer name) throws NameAlreadyBoundException {
            try {
                bind( name, null);
                name.reset();
                return (IvmContext)resolve( name);
            }
            catch( NameNotFoundException exception) {
                exception.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    static class NameTokenizer implements java.io.Serializable {
        final static int IS_EQUAL = 0;
        final static int IS_LESS = -1;
        final static int IS_GREATER = 1;

        String[] components;
        int pos = 0;
        int hashcode;

        public NameTokenizer(String path) {
            path = normalize(path);
            //System.out.println("[] path "+path);
            if (path == null || path.equals("/")) {
                // A blank string is a legal name and refers to the current/root
                // context.
                components = new String[1];
                components[0] = "";
                hashcode = 0;
            } else if (path.length() > 0) {
                java.util.StringTokenizer st =
                new java.util.StringTokenizer(path, "/");
                components = new String[st.countTokens()];
                for (int i = 0; st.hasMoreTokens() && i < components.length; i++)
                    components[i] = st.nextToken();
                hashcode = components[0].hashCode();
            } else {
                // A blank string is a legal name and refers to the current/root
                // context.
                components = new String[1];
                components[0] = "";
                hashcode = 0;
            }
        }

        public String getComponent() {
            return components[pos];
        }
        public boolean next() {
            if (components.length > pos + 1) {
                hashcode = components[++pos].hashCode();
                return true;
            } else {
                return false; // maintain position
            }
        }
        public void reset() {
            pos = 0;
            hashcode = components[0].hashCode();
        }
        public int compareTo(int otherHash) {
            if (hashcode == otherHash)
                return 0;
            else if (hashcode > otherHash)
                return 1;
            else
                return -1;
        }
        public int getComponentHashCode() {
            return hashcode;
        }
        public int compareTo(String other) {
            int otherHash = other.hashCode();
            return compareTo(otherHash);
        }
        public String toString() {
            if (components.length == 0) {
                return "";
            }
            StringBuffer buffer = new StringBuffer(components[0]);
            for (int i = 1; i < components.length; ++i) {
                buffer.append('/');
                buffer.append(components[i]);
            }
            return buffer.toString();
        }

        /*
         * A normal Unix pathname contains no duplicate slashes and does not end
         */

        /*
         * Normalize the given pathname, whose length is len, starting at the given
         */
        private String normalize(String pathname, int len, int off) {
            if (len == 0)
                return pathname;
            int n = len;
            while ((n > 0) && (pathname.charAt(n - 1) == '/'))
                n--;
            if (n == 0)
                return "/";
            StringBuffer sb = new StringBuffer(pathname.length());
            if (off > 0)
                sb.append(pathname.substring(0, off));
            char prevChar = 0;
            for (int i = off; i < n; i++) {
                char c = pathname.charAt(i);
                if ((prevChar == '/') && (c == '/'))
                    continue;
                sb.append(c);
                prevChar = c;
            }
            return sb.toString();
        }

        /*
         * Check that the given pathname is normal. If not, invoke the real
         * normalizer on the part of the pathname that requires normalization.
         */
        private String normalize(String pathname) {
            int n = pathname.length();
            char prevChar = 0;
            for (int i = 0; i < n; i++) {
                char c = pathname.charAt(i);
                if ((prevChar == '/') && (c == '/'))
                    return normalize(pathname, n, i - 1);
                prevChar = c;
            }
            if (prevChar == '/')
                return normalize(pathname, n, n - 1);
            return pathname;
        }
    }
}
