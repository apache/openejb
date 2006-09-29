/**
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
package org.apache.openejb.corba.security.config;

import java.util.List;
import java.util.Properties;
import java.net.InetSocketAddress;

import org.omg.CORBA.ORB;

import org.apache.openejb.corba.security.config.css.CSSConfig;
import org.apache.openejb.corba.security.config.tss.TSSConfig;


/**
 * Translates TSS and CSS configurations into CORBA startup args and properties.
 *
 * @version $Revision$ $Date$
 */
public interface ConfigAdapter {

    public String[] translateToArgs(TSSConfig config, List args) throws ConfigException;

    public Properties translateToProps(TSSConfig config, Properties props) throws ConfigException;

    public void postProcess(TSSConfig config, ORB orb) throws ConfigException;

    public InetSocketAddress getDefaultListenAddress(TSSConfig config, ORB orb) throws ConfigException;

    public String[] translateToArgs(CSSConfig config, List args) throws ConfigException;

    public Properties translateToProps(CSSConfig config, Properties pros) throws ConfigException;

    public void postProcess(CSSConfig config, ORB orb) throws ConfigException;
}
