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


/**
 * This class is a combination linked list and binary tree for resolving name-object lookups
 * and binding objects to names.  The linked list (subTree) hold subcontexts while the binary
 * tree (lessTree/grtrTree) allows for quick navigation of the a hierarchical path used in the \
 * JNDI ENC.  Resolving paths requires the use of the ParsedName class.
 *
 * Navigation through the binary tree is determined by the hashCode of the ParsedName components
 * as compared to the atomicName of the NameNode.  When the hashcodes and String values are equal
 * navigation proceeds down the subtree or the name is immediately resolved if its the last component
 * in the ParsedName.  When a hashcode is greater or when the hashcodes are equal but the objects are
 * not equal navigation proceeds down the grtrTree.  When the hashcode of the component is less then 
 * the atomicName navigation proceeds down the lessTree.
 */
public class NameNode implements java.io.Serializable {
    public String atomicName;
    public int atomicHash;
    public NameNode lessTree;
    public NameNode grtrTree;
    public NameNode subTree;
    public NameNode parent;
    public Object myObject;
    public transient IvmContext myContext;
    
    public NameNode(NameNode parent, ParsedName name,  Object obj){
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
    public Object resolve(ParsedName name)throws javax.naming.NameNotFoundException{
        int compareResult = name.compareTo(atomicHash);
        
        if(compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)){// hashcodes and String valuse are equal
            if(name.next()){
                if(subTree == null) throw new javax.naming.NameNotFoundException("Can not resolve "+name);
                return subTree.resolve(name);
            }else
                return getBinding();
        }else if(compareResult == ParsedName.IS_LESS){// parsed hash is less than
            if(lessTree == null) throw new javax.naming.NameNotFoundException("Can not resolve "+name);
            return lessTree.resolve(name);
        
        }else{//ParsedName.IS_GREATER
              //...or ParsedName.IS_EQUAL but components are not the same string (hash code collision)
            if(grtrTree == null) throw new javax.naming.NameNotFoundException("Can not resolve "+name);
            return grtrTree.resolve(name);
        }
    }
    
    public void bind(ParsedName name, Object obj) throws javax.naming.NameAlreadyBoundException {
        int compareResult = name.compareTo(atomicHash);
        if(compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)){
            if(name.next()){
		if( myObject != null) {
		    throw new javax.naming.NameAlreadyBoundException();
		}
                if(subTree==null)
                    subTree = new NameNode(this, name,obj);
                else
                    subTree.bind(name,obj);
            }
	    else {
		if( subTree != null) {
		    throw new javax.naming.NameAlreadyBoundException();
		}
                myObject = obj;// bind the object to this node
	    }
        }else if(compareResult == ParsedName.IS_LESS){
            if(lessTree == null)
                lessTree = new NameNode(this.parent, name, obj);
            else
                lessTree.bind(name,obj);
        }else{//ParsedName.IS_GREATER ...
              //...or ParsedName.IS_EQUAL but components are not the same string (hash code collision)
            if(grtrTree == null)
                grtrTree = new NameNode(this.parent, name, obj);
            else
                grtrTree.bind(name,obj);
        }
    }

    public IvmContext createSubcontext( ParsedName name) throws javax.naming.NameAlreadyBoundException {
	try {
	    bind( name, null);
	    name.reset();
	    return (IvmContext)resolve( name);
	}
	catch( javax.naming.NameNotFoundException exception) {
	    exception.printStackTrace();
	    throw new RuntimeException();
	}
    }
}
