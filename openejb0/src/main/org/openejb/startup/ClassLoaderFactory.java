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
import java.util.ArrayList;
import java.net.URL;

/* openejb imports */
import org.openejb.loader.StandardClassLoader;

/**
 * @author <a href="mailto:"></a>
 *
 */
public class ClassLoaderFactory
{
  private static int debug = 0;

  private static ClassLoaderFactory instance = null;

  protected ClassLoaderFactory()
  {
  }

  public static ClassLoaderFactory getInstance()
  {
    if (instance == null)
    {
      instance = new ClassLoaderFactory();
    }

    return instance;
  }

  public int getDebug()
  {
    return debug;
  }

  public void setDebug(int debugLevel)
  {
    debug = debugLevel;
  }

  public ClassLoader createClassLoader
  (
    File unpacked[],
    File packed[],
    ClassLoader parent
  )
  throws Exception
  {
    if (debug >= 1)
    {
      log("Creating new class loader");
    }

    ArrayList list = new ArrayList();

    // add unpacked (classes) directories
    if (unpacked != null)
    {
      for (int i = 0; i < unpacked.length; i++)
      {
        File file = unpacked[i];

        if (!file.isDirectory() || !file.exists() || !file.canRead())
        {
          continue;
        }
        if (debug >= 1)
        {
          log("  Including directory " + file.getAbsolutePath());
        }

        URL url = new URL("file", null, file.getCanonicalPath() + File.separator);
        list.add(url.toString());
      }
    }

    // add packed directories JAR/ZIP files
    if (packed != null)
    {
      for (int i = 0; i < packed.length; i++)
      {
        File directory = packed[i];

        if (!directory.isDirectory() || !directory.exists() || !directory.canRead())
        {
          continue;
        }

        String filenames[] = directory.list();

        for (int j = 0; j < filenames.length; j++)
        {
          String filename = filenames[j].toLowerCase();

          if (!filename.endsWith(".jar") || filename.endsWith(".zip"))
          {
            continue;
          }

          File file = new File(directory, filenames[j]);

          if (debug >= 1)
          {
            log("  Including jar file " + file.getAbsolutePath());
          }

          URL url = new URL("file", null, file.getCanonicalPath());
          list.add(url.toString());
        }
      }
    }

    // construct the classloader itself
    String array[]                  = (String[]) list.toArray(new String[list.size()]);
    StandardClassLoader classLoader = null;

    if (parent == null)
    {
      classLoader = new StandardClassLoader(array);
    }
    else
    {
      classLoader = new StandardClassLoader(array, parent);
    }

    classLoader.setDebug(this.getDebug());
    classLoader.setDelegate(true);

    return classLoader;
  }

  private void log(String message)
  {
    System.out.print("ClassLoaderFactory:  ");
    System.out.println(message);
  }

  private void log(String message, Throwable exception)
  {
    log(message);
    exception.printStackTrace(System.out);
  }
}
