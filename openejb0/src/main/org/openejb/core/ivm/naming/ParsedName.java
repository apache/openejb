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
        path = normalize(path);
        //System.out.println("[] path "+path);
        if (path == null || path.equals("/") ) {
	    // A blank string is a legal name and refers to the current/root context.
	    components = new String[1];
	    components[0] = "";
	    hashcode = 0;
        } else if( path.length() > 0) {
	    java.util.StringTokenizer st = new java.util.StringTokenizer(path, "/");
	    components = new String[st.countTokens()];
	    for(int i = 0; st.hasMoreTokens() && i < components.length; i++)
		components[i] = st.nextToken();
	    hashcode = components[0].hashCode();
	}
	else {
	    // A blank string is a legal name and refers to the current/root context.
	    components = new String[1];
	    components[0] = "";
	    hashcode = 0;
	}
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
    public String toString() {
	if( components.length == 0) {
	    return "";
	}
	StringBuffer buffer = new StringBuffer( components[0]);
	for( int i = 1; i < components.length; ++i) {
	    buffer.append('/');
	    buffer.append( components[i]);
	}
	return buffer.toString();
    }

        /* A normal Unix pathname contains no duplicate slashes and does not end
       with a slash.  It may be the empty string. */

    /* Normalize the given pathname, whose length is len, starting at the given
       offset; everything before this offset is already normal. */
    private String normalize(String pathname, int len, int off) {
	if (len == 0) return pathname;
	int n = len;
	while ((n > 0) && (pathname.charAt(n - 1) == '/')) n--;
	if (n == 0) return "/";
	StringBuffer sb = new StringBuffer(pathname.length());
	if (off > 0) sb.append(pathname.substring(0, off));
	char prevChar = 0;
	for (int i = off; i < n; i++) {
	    char c = pathname.charAt(i);
	    if ((prevChar == '/') && (c == '/')) continue;
	    sb.append(c);
	    prevChar = c;
	}
	return sb.toString();
    }

    /* Check that the given pathname is normal.  If not, invoke the real
       normalizer on the part of the pathname that requires normalization.
       This way we iterate through the whole pathname string only once. */
    private String normalize(String pathname) {
	int n = pathname.length();
	char prevChar = 0;
	for (int i = 0; i < n; i++) {
	    char c = pathname.charAt(i);
	    if ((prevChar == '/') && (c == '/'))
		return normalize(pathname, n, i - 1);
	    prevChar = c;
	}
	if (prevChar == '/') return normalize(pathname, n, n - 1);
	return pathname;
    }

}
