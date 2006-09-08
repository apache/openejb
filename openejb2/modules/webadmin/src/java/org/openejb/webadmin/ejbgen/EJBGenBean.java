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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.openejb.webadmin.HttpRequest;
import org.openejb.webadmin.HttpResponse;
import org.openejb.webadmin.WebAdminBean;
import org.openejb.util.FileUtils;
import org.openejb.util.HtmlUtilities;

/** Page for user to input needed data to build EJB Skeleton.
 */
public class EJBGenBean extends WebAdminBean
{	
	private String psep = System.getProperty("file.separator");  //Operating System File Separator
	private String oehp = System.getProperty("openejb.home");  //Path to OPENEJB_HOME
	private String surl = "";
	
	/**
	 * Called after a new instance of EJBGenBean is created
	 */
	public void ejbCreate()
	{
		// The section variable must match 
		// the deployment id name
		section = "/EJBGenerator/CreateEJB";
	}

	/** called after all content is written to the browser
	 * @param request the http request
	 * @param response the http response
	 * @throws IOException if an exception is thrown
	 */ 
	public void postProcess(HttpRequest request, HttpResponse response) throws IOException
	{
	}

	/** called before any content is written to the browser
	 * @param request the http request
	 * @param response the http response
	 * @throws IOException if an exception is thrown
	 */    
	public void preProcess(HttpRequest request, HttpResponse response) throws IOException
	{
	}

	/** writes the main body content to the broswer.
	 *  This content is inside a <code>&lt;p&gt;</code> block.
	 * @param body the output to write to
	 * @exception IOException if an exception is thrown
	 */
	public void writeBody(PrintWriter body) throws IOException
	{
		String ejbname = request.getFormParameter("ejbname");
		String ejbdesc = request.getFormParameter("ejbdesc");
		String ejbauth = request.getFormParameter("ejbauth");
		String ejbpack = request.getFormParameter("ejbpack");
		String ejbtype = request.getFormParameter("ejbtype");
		String ejbsloc = request.getFormParameter("ejbsloc");
		String generate = request.getFormParameter("generate");
		String ejbstyp = request.getFormParameter("ejbstyp");
		boolean error = false;
		
		//checks to see if the page has been submitted
		if(generate != null)
		{
			if(checkSourceDir(ejbsloc))
			{
				error = false;
			}
			else
			{
				error = true;
			}
			
			if(error)
			{
				writeGUI(body,true);
			}
			else
			{
				if(ejbstyp.equals("local"))
				{
					surl = ejbsloc + psep;
				}
				else
				{
					surl = "/BeanSources/";	
				}
				
				if(ejbtype.equals("BMP"))
				{		
					BMPBean newEJB = new BMPBean(ejbname,ejbdesc,ejbauth,ejbpack,ejbsloc,ejbstyp);
					showBeanFiles(body);
					System.out.println("EJB Generator has created " + ejbname + "!");
				}
				else if(ejbtype.equals("CMP"))
				{
					CMPBean newEJB = new CMPBean(ejbname,ejbdesc,ejbauth,ejbpack,ejbsloc,ejbstyp);
					showBeanFiles(body);
					System.out.println("EJB Generator has created " + ejbname + "!");
				}
				else if(ejbtype.equals("Stateful"))
				{
					StatefulBean newEJB = new StatefulBean(ejbname,ejbdesc,ejbauth,ejbpack,ejbsloc,ejbstyp);
					showBeanFiles(body);
					System.out.println("EJB Generator has created " + ejbname + "!");
				}
				else
				{
					StatelessBean newEJB = new StatelessBean(ejbname,ejbdesc,ejbauth,ejbpack,ejbsloc,ejbstyp);
					showBeanFiles(body);
					System.out.println("EJB Generator has created " + ejbname + "!");
				}
				
				if(ejbstyp.equals("remote"))
				{
					File nsdir = new File(oehp + psep + "htdocs" + psep + "EJBGenerator" + psep + "BeanSources");
					
					if(!nsdir.exists())
					{
						nsdir.mkdirs();
					}
					
					File nzdir = new File(nsdir,ejbname);
					
					if(!nzdir.exists())
					{
						nzdir.mkdirs();
					}
					
					FileUtils.copyFile(new File(nzdir, ejbname + ".zip"), 
					new File(ejbsloc + psep + ejbname + psep + ejbname + ".zip"));
				}
			}
		}
		else
		{
			writeGUI(body,false);
		}
	}

	/** Write the TITLE of the HTML document.  This is the part
	 * that goes into the <code>&lt;head&gt;&lt;title&gt;
	 * &lt;/title&gt;&lt;/head&gt;</code> tags
	 *
	 * @param body the output to write to
	 * @exception IOException of an exception is thrown
	 *
	 */
	public void writeHtmlTitle(PrintWriter body) throws IOException
	{
		body.print(HTML_TITLE);
	}

	/** Write the title of the page.  This is displayed right
	 * above the main block of content.
	 * 
	 * @param body the output to write to
	 * @exception IOException if an exception is thrown
	 */
	public void writePageTitle(PrintWriter body) throws IOException
	{
		body.print("EJB Generator");
	}

	/** Write the sub items for this bean in the left navigation bar of
	 * the page.  This should look somthing like the one below:
	 *
	 *      <code>
	 *      &lt;tr&gt;
	 *       &lt;td valign="top" align="left"&gt;
	 *        &lt;a href="system?show=deployments"&gt;&lt;span class="subMenuOff"&gt;
	 *        &nbsp;&nbsp;&nbsp;Deployments
	 *        &lt;/span&gt;
	 *        &lt;/a&gt;&lt;/td&gt;
	 *      &lt;/tr&gt;
	 *      </code>
	 *
	 * Alternately, the bean can use the method formatSubMenuItem(..) which
	 * will create HTML like the one above
	 *
	 * @param body the output to write to
	 * @exception IOException if an exception is thrown
	 *
	 */
	public void writeSubMenuItems(PrintWriter body) throws IOException
	{
	}
	
	/**This will verify that the file directory to save the source to
	 * actually exists.  If it does, it will attempt to create the EJB
	 * skeleton.  If not, it will redirect to information page with
	 * all information back in place and will prompt the user to enter
	 * a valid working directory.
	 * 
	 * @param path String to the path of the source to verify
	 * it exists before trying to generate EJB Template
	 * @return Returns false if the source isn't valid
	 */
	private boolean checkSourceDir(String path)
	{
		boolean goodDir = false;
		File srcDir = new File(path);
		
		if(srcDir.exists() && srcDir.isDirectory() && srcDir.canWrite())
		{
			goodDir = true;
		}
		else
		{
			goodDir = false;
		}
		return goodDir;
	}
	
	/**Writes HTML to the page for the EJB Information.
	 * This is the GUI for the user to input information
	 * about the EJB so the EJB Generator can build the 
	 * appropriate template.
	 * 
	 * @param body  Recieves PrintWriter from writeBody() method
	 * @param error Recieves error from writeBody() method
	 */
	private void writeGUI(PrintWriter body, boolean error)
	{	
		String ejbname = request.getFormParameter("ejbname");
		String ejbdesc = request.getFormParameter("ejbdesc");
		String ejbauth = request.getFormParameter("ejbauth");
		String ejbpack = request.getFormParameter("ejbpack");
		String ejbtype = request.getFormParameter("ejbtype");
		String ejbsloc = request.getFormParameter("ejbsloc");
		String ejbstyp = request.getFormParameter("ejbstyp");
		String generate = request.getFormParameter("generate");
		String namehelp = "This is the name of your EJB.  It will be appended to all of your source " +
		                  "files.  For example, if your EJB Name was Foo, your Bean.java file would be " +
		                  "&QUOT;FooBean.java&QUOT;.";
		String deschelp = "This has not been implemented yet but it will end up being in your JavaDoc " +
						  "information describing your bean and the bean&#8217;s purpose.";
		String authhelp = "This puts your name in the JavaDoc information.";
		String packhelp = "This will build the proper package for your EJB.  For example.  If your package " +
						  " was foo.bar, your .java files for your EJB would be put in the foo/bar/ directory.";
		String typehelp = "This is to determine which EJB to build for you.";
		String styphelp = "This is very important as it can keep you from reaching your source if not used " +
						  "properly.  If you are accessing the WebAdmin module from http://localhost:4203, you " +
						  "should always select &QUOT;local&QUOT;.  If the server you are accessing has been setup to " +
						  "be accessed remotely the you should select remote.";
		String slochelp = "This is where your actual files will be created.  If you are accessing locally, your " +
						  "directory structure is already setup and there is no need to download the " +
						  "zip file.  Just go to your EJB Save Location and you&#8217;ll see a folder with your EJB " +
						  "Name.  Inside of it will be your META-INF folder and the top level of your EJB&#8217;s " +
						  "package.";
			
		if(ejbname == null)
		{
			ejbname = "";
		}
		if(ejbdesc == null)
		{
			ejbdesc = "";
		}
		if(ejbauth == null)
		{
			ejbauth = "";
		}
		if(ejbpack == null)
		{
			ejbpack = "";
		}
		if(ejbtype == null)
		{
			ejbtype = "";
		}
		if(ejbsloc == null)
		{
			ejbsloc = "";
		}
		if(ejbstyp == null)
		{
			ejbstyp = "";
		}

		body.println("Welcome to the EJB Generator!  This tool will help trim development");
		body.println("time by building the skeleton for your EJB.  Fill out all ");
		body.println("information and submit.  For a brief explanation, click the column with the name of the field.");
		body.println("<br>");
		body.println("<br>");
		body.println("<h2>General EJB Information</h2>");
		body.println("<form action=\""+section+"\" method=\"post\" enctype=\"multipart/form-data\" onsubmit=\"return validate(this)\">");
		body.println("<table width=\"100%\" border=\"1\">");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + namehelp + "\')\"><font face=\"arial\" color=\"white\">EJB Name:&nbsp;&nbsp;&nbsp;</font></td><td>" +
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
			HtmlUtilities.createTextFormField("ejbname",ejbname,40,0) + "</td>");
		body.println("</tr>");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + deschelp + "\')\"><font face=\"arial\" color=\"white\">EJB <br> Description:&nbsp;&nbsp;&nbsp;</font></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" 
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
			HtmlUtilities.createTextArea("ejbdesc",ejbdesc,3,30,"","","") + "</td>");
		body.println("</tr>");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + authhelp + "\')\"><font face=\"arial\" color=\"white\">Author:&nbsp;&nbsp;&nbsp;</font></td><td>" +
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
			HtmlUtilities.createTextFormField("ejbauth",ejbauth,40,0) + "</td>");
		body.println("</tr>");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + packhelp + "\')\"><font face=\"arial\" color=\"white\">Package:&nbsp;&nbsp;&nbsp;</font></td><td>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
		HtmlUtilities.createTextFormField("ejbpack",ejbpack,40,0) + "</td>");
		body.println("</tr>");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + typehelp + "\')\"><font face=\"arial\" color=\"white\">EJB Type:&nbsp;&nbsp;&nbsp;</font></td><td>" +
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
			HtmlUtilities.createSelectFormField("ejbtype",""));
		if(ejbtype.equals("BMP") || ejbtype.equals(""))
		{
			body.println(HtmlUtilities.createSelectOption("BMP","Entity (BMP)",true));
			body.println(HtmlUtilities.createSelectOption("CMP","Entity (CMP)",false));
			body.println(HtmlUtilities.createSelectOption("Stateful","Session (Stateful)",false));
			body.println(HtmlUtilities.createSelectOption("Stateless","Session (Stateless)",false));
		}
		else if(ejbtype.equals("CMP"))
		{
			body.println(HtmlUtilities.createSelectOption("BMP","Entity (BMP)",false));
			body.println(HtmlUtilities.createSelectOption("CMP","Entity (CMP)",true));
			body.println(HtmlUtilities.createSelectOption("Stateful","Session (Stateful)",false));
			body.println(HtmlUtilities.createSelectOption("Stateless","Session (Stateless)",false));
		}
		else if(ejbtype.equals("Stateful"))
		{
			body.println(HtmlUtilities.createSelectOption("BMP","Entity (BMP)",false));
			body.println(HtmlUtilities.createSelectOption("CMP","Entity (CMP)",false));
			body.println(HtmlUtilities.createSelectOption("Stateful","Session (Stateful)",true));
			body.println(HtmlUtilities.createSelectOption("Stateless","Session (Stateless)",false));
		}
		else
		{
			body.println(HtmlUtilities.createSelectOption("BMP","Entity (BMP)",false));
			body.println(HtmlUtilities.createSelectOption("CMP","Entity (CMP)",false));
			body.println(HtmlUtilities.createSelectOption("Stateful","Session (Stateful)",false));
			body.println(HtmlUtilities.createSelectOption("Stateless","Session (Stateless)",true));
		}
		//body.println(HtmlUtilities.createSelectOption("BMP","Entity (BMP)",true));
		//body.println(HtmlUtilities.createSelectOption("CMP","Entity (CMP)",false));
		//body.println(HtmlUtilities.createSelectOption("Stateful","Session (Stateful)",false));
		//body.println(HtmlUtilities.createSelectOption("Stateless","Session (Stateless)",false));
		body.println("</select>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.println("</td>");
		body.println("</tr>");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + styphelp + "\')\"><font face=\"arial\" color=\"white\">Access Type:</font></td>");
		body.println("<td><center><font face=\"arial\" color=\"white\">");
		body.println("Local&nbsp;&nbsp;&nbsp;" + HtmlUtilities.createInputFormField("radio","ejbstyp","local",0,0,"","","","",true,false,true) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.println("Remote&nbsp;&nbsp;&nbsp;" + HtmlUtilities.createInputFormField("radio","ejbstyp","remote",0,0,"","","","",false,false,true));
		body.println("</font></center></td>");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td onclick=\"popupMsg(\'" + slochelp + "\')\"><font face=\"arial\" color=\"white\">Source<br>Location:&nbsp;&nbsp;&nbsp;</font></td><td>" +
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
			HtmlUtilities.createTextFormField("ejbsloc",ejbsloc,40,0) + "</td>");
		body.println("</tr>");
		if(error)
		{
			body.println("<tr bgcolor=\"#5A5CB8\">");
			body.println("<td colspan=\"2\"><font face=\"arial\" color=\"red\"><strong><center>Please enter a valid Source Location!</center></strong></font></td></tr>");
		}
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td colspan=\"2\"><center>" + HtmlUtilities.createSubmitFormButton("generate","Generate") + "</center></td>");
		body.println("</tr>");
		body.println("</table>");
		body.println("</form>");	
	}
	
	/**This method is used to get the path to where the EJB
	 * Template's source is stored locally on the computer.
	 * 
	 * @return Returns the full path to the local directory
	 */
	public String getBeanDir()
	{
		//String[] dirs = request.getFormParameter("ejbpack").split("\\.");
		StringTokenizer dirs = new StringTokenizer(request.getFormParameter("ejbpack"),"\\.");
		String beandir = request.getFormParameter("ejbsloc") + psep + request.getFormParameter("ejbname");
		
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
	
	/**This method is used to get the proper URL to the 
	 * files for local access only.
	 * 
	 * @return Returns the proper URL appendage to be 
	 * added to the URL for proper file access
	 */
	public String getBeanURL()
	{
		//String[] dirs = request.getFormParameter("ejbpack").split("\\.");
		StringTokenizer dirs = new StringTokenizer(request.getFormParameter("ejbpack"),"\\.");
		String beanurl = request.getFormParameter("ejbname");
		
		//for(int i = 0; i < dirs.length; i++)
		//{
		//	beanurl = beanurl + "/" + dirs[i];	
		//}
		
		while (dirs.hasMoreTokens())
		{
			beanurl = beanurl + "/" + dirs.nextToken();
		}

		//System.out.println(beanurl);
		return beanurl;
	}
	
	/**This is the response after a good EJB Template generation
	 * 
	 * @param body PrinterWriter is passed from the writeBody() method
	 */
	public void showBeanFiles(PrintWriter body)
	{
		String beanstr = getBeanDir();
		String beanurl = getBeanURL();
		File beanDir = new File(beanstr);
		File metaDir = new File(request.getFormParameter("ejbsloc") + psep + request.getFormParameter("ejbname") +
		psep + "META-INF");
		File[] beanFiles = beanDir.listFiles();
		File[] metaFiles = metaDir.listFiles();

		body.println("<h1>Your Bean Skeleton Has Been Created</h1><br>");
		body.println("If you are accessing this tool remotely, where you ");
		body.println("aren\'t accessing it from the server running this instance of OpenEJB, ");
		body.println("the links are provided so you can copy and paste the source to your local ");
		body.println("computer.  Otherwise, your full directory structure for your EJB is in tact ");
		body.println("and you can edit the sources with your favorite editor!<br><br>");
		body.println("<table width=\"50%\" border=\"1\" align=\"center\">");
		body.println("<tr bgcolor=\"#5A5CB8\">");
		body.println("<td><center><font face=\"arial\" color=\"white\">Bean Files</font></center></td>");
		body.println("</tr>");
//		for(int i = 0; i < beanFiles.length; i++)
//		{
//			body.println("<tr bgcolor=\"#ffffff\">");
//			body.println("<td><center><font face=\"arial\" color=\"white\">" +
//			//"<a href=\"" + beanFiles[i].getPath() + "\">" + beanFiles[i].getName() +
//			"<a href=\""+ surl + beanurl + "/" + beanFiles[i].getName() +
//			"\">" + beanFiles[i].getName() +
////			body.println(HtmlUtilities.createAnchor(
////			"javascript:popUpHelp('" + surl + beanurl + "/" + beanFiles[i].getName() + "')",
////			beanFiles[i].getName(),
////			HtmlUtilities.ANCHOR_HREF_TYPE));
//			"</a></font></center></td>");
//			body.println("</font></center></td>");
//			body.println("</tr>");
//		}
		
//		for(int i = 0; i < metaFiles.length; i++)
//		{
//			body.println("<tr bgcolor=\"#ffffff\">");
//			body.println("<td><center><font face=\"arial\" color=\"white\"><center>" +
//			"<a href=\""+ surl + request.getFormParameter("ejbname") + "/META-INF/" +
//			metaFiles[i].getName() +
//			"\">" + metaFiles[i].getName() +
////			body.println(HtmlUtilities.createAnchor(
////			"javascript:popUpHelp('" + surl + request.getFormParameter("ejbname") +
////			"/META-INF/" + metaFiles[i].getName() + "')",
////			metaFiles[i].getName(),
////			HtmlUtilities.ANCHOR_HREF_TYPE));
//			"</a></center></font></center></td>");
//			body.println("</tr>");
//		}
		
//		body.println(HtmlUtilities.createAnchor("javascript:popUpHelp('" + surl +
//		request.getFormParameter("ejbname") + "/" +
//		request.getFormParameter("ejbname") + ".zip')",request.getFormParameter("ejbname") + ".zip",
//		HtmlUtilities.ANCHOR_HREF_TYPE));
		
		body.println("<tr bgcolor=\"#ffffff\">");
		
		if(request.getFormParameter("ejbstyp").equals("remote"))
		{
			body.println("<td><center><font face=\"arial\" color=\"white\"><center>" +
			"<a href=\"" + request.getURI().getFile() + surl + request.getFormParameter("ejbname") + "/" +
			request.getFormParameter("ejbname") + ".zip" + "\">" +
			request.getFormParameter("ejbname") + ".zip" + "</a></center></font></center></td>");
		}
		else
		{
			body.println("<td><center><font face=\"arial\" color=\"white\"><center>" +
			"<a href=\"" + request.getFormParameter("ejbsloc") + "/" + request.getFormParameter("ejbname") +
			"/" + request.getFormParameter("ejbname") + ".zip" + "\">" +
			request.getFormParameter("ejbname") + ".zip" + "</a></center></font></center></td>");
		}
		
		body.println("</tr>");
		body.println("</table>");
		body.println("<br>");	
		body.println("As you can see, there are all the source files needed for your skeleton.");
		body.println("  To save time, a .zip file has been created also to give you the ability");
		body.println(" to extract the files and the proper directory structure to fit your EJB ");
		body.println("and it's packaging!");
	}
}
