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

/** BMP Entity Bean Template extending EJBTemplate
 */
import java.io.File;

public class BMPBean extends EJBTemplate
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
	private String pkcode;
	
	/**Constructor for BMP Entity EJB Bean Template
	 * 
	 * @param name Name variable passed from form
	 * @param desc Description variable passed from form
	 * @param auth Author variable passed from form
	 * @param pack Package variable passed from form
	 * @param sloc Save Location variable passed from form
	 * @param styp Save Type variable passed from form
	 */
	public BMPBean(String name, String desc, String auth, String pack, String sloc, String styp)
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
		createPKCode();
		super.writeClass(ejbname + "PK.java", pkcode);
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
		objcode = objcode + "\tpublic void setData (String data) throws RemoteException;\n";
		objcode = objcode + "\tpublic String getData () throws RemoteException;\n";
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
		hmecode = hmecode + "import java.util.*;\n";
		hmecode = hmecode + "import javax.ejb.*;\n\n";
		hmecode = hmecode + "public interface " + ejbname + "Home extends EJBHome\n";
		hmecode = hmecode + "{\n";
		hmecode = hmecode + "\tpublic " + ejbname + "Object create() throws RemoteException, CreateException;\n";
		hmecode = hmecode + "\tpublic " + ejbname + "Object findByPrimaryKey (" + ejbname + "PK pk) throws FinderException, RemoteException;\n";
		hmecode = hmecode + "\tpublic " + ejbname + "Object remove() throws RemoteException, CreateException;\n";
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
		bencode = bencode + "import java.sql.*;\n";
		bencode = bencode + "import javax.sql.*;\n";
		bencode = bencode + "import javax.naming.*;\n";
		bencode = bencode + "import javax.ejb.*;\n\n";
		bencode = bencode + "public class " + ejbname + "Bean implements EntityBean\n";
		bencode = bencode + "{\n";
		bencode = bencode + "\tprotected EntityContext ctx = null;\n";
		bencode = bencode + "\tprotected int id;\n";
		bencode = bencode + "\tprotected String data;\n\n";
		bencode = bencode + "\tpublic void ejbCreate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void setData(String data)\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\tthis.data = data;\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic String getData()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\treturn this.data;\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void setId(int id)\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\tthis.id = id;\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic int getId()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\treturn this.id;\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void setEntityContext(EntityContext ctx)\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\tthis.ctx = ctx;\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void unsetEntityContext()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\tthis.ctx = null;\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic EntityContext getEntityContext()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t\treturn this.ctx;\n";
		bencode = bencode + "\t}\n";
		bencode = bencode + "\tpublic void ejbActivate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbPassivate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbPostCreate()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbLoad()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbStore()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbRemove()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n\n";
		bencode = bencode + "\tpublic void ejbFindByPrimaryKey()\n";
		bencode = bencode + "\t{\n";
		bencode = bencode + "\t}\n";
		bencode = bencode + "}";
	}
	
	/** This creates the code for the PrimaryKey
	 * 
	 */
	public void createPKCode()
	{
		pkcode = "/**\n";
		pkcode = pkcode + "**/\n\n";
		pkcode = pkcode + "package " + ejbpack + ";\n\n";
		pkcode = pkcode + "import java.io.*;\n\n";
		pkcode = pkcode + "public class " + ejbname + "PK implements Serializable\n";
		pkcode = pkcode + "{\n";
		pkcode = pkcode + "\tpublic int id;\n\n";
		pkcode = pkcode + "\tpublic " + ejbname + "PK()\n";
		pkcode = pkcode + "\t{\n";
		pkcode = pkcode + "\t\tthis.id = 0;\n";
		pkcode = pkcode + "\t}\n\n";
		pkcode = pkcode + "\tpublic " + ejbname + "PK(int id)\n";
		pkcode = pkcode + "\t{\n";
		pkcode = pkcode + "\t\tthis.id = id;\n";
		pkcode = pkcode + "\t}\n\n";
		pkcode = pkcode + "\tpublic int hashCode()\n";
		pkcode = pkcode + "\t{\n";
		pkcode = pkcode + "\t\treturn this.id;\n";
		pkcode = pkcode + "\t}\n\n";
		pkcode = pkcode + "\tpublic boolean equals(Object obj)\n";
		pkcode = pkcode + "\t{\n";
		pkcode = pkcode + "\t\tif (obj instanceof " + ejbname + "PK)\n";
		pkcode = pkcode + "\t\t{\n";
		pkcode = pkcode + "\t\t\treturn (id == ((" + ejbname + "PK)obj).id);\n";
		pkcode = pkcode + "\t\t}\n";
		pkcode = pkcode + "\t\treturn false;\n";
		pkcode = pkcode + "\t}\n";
		pkcode = pkcode + "}";
	}

	/** This creates the code for the ejb-xml.jar
	 * 
	 */
	public void createEJBXML()
	{
		String ejbstr;
		
		ejbstr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		ejbstr = ejbstr + "<ejb-jar>\n";
		ejbstr = ejbstr + "\t<enterprise-beans>\n";
		ejbstr = ejbstr + "\t\t<entity>\n";
		ejbstr = ejbstr + "\t\t\t<ejb-name>" + ejbname + "</ejb-name>\n";
		ejbstr = ejbstr + "\t\t\t<home>" + ejbpack + "." + ejbname + "Home</home>\n";
		ejbstr = ejbstr + "\t\t\t<remote>" + ejbpack + "." + ejbname + "Object</remote>\n";
		ejbstr = ejbstr + "\t\t\t<ejb-class>" + ejbpack + "." + ejbname + "Bean</ejb-class>\n";
		ejbstr = ejbstr + "\t\t\t<persistance-type>Bean</persistance-type>\n";
		ejbstr = ejbstr + "\t\t\t<prim-key-class>" + ejbpack + "." + ejbname + "PK</prim-key-class>\n";
		ejbstr = ejbstr + "\t\t\t<reentrant>False</reentrant>\n";
		ejbstr = ejbstr + "\t\t</entity>\n";
		ejbstr = ejbstr + "\t</enterprise-beans>\n";
		ejbstr = ejbstr + "</ejb-jar>";
	
		ejbcode = ejbstr;
	}
	
//	public void createOPEXML()
//	{
//	}
}
