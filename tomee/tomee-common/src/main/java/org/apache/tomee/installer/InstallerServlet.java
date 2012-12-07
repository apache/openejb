/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.installer;

import static org.apache.tomee.installer.Installer.Status.NONE;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.io.Writer;

/**
 * Installs OpenEJB into Tomcat.
 * <p/>
 * NOTE: This servlet can not use any classes from OpenEJB since it is installing OpenEJB itself.
 */
public class InstallerServlet extends HttpServlet {
    protected Paths paths;
    protected Installer installer;
    protected int attempts;
    private ServletConfig servletConfig;

    public void init(ServletConfig servletConfig) throws ServletException {
        this.servletConfig = servletConfig;

        String path = servletConfig.getServletContext().getRealPath("/");

        File openejbWarDir = null;
        if (path != null) {
            openejbWarDir = new File(path);
        }

        paths = new Paths(openejbWarDir);
        installer = new Installer(paths);
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doIt(httpServletRequest, httpServletResponse);
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doIt(httpServletRequest, httpServletResponse);
    }

    protected void doIt(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // if they clicked the install button...
        if ("install".equalsIgnoreCase(req.getParameter("action"))) {
            // If not already installed, try to install
            if (installer.getStatus() == NONE) {
                attempts++;

                paths.reset();
                installer.reset();

                if ("true".equalsIgnoreCase(req.getParameter("auto"))) {
                    paths.setCatalinaHomeDir(System.getProperty("catalina.home"));
                    paths.setCatalinaBaseDir(System.getProperty("catalina.base"));
                    paths.setServerXmlFile(System.getProperty("catalina.base") + "/conf/server.xml");

                } else {
                    paths.setCatalinaHomeDir(req.getParameter("catalinaHome"));
                    paths.setCatalinaBaseDir(req.getParameter("catalinaBase"));
                    paths.setServerXmlFile(req.getParameter("serverXml"));

                }

                if (paths.verify()) {
                    installer.installAll();
                }
            }

            // send redirect to avoid double post lameness
            res.sendRedirect(req.getRequestURI());
        } else {

            req.setAttribute("installer", installer);
            req.setAttribute("paths", paths);
            RequestDispatcher rd = servletConfig.getServletContext().getRequestDispatcher("/installer-view.jsp");
            try {
                rd.forward(req,res);

            } catch (Exception e) {
                res.setContentType("text/plain");
                Writer writer = res.getWriter();
                for (String s : installer.getAlerts().getErrors()) {
                    writer.write("[ERROR] " + s);
                }
                for (String s : installer.getAlerts().getWarnings()) {
                    writer.write("[WARN] " + s);
                }
                for (String s : installer.getAlerts().getInfos()) {
                    writer.write("[INFO] " + s);
                }
            }
        }
    }

    public void dump(ServletOutputStream out) throws IOException {
        printFile(out, "Catalina home: ", paths.getCatalinaHomeDir());
        printFile(out, "Catalina base: ", paths.getCatalinaBaseDir());
        printFile(out, "Catalina server.xml: ", paths.getServerXmlFile());
        printFile(out, "Catalina conf: ", paths.getCatalinaConfDir());
        printFile(out, "Catalina lib: ", paths.getCatalinaLibDir());
        printFile(out, "Catalina bin: ", paths.getCatalinaBinDir());
        printFile(out, "Catalina catalina.sh: ", paths.getCatalinaShFile());
        printFile(out, "Catalina catalina.bat: ", paths.getCatalinaBatFile());
        printFile(out, "OpenEJB lib: ", paths.getOpenEJBLibDir());
        printFile(out, "OpenEJB loader jar: ", paths.getOpenEJBTomcatLoaderJar());
        printFile(out, "OpenEJB javaagent jar: ", paths.getOpenEJBJavaagentJar());
    }

    private void printFile(ServletOutputStream out, String description, File file) throws IOException {
        out.println(description + ":");
        out.println("    " + file);
    }
}
