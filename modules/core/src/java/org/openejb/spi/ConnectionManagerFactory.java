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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.spi;

import java.io.PrintWriter;
import java.util.Properties;

import javax.resource.spi.ManagedConnectionFactory;

import org.openejb.OpenEJBException;

/**
 * Interface for a service that creates ConnectionManager implementations on
 * demand.  This is used to interface custom ConnectionManagers with
 * OpenEJB.  The exact method of identifying and loading
 * ConnectionManagerFactory instances depends on the Assembler implementation
 * you're using.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public interface ConnectionManagerFactory {
    /**
     * Provides logging capabilities, which should be passed on to
     * ConnectionManager instances created by this factory.
     *
     * <p><font color="red">Note:</font> This method will go away once
     * Logging is made a primary service of OpenEJB.</p>
     */
    public void setLogWriter(PrintWriter logger);

    /**
     * Configures the factory itself.  If the factory does not require
     * additional configuration it may provide an empty implementation.
     */
    public void setProperties(Properties props);

    /**
     * Create a ConnectionManager to use for the specified
     * ManagedConnectionFactory.  This method may return the same
     * ConnectionManager from different calls, if a single
     * ConnectionManager instance can handle multiple
     * ManagedConnectionFactories, but that is not required.
     * @param name The deployment ID of the connector, which may be used
     *             for logging, etc.
     * @param properties The deployment properties for this combination of
     *                   ConnectionManager and ManagedConnectionFactory,
     *                   which includes things like connection pool
     *                   parameters.
     * @param factory The ManagedConnectionFactory to configure a
     *                ConnectionManager for.
     */
    public OpenEJBConnectionManager createConnectionManager(
               String name, ConnectionManagerConfig config,
               ManagedConnectionFactory factory) throws OpenEJBException;
}