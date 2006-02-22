/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.openejb.core.ivm.naming;


import java.io.ObjectStreamException;
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
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.openejb.core.ThreadContext;
import org.openejb.OpenEJB;

import com.sun.naming.internal.ResourceManager;

/*
* This class wrappers a specific NameNode which is the data model for the JNDI
* name space. This class provides javax.naming.Context specific functionality
* to the NameNode so that it can be used by beans the JNDI ENC.
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
	return new IvmContext(new NameNode(null,new ParsedName("/"),null));
    }

    public IvmContext(){
        this(new NameNode(null, new ParsedName("root"), null));
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
        if ( compositName.equals("java:comp/UserTransaction") && ThreadContext.getThreadContext().getDeploymentInfo() == null ) {
            return new org.openejb.core.CoreUserTransaction();
        }


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
            obj = mynode.resolve(new ParsedName(compoundName));
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
             org.openejb.core.ivm.naming.Reference types that check to
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
                if(className.equals("org.openejb.core.ivm.naming.java.javaURLContextFactory"))
                    continue;
                try {
                    ClassLoader cl = OpenEJB.getContextClassLoader();
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
            mynode.bind(new ParsedName(name), obj);
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
            return null; // should be simple enumeration of one element
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
            return null; // should be simple enumeration of one element
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
            return mynode.createSubcontext(new ParsedName(name));
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
    
    protected Object writeReplace() throws ObjectStreamException{
        if(org.openejb.core.ivm.IntraVmCopyMonitor.isStatefulPassivationOperation()){
            // if the handle is referenced by a stateful bean that is being passivated by the container, we allow this object to be serialized.
            return new JndiEncArtifact(this);
        }
        // under no other circumstances should this object be serialized
        throw new java.io.NotSerializableException("IntraVM java.naming.Context objects can not be passed as arguments");
    }
    
    
    /* for testing only*/
    public static void main(String str []) throws Exception{
        String str1 = "root/comp/env/rate/work/doc/lot/pop";
        String str2 = "root/comp/env/rate/work/doc/lot/price";
        String str3 = "root/comp/env/rate/work/doc/lot";
        String str4 = "root/comp/env/rate/work/doc/lot/break/story";

        IvmContext context = new IvmContext();
        context.bind(str1, new Integer(1));
        context.bind(str2, new Integer(2));
        context.bind(str4, new Integer(3));
/*
        Object obj = context.lookup(str1);
        obj = context.lookup(str2);
        obj = context.lookup(str1);
        obj = context.lookup(str3);
        obj = obj;

        NamingEnumeration ne = context.list(str3);
        while(ne.hasMore()){
            NameClassPair ncp = (NameClassPair)ne.nextElement();
            System.out.println(ncp.getName()+" "+ncp.getClassName());
        }
        */
        
        Context subcntx = (Context)context.lookup(str3);
        org.openejb.core.ivm.IntraVmCopyMonitor x = null;
        java.io.FileOutputStream fos = new java.io.FileOutputStream("x.ser");
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
        org.openejb.core.ivm.IntraVmCopyMonitor.prePassivationOperation();
        oos.writeObject(subcntx);
        org.openejb.core.ivm.IntraVmCopyMonitor.postPassivationOperation();
        oos.flush();
        oos.close();
        java.io.FileInputStream fis = new java.io.FileInputStream("x.ser");
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
        Object newObj = ois.readObject();
        ois.close();
    } 
    //*/
}
