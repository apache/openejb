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

package org.openejb.startup;

/* base java imports */
import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;

/**
 * @author <a href="mailto:daniel.haischt@daniel-s-haischt.biz">Daniel S. Haischt</a>
 *
 */
public final class Bootstrap
{
  private static int debug = 0;

  private static String startupClassName = null;

  public static void main(String[] args)
  {
    for (int i = 0; i < args.length; i++)
    {
      if ("-debug".equals(args[i]))
      {
        if (i + 1 < args.length)
        {
          debug = Integer.parseInt(args[++i]);
        }
      }
      else if ("-class".equals(args[i]))
      {
        if (i + 1 < args.length)
        {
          startupClassName = args[++i];
        }
      }
    }

    if (startupClassName == null)
    {
      log("Need a initial class to bootstrap!");
      System.exit(2);
    }

    if (System.getProperty("openejb.base") == null)
    {
      System.setProperty("openejb.base", getOpenEJBHome());
    }

    ClassLoader commonLoader  = null;
    ClassLoader serverLoader  = null;
    ClassLoader modulesLoader = null;
    ClassLoader beansLoader   = null;

    try
    {
      File unpacked[]  = new File[1];
      File packedOne[] = new File[1];
      File packedTwo[] = new File[2];

      ClassLoaderFactory factory = ClassLoaderFactory.getInstance();

      factory.setDebug(debug);

      unpacked[0] = new File(getOpenEJBHome(),
                             "common" + File.separator + "classes");
      packedTwo[0] = new File(getOpenEJBHome(),
                              "common" + File.separator + "endorsed");
      packedTwo[1] = new File(getOpenEJBHome(),
                              "common" + File.separator + "lib");

      commonLoader = factory.createClassLoader(unpacked, packedTwo, null);

      unpacked[0] = new File(getOpenEJBHome(),
                             "server" + File.separator + "classes");
      packedOne[0] = new File(getOpenEJBHome(),
                              "server" + File.separator + "lib");

      serverLoader = factory.createClassLoader(unpacked, packedOne, commonLoader);

      unpacked[0] = new File(getOpenEJBBase(),
                             "modules" + File.separator + "classes");
      packedOne[0] = new File(getOpenEJBBase(),
                              "modules" + File.separator + "lib");

      modulesLoader = factory.createClassLoader(unpacked, packedOne, commonLoader);

      unpacked[0] = new File(getOpenEJBBase(),
                             "beans" + File.separator + "classes");
      packedOne[0] = new File(getOpenEJBBase(),
                              "beans" + File.separator + "lib");

      beansLoader = factory.createClassLoader(unpacked, packedOne, commonLoader);
    }
    catch (Throwable t)
    {
      log("Class loader creation threw exception", t);
      System.exit(1);
    }

    Thread.currentThread().setContextClassLoader(serverLoader);

    try
    {
      //SecurityClassLoad.securityClassLoad(serverLoader);

      if (debug >= 1)
      {
        log("Setting startup class properties");
      }

      Class startupClass     = serverLoader.loadClass(startupClassName);
      Object startupInstance = startupClass.newInstance();

      if (debug >= 1)
      {
        log("Setting startup class properties");
      }

      String methodName  = "setParentClassLoader";
      Class paramTypes[] = new Class[1];

      paramTypes[0] = Class.forName("java.lang.ClassLoader");
      Object paramValues[] = new Object[1];
      paramValues[0] = commonLoader;
      Method method = startupInstance.getClass().getMethod(methodName, paramTypes);

      method.invoke(startupInstance, paramValues);

      if (debug >= 1)
      {
        log("Calling startup class process() method");
      }

      methodName = "process";
      paramTypes = new Class[1];
      paramTypes[0] = args.getClass();
      paramValues = new Object[1];
      paramValues[0] = args;
      method = startupInstance.getClass().getMethod(methodName, paramTypes);

      method.invoke(startupInstance, paramValues);
    }
    catch (Exception e)
    {
      System.out.println("Exception during startup processing");
      e.printStackTrace(System.out);
      System.exit(2);
    }
  }

  private static String getOpenEJBHome()
  {
    return System.getProperty("openejb.home", System.getProperty("user.dir"));
  }

  private static String getOpenEJBBase()
  {
    return System.getProperty("openejb.base", getOpenEJBHome());
  }

  private static void log(String message)
  {
    System.out.print("Bootstrap: ");
    System.out.println(message);
  }

  private static void log(String message, Throwable exception)
  {
    log(message);
    exception.printStackTrace(System.out);
  }
}
