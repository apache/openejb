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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.webadmin.ejbgen;

/** Stateless Session Bean Template extending EJBTemplate
 */
import java.io.File;

public class StatelessBean extends EJBTemplate
{
	private String ejbname;
	private String ejbdesc;
	private String ejbauth;
	private String ejbpack;
	private String ejbsloc;
	private String ejbstyp;
	private File cdir;
	private String objcode;
	private String hmecode;
	private String bencode;
	private String ejbcode;
	private String opecode;
	
	/**Constructor for Stateless EJB Bean Template
	 * 
	 * @param name Name variable passed from form
	 * @param desc Description variable passed from form
	 * @param auth Author variable passed from form
	 * @param pack Package variable passed from form
	 * @param sloc Save Location variable passed from form
	 * @param styp Save Type variable passed from form
	 */
	public StatelessBean(String name, String desc, String auth, String pack, String sloc, String styp)
	{
		ejbname = name;
		ejbdesc = desc;
		ejbauth = auth;
		ejbpack = pack;
		ejbsloc = sloc;
		ejbstyp = styp;
		super.setVars(name,desc,auth,pack,sloc,styp);
		cdir = new File(sloc);
		createEJB();
	}
	
	/**This method actually calls all methods to create
	 * the EJB Template
	 */
	public void createEJB()
	{		
		if(ejbpack.equals("") != true)
		{
			super.createPackage();
		}
		
		super.createClass(ejbname,"Object.java");
		createObjCode();
		super.writeClass(ejbname + "Object.java", objcode);		
		super.createClass(ejbname,"Home.java");
		createHmeCode();
		super.writeClass(ejbname + "Home.java", hmecode);
		super.createClass(ejbname,"Bean.java");
		createBenCode();
		super.writeClass(ejbname + "Bean.java", bencode);
		createEJBXML();
		super.createXML("ejb-jar.xml", ejbcode);
		super.buildZipFile();
//		createOPEXML();
//		super.createXML("openejb-jar.xml", opecode);
	}
	
	/** This creates the code for the Remote Interface
	 * 
	 */
	public void createObjCode()
	{		
		objcode = "/**\n";
		objcode = objcode + "**/\n\n";
		objcode = objcode + "package " + ejbpack + ";\n\n";
		objcode = objcode + "import java.rmi.*;\n";
		objcode = objcode + "import javax.ejb.*;\n\n";
		objcode = objcode + "public interface " + ejbname + "Object extends EJBObject\n";
		objcode = objcode + "{\n\n";
		objcode = objcode + "}";
	}
	
	/** This creates the code for the Home Interface
	 * 
	 */
	public void createHmeCode()
	{		
		hmecode = "/**\n";
		hmecode = hmecode + "**/\n\n";
		hmecode = hmecode + "package " + ejbpack + ";\n\n";
		hmecode = hmecode + "import java.rmi.*;\n";
		hmecode = hmecode + "import javax.ejb.*;\n\n";
		hmecode = hmecode + "public interface " + ejbname + "Home extends EJBHome\n";
		hmecode = hmecode + "{\n";
		hmecode = hmecode + "\tpublic " + ejbname + "Object create() throws RemoteException, CreateException;\n";
		hmecode = hmecode + "}";
	}
	
	/** This creates the code for the Bean
	 * 
	 */
	public void createBenCode()
	{		
		bencode = "/**\n";
		bencode = bencode + "**/\n\n";
		bencode = bencode + "package " + ejbpack + ";\n\n";
		bencode = bencode + "import java.rmi.*;\n";
		bencode = bencode + "import javax.ejb.*;\n\n";
		bencode = bencode + "public class " + ejbname + "Bean implements SessionBean\n";
		bencode = bencode + "{\n";
		bencode = bencode + "\tprivate SessionContext sessionContext;\n\n";
		bencode = bencode + "\tpublic void ejbCreate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbRemove()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbActivate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbPassivate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void setSessionContext(SessionContext sessionContext)\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\tthis.sessionContext = sessionContext;\n";
		bencode = bencode + "\t}\n";
		bencode = bencode + "}";
	}
	
//	public void createOPEXML()
//	{
//		String opestr;
//		
//		opestr = "<?xml version=\"1.0\"?>\n";
//		opestr = opestr + "<openejb-jar xmlns=\"http://www.openejb.org/openejb-jar/1.1\">\n";
//		opestr = opestr + "\t<ejb-deployment ejb-name=\"" + ejbname + "\" deployment-id=\"" + ejbname + "\" container-id=\"Default Stateless Container\"/>\n";
//		opestr = opestr + "</openejb-jar>";
//		
//		opecode = opestr;
//	}
	
	/** This creates the code for the ejb-xml.jar
	 * 
	 */
	public void createEJBXML()
	{
		String ejbstr;
		
		ejbstr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		ejbstr = ejbstr + "<ejb-jar>\n";
		ejbstr = ejbstr + "\t<enterprise-beans>\n";
		ejbstr = ejbstr + "\t\t<session>\n";
		ejbstr = ejbstr + "\t\t\t<ejb-name>" + ejbname + "</ejb-name>\n";
		ejbstr = ejbstr + "\t\t\t<home>" + ejbpack + "." + ejbname + "Home</home>\n";
		ejbstr = ejbstr + "\t\t\t<remote>" + ejbpack + "." + ejbname + "Object</remote>\n";
		ejbstr = ejbstr + "\t\t\t<ejb-class>" + ejbpack + "." + ejbname + "Bean</ejb-class>\n";
		ejbstr = ejbstr + "\t\t\t<session-type>Stateless</session-type>\n";
		ejbstr = ejbstr + "\t\t\t<transaction-type>Container</transaction-type>\n";
		ejbstr = ejbstr + "\t\t</session>\n";
		ejbstr = ejbstr + "\t</enterprise-beans>\n";
		ejbstr = ejbstr + "\t<assembly-descriptor>\n";
		ejbstr = ejbstr + "\t\t<container-transaction>\n";
		ejbstr = ejbstr + "\t\t\t<method>\n";
		ejbstr = ejbstr + "\t\t\t\t<ejb-name>" + ejbname + "</ejb-name>\n";
		ejbstr = ejbstr + "\t\t\t\t<method-name>*</method-name>\n";
		ejbstr = ejbstr + "\t\t\t</method>\n";
		ejbstr = ejbstr + "\t\t<trans-attribute>Required</trans-attribute>\n";
		ejbstr = ejbstr + "\t</container-transaction>\n";
		ejbstr = ejbstr + "\t</assembly-descriptor>\n";
		ejbstr = ejbstr + "</ejb-jar>";
		
		ejbcode = ejbstr;
	}
}
