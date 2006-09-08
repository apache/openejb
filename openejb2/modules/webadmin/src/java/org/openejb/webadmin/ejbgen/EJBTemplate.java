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

/** EJBTemplate file housing methods to build all EJB Skeletons
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class EJBTemplate
{
	private String name;
	private String desc;
	private String auth;
	private String pack;
	private String sloc;
	private String styp;
	private File cdir;
	private File ddir;
	private File backup;
	private File packup;
	private String psep = System.getProperty("file.separator");
	
	public abstract void createEJB();
	public abstract void createEJBXML();
//	public abstract void createOPEXML();
	public abstract void createObjCode();
	public abstract void createHmeCode();
	public abstract void createBenCode();
	
	/** The setVars method will initialize all String
	 *  variables needed for the methods in this class.
	 * @param ejbname the ejb's name
	 * @param ejbdesc the ejb's description
	 * @param ejbauth the ejb's author
	 * @param ejbpack the ejb's package
	 * @param ejbsloc the ejb's save location
	**/ 
	public void setVars(String ejbname, String ejbdesc, String ejbauth, String ejbpack, String ejbsloc, String ejbstyp)
	{
		name = ejbname;
		desc = ejbdesc;
		auth = ejbauth;
		pack = ejbpack;
		styp = ejbstyp;
		sloc = ejbsloc + psep + ejbname;
	}
	
	
	/** This method backs up all structure and classes for the
	 *  EJB if there are already classes in the save location
	 *  with the same name.
	**/ 
	public void createBackup()
	{
		File metaback = new File(backup.getPath() + psep + "META-INF");
		File cbdir = new File(backup.getPath());
		//String[] dirs = pack.split("\\.");
		StringTokenizer dirs = new StringTokenizer(pack,"\\.");
		
		backup.mkdir();
		metaback.mkdir();
	
		//for(int i = 0; i < dirs.length; i++)
		//{			
		//	File ndir = new File(cbdir.getPath() + psep + dirs[i]);
		//	ndir.mkdir();
		//	cbdir = new File(ndir.getPath());
		//}	
		
		while (dirs.hasMoreTokens())
		{
			File ndir = new File(cbdir.getPath() + psep + dirs.nextToken());
			ndir.mkdir();
			cbdir = new File(ndir.getPath());
		}
		
		packup = new File(cbdir.getPath());
	}
	
	/** This method creates the directory structure to fit
	 *  the same structure as the package input by the user.
	**/ 
	public void createPackage()
	{
		cdir = new File(sloc);
		ddir = new File(sloc);
		backup = new File(ddir.getPath() + psep + "backup");
		//String[] dirs = pack.split("\\.");
		StringTokenizer dirs = new StringTokenizer(pack,"\\.");
		
		ddir.mkdir();
	
		//for(int i = 0; i < dirs.length; i++)
		//{			
		//	File ndir = new File(cdir.getPath() + psep + dirs[i]);
		//	ndir.mkdir();
		//	cdir = new File(ndir.getPath());
		//}
		
		while (dirs.hasMoreTokens())
		{
			File ndir = new File(cdir.getPath() + psep + dirs.nextToken());
			ndir.mkdir();
			cdir = new File(ndir.getPath());
		}
		
		//cdir = new File(odir);
	}
	
	/** This method actually creates the file for the proper class.
	 *  It's results depend on what is passed to it.  It concats
	 *  the two parameters together to form a single file name.
	 * @param ejbname the name of the class
	 * @param ejbobj the type of the class
	**/ 
	public void createClass(String ejbname, String ejbobj)
	{
		File ejbObj = new File(cdir.getPath() + psep + ejbname + ejbobj);
	
		if(ejbObj.exists())
		{						
			createBackup();
			
			File fbackup = new File(packup.getPath() + psep + ejbname + ejbobj);
			
			ejbObj.renameTo(fbackup);
			ejbObj.delete();
		}
	
		try
		{
			ejbObj.createNewFile();
		}
		catch(IOException e)
		{
			System.out.println("Couldn\'t create file!");
		}
	}
	
	/** This method does the writing to the class created
	 *  by the createClass() method.  It will create a File
	 *  object of the filename passed and write code to it
	 *  as passed by code.
	 * @param filename the filename in String datatype
	 * @param code the code to be written to the filename
	**/ 
	public void writeClass(String filename, String code)
	{
		File wrtFile = new File(cdir.getPath()+ psep + filename);	
		try
		{
			FileWriter wrtWriter = new FileWriter(wrtFile);
			wrtWriter.write(code);
			wrtWriter.close();
		}
		catch (IOException e)
		{
			System.out.println("File Not Found. - " + e);
		}	
	}
	
	/** Same as writeClass() method but it creates the structure
	 *  and files that are XML Files.
	 * @param filename the filename in String datatype
	 * @param code the code to be written to the filename
	 * @see writeClass()
	**/ 
	public void createXML(String filename, String code)
	{
		String location = sloc;
		File metainf = new File(location + psep + "META-INF");
		File xmlfile = new File(location + psep + "META-INF" + psep + filename);
		
		if(metainf.exists())
		{
			if(xmlfile.exists())
			{
				File nfile = new File(backup.getPath() + psep + "META-INF" + psep + filename);
				xmlfile.renameTo(nfile);
				xmlfile.delete();
			}
		}
		else
		{
			metainf.mkdir();
		}
		
		try
		{
			xmlfile.createNewFile();
			
			try
			{
				FileWriter ejbWriter = new FileWriter(xmlfile);
				
				ejbWriter.write(code);
				ejbWriter.close();
			}
			catch(IOException e1)
			{
				System.out.println("I/O Exception: " + e1);
			}
		}
		catch(IOException e)
		{
			System.out.println("I/O Exception: " + e);
		}
	}
	
	/**This will build a zip file for all the files built
	 * by the EJB Generator
	 */
	public void buildZipFile()
	{
		File bdir = new File(getBeanDir());
		File mdir = new File(sloc + psep + "META-INF");
		File zdir = new File(sloc);
		String zname = name + ".zip";
		File myZipFile = new File(zdir,zname);
		byte[] buf = new byte[1024];
	
		//System.out.println(bdir);
		//System.out.println(mdir);
		//System.out.println(zdir);
		//System.out.println(zname);
	
		try
		{
			myZipFile.createNewFile();
		
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(myZipFile));
			File[] mfiles = mdir.listFiles();
			File[] bfiles = bdir.listFiles();
			//String[] dirs = pack.split("\\.");
			StringTokenizer dirs = new StringTokenizer(pack,"\\.");
			String dpath = "";
		
			//META-INF
			out.putNextEntry(new ZipEntry("META-INF/"));
		
			for(int i = 0; i < mfiles.length; i++)
			{
				File cfile = new File(sloc + psep + "META-INF" +
				psep + mfiles[i].getName());
				FileInputStream in = new FileInputStream(cfile);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry("META-INF/" + mfiles[i].getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}
		
			//System.out.println("META-INF zipped!");
		
			//for(int i = 0; i < dirs.length; i ++)
			//{
			//	dpath = dpath + dirs[i] + "/";
			//	out.putNextEntry(new ZipEntry(dpath));
				//System.out.println(dpath);
			//}
			
			while(dirs.hasMoreTokens())
			{
				dpath = dpath + dirs.nextToken() + "/";
				out.putNextEntry(new ZipEntry(dpath));
				//System.out.println(dpath);
			}
		
			//System.out.println("Directories zipped!");
		
			for(int i = 0; i < bfiles.length; i++)
			{
				File cfile = new File(getBeanDir() + psep + bfiles[i].getName());
				FileInputStream in = new FileInputStream(cfile);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(dpath + bfiles[i].getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}
		
			//System.out.println("Zip Completed!");
		
			out.close();
		}
		catch(IOException e)
		{
			System.out.println("I/O Exception: " + e);
		}
	}
	
	/**This method is used to get the path to where the EJB
	 * Template's source is stored locally on the computer.
	 * 
	 * @return Returns the full path to the local directory
	 */
	public String getBeanDir()
	{
		//String[] dirs = request.getFormParameter("ejbpack").split("\\.");
		StringTokenizer dirs = new StringTokenizer(pack,"\\.");
		String beandir = sloc;
		
		//for(int i = 0; i < dirs.length; i++)
		//{
		//	beandir = beandir + psep + dirs[i];	
		//}
		
		while (dirs.hasMoreTokens())
		{
			beandir = beandir + psep + dirs.nextToken();
		}

		
		//System.out.println(beandir);
		return beandir;
	}
}
