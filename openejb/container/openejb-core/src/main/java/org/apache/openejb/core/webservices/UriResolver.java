/*
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
package org.apache.openejb.core.webservices;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.Base64;
import org.apache.openejb.util.URLs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Resolves a File, classpath resource, or URL according to the follow rules:
 * <ul>
 * <li>Check to see if a file exists, relative to the base URI.</li>
 * <li>If the file doesn't exist, check the classpath</li>
 * <li>If the classpath doesn't exist, try to create URL from the URI.</li>
 * </ul>
 */
// Imported from CXF
public class UriResolver {
    private File file;
    private URI uri;
    private URL url;
    private InputStream is;
    private Class calling;

    public UriResolver() {
    }

    public UriResolver(String path) throws IOException {
        this("", path);
    }

    public UriResolver(String baseUriStr, String uriStr) throws IOException {
        this(baseUriStr, uriStr, null);
    }

    public UriResolver(String baseUriStr, String uriStr, Class calling) throws IOException {
        this.calling = (calling != null) ? calling : getClass();
        if (uriStr.startsWith("classpath:")) {
            tryClasspath(uriStr);
        } else if (baseUriStr != null && baseUriStr.startsWith("jar:")) {
            tryJar(baseUriStr, uriStr);
        } else if (uriStr.startsWith("jar:")) {
            tryJar(uriStr);
        } else {
            tryFileSystem(baseUriStr, uriStr);
        }
    }


    public void resolve(String baseUriStr, String uriStr, Class callingCls) throws IOException {
        this.calling = (callingCls != null) ? callingCls : getClass();
        this.file = null;
        this.uri = null;

        this.is = null;

        if (uriStr.startsWith("classpath:")) {
            tryClasspath(uriStr);
        } else if (baseUriStr != null && baseUriStr.startsWith("jar:")) {
            tryJar(baseUriStr, uriStr);
        } else if (uriStr.startsWith("jar:")) {
            tryJar(uriStr);
        } else {
            tryFileSystem(baseUriStr, uriStr);
        }
    }



    private void tryFileSystem(String baseUriStr, String uriStr) throws IOException, MalformedURLException {
        URI relative;
        File uriFile = new File(uriStr);
        uriFile = new File(uriFile.getAbsolutePath());

        if (uriFile.exists()) {
            relative = uriFile.toURI();
        } else {
            relative = URLs.uri(uriStr);
        }

        if (relative.isAbsolute()) {
            uri = relative;
            url = relative.toURL();

            try {
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();

                String host = System.getProperty("http.proxyHost");
                if (host != null) {
                    //comment out unused port to pass pmd check
                    /*String ports = System.getProperty("http.proxyPort");
                    int port = 80;
                    if (ports != null) {
                        port = Integer.parseInt(ports);
                    }*/

                    String username = System.getProperty("http.proxy.user");
                    String password = System.getProperty("http.proxy.password");

                    if (username != null && password != null) {
                        String encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
                        huc.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
                    }
                }
                is =  huc.getInputStream();
            } catch (ClassCastException ex) {
                is = IO.read(url);
            }
        } else if (baseUriStr != null) {
            URI base;
            File baseFile = new File(baseUriStr);

            if (!baseFile.exists() && baseUriStr.startsWith("file:/")) {
                baseFile = new File(baseUriStr.substring(6));
            }

            if (baseFile.exists()) {
                base = baseFile.toURI();
            } else {
                base = URLs.uri(baseUriStr);
            }

            base = base.resolve(relative);
            if (base.isAbsolute()) {
                try {
                    baseFile = new File(base);
                    if (baseFile.exists()) {
                        is = IO.read(base.toURL());
                        uri = base;
                    } else {
                        tryClasspath(base.toString().startsWith("file:")
                                     ? base.toString().substring(5) : base.toString());
                    }
                } catch (Throwable th) {
                    tryClasspath(base.toString().startsWith("file:")
                                 ? base.toString().substring(5) : base.toString());
                }
            }
        }

        if (uri != null && "file".equals(uri.getScheme())) {
            try {
                file = new File(uri);
            } catch (IllegalArgumentException iae) {
                file = org.apache.openejb.util.URLs.toFile(uri.toURL());
                if (!file.exists()) {
                    file = null;
                }
            }
        }

        if (is == null && file != null && file.exists()) {
            uri = file.toURI();
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new OpenEJBRuntimeException("File was deleted! " + uriStr, e);
            }
            url = file.toURI().toURL();
        } else if (is == null) {
            tryClasspath(uriStr);
        }
    }

    private void tryJar(String baseStr, String uriStr) throws IOException {
        int i = baseStr.indexOf('!');
        if (i == -1) {
            tryFileSystem(baseStr, uriStr);
        }

        String jarBase = baseStr.substring(0, i + 1);
        String jarEntry = baseStr.substring(i + 1);
        URI u = URLs.uri(jarEntry).resolve(uriStr);

        tryJar(jarBase + u.toString());

        if (is != null) {
            if (u.isAbsolute()) {
                url = u.toURL();
            }
            return;
        }

        tryFileSystem("", uriStr);
    }

    private void tryJar(String uriStr) throws IOException {
        int i = uriStr.indexOf('!');
        if (i == -1) {
            return;
        }

        url = new URL(uriStr);
        try {
            is = IO.read(url);
            try {
                uri = url.toURI();
            } catch (URISyntaxException ex) {
                // ignore
            }
        } catch (IOException e) {
            uriStr = uriStr.substring(i + 1);
            tryClasspath(uriStr);
        }
    }

    private void tryClasspath(String uriStr) throws IOException {
        if (uriStr.startsWith("classpath:")) {
            uriStr = uriStr.substring(10);
        }
        url = getResource(uriStr, calling);
        if (url == null) {
            tryRemote(uriStr);
        } else {
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                // processing the jar:file:/ type value
                String urlStr = url.toString();
                if (urlStr.startsWith("jar:")) {
                    int pos = urlStr.indexOf('!');
                    if (pos != -1) {
                        uri = URLs.uri("classpath:" + urlStr.substring(pos + 1));
                    }
                }

            }
            is = IO.read(url);
        }
    }

    private void tryRemote(String uriStr) throws IOException {
        try {
            url = new URL(URLEncoder.encode(uriStr, "UTF-8"));
            uri = URLs.uri(url.toString());
            is = IO.read(url);
        } catch (MalformedURLException e) {
            // do nothing
        }
    }

    public URI getURI() {
        return uri;
    }

    public URL getURL() {
        return url;
    }

    public InputStream getInputStream() {
        return is;
    }

    public boolean isFile() {
        return file != null && file.exists();
    }

    public File getFile() {
        return file;
    }

    public boolean isResolved() {
        return is != null;
    }

    /**
     * Load a given resource. <p/> This method will try to load the resource
     * using the following methods (in order):
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>callingClass.getClassLoader()
     * </ul>
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static URL getResource(String resourceName, Class callingClass) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        if (url == null) {
            url = UriResolver.class.getClassLoader().getResource(resourceName);
        }

        if (url == null) {
            ClassLoader cl = callingClass.getClassLoader();

            if (cl != null) {
                url = cl.getResource(resourceName);
            }
        }

        if (url == null) {
            url = callingClass.getResource(resourceName);
        }

        if ((url == null) && (resourceName != null) && (resourceName.charAt(0) != '/')) {
            return getResource('/' + resourceName, callingClass);
        }

        return url;
    }
}
