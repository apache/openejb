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

import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.*;

/**
 * @author <a href="mailto:"></a>
 *
 */
public final class Extension
{
  private String extensionName = null;

  private String implementationURL = null;

  private String implementationVendor = null;

  private String implementationVendorId = null;

  private String implementationVersion = null;

  private String specificationVendor = null;

  private String specificationVersion = null;

  public String getExtensionName()
  {
    return this.extensionName;
  }

  public void setExtensionName(String extensionName)
  {
    this.extensionName = extensionName;
  }

  public String getImplementationURL()
  {
    return this.implementationURL;
  }

  public void setImplementationURL(String implementationURL)
  {
    this.implementationURL = implementationURL;
  }

  public String getImplementationVendor()
  {
    return this.implementationVendor;
  }

  public void setImplementationVendor(String implementationVendor)
  {
    this.implementationVendor = implementationVendor;
  }

  public String getImplementationVendorId()
  {
    return this.implementationVendorId;
  }

  public void setImplementationVendorId(String implementationVendorId)
  {
    this.implementationVendorId = implementationVendorId;
  }

  public String getImplementationVersion()
  {
    return this.implementationVersion;
  }

  public void setImplementationVersion(String implementationVersion)
  {
    this.implementationVersion = implementationVersion;
  }

  public String getSpecificationVendor()
  {
    return this.specificationVendor;
  }

  public void setSpecificationVendor(String specificationVendor)
  {
    this.specificationVendor = specificationVendor;
  }

  public String getSpecificationVersion()
  {
    return this.specificationVersion;
  }

  public void setSpecificationVersion(String specificationVersion)
  {
    this.specificationVersion = specificationVersion;
  }

  public boolean isCompatibleWith(Extension required)
  {
    if (extensionName == null)
    {
      return false;
    }

    if (!extensionName.equals(required.getExtensionName()))
    {
      return false;
    }

    if (!isNewer(specificationVersion, required.getSpecificationVersion()))
    {
      return false;
    }

    if (implementationVendorId == null)
    {
      return false;
    }

    if (!implementationVendorId.equals(required.getImplementationVendorId()))
    {
      return false;
    }

    if (!isNewer(implementationVersion, required.getImplementationVersion()))
    {
      return false;
    }

    return true;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer("Extension[");

    sb.append(extensionName);

    if (implementationURL != null)
    {
      sb.append(", implementationURL=");
      sb.append(implementationURL);
    }
    if (implementationVendor != null)
    {
      sb.append(", implementationVendor=");
      sb.append(implementationVendor);
    }
    if (implementationVendorId != null)
    {
      sb.append(", implementationVersion=");
      sb.append(implementationVersion);
    }
    if (specificationVendor != null)
    {
      sb.append(", specificationVendor=");
      sb.append(specificationVendor);
    }
    if (specificationVersion != null)
    {
      sb.append(", specificationVersion=");
      sb.append(specificationVersion);
    }

    sb.append("]");

    return sb.toString();
  }

  public static List getRequired(Manifest manifest)
  {
    ArrayList results = new ArrayList();
    Attributes attributes = manifest.getMainAttributes();

    if (attributes != null)
    {
      Iterator required = getRequired(attributes).iterator();
      while (required.hasNext())
      {
        results.add(required.next());
      }
    }

    Map entries = manifest.getEntries();
    Iterator keys = entries.keySet().iterator();

    while (keys.hasNext())
    {
      String key = (String) keys.next();
      attributes = (Attributes) entries.get(key);
      Iterator required = getRequired(attributes).iterator();

      while (required.hasNext())
      {
        results.add(required.next());
      }
    }

    return (results);
  }

  public static List getAvailable(Manifest manifest)
  {
    ArrayList results = new ArrayList();
    if (manifest == null)
    {
      return (results);
    }

    Extension extension = null;
    Attributes attributes = manifest.getMainAttributes();

    if (attributes != null)
    {
      extension = getAvailable(attributes);

      if (extension != null)
      {
        results.add(extension);
      }
    }

    Map entries = manifest.getEntries();
    Iterator keys = entries.keySet().iterator();

    while (keys.hasNext())
    {
      String key = (String) keys.next();
      attributes = (Attributes) entries.get(key);
      extension = getAvailable(attributes);
      if (extension != null)
      {
        results.add(extension);
      }
    }

    return (results);
  }

  private static Extension getAvailable(Attributes attributes)
  {
    String name         = attributes.getValue("Extension-Name");
    Extension extension = new Extension();

    if (name == null)
    {
      return null;
    }

    extension.setExtensionName(name);
    extension.setImplementationVendor
        (attributes.getValue("Implementation-Vendor"));
    extension.setImplementationVendorId
        (attributes.getValue("Implementation-Vendor-Id"));
    extension.setImplementationVersion
        (attributes.getValue("Implementation-Version"));
    extension.setSpecificationVendor
        (attributes.getValue("Specification-Vendor"));
    extension.setSpecificationVersion
        (attributes.getValue("Specification-Version"));

    return extension;
  }

  private static List getRequired(Attributes attributes)
  {
    ArrayList results = new ArrayList();
    String names      = attributes.getValue("Extension-List");

    if (names == null)
    {
      return results;
    }

    names += " ";

    while (true)
    {
      int space = names.indexOf(' ');

      if (space < 0)
      {
        break;
      }

      String name = names.substring(0, space).trim();
      names = names.substring(space + 1);
      String value = attributes.getValue(name + "-Extension-Name");

      if (value == null)
      {
        continue;
      }

      Extension extension = new Extension();

      extension.setExtensionName(value);
      extension.setImplementationURL
          (attributes.getValue(name + "-Implementation-URL"));
      extension.setImplementationVendorId
          (attributes.getValue(name + "-Implementation-Vendor-Id"));
      extension.setImplementationVersion
          (attributes.getValue(name + "-Implementation-Version"));
      extension.setSpecificationVersion
          (attributes.getValue(name + "-Specification-Version"));

      results.add(extension);
    }

    return results;
  }

  private boolean isNewer(String first, String second) throws NumberFormatException
  {
    if ((first == null) || (second == null))
    {
      return false;
    }
    if (first.equals(second))
    {
      return true;
    }

    StringTokenizer fTok = new StringTokenizer(first, ".", true);
    StringTokenizer sTok = new StringTokenizer(second, ".", true);
    int fVersion         = 0;
    int sVersion         = 0;

    while (fTok.hasMoreTokens() || sTok.hasMoreTokens())
    {
      if (fTok.hasMoreTokens())
      {
        fVersion = Integer.parseInt(fTok.nextToken());
      }
      else
      {
        fVersion = 0;
      }

      if (sTok.hasMoreTokens())
      {
        sVersion = Integer.parseInt(sTok.nextToken());
      }
      else
      {
        sVersion = 0;
      }

      if (fVersion < sVersion)
      {
        return false;
      }
      else
      {
        return true;
      }

      /*if (fTok.hasMoreTokens())
      {
        fTok.nextToken();
      }
      if (sTok.hasMoreTokens())
      {
        sTok.nextToken();
      }*/
    }

    return true;
  }
}
