/* Redistribution and use of this software and associated documentation
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
 * http://openejb.sf.net/
 */

package org.openejb.loader;

/* base java imports */
import java.net.*;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.JarInputStream;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.security.*;

/* openejb imports */
import org.openejb.core.ivm.naming.JndiPermission;

/**
 * @author <a href="mailto:"></a>
 *
 */
public class StandardClassLoader extends URLClassLoader
                                 implements Reloader
{
  private ClassLoader parent = null;

  private SecurityManager securityManager = null;

  private ClassLoader system = null;

  private ArrayList permissionList = new ArrayList();

  private boolean policy_refresh = false;

  private HashMap loaderPC = new HashMap();

  protected URLStreamHandlerFactory factory = null;

  protected String repositories[] = new String[0];

  protected ArrayList available = new ArrayList();

  protected ArrayList required = new ArrayList();

  protected int debug = 0;

  protected boolean delegate = false;

  public StandardClassLoader()
  {
    super(new URL[0]);
    this.parent = getParent();
    this.system = getSystemClassLoader();
    this.securityManager = System.getSecurityManager();
  }

  public StandardClassLoader(URLStreamHandlerFactory factory)
  {
    super(new URL[0], null, factory);
    this.factory = factory;
  }

  public StandardClassLoader(ClassLoader parent)
  {
    super((new URL[0]), parent);
    this.parent = parent;
    this.system = getSystemClassLoader();
    this.securityManager = System.getSecurityManager();
  }

  public StandardClassLoader
  (
    ClassLoader parent,
    URLStreamHandlerFactory factory
  )
  {
    super((new URL[0]), parent, factory);
    this.factory = factory;
  }

  public StandardClassLoader(String repositories[])
  {
    super(convert(repositories));
    this.parent = getParent();
    this.system = getSystemClassLoader();
    this.securityManager = System.getSecurityManager();

    if (this.repositories != null)
    {
      for (int i = 0; i < repositories.length; i++)
      {
        this.addRepositoryInternal(repositories[i]);
      }
    }
  }

  public StandardClassLoader(String repositories[], ClassLoader parent)
  {
    super(convert(repositories), parent);
    this.parent = parent;
    this.system = getSystemClassLoader();
    this.securityManager = System.getSecurityManager();

    if (repositories != null)
    {
      for (int i = 0; i < repositories.length; i++)
      {
        this.addRepositoryInternal(repositories[i]);
      }
    }
  }

  public StandardClassLoader(URL repositories[], ClassLoader parent)
  {
    super(repositories, parent);
    this.parent = parent;
    this.system = getSystemClassLoader();
    this.securityManager = System.getSecurityManager();

    if (repositories != null)
    {
      for (int i = 0; i < repositories.length; i++)
      {
        this.addRepositoryInternal(repositories[i].toString());
      }
    }
  }

  public int getDebug()
  {
    return this.debug;
  }

  public void setDebug(int debug)
  {
    this.debug = debug;
  }

  public boolean getDelegate()
  {
    return this.delegate;
  }

  public void setDelegate(boolean delegate)
  {
    this.delegate = delegate;
  }

  public void setPermissions(String path)
  {
    if( this.securityManager != null )
    {
      if( path.startsWith("jndi:") || path.startsWith("jar:jndi:") )
      {
        this.permissionList.add(new JndiPermission(path + "*"));
      }
      else
      {
        this.permissionList.add(new FilePermission(path + "-","read"));
      }
    }
  }

  public void setPermissions(URL url)
  {
    setPermissions(url.toString());
  }

  public void addRepository(String repository)
  {
    if (debug >= 1)
    {
      this.log("addRepository(" + repository + ")");
    }

    try
    {
      URLStreamHandler streamHandler = null;
      String protocol                = parseProtocol(repository);

      if (factory != null)
      {
        streamHandler = factory.createURLStreamHandler(protocol);
      }

      URL url = new URL(null, repository, streamHandler);

      super.addURL(url);
    }
    catch (MalformedURLException mue)
    {
      throw new IllegalArgumentException(mue.toString());
    }

    this.addRepositoryInternal(repository);
  }

  public Extension[] findAvailable()
  {
    ArrayList results  = new ArrayList();
    Iterator available = this.available.iterator();
    ClassLoader loader = this;

    while (available.hasNext())
    {
      results.add(available.next());
    }

    while (true)
    {
      loader = loader.getParent();

      if (loader == null)
      {
        break;
      }
      if (!(loader instanceof StandardClassLoader))
      {
        continue;
      }

      Extension extensions[] = ((StandardClassLoader) loader).findAvailable();

      for (int i = 0; i < extensions.length; i++)
      {
        results.add(extensions[i]);
      }
    }

    Extension extensions[] = new Extension[results.size()];

    return ((Extension[]) results.toArray(extensions));
  }

  public String[] findRepositories()
  {
    return this.repositories;
  }

  public Extension[] findRequired()
  {
    ArrayList results  = new ArrayList();
    Iterator required  = this.required.iterator();
    ClassLoader loader = this;

    while (required.hasNext())
    {
      results.add(required.next());
    }

    while (true)
    {
      loader = loader.getParent();

      if (loader == null)
      {
        break;
      }
      if (!(loader instanceof StandardClassLoader))
      {
        continue;
      }

      Extension extensions[] = ((StandardClassLoader) loader).findRequired();

      for (int i = 0; i < extensions.length; i++)
      {
        results.add(extensions[i]);
      }
    }

    Extension extensions[] = new Extension[results.size()];

    return ((Extension[]) results.toArray(extensions));
  }

  public boolean modified()
  {
    return false;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer("StandardClassLoader\r\n");
    sb.append("  available:\r\n");
    Iterator available = this.available.iterator();

    while (available.hasNext())
    {
      sb.append("    ");
      sb.append(available.next().toString());
      sb.append("\r\n");
    }

    sb.append("  delegate: ");
    sb.append(delegate);
    sb.append("\r\n");
    sb.append("  repositories:\r\n");

    for (int i = 0; i < repositories.length; i++)
    {
      sb.append("    ");
      sb.append(repositories[i]);
      sb.append("\r\n");
    }

    sb.append("  required:\r\n");
    Iterator required = this.required.iterator();

    while (required.hasNext())
    {
      sb.append("    ");
      sb.append(required.next().toString());
      sb.append("\r\n");
    }
    if (getParent() != null)
    {
      sb.append("----------> Parent Classloader:\r\n");
      sb.append(getParent().toString());
      sb.append("\r\n");
    }

    return (sb.toString());
  }

  public Class findClass(String name) throws ClassNotFoundException
  {
    if (debug >= 3)
    {
      log("    findClass(" + name + ")");
    }

    if (securityManager != null)
    {
      int i = name.lastIndexOf('.');

      if (i >= 0)
      {
        try
        {
          if (debug >= 4)
          {
            log("      securityManager.checkPackageDefinition");
          }

          securityManager.checkPackageDefinition(name.substring(0,i));
        }
        catch (Exception e)
        {
          if (debug >= 4)
          {
            log("      -->Exception-->ClassNotFoundException", e);
          }

          throw new ClassNotFoundException(name);
        }
      }
    }

    Class clazz = null;

    try
    {
      if (debug >= 4)
      {
        log("      super.findClass(" + name + ")");
      }

      try
      {
        synchronized (this)
        {
          clazz = findLoadedClass(name);

          if (clazz != null)
          {
            return clazz;
          }

          clazz = super.findClass(name);
        }
      }
      catch(AccessControlException ace)
      {
        throw new ClassNotFoundException(name);
      }
      catch (RuntimeException re)
      {
        if (debug >= 4)
        {
          log("      -->RuntimeException Rethrown", re);
        }

        throw re;
      }

      if (clazz == null)
      {
        if (debug >= 3)
        {
          log("    --> Returning ClassNotFoundException");
        }

        throw new ClassNotFoundException(name);
      }
    }
    catch (ClassNotFoundException cnfe)
    {
      if (debug >= 3)
      {
        log("    --> Passing on ClassNotFoundException", cnfe);
      }

      throw cnfe;
    }

    if (debug >= 4)
    {
      log("      Returning class " + clazz);
    }
    if ((debug >= 4) && (clazz != null))
    {
      log("      Loaded by " + clazz.getClassLoader());
    }

    return (clazz);
  }

  public URL findResource(String name)
  {
    if (debug >= 3)
    {
      log("    findResource(" + name + ")");
    }

    URL url = super.findResource(name);

    if (debug >= 3)
    {
      if (url != null)
      {
        log("    --> Returning '" + url.toString() + "'");
      }
      else
      {
        log("    --> Resource not found, returning null");
      }
    }

    return url;
  }

  public Enumeration findResources(String name) throws IOException
  {
    if (debug >= 3)
    {
      log("    findResources(" + name + ")");
    }

    return (super.findResources(name));
  }

  public URL getResource(String name)
  {
    if (debug >= 2)
    {
      log("getResource(" + name + ")");
    }

    URL url = null;

    if (this.delegate)
    {
      if (debug >= 3)
      {
        log("  Delegating to parent classloader");
      }

      ClassLoader loader = parent;

      if (loader == null)
      {
        loader = system;
      }

      url = loader.getResource(name);

      if (url != null)
      {
        if (debug >= 2)
        {
          log("  --> Returning '" + url.toString() + "'");
        }

        return (url);
      }
    }

    if (debug >= 3)
    {
      log("  Searching local repositories");
    }

    url = findResource(name);

    if (url != null)
    {
      if (debug >= 2)
      {
        log("  --> Returning '" + url.toString() + "'");
      }

      return (url);
    }

    if(!delegate)
    {
      ClassLoader loader = parent;

      if (loader == null)
      {
        loader = system;
      }

      url = loader.getResource(name);

      if (url != null)
      {
        if (debug >= 2)
        {
          log("  --> Returning '" + url.toString() + "'");
        }

        return url;
      }
    }

    if (debug >= 2)
    {
      log("  --> Resource not found, returning null");
    }

    return null;
  }

  public InputStream getResourceAsStream(String name)
  {
    if (debug >= 2)
    {
      log("getResourceAsStream(" + name + ")");
    }

    InputStream stream = findLoadedResource(name);

    if (stream != null)
    {
      if (debug >= 2)
      {
        log("  --> Returning stream from cache");
      }

      return (stream);
    }

    if (delegate)
    {
      if (debug >= 3)
      {
        log("  Delegating to parent classloader");
      }

      ClassLoader loader = parent;

      if (loader == null)
      {
        loader = system;
      }

      stream = loader.getResourceAsStream(name);

      if (stream != null)
      {
        if (debug >= 2)
        {
          log("  --> Returning stream from parent");
        }

        return stream;
      }
    }

    if (debug >= 3)
    {
      log("  Searching local repositories");
    }

    URL url = findResource(name);

    if (url != null)
    {
      if (debug >= 2)
      {
        log("  --> Returning stream from local");
      }

      try
      {
        return (url.openStream());
      }
      catch (IOException ioe)
      {
        log("url.openStream(" + url.toString() + ")", ioe);
        return null;
      }
    }

    if (!delegate)
    {
      if (debug >= 3)
      {
        log("  Delegating to parent classloader");
      }

      ClassLoader loader = parent;

      if (loader == null)
      {
        loader = system;
      }

      stream = loader.getResourceAsStream(name);

      if (stream != null)
      {
        if (debug >= 2)
        {
          log("  --> Returning stream from parent");
        }

        return stream;
      }
    }

    if (debug >= 2)
    {
      log("  --> Resource not found, returning null");
    }

    return null;
  }

  public Class loadClass(String name) throws ClassNotFoundException
  {
    return (this.loadClass(name, false));
  }

  public Class loadClass(String name, boolean resolve) throws ClassNotFoundException
  {
    if (debug >= 2)
    {
      log("loadClass(" + name + ", " + resolve + ")");
    }

    Class clazz = this.findLoadedClass(name);

    if (clazz != null)
    {
      if (debug >= 3)
      {
        log("  Returning class from cache");
      }

      if (resolve)
      {
        this.resolveClass(clazz);
      }

      return clazz;
    }

    if( name.startsWith("java.") )
    {
      ClassLoader loader = system;
      clazz              = loader.loadClass(name);

      if (clazz != null)
      {
        if (resolve)
        {
          this.resolveClass(clazz);
        }

        return clazz;
      }

      throw new ClassNotFoundException(name);
    }

    if (securityManager != null)
    {
      int i = name.lastIndexOf('.');

      if (i >= 0)
      {
        try
        {
          securityManager.checkPackageAccess(name.substring(0,i));
        }
        catch (SecurityException se)
        {
          String error = "Security Violation, attempt to use " +
                         "Restricted Class: " + name;

          System.out.println(error);
          se.printStackTrace();
          log(error);

          throw new ClassNotFoundException(error);
        }
      }
    }

    if (delegate)
    {
      if (debug >= 3)
      {
        log("  Delegating as requested to parent classloader --> " + this.getParent().toString());
      }

      ClassLoader loader = parent;

      if (loader == null)
      {
        loader = system;
      }

      try
      {
        clazz = loader.loadClass(name);

        if (clazz != null)
        {
          if (debug >= 3)
          {
            log("  Loading class [" + name + "] from parent");
          }

          if (resolve)
          {
            this.resolveClass(clazz);
          }

          return clazz;
        }
      }
      catch (ClassNotFoundException cnfe)
      {
        ;
        //cnfe.printStackTrace();
      }
    }

    if (debug >= 3)
    {
      log("  Searching local repositories");
    }

    try
    {
      clazz = this.findClass(name);

      if (clazz != null)
      {
        if (debug >= 3)
        {
          log("  Loading class [" + name + "] from local repository");
        }

        if (resolve)
        {
          this.resolveClass(clazz);
        }

        return clazz;
      }
    }
    catch (ClassNotFoundException cnfe)
    {
      ;
      //cnfe.printStackTrace();
    }

    if (!delegate)
    {
      if (debug >= 3)
      {
        log("  Delegating unconditionally to parent classloader -->" + this.getParent().toString());
      }

      ClassLoader loader = parent;

      if (loader == null)
      {
        loader = system;
      }

      try
      {
        clazz = loader.loadClass(name);

        if (clazz != null)
        {
          if (debug >= 3)
          {
            log("  Loading class [" + name + "] from parent");
          }

          if (resolve)
          {
            this.resolveClass(clazz);
          }

          return clazz;
        }
      }
      catch (ClassNotFoundException cnfe)
      {
        ;
        //cnfe.printStackTrace();
      }
    }

    throw new ClassNotFoundException("Could not find class of name: " + name);
  }

  private void log(String message)
  {
    System.out.println("StandardClassLoader: " + message);
  }

  private void log(String message, Throwable throwable)
  {
    System.out.println("StandardClassLoader: " + message);
    throwable.printStackTrace(System.out);
  }

  protected final PermissionCollection getPermissions(CodeSource codeSource)
  {
    if (!policy_refresh)
    {
      Policy policy = Policy.getPolicy();

      policy.refresh();
      policy_refresh = true;
    }

    String codeUrl          = codeSource.getLocation().toString();
    PermissionCollection pc = null;

    if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null)
    {
      pc = super.getPermissions(codeSource);

      if (pc != null)
      {
        Iterator perms = permissionList.iterator();

        while (perms.hasNext())
        {
          Permission p = (Permission)perms.next();
          pc.add(p);
        }

        loaderPC.put(codeUrl,pc);
      }
    }

    return pc;
  }

  protected InputStream findLoadedResource(String name)
  {
    return null;
  }

  protected void addRepositoryInternal(String repository)
  {
    URLStreamHandler streamHandler = null;
    String protocol                = parseProtocol(repository);

    if (factory != null)
    {
      streamHandler = factory.createURLStreamHandler(protocol);
    }

    if (!repository.endsWith(File.separator))
    {
      JarFile jarFile = null;

      try
      {
        Manifest manifest = null;

        if (repository.startsWith("jar:"))
        {
          URL url               = new URL(null, repository, streamHandler);
          JarURLConnection conn = (JarURLConnection) url.openConnection();

          conn.setAllowUserInteraction(false);
          conn.setDoInput(true);
          conn.setDoOutput(false);
          conn.connect();

          jarFile = conn.getJarFile();
        }
        else if (repository.startsWith("file://"))
        {
          jarFile = new JarFile(repository.substring(7));
        }
        else if (repository.startsWith("file:"))
        {
          jarFile = new JarFile(repository.substring(5));
        }
        else if (repository.endsWith(".jar"))
        {
          URL url            = new URL(null, repository, streamHandler);
          URLConnection conn = url.openConnection();
          JarInputStream jis = new JarInputStream(conn.getInputStream());

          manifest = jis.getManifest();
        }
        else
        {
          throw new IllegalArgumentException
                    (
                      "addRepositoryInternal:  Invalid URL '" +
                      repository + "'"
                    );
        }

        if (!((manifest == null) && (jarFile == null)))
        {
          if ((manifest == null) && (jarFile != null))
          {
            manifest = jarFile.getManifest();
          }
          if (manifest != null)
          {
            Iterator extensions = Extension.getAvailable(manifest).iterator();

            while (extensions.hasNext())
            {
              this.available.add(extensions.next());
            }

            extensions = Extension.getRequired(manifest).iterator();

            while (extensions.hasNext())
            {
              this.required.add(extensions.next());
            }
          }
        }
      }
      catch (Throwable t)
      {
        t.printStackTrace();
        throw new IllegalArgumentException("addRepositoryInternal: " + t);
      }
      finally
      {
        if (jarFile != null)
        {
          try
          {
            jarFile.close();
          }
          catch (Throwable t)
          {
            t.printStackTrace();
          }
        }
      }
    }

    synchronized (repositories)
    {
      String results[] = new String[repositories.length + 1];
      System.arraycopy(repositories, 0, results, 0, repositories.length);
      results[repositories.length] = repository;
      repositories = results;
    }
  }

  protected static String parseProtocol(String spec)
  {
    if (spec == null)
    {
      return "";
    }

    int pos = spec.indexOf(':');

    if (pos <= 0)
    {
      return "";
    }

    return spec.substring(0, pos).trim();
  }

  protected static URL[] convert
  (
    String input[],
    URLStreamHandlerFactory factory
  )
  {
    URLStreamHandler streamHandler = null;
    URL url[]                      = new URL[input.length];

    for (int i = 0; i < url.length; i++)
    {
      try
      {
        String protocol = parseProtocol(input[i]);

        if (factory != null)
        {
          streamHandler = factory.createURLStreamHandler(protocol);
        }
        else
        {
          streamHandler = null;
        }

        url[i] = new URL(null, input[i], streamHandler);
      }
      catch (MalformedURLException mue)
      {
        url[i] = null;
      }
    }

    return (url);
  }

  protected static URL[] convert(String input[])
  {
    return convert(input, null);
  }
}
