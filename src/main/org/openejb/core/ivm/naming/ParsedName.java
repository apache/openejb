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
 * This class represents a compound path name; a path made of several atomic names.
 * It provides an interface for navigating the components sequentially. This class
 * assumes that the path separator is a '\' character. The "java:" component of a
 * JNDI ENC path must be striped out of the path before it used to construct an 
 * instance of this class.
 */
public class ParsedName implements java.io.Serializable{
    final static int IS_EQUAL = 0;
    final static int IS_LESS = -1;
    final static int IS_GREATER = 1;
    
    String [] components;
    int pos = 0;
    int hashcode;
    
    public ParsedName(String path){
        java.util.StringTokenizer st = new java.util.StringTokenizer(path, "/");
        components = new String[st.countTokens()];
        for(int i = 0; st.hasMoreTokens() && i < components.length; i++)
            components[i] = st.nextToken();
        hashcode = components[0].hashCode();
    }
    public String getComponent( ){
        return components[pos];
    }
    public boolean next( ){
        if(components.length > pos+1){
            hashcode = components[++pos].hashCode();
            return true;
        }else{
            return false;// maintain position
        }
    }
    public void reset(){
        pos = 0;
        hashcode = components[0].hashCode();
    }
    public int compareTo(int otherHash){
        if(hashcode == otherHash)
            return 0;
        else if(hashcode > otherHash )
            return 1;
        else 
            return -1;
    }
    public int getComponentHashCode( ){
        return hashcode;
    }
    public int compareTo(String other){
        int otherHash = other.hashCode();
        return compareTo(otherHash);
    }
    // for testing only
    public static void main(String [] args){
        
        ParsedName name = new ParsedName("comp/env/jdbc/mydatabase");
        while(name.next())
            System.out.println(name.getComponent());
    }
}
    